package com.ecommerce.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    public void sendOrderConfirmation(String toEmail, String userName, Long orderId, String totalAmount) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Order Confirmation - Order #" + orderId);
            message.setText(
                    "Hi " + userName + ",\n\n" +
                            "Thank you for your order! Your order has been placed successfully.\n\n" +
                            "Order ID: #" + orderId + "\n" +
                            "Total Amount: ₹" + totalAmount + "\n\n" +
                            "We will notify you once your order ships.\n\n" +
                            "Thank you for shopping with ShopEase!\n"
            );
            mailSender.send(message);
            logger.info("Order confirmation email sent to: {}", toEmail);
        } catch (Exception e) {
            // Don't crash the order process if email fails - just log it
            logger.error("Failed to send email: {}", e.getMessage());
        }
    }
}