package com.zharkyn.aiassistant_backend.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.zharkyn.aiassistant_backend.model.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Repository
@Slf4j
public class MessageRepository {
    
    private static final String COLLECTION_NAME = "messages";
    private final Firestore firestore;

    @Autowired
    public MessageRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    public String create(Message message) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document();
        message.setId(docRef.getId());
        message.setTimestamp(System.currentTimeMillis());
        
        ApiFuture<com.google.cloud.firestore.WriteResult> result = docRef.set(message);
        result.get();
        
        log.info("Message created with ID: {}", docRef.getId());
        return docRef.getId();
    }

    public Message getById(String id) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(id);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();
        
        if (document.exists()) {
            return document.toObject(Message.class);
        }
        return null;
    }

    public List<Message> getBySessionId(String sessionId) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                .whereEqualTo("sessionId", sessionId)
                .orderBy("timestamp")
                .get();
        
        QuerySnapshot querySnapshot = future.get();
        List<Message> messages = querySnapshot.getDocuments().stream()
                .map(doc -> doc.toObject(Message.class))
                .collect(Collectors.toList());
        
        log.info("Found {} messages for session {}", messages.size(), sessionId);
        return messages;
    }

    public void deleteBySessionId(String sessionId) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                .whereEqualTo("sessionId", sessionId)
                .get();
        
        QuerySnapshot querySnapshot = future.get();
        
        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
            document.getReference().delete();
        }
        
        log.info("Deleted all messages for session: {}", sessionId);
    }
}