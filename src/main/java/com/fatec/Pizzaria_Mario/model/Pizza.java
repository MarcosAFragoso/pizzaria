package com.fatec.Pizzaria_Mario.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.List;

@Document(collection = "pizzas") // Nome da coleção no MongoDB
@Data // Lombok: Gera getters, setters, toString, equals, hashCode
@NoArgsConstructor // Lombok: Gera construtor sem argumentos
@AllArgsConstructor // Lombok: Gera construtor com todos os argumentos
public class Pizza {

    @Id
    private String id;
    private String nome;
    private String descricao;
    private List<String> ingredientes;
    private BigDecimal preco;
    private String imagemUrl; // Caminho para a imagem da pizza (ex: /images/pizzas/calabresa.jpg)
    private boolean disponivel;

    // Se não usar Lombok @AllArgsConstructor, crie construtores manualmente se necessário
    // Se não usar Lombok @Data, crie getters e setters manualmente
}