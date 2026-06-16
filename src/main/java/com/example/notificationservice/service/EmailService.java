package com.example.notificationservice.service;

import com.example.notificationservice.kafka.dto.BalanceUpdateEvent;
import com.example.notificationservice.kafka.dto.UserRegistrationEvent;

public interface EmailService {

    void sendWelcomeAlert(UserRegistrationEvent event);

    void sendTransactionAlertEmail(BalanceUpdateEvent event);
}
