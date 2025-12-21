package com.ebank.security;

import com.ebank.entities.User;
import com.ebank.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    // Injection de votre UserRepository pour l'accès aux données
    private final UserRepository userRepository;

    /**
     * Méthode principale appelée par Spring Security pour charger les détails d'un utilisateur.
     * @param username Le login de l'utilisateur.
     * @return Un objet UserDetails contenant les informations (login, mot de passe, rôles).
     * @throws UsernameNotFoundException Si l'utilisateur n'existe pas (partie de RG_2).
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // 1. Recherche l'utilisateur dans la base de données
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Login ou mot de passe erronés")); // RG_2

        // 2. Construit la liste des autorités (rôles)
        Collection<? extends GrantedAuthority> authorities = mapRolesToAuthorities(user);

        // 3. Retourne l'objet UserDetails de Spring Security
        // Cet objet sera utilisé pour comparer le mot de passe soumis avec celui crypté en base.
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(), // Le mot de passe crypté de la base de données
                authorities
        );
    }

    /**
     * Convertit le rôle de l'entité User en une collection d'autorités pour Spring Security.
     * Le rôle doit être préfixé par "ROLE_" ou simplement utilisé tel quel pour les autorités.
     * Dans notre cas, nous utilisons le rôle directement comme autorité (ex: "CLIENT", "AGENT_GUICHET").
     */
    private Collection<? extends GrantedAuthority> mapRolesToAuthorities(User user) {
        return Collections.singletonList(new SimpleGrantedAuthority(user.getRole().name()));
    }
}