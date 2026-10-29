
package com.foodie.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MerchantEmailService {

    private final JavaMailSender mailSender;
    private final String senderEmail;

    public MerchantEmailService(
            JavaMailSender mailSender,
            @Value("${spring.mail.username}") String senderEmail) {
        this.mailSender = mailSender;
        this.senderEmail = senderEmail;
    }

    public void sendApprovalEmail(String merchantEmail, String firstName) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(senderEmail);
        message.setTo(merchantEmail);
        message.setSubject("Foodie - Merchant Account Approved");

        message.setText(
                "Dear " + firstName + ",\n\n"
                        + "Congratulations! Your Foodie merchant account "
                        + "has been approved.\n\n"
                        + "You can now log in to the Foodie Merchant Portal.\n\n"
                        + "Best regards,\n"
                        + "Foodie Team"
        );

        mailSender.send(message);
    }
}
