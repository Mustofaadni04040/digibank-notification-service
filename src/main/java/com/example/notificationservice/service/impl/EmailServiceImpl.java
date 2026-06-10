package com.example.notificationservice.service.impl;

import com.example.notificationservice.entity.Notification;
import com.example.notificationservice.enums.NotificationStatus;
import com.example.notificationservice.enums.NotificationType;
import com.example.notificationservice.kafka.dto.UserRegistrationEvent;
import com.example.notificationservice.repository.NotificationRepository;
import com.example.notificationservice.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final NotificationRepository notificationRepository;

    @Value("${spring.mail.username}")
    private String fromEmail;


    @Override
    public void sendWelcomeAlert(UserRegistrationEvent event) {

        try {
            Context context = new Context();
            context.setVariable("firstName", event.getFirstName());
            context.setVariable("lastName", event.getLastName());
            context.setVariable("email", event.getEmail());
            context.setVariable("accountNumber", event.getAccountNumber());
            context.setVariable("bankName", event.getBankName());

            String htmlEmailTemplate = templateEngine.process("welcome-email", context);
            Notification notificationToSave = Notification.builder()
                    .recipientEmail(event.getEmail())
                    .type(NotificationType.EMAIL)
                    .subject("Welcome to " +event.getBankName()+ " Your Account is Ready!")
                    .message(htmlEmailTemplate)
                    .status(NotificationStatus.SENT)
                    .build();

            sendEmailOut(event.getEmail(), notificationToSave.getSubject(), htmlEmailTemplate);

            notificationRepository.save(notificationToSave);
            log.info("Welcome email sent successfully");
        } catch (Exception e) {
            log.info("Error sending welcome email: {}", e.getMessage());
            Notification notificationToSave = Notification.builder()
                    .recipientEmail(event.getEmail())
                    .type(NotificationType.EMAIL)
                    .subject("Failed to send email")
                    .message("Failed to send email")
                    .status(NotificationStatus.FAILED)
                    .build();

            notificationRepository.save(notificationToSave);
        }
    }

    @Override
    public void sendCreditAlert() {

    }

    @Override
    public void sendDebitAlert() {

    }



    private void sendEmailOut(String recipientEmail, String subject, String emailTemplate) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(
                mimeMessage,
                true,
                "UTF-8"
        );

        helper.setFrom(fromEmail);
        helper.setTo(recipientEmail);
        helper.setSubject(subject);
        helper.setText(emailTemplate, true);

        mailSender.send(mimeMessage);
    }
}
