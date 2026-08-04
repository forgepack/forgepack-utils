package dev.forgepack.utils.internal.service;

import dev.forgepack.utils.api.service.ServiceEmail;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class ServiceEmailImpl implements ServiceEmail {

    private final JavaMailSender emailSender;
    private static final Logger log = LoggerFactory.getLogger(ServiceEmailImpl.class);

    public ServiceEmailImpl(JavaMailSender emailSender) {
        this.emailSender = emailSender;
    }

    public void sendSimpleMessage(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noreply@gadelha.eti.br");
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            emailSender.send(message);
            log.info("Email sent successfully to: {} with subject: {}", to, subject);
        } catch (Exception e) {
            log.error("Failed to send email to: {} with subject: {} - Error: {}", to, subject, e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        }
    }

    public void sendHtmlMessageWithAttachment(String to, String subject, String htmlContent, byte[] attachmentData, String attachmentName, String mimeType) {
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom("noreply@gadelha.eti.br");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            helper.addAttachment(attachmentName, new ByteArrayResource(attachmentData), mimeType);
            emailSender.send(message);
            log.info("HTML email with attachment sent successfully to: {} with subject: {}", to, subject);
        } catch (MessagingException e) {
            log.error("Failed to send HTML email with attachment to: {} with subject: {} - Error: {}", to, subject, e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        }
    }
    public String buildWelcomeEmailContent(String username, String password, String secret) {
        return String.format("""
            <p><strong>Username:</strong> %s</p>
            <p><strong>Password:</strong> %s</p>
            <p><strong>Secret:</strong> %s</p>
            <p><strong>TOTP QR Code:</strong> Veja o anexo "qrcode.png"</p>
            <p>Escaneie o QR Code com seu aplicativo autenticador (Google Authenticator, Authy, etc.)</p>
            """, username, password, secret);
    }
}
