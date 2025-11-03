package com.zharkyn.aiassistant_backend.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.zharkyn.aiassistant_backend.model.VoiceMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Repository
@Slf4j
public class VoiceMessageRepository {
    
    private static final String COLLECTION_NAME = "voice_messages";
    private final Firestore firestore;

    @Autowired
    public VoiceMessageRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    public String create(VoiceMessage message) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document();
        message.setId(docRef.getId());
        message.setTimestamp(System.currentTimeMillis());
        
        ApiFuture<com.google.cloud.firestore.WriteResult> result = docRef.set(message);
        result.get();
        
        log.info("Voice message created with ID: {}", docRef.getId());
        return docRef.getId();
    }

    public List<VoiceMessage> getBySessionId(String sessionId) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                .whereEqualTo("sessionId", sessionId)
                .orderBy("timestamp")
                .get();
        
        QuerySnapshot querySnapshot = future.get();
        List<VoiceMessage> messages = querySnapshot.getDocuments().stream()
                .map(doc -> doc.toObject(VoiceMessage.class))
                .collect(Collectors.toList());
        
        log.info("Found {} voice messages for session {}", messages.size(), sessionId);
        return messages;
    }
}