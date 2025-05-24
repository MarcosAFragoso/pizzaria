package com.fatec.Pizzaria_Mario.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.util.StringUtils; // Import para StringUtils

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Endereco {
    private String cep;
    private String rua;
    private String numero;
    private String complemento;
    private String bairro;
    private String cidade;
    private String estado;

    public boolean isVazio() {
        return !StringUtils.hasText(cep) &&
               !StringUtils.hasText(rua) &&
               !StringUtils.hasText(numero) &&
               !StringUtils.hasText(bairro) &&
               !StringUtils.hasText(cidade) &&
               !StringUtils.hasText(estado);
    }
}