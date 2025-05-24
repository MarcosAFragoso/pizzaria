package com.fatec.Pizzaria_Mario.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AcompanhamentoSelecionado {
    private Acompanhamento acompanhamento; // A referência ao acompanhamento em si
    private int quantidadeAcompanhamento; // Quantidade deste acompanhamento para este item de pizza
}