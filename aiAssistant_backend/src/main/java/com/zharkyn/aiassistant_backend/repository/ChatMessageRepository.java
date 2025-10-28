package com.zharkyn.aiassistant_backend.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;
import com.zharkyn.aiassistant_backend.model.ChatMessage;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Repository
public class ChatMessageRepository {
    private static final String COLLECTION_NAME = "chat_messages";

    public ChatMessage save(ChatMessage message) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        if (message.getId() == null) {
            var docRef = dbFirestore.collection(COLLECTION_NAME).document();
            message.setId(docRef.getId());
            docRef.set(message).get();
        } else {
            dbFirestore.collection(COLLECTION_NAME).document(message.getId()).set(message).get();
        }
        return message;
    }

    /**
     * ✅ ИСПРАВЛЕНО: Убрана сортировка на уровне Firestore, чтобы не требовать индекс.
     * Сортировка выполняется в Java после получения данных.
     */
    public List<ChatMessage> findBySessionIdOrderByTimestampAsc(String sessionId) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        
        // Выполняем запрос БЕЗ orderBy - это не требует индекса
        ApiFuture<QuerySnapshot> future = dbFirestore.collection(COLLECTION_NAME)
                .whereEqualTo("sessionId", sessionId)
                .get();
        
        // Получаем результаты и сортируем в памяти
        return future.get().getDocuments().stream()
                .map(doc -> doc.toObject(ChatMessage.class))
                .sorted(Comparator.comparing(ChatMessage::getTimestamp))  // Сортировка в Java
                .collect(Collectors.toList());
    }
}