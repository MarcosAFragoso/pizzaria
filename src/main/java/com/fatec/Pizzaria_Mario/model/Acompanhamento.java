package com.fatec.Pizzaria_Mario.model;

import jakarta.validation.constraints.DecimalMin; // IMPORT
import jakarta.validation.constraints.NotBlank;  // IMPORT
import jakarta.validation.constraints.NotNull;   // IMPORT
import jakarta.validation.constraints.Size;     // IMPORT
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Document(collection = "acompanhamentos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Acompanhamento {
    @Id
    private String id;

    @NotBlank(message = "O nome do acompanhamento é obrigatório.")
    @Size(min = 3, max = 100, message = "O nome deve ter entre 3 e 100 caracteres.")
    private String nome;

    @Size(max = 255, message = "A descrição pode ter no máximo 255 caracteres.")
    private String descricao; // Pode ser opcional

    @NotNull(message = "O preço é obrigatório.")
    @DecimalMin(value = "0.01", message = "O preço deve ser maior que zero.")
    private BigDecimal preco;

    @NotBlank(message = "O tipo do acompanhamento é obrigatório.") // Ex: PORCAO, ADICIONAL_PIZZA, BEBIDA_REFRIGERANTE
    private String tipo; 

    private String imagemUrl; // Opcional, pode ser deixado em branco

    private boolean disponivel = true; // Padrão para disponível
}