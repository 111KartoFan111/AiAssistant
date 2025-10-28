package com.zharkyn.aiassistant_backend.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.cloud.FirestoreClient;
import com.zharkyn.aiassistant_backend.model.User;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Repository
public class UserRepository {

    private static final String COLLECTION_NAME = "users";

    public User save(User user) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        if (user.getId() == null) {
            var docRef = dbFirestore.collection(COLLECTION_NAME).document();
            user.setId(docRef.getId());
            docRef.set(user).get();
        } else {
            dbFirestore.collection(COLLECTION_NAME).document(user.getId()).set(user).get();
        }
        return user;
    }

    public Optional<User> findByEmail(String email) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> future = dbFirestore.collection(COLLECTION_NAME).whereEqualTo("email", email).limit(1).get();
        QuerySnapshot querySnapshot = future.get();
        if (!querySnapshot.isEmpty()) {
            QueryDocumentSnapshot document = querySnapshot.getDocuments().get(0);
            return Optional.of(document.toObject(User.class));
        }
        return Optional.empty();
    }
}
