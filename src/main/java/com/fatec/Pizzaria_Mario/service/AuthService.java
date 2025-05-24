package com.fatec.Pizzaria_Mario.service;

import com.fatec.Pizzaria_Mario.model.Endereco; // Importar Endereco
import com.fatec.Pizzaria_Mario.model.User;
import com.fatec.Pizzaria_Mario.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List; // Adicionado para roles
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired(required = false) // Para não quebrar se EmailService não estiver configurado
    private EmailService emailService;

    private static final int TOKEN_EXPIRY_HOURS = 2;

    public User registerUser(String nomeCompleto, String email, String senhaNaoHasheada) throws Exception {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new Exception("E-mail já cadastrado: " + email);
        }
        String senhaHasheada = passwordEncoder.encode(senhaNaoHasheada);
        User newUser = new User(nomeCompleto, email, senhaHasheada);
        // Atribuir role padrão USER. Se for um e-mail admin específico, adicionar ROLE_ADMIN
        if ("admin@pizzaria.com".equalsIgnoreCase(email)) { // Defina seu e-mail de admin
            newUser.setRoles(List.of("USER", "ADMIN"));
        } else {
            newUser.setRoles(List.of("USER"));
        }
        return userRepository.save(newUser);
    }

    public void generatePasswordResetToken(String email) throws Exception {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            System.out.println("Tentativa de reset para e-mail não encontrado: " + email);
            return;
        }
        User user = userOptional.get();
        String token = UUID.randomUUID().toString();
        user.setResetPasswordToken(token);
        user.setResetPasswordTokenExpiryDate(LocalDateTime.now().plusHours(TOKEN_EXPIRY_HOURS));
        userRepository.save(user);
        if (emailService != null) {
          emailService.sendPasswordResetTokenEmail(user.getEmail(), token);
        } else {
          System.out.println("SIMULAÇÃO: E-mail de reset de senha seria enviado para " + user.getEmail() + " com token " + token);
        }
    }

    public boolean validatePasswordResetToken(String token) {
        Optional<User> userOptional = userRepository.findByResetPasswordToken(token);
        if (userOptional.isEmpty()) return false;
        User user = userOptional.get();
        return user.getResetPasswordTokenExpiryDate() != null && user.getResetPasswordTokenExpiryDate().isAfter(LocalDateTime.now());
    }

    public void resetPassword(String token, String novaSenhaNaoHasheada) throws Exception {
        if (!validatePasswordResetToken(token)) {
            throw new Exception("Token de redefinição inválido ou expirado.");
        }
        User user = userRepository.findByResetPasswordToken(token)
                .orElseThrow(() -> new Exception("Usuário não encontrado para o token."));
        user.setSenha(passwordEncoder.encode(novaSenhaNaoHasheada));
        user.setResetPasswordToken(null);
        user.setResetPasswordTokenExpiryDate(null);
        userRepository.save(user);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User updateUserProfile(String emailAutenticado, String novoNomeCompleto, String novoEmail,
                                  String senhaAtual, String novaSenha, String confirmarNovaSenha) throws Exception {
        User user = userRepository.findByEmail(emailAutenticado)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário autenticado não encontrado."));

        if (StringUtils.hasText(novoNomeCompleto) && !novoNomeCompleto.equals(user.getNomeCompleto())) {
            user.setNomeCompleto(novoNomeCompleto);
        }
        if (StringUtils.hasText(novoEmail) && !novoEmail.equalsIgnoreCase(user.getEmail())) {
            Optional<User> existingUserWithNewEmail = userRepository.findByEmail(novoEmail);
            if (existingUserWithNewEmail.isPresent() && !existingUserWithNewEmail.get().getId().equals(user.getId())) {
                throw new IllegalArgumentException("O novo e-mail fornecido já está em uso.");
            }
            user.setEmail(novoEmail);
        }
        boolean tentarAlterarSenha = StringUtils.hasText(novaSenha) || StringUtils.hasText(confirmarNovaSenha) || StringUtils.hasText(senhaAtual);
        if (tentarAlterarSenha) {
            if (!StringUtils.hasText(senhaAtual)) throw new IllegalArgumentException("Senha atual é obrigatória para alterar a senha.");
            if (!passwordEncoder.matches(senhaAtual, user.getSenha())) throw new IllegalArgumentException("Senha atual incorreta.");
            if (!StringUtils.hasText(novaSenha) || !StringUtils.hasText(confirmarNovaSenha)) throw new IllegalArgumentException("Nova senha e confirmação são obrigatórias para alterar.");
            if (!novaSenha.equals(confirmarNovaSenha)) throw new IllegalArgumentException("Nova senha e confirmação não coincidem.");
            user.setSenha(passwordEncoder.encode(novaSenha));
        }
        return userRepository.save(user);
    }

    public void atualizarEnderecoEtelefoneUsuario(String emailAutenticado, Endereco novoEndereco, String novoTelefone) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(emailAutenticado)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado para atualizar endereço/telefone: " + emailAutenticado));
        boolean modificado = false;
        if (novoEndereco != null && !novoEndereco.isVazio()) {
            user.setEndereco(novoEndereco);
            modificado = true;
        }
        if (StringUtils.hasText(novoTelefone)) {
            user.setTelefone(novoTelefone);
            modificado = true;
        }
        if (modificado) {
            userRepository.save(user);
        }
    }
}