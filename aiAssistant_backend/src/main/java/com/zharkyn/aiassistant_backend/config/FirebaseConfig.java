package com.zharkyn.aiassistant_backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Configuration
public class FirebaseConfig {

    @Bean
    public FirebaseApp initializeFirebase() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            log.info("Initializing Firebase...");
            
            InputStream serviceAccount;
            
            try {
                serviceAccount = new FileInputStream("/app/serviceAccountKey.json");
                log.info("Loaded Firebase credentials from /app/serviceAccountKey.json");
            } catch (Exception e) {
                serviceAccount = getClass().getClassLoader()
                    .getResourceAsStream("serviceAccountKey.json");
                log.info("Loaded Firebase credentials from classpath");
            }
            
            if (serviceAccount == null) {
                throw new IOException("Firebase credentials file not found!");
            }
            
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();
            
            log.info("Firebase initialized successfully");
            return FirebaseApp.initializeApp(options);
        }
        return FirebaseApp.getInstance();
    }

    @Bean
    public Firestore firestore() throws IOException {
        initializeFirebase();
        log.info("Creating Firestore bean");
        return FirestoreClient.getFirestore();
    }
}