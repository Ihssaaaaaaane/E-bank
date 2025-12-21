package com.ebank.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

// SERVICE pour RG_7
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String from;

    public void sendLoginCredentials(String recipientEmail, String username, String password) {
        if (from == null || from.isBlank()) {
            throw new IllegalStateException("Configuration email manquante: app.mail.from (ou GMAIL_USERNAME).");
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(recipientEmail);
        message.setFrom(from);
        message.setSubject("Vos identifiants eBank (RG_7)");

        message.setText(
                "Cher Client,\n\n" +
                "Votre compte a été créé.\n\n" +
                "Login: " + username + "\n" +
                "Mot de passe temporaire: " + password + "\n\n" +
                "Veuillez vous connecter et changer votre mot de passe dès que possible.\n\n" +
                "Cordialement,\n" +
                "eBank"
        );

        try {
            mailSender.send(message);
        } catch (MailException ex) {
            throw new RuntimeException("Échec de l'envoi de l'email (RG_7). Vérifiez la configuration SMTP.", ex);
        }
    }

    public void sendPasswordResetLink(String recipientEmail, String resetLink) {
        if (from == null || from.isBlank()) {
            throw new IllegalStateException("Configuration email manquante: app.mail.from (ou GMAIL_USERNAME).");
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(recipientEmail);
        message.setFrom(from);
        message.setSubject("Réinitialisation de votre mot de passe eBank");

        message.setText(
                "Bonjour,\n\n" +
                "Vous avez demandé la réinitialisation de votre mot de passe.\n\n" +
                "Cliquez sur le lien suivant pour définir un nouveau mot de passe (valide 30 minutes) :\n" +
                resetLink + "\n\n" +
                "Si vous n'êtes pas à l'origine de cette demande, ignorez cet email.\n\n" +
                "Cordialement,\n" +
                "eBank"
        );

        try {
            mailSender.send(message);
        } catch (MailException ex) {
            throw new RuntimeException("Échec de l'envoi de l'email de réinitialisation. Vérifiez la configuration SMTP.", ex);
        }
    }
}