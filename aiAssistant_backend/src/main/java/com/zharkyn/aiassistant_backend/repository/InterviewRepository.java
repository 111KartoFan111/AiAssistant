package com.zharkyn.aiassistant_backend.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.zharkyn.aiassistant_backend.model.Interview;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Repository
@Slf4j
public class InterviewRepository {
    
    private static final String COLLECTION_NAME = "interviews";
    private final Firestore firestore;

    @Autowired
    public InterviewRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    public String create(Interview interview) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document();
        interview.setId(docRef.getId());
        interview.setCreatedAt(System.currentTimeMillis());
        interview.setUpdatedAt(System.currentTimeMillis());
        
        ApiFuture<com.google.cloud.firestore.WriteResult> result = docRef.set(interview);
        result.get();
        
        log.info("Interview created with ID: {}", docRef.getId());
        return docRef.getId();
    }

    public Interview getById(String id) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(id);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();
        
        if (document.exists()) {
            return document.toObject(Interview.class);
        }
        return null;
    }

    public List<Interview> getByUserId(String userId) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", com.google.cloud.firestore.Query.Direction.DESCENDING)
                .get();
        
        QuerySnapshot querySnapshot = future.get();
        return querySnapshot.getDocuments().stream()
                .map(doc -> doc.toObject(Interview.class))
                .collect(Collectors.toList());
    }

    public void update(String id, Interview interview) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(id);
        interview.setUpdatedAt(System.currentTimeMillis());
        
        ApiFuture<com.google.cloud.firestore.WriteResult> result = docRef.set(interview);
        result.get();
        
        log.info("Interview updated: {}", id);
    }

    public void delete(String id) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(id);
        ApiFuture<com.google.cloud.firestore.WriteResult> result = docRef.delete();
        result.get();
        
        log.info("Interview deleted: {}", id);
    }
}