package com.fatec.Pizzaria_Mario.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Document(collection = "acompanhamentos") // Salvaremos as opções de acompanhamento no banco
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Acompanhamento {
    @Id
    private String id;
    private String nome;
    private String descricao;
    private BigDecimal preco;
    private String tipo; // Ex: "PORCAO", "ADICIONAL_PIZZA", "BEBIDA_REFRIGERANTE", "BEBIDA_ALCOOLICA"
    private String imagemUrl; // Opcional
    private boolean disponivel;
}