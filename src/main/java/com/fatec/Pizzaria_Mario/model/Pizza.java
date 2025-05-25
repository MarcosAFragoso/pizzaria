package com.fatec.Pizzaria_Mario.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
// import jakarta.validation.constraints.NotEmpty; // Removido se não usado
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "pizzas")
@Data
@NoArgsConstructor
public class Pizza {

    @Id
    private String id;

    @NotBlank(message = "O nome da pizza é obrigatório.")
    @Size(min = 3, max = 100, message = "O nome da pizza deve ter entre 3 e 100 caracteres.")
    private String nome;

    @Size(max = 255, message = "A descrição pode ter no máximo 255 caracteres.")
    private String descricao;

    private List<String> ingredientes = new ArrayList<>();

    @NotNull(message = "O preço é obrigatório.")
    @DecimalMin(value = "0.01", message = "O preço deve ser maior que zero.")
    private BigDecimal preco;

    @NotBlank(message = "O caminho da imagem é obrigatório.")
    private String imagemUrl; 

    private boolean disponivel = true;
}