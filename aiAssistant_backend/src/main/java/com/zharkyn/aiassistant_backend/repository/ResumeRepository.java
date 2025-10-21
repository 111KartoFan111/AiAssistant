package com.zharkyn.aiassistant_backend.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;
import com.zharkyn.aiassistant_backend.model.Resume;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Repository
public class ResumeRepository {
    private static final String COLLECTION_NAME = "resumes";

    public Resume save(Resume resume) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        if (resume.getId() == null) {
            var docRef = dbFirestore.collection(COLLECTION_NAME).document();
            resume.setId(docRef.getId());
            docRef.set(resume).get();
        } else {
            dbFirestore.collection(COLLECTION_NAME).document(resume.getId()).set(resume).get();
        }
        return resume;
    }

    public Optional<Resume> findByUserId(String userId) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> future = dbFirestore.collection(COLLECTION_NAME)
                .whereEqualTo("userId", userId)
                .limit(1)
                .get();
        QuerySnapshot querySnapshot = future.get();
        if (!querySnapshot.isEmpty()) {
            return Optional.of(querySnapshot.getDocuments().get(0).toObject(Resume.class));
        }
        return Optional.empty();
    }

    public Optional<Resume> findById(String id) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        ApiFuture<DocumentSnapshot> future = dbFirestore.collection(COLLECTION_NAME).document(id).get();
        DocumentSnapshot document = future.get();
        if (document.exists()) {
            return Optional.of(document.toObject(Resume.class));
        }
        return Optional.empty();
    }

    public void deleteById(String id) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        dbFirestore.collection(COLLECTION_NAME).document(id).delete().get();
    }
}