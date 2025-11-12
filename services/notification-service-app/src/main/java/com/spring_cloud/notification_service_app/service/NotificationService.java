package com.spring_cloud.notification_service_app.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    // Eda simple ga console lo log cheyyadaniki
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    @KafkaListener(
            topics = "order-notifications", // 1. Ee topic ni vinu
            groupId = "notification-group"  // 2. Ee group lo part ga
    )
    public void handleOrderNotification(String message) {
        // Kafka nunchi message ragane, ee method automatic ga run avuthundi

        log.info("---------------------------------");
        log.info("📬 MESSAGE RECEIVED: " + message);
        log.info("✉️ Sending email/SMS for order...");
        log.info("---------------------------------");
    }

}
