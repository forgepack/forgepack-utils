package dev.forgepack.utils.api.service;

/**
 * Contract for email delivery services.
 *
 * <p>Provides operations for sending plain text and HTML emails, including support
 * for attachments and dynamic content generation.</p>
 *
 * <p>Implementations are expected to handle underlying transport concerns
 * (e.g., SMTP, third-party providers), encoding, and error handling.</p>
 *
 * @author Marcelo Ribeiro Gadelha
 * @since 1.0
 * @see <a href="http://www.gadelha.eti.br">www.gadelha.eti.br</a>
 */
public interface EmailService {

    /**
     * Sends a simple plain text email message.
     *
     * @param to recipient email address
     * @param subject subject of the email
     * @param text plain text content of the email body
     * @throws RuntimeException if the message cannot be sent
     */
    void sendSimpleMessage(String to, String subject, String text);

    /**
     * Sends an HTML email message with a single attachment.
     *
     * @param to recipient email address
     * @param subject subject of the email
     * @param htmlContent HTML content of the email body
     * @param attachmentData binary data of the attachment
     * @param attachmentName file name of the attachment
     * @param mimeType MIME type of the attachment (e.g., "application/pdf", "image/png")
     * @throws RuntimeException if the message cannot be sent or attachment processing fails
     */
    void sendHtmlMessageWithAttachment(String to, String subject, String htmlContent,
                                       byte[] attachmentData, String attachmentName, String mimeType);

    /**
     * Builds the HTML content for a welcome email.
     *
     * <p>Typically used during user onboarding to provide initial credentials
     * and additional security information.</p>
     *
     * @param username the user's login identifier
     * @param password the user's initial or temporary password
     * @param secret secret value for additional security setup (e.g., MFA/OTP seed)
     * @return a formatted HTML string representing the email body
     */
    String buildWelcomeEmailContent(String username, String password, String secret);
}
