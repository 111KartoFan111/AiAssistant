package com.zharkyn.aiassistant_backend.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.zharkyn.aiassistant_backend.model.VoiceMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Репозиторий для работы с голосовыми сообщениями в Firestore
 */
@Slf4j
@Repository
public class VoiceMessageRepository {

    private static final String COLLECTION_NAME = "voice_messages";

    /**
     * Сохранить голосовое сообщение
     */
    public VoiceMessage save(VoiceMessage voiceMessage) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        
        if (voiceMessage.getId() == null || voiceMessage.getId().isEmpty()) {
            // Создаем новое сообщение
            ApiFuture<DocumentReference> future = dbFirestore.collection(COLLECTION_NAME).add(voiceMessage);
            DocumentReference docRef = future.get();
            voiceMessage.setId(docRef.getId());
            log.info("Voice message created with ID: {}", voiceMessage.getId());
        } else {
            // Обновляем существующее сообщение
            ApiFuture<WriteResult> future = dbFirestore.collection(COLLECTION_NAME)
                    .document(voiceMessage.getId())
                    .set(voiceMessage);
            future.get();
            log.info("Voice message updated with ID: {}", voiceMessage.getId());
        }
        
        return voiceMessage;
    }

    /**
     * Найти все сообщения по ID сессии, отсортированные по времени
     */
    public List<VoiceMessage> findBySessionIdOrderByTimestampAsc(String sessionId) 
            throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        
        ApiFuture<QuerySnapshot> future = dbFirestore.collection(COLLECTION_NAME)
                .whereEqualTo("sessionId", sessionId)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get();
        
        QuerySnapshot querySnapshot = future.get();
        List<VoiceMessage> messages = querySnapshot.toObjects(VoiceMessage.class);
        
        // Устанавливаем ID из документа
        for (int i = 0; i < messages.size(); i++) {
            messages.get(i).setId(querySnapshot.getDocuments().get(i).getId());
        }
        
        log.info("Found {} voice messages for session {}", messages.size(), sessionId);
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
        
        log.info("Deleted all voice messages for session {}", sessionId);
    }
}