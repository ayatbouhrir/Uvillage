package com.example.uvillage.util;

import com.example.uvillage.models.User;
import com.example.uvillage.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class DatabaseUtil {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseUtil.class);
    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private static final Map<String, String> userCredentials = new HashMap<>();
    private static final String DOLIBAR_API_URL = "http://localhost:8380/Uvillage_war_exploded/login";
    private static final int MAX_ATTEMPTS = 3;
    private static final long DELAY_MILLISECONDS = 1000;

    private final UserRepository userRepository;
    private final RestTemplate restTemplate;

    @PostConstruct
    public void init() {
        if (userRepository == null) {
            System.out.println("UserRepository is not initialized");
        } else {
            System.out.println("UserRepository is initialized");
        }
    }

    @Autowired
    public DatabaseUtil(UserRepository userRepository, RestTemplate restTemplate) {
        this.userRepository = userRepository;
        this.restTemplate = restTemplate;

        // Initialize user credentials (hash the passwords)
        userCredentials.put("admin", passwordEncoder.encode("admin"));
    }

    public boolean authenticateUser(String username, String password) {
        System.out.println("\n\n authenticateUser ======== \n");
        try {
            if (userRepository == null) {
                logger.error("UserRepository is not initialized.");
                return false;
            }

            // Spring Security
            User user = userRepository.findByUsername(username);
            if (user != null && passwordEncoder.matches(password, user.getPassword())) {
                return true;
            }

            // Basic authentication
            if (userCredentials.containsKey(username) && passwordEncoder.matches(password, userCredentials.get(username))) {
                return true;
            }

            // MD5 Hashing (not recommended for passwords)
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                byte[] hash = md.digest(password.getBytes());
                StringBuilder hexString = new StringBuilder();
                for (byte b : hash) {
                    String hex = Integer.toHexString(0xff & b);
                    if (hex.length() == 1) hexString.append('0');
                    hexString.append(hex);
                }
                String hashedPassword = hexString.toString();
                user = userRepository.findByUsername(username);
                if (user != null && user.getPassword().equals(hashedPassword)) {
                    return true;
                }
            } catch (NoSuchAlgorithmException e) {
                logger.error("MD5 hashing algorithm not found", e);
            }

            // REST API Dolibar
            try {
                Map<String, String> requestBody = new HashMap<>();
                requestBody.put("username", username);
                requestBody.put("password", password);
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);
                ResponseEntity<Boolean> response = restTemplate.exchange(DOLIBAR_API_URL, HttpMethod.POST, entity, Boolean.class);
                if (Boolean.TRUE.equals(response.getBody())) {
                    return true;
                }
            } catch (Exception e) {
                logger.error("Error during REST API authentication", e);
            }

            // Brute Force (simplified, not recommended for production)
            int attempts = 0;
            while (attempts < MAX_ATTEMPTS) {
                if ("admin".equals(username) && "admin".equals(password)) {
                    return true;
                }
                System.out.println("MAX_ATTEMPTS :" + MAX_ATTEMPTS);
                attempts++;
                try {
                    TimeUnit.MILLISECONDS.sleep(DELAY_MILLISECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.error("Thread interrupted during brute force delay", e);
                }
            }

            // If none of the authentication methods succeeded, return false
            return false;

        } catch (Exception e) {
            logger.error("Unexpected error during authentication", e);
            return false;
        }
    }
}
