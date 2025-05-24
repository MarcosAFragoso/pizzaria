package com.fatec.Pizzaria_Mario.service;

import com.fatec.Pizzaria_Mario.model.User; // Seu model User
import com.fatec.Pizzaria_Mario.repository.UserRepository;
import com.fatec.Pizzaria_Mario.security.CustomUserDetails; // Importe o CustomUserDetails
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority; // Importar
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors; // Importar

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        System.out.println("CustomUserDetailsService: Tentando carregar usuário com e-mail: " + email);
        User appUser = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    System.out.println("CustomUserDetailsService: Usuário NÃO encontrado com e-mail: " + email);
                    return new UsernameNotFoundException("Usuário não encontrado com o e-mail: " + email);
                });
        System.out.println("CustomUserDetailsService: Usuário encontrado: " + appUser.getEmail() + ", Roles: " + appUser.getRoles());

        Collection<? extends GrantedAuthority> authorities;
        if (appUser.getRoles() == null || appUser.getRoles().isEmpty()) {
            authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")); // Role padrão
        } else {
            authorities = appUser.getRoles().stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                    .collect(Collectors.toList());
        }
        
        System.out.println("CustomUserDetailsService: Autoridades para " + appUser.getEmail() + ": " + authorities);

        return new CustomUserDetails(
                appUser.getId(),          // Passando o ID do usuário
                appUser.getEmail(),       // Username (e-mail)
                appUser.getSenha(),       // Senha hasheada
                true,                     // enabled
                true,                     // accountNonExpired
                true,                     // credentialsNonExpired
                true,                     // accountNonLocked
                authorities,              // authorities
                appUser.getNomeCompleto() // nomeCompleto
        );
    }
}