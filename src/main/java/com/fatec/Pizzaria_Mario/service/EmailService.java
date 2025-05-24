package com.fatec.Pizzaria_Mario.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendPasswordResetTokenEmail(String toEmail, String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Pizzaria Mario - Redefinição de Senha");
            // Idealmente, o link deve apontar para o seu frontend/servidor
            String resetUrl = "http://localhost:8080/redefinir-senha?token=" + token; // Ajuste o URL base se necessário
            message.setText("Olá,\n\nVocê solicitou a redefinição da sua senha.\n\n"
                    + "Clique no link abaixo para criar uma nova senha:\n" + resetUrl + "\n\n"
                    + "Se você não solicitou isso, por favor, ignore este e-mail.\n\n"
                    + "Obrigado,\nEquipe Pizzaria Mario");

            mailSender.send(message);
            System.out.println("E-mail de redefinição de senha enviado para: " + toEmail); // Log para console
        } catch (Exception e) {
            // Em um app real, logue o erro de forma mais robusta
            System.err.println("Erro ao enviar e-mail para " + toEmail + ": " + e.getMessage());
            // Você pode querer lançar uma exceção customizada aqui ou tratar o erro de forma diferente
        }
    }
}