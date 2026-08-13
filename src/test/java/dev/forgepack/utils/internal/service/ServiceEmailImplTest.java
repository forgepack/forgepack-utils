package dev.forgepack.utils.internal.service;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceEmailImplTest {

    @Mock
    private JavaMailSender mailSender;

    private ServiceEmailImpl service;

    @BeforeEach
    void setUp() {
        service = new ServiceEmailImpl(mailSender);
        ReflectionTestUtils.setField(service, "fromAddress", "noreply@test.com");
    }

    @Test
    void sendSimpleMessage_shouldDelegateToMailSender() {
        service.sendSimpleMessage("user@example.com", "Subject", "Body");
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendSimpleMessage_whenMailSenderFails_shouldThrowRuntimeException() {
        doThrow(new MailSendException("SMTP error")).when(mailSender).send(any(SimpleMailMessage.class));

        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> service.sendSimpleMessage("user@example.com", "Subject", "Body"))
                .withMessageContaining("Failed to send email");
    }

    @Test
    void sendHtmlMessageWithAttachment_shouldDelegateToMailSender() {
        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        service.sendHtmlMessageWithAttachment(
                "user@example.com", "Subject", "<p>Hello</p>",
                new byte[]{1, 2, 3}, "file.pdf", "application/pdf"
        );

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendHtmlMessageWithAttachment_whenMailSenderFails_shouldThrowRuntimeException() {
        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new MailSendException("SMTP error")).when(mailSender).send(any(MimeMessage.class));

        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> service.sendHtmlMessageWithAttachment(
                        "user@example.com", "Subject", "<p>Hello</p>",
                        new byte[]{1, 2, 3}, "file.pdf", "application/pdf"
                ))
                .withMessageContaining("Failed to send email");
    }

    @Test
    void buildWelcomeEmailContent_shouldContainAllCredentials() {
        String content = service.buildWelcomeEmailContent("alice", "pass123", "secretXYZ");
        assertThat(content)
                .contains("alice")
                .contains("pass123")
                .contains("secretXYZ");
    }

    @Test
    void buildWelcomeEmailContent_shouldMentionQrCode() {
        String content = service.buildWelcomeEmailContent("user", "pwd", "secret");
        assertThat(content).containsIgnoringCase("QR Code");
    }
}
