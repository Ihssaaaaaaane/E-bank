package com.ebank.services;

import com.ebank.entities.Client;
import com.ebank.entities.PasswordResetToken;
import com.ebank.entities.User;
import com.ebank.repositories.ClientRepository;
import com.ebank.repositories.PasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final ClientRepository clientRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${app.frontend.base-url:http://localhost:3000}")
    private String frontendBaseUrl;

    @Transactional
    public void requestPasswordReset(String email) {
        Optional<Client> clientOpt = clientRepository.findByEmail(email);
        if (clientOpt.isEmpty() || clientOpt.get().getUser() == null) {
            return;
        }

        User user = clientOpt.get().getUser();

        passwordResetTokenRepository.deleteByUser(user);

        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setToken(UUID.randomUUID().toString());
        token.setCreatedAt(LocalDateTime.now());
        token.setExpiresAt(LocalDateTime.now().plusMinutes(30));
        token.setUsed(false);

        passwordResetTokenRepository.save(token);

        String resetLink = frontendBaseUrl + "/reset-password?token=" + token.getToken();
        emailService.sendPasswordResetLink(email, resetLink);
    }

    @Transactional
    public void resetPassword(String tokenValue, String newPassword) {
        PasswordResetToken token = passwordResetTokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new IllegalArgumentException("Lien invalide ou expiré."));

        if (token.isUsed()) {
            throw new IllegalArgumentException("Ce lien a déjà été utilisé.");
        }

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Lien invalide ou expiré.");
        }

        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));

        token.setUsed(true);
        passwordResetTokenRepository.save(token);

        passwordResetTokenRepository.deleteByUser(user);
    }
}
