package com.fatec.Pizzaria_Mario.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList; // Adicionado
import java.util.List;    // Adicionado (ou Collection se preferir uma interface mais genérica)

@Document(collection = "users")
@Data
@NoArgsConstructor
public class User {

    @Id
    private String id;

    private String nomeCompleto;

    @Indexed(unique = true)
    private String email;

    private String senha;

    private String telefone;
    private Endereco endereco; // Usando a classe Endereco

    private String resetPasswordToken;
    private LocalDateTime resetPasswordTokenExpiryDate;

    private List<String> roles = new ArrayList<>(); // Inicializado para evitar NullPointerException

    public User(String nomeCompleto, String email, String senha) {
        this.nomeCompleto = nomeCompleto;
        this.email = email;
        this.senha = senha;
        this.roles = new ArrayList<>(); // Garante inicialização
        this.roles.add("USER"); // Adiciona role padrão USER no registro
    }

    // Lombok @Data gera getters e setters. Se não usar, crie-os manualmente.
    // Para roles, se for List<String>:
    // public List<String> getRoles() { return roles; }
    // public void setRoles(List<String> roles) { this.roles = roles; }
}