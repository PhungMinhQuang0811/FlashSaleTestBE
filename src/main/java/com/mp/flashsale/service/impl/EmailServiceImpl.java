package com.mp.flashsale.service.impl;

import com.mp.flashsale.exception.AppException;
import com.mp.flashsale.exception.ErrorCode;
import com.mp.flashsale.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmailServiceImpl implements EmailService {
    JavaMailSender mailSender;

    @Value("${application.email}")
    @NonFinal
    String fromEmail;
    @Override
    public void sendRegisterEmail(String to, String confirmUrl) {
        String subject = "Welcome to FlashSale - Verify Your Email";
        String htmlContent = "<h3>Thank you for registering!</h3>"
                + "<p>To complete your registration and start hunting deals, please verify your email by clicking the link below:</p>"
                + "<p><a href=\"" + confirmUrl + "\" style=\"color: #ff4d4f; font-weight: bold;\">Verify My Account</a></p>"
                + "<p>If you did not sign up for this service, please ignore this email.</p>";
        executeSend(to, subject, htmlContent, ErrorCode.SEND_VERIFY_EMAIL_TO_USER_FAIL);
    }

    @Override
    public void sendForgotPasswordEmail(String to, String forgotPasswordUrl) {
        String subject = "FlashSale - Password Reset Request";
        String htmlContent = "<p>We received a request to reset your password.</p>"
                + "<p>Please click <a href=\"" + forgotPasswordUrl + "\">here</a> to reset your password.</p>"
                + "<p>For your security, this link will expire in 24 hours.</p>"
                + "<p>If you didn't request this, please ignore this email.</p>";

        executeSend(to, subject, htmlContent, ErrorCode.SEND_FORGOT_PASSWORD_EMAIL_TO_USER_FAIL);
    }

    @Override
    public void sendOrderSuccessEmail(String toCustomer, String itemName, String orderNumber, long price) {
        String subject = "Order Confirmed - #" + orderNumber;
        String htmlContent = "<h3>Congratulations!</h3>"
                + "<p>You have successfully secured a deal for: <strong>" + itemName + "</strong>.</p>"
                + "<p>Order Number: <strong>" + orderNumber + "</strong></p>"
                + "<p>Total Amount: <strong>" + price + " VND</strong></p>"
                + "<p>Thank you for shopping with FlashSale!</p>";

        executeSend(toCustomer, subject, htmlContent, ErrorCode.SEND_ORDER_SUCCESS_EMAIL_FAIL);
    }

    @Override
    public void sendNewOrderNotificationToSeller(String toSeller, String itemName, String orderNumber) {
        String subject = "New Flash Sale Order - #" + orderNumber;
        String htmlContent = "<h3>You have a new order!</h3>"
                + "<p>Your item <strong>" + itemName + "</strong> has been purchased.</p>"
                + "<p>Order Number: <strong>" + orderNumber + "</strong></p>"
                + "<p>Please process the shipment as soon as possible.</p>";

        executeSend(toSeller, subject, htmlContent, ErrorCode.SEND_NEW_ORDER_NOTIFICATION_FAIL);
    }

    @Override
    public void sendWalletUpdateEmail(String to, String walletUrl) {
        String subject = "Wallet Balance Updated";
        String htmlContent = String.format(
                "<p>Your wallet balance has been updated at <strong>%s</strong>.</p>"
                        + "<p>Please check your <a href=\"%s\">wallet dashboard</a> for transaction details.</p>"
                        + "<p>Thank you!</p>",
                getCurrentFormattedDateTime(), walletUrl);

        executeSend(to, subject, htmlContent, ErrorCode.SEND_WALLET_UPDATE_EMAIL_FAIL);
    }
    @Async
    public void sendEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
        helper.setTo(to);
        helper.setFrom(fromEmail);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);
        mailSender.send(mimeMessage);
    }
    private String getCurrentFormattedDateTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
    }
    private void executeSend(String to, String subject, String body, ErrorCode errorCode) {
        try {
            sendEmail(to, subject, body);
        } catch (MessagingException e) {
            log.error("Email error: {}", e.getMessage());
            throw new AppException(errorCode);
        }
    }
}
