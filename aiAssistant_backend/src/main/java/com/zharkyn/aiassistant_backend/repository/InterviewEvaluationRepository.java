package com.zharkyn.aiassistant_backend.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.zharkyn.aiassistant_backend.model.InterviewEvaluation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * Репозиторий для работы с оценками интервью в Firestore
 */
@Slf4j
@Repository
public class InterviewEvaluationRepository {

    private static final String COLLECTION_NAME = "interview_evaluations";

    /**
     * Сохранить оценку
     */
    public InterviewEvaluation save(InterviewEvaluation evaluation) 
            throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        
        if (evaluation.getId() == null || evaluation.getId().isEmpty()) {
            // Создаем новую оценку
            ApiFuture<DocumentReference> future = dbFirestore.collection(COLLECTION_NAME).add(evaluation);
            DocumentReference docRef = future.get();
            evaluation.setId(docRef.getId());
            log.info("Evaluation created with ID: {}", evaluation.getId());
        } else {
            // Обновляем существующую
            ApiFuture<WriteResult> future = dbFirestore.collection(COLLECTION_NAME)
                    .document(evaluation.getId())
                    .set(evaluation);
            future.get();
            log.info("Evaluation updated with ID: {}", evaluation.getId());
        }
        
        return evaluation;
    }

    /**
     * Найти оценку по ID интервью и номеру вопроса
     */
    public Optional<InterviewEvaluation> findByInterviewIdAndQuestionNumber(
            String interviewId, Integer questionNumber) 
            throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        
        ApiFuture<QuerySnapshot> future = dbFirestore.collection(COLLECTION_NAME)
                .whereEqualTo("interviewId", interviewId)
                .whereEqualTo("questionNumber", questionNumber)
                .limit(1)
                .get();
        
        QuerySnapshot querySnapshot = future.get();
        
        if (!querySnapshot.isEmpty()) {
            DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
            InterviewEvaluation evaluation = doc.toObject(InterviewEvaluation.class);
            evaluation.setId(doc.getId());
            return Optional.of(evaluation);
        }
        
        return Optional.empty();
    }

    /**
     * Получить все оценки для интервью
     */
    public List<InterviewEvaluation> findByInterviewId(String interviewId) 
            throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        
        ApiFuture<QuerySnapshot> future = dbFirestore.collection(COLLECTION_NAME)
                .whereEqualTo("interviewId", interviewId)
                .orderBy("questionNumber", Query.Direction.ASCENDING)
                .get();
        
        QuerySnapshot querySnapshot = future.get();
        List<InterviewEvaluation> evaluations = querySnapshot.toObjects(InterviewEvaluation.class);
        
        // Устанавливаем ID из документа
        for (int i = 0; i < evaluations.size(); i++) {
            evaluations.get(i).setId(querySnapshot.getDocuments().get(i).getId());
        }
        
        log.info("Found {} evaluations for interview {}", evaluations.size(), interviewId);
        return evaluations;
    }

    /**
     * Удалить все оценки для интервью
     */
    public void deleteByInterviewId(String interviewId) 
            throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        
        ApiFuture<QuerySnapshot> future = dbFirestore.collection(COLLECTION_NAME)
                .whereEqualTo("interviewId", interviewId)
                .get();
        
        QuerySnapshot querySnapshot = future.get();
        
        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
            document.getReference().delete();
        }
        
        log.info("Deleted all evaluations for interview {}", interviewId);
    }
}