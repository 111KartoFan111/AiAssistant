package com.zharkyn.aiassistant_backend.repository;

import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import com.zharkyn.aiassistant_backend.model.InterviewSession;
import org.springframework.stereotype.Repository;

import java.util.concurrent.ExecutionException;

@Repository
public class InterviewSessionRepository {
    private static final String COLLECTION_NAME = "interview_sessions";

    public InterviewSession save(InterviewSession session) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        if (session.getId() == null) {
            var docRef = dbFirestore.collection(COLLECTION_NAME).document();
            session.setId(docRef.getId());
            docRef.set(session).get();
        } else {
            dbFirestore.collection(COLLECTION_NAME).document(session.getId()).set(session).get();
        }
        return session;
    }
}
