package com.zharkyn.aiassistant_backend.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.zharkyn.aiassistant_backend.model.ChatMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Репозиторий для работы с сообщениями чата в Firestore
 */
@Slf4j
@Repository
public class ChatMessageRepository {

    private static final String COLLECTION_NAME = "chat_messages";

    /**
     * Сохранить сообщение
     */
    public ChatMessage save(ChatMessage message) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        
        if (message.getId() == null || message.getId().isEmpty()) {
            ApiFuture<DocumentReference> future = dbFirestore.collection(COLLECTION_NAME).add(message);
            DocumentReference docRef = future.get();
            message.setId(docRef.getId());
            log.info("Message created with ID: {}", message.getId());
        } else {
            ApiFuture<WriteResult> future = dbFirestore.collection(COLLECTION_NAME)
                    .document(message.getId())
                    .set(message);
            future.get();
            log.info("Message updated with ID: {}", message.getId());
        }
        
        return message;
    }

    /**
     * Найти все сообщения по ID сессии
     */
    public List<ChatMessage> findBySessionIdOrderByTimestampAsc(String sessionId) 
            throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        
        ApiFuture<QuerySnapshot> future = dbFirestore.collection(COLLECTION_NAME)
                .whereEqualTo("sessionId", sessionId)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get();
        
        QuerySnapshot querySnapshot = future.get();
        List<ChatMessage> messages = querySnapshot.toObjects(ChatMessage.class);
        
        for (int i = 0; i < messages.size(); i++) {
            messages.get(i).setId(querySnapshot.getDocuments().get(i).getId());
        }
        
        log.info("Found {} messages for session {}", messages.size(), sessionId);
        return messages;
    }

    /**
     * Удалить все сообщения для сессии
     */
    public void deleteBySessionId(String sessionId) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        
        ApiFuture<QuerySnapshot> future = dbFirestore.collection(COLLECTION_NAME)
                .whereEqualTo("sessionId", sessionId)
                .get();
        
        QuerySnapshot querySnapshot = future.get();
        
        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
            document.getReference().delete();
        }
        
        log.info("Deleted all messages for session {}", sessionId);
    }
}