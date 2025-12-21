// src/main/java/com/ebank/services/UserService.java (Nouveau Service)
package com.ebank.services;

import com.ebank.dtos.ChangePasswordRequest;
import com.ebank.entities.User;
import com.ebank.exceptions.InvalidCredentialsException;
import com.ebank.exceptions.ResourceNotFoundException;
import com.ebank.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {

        // 1. Trouver l'utilisateur
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé."));

        // 2. Vérifier le mot de passe actuel (RG: Sécurité)
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Le mot de passe actuel est incorrect.");
        }

        // 3. Vérifier que le nouveau mot de passe est différent
        if (request.getCurrentPassword().equals(request.getNewPassword())) {
            throw new InvalidCredentialsException("Le nouveau mot de passe doit être différent de l'ancien.");
        }

        // 4. Encoder et enregistrer le nouveau mot de passe
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        // Pas besoin d'appeler userRepository.save(user) grâce à @Transactional et l'état managed de 'user'
    }
}