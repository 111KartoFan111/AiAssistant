package com.zharkyn.aiassistant_backend.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.zharkyn.aiassistant_backend.model.VoiceInterview;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Repository
@Slf4j
public class VoiceInterviewRepository {
    
    private static final String COLLECTION_NAME = "voice_interviews";
    private final Firestore firestore;

    @Autowired
    public VoiceInterviewRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    public String create(VoiceInterview interview) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document();
        interview.setId(docRef.getId());
        interview.setCreatedAt(System.currentTimeMillis());
        interview.setUpdatedAt(System.currentTimeMillis());
        
        ApiFuture<com.google.cloud.firestore.WriteResult> result = docRef.set(interview);
        result.get();
        
        log.info("Voice interview created with ID: {}", docRef.getId());
        return docRef.getId();
    }

    public VoiceInterview getById(String id) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(id);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();
        
        if (document.exists()) {
            return document.toObject(VoiceInterview.class);
        }
        return null;
    }

    public List<VoiceInterview> getByUserId(String userId) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                .whereEqualTo("userId", userId)
                .get();
        
        QuerySnapshot querySnapshot = future.get();
        return querySnapshot.getDocuments().stream()
                .map(doc -> doc.toObject(VoiceInterview.class))
                .collect(Collectors.toList());
    }

    public void update(String id, VoiceInterview interview) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(id);
        interview.setUpdatedAt(System.currentTimeMillis());
        
        ApiFuture<com.google.cloud.firestore.WriteResult> result = docRef.set(interview);
        result.get();
        
        log.info("Voice interview updated: {}", id);
    }
}