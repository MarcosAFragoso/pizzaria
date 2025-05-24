package com.fatec.Pizzaria_Mario.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List; // Importar List

@Data
@NoArgsConstructor
public class ItemPedido {
    // ... (campos existentes: pizza1, pizza2, tipo, quantidade, precoCalculado, observacoes) ...
    private Pizza pizza1;
    private Pizza pizza2;
    private String tipo;
    private int quantidade;
    private BigDecimal precoCalculado;
    private String observacoes;

    // NOVO: Lista para acompanhamentos/adicionais deste item de pizza
    private List<AcompanhamentoSelecionado> acompanhamentosSelecionados = new ArrayList<>();

    // Construtor para pizza inteira
    public ItemPedido(Pizza pizza, int quantidade) {
        this.pizza1 = pizza;
        this.tipo = "INTEIRA";
        this.quantidade = quantidade;
        this.precoCalculado = pizza.getPreco().multiply(new BigDecimal(quantidade));
    }

    // Construtor para pizza metade/metade
    public ItemPedido(Pizza pizza1, Pizza pizza2, int quantidade) {
        this.pizza1 = pizza1;
        this.pizza2 = pizza2;
        this.tipo = "METADE_METADE";
        this.quantidade = quantidade;
        BigDecimal precoMetade1 = pizza1.getPreco().divide(new BigDecimal(2));
        BigDecimal precoMetade2 = pizza2.getPreco().divide(new BigDecimal(2));
        this.precoCalculado = (precoMetade1.add(precoMetade2)).multiply(new BigDecimal(quantidade));
    }

    public String getNomeExibicao() {
        if ("INTEIRA".equals(tipo) && pizza1 != null) {
            return pizza1.getNome();
        } else if ("METADE_METADE".equals(tipo) && pizza1 != null && pizza2 != null) {
            return "Metade " + pizza1.getNome() + ", Metade " + pizza2.getNome();
        }
        return "Item inválido";
    }

    // Método para recalcular o preço do item incluindo acompanhamentos
    public void recalcularPrecoTotalItem() {
        BigDecimal precoBasePizza;
        if ("INTEIRA".equals(tipo) && pizza1 != null) {
            precoBasePizza = pizza1.getPreco();
        } else if ("METADE_METADE".equals(tipo) && pizza1 != null && pizza2 != null) {
            BigDecimal precoMetade1 = pizza1.getPreco().divide(new BigDecimal(2));
            BigDecimal precoMetade2 = pizza2.getPreco().divide(new BigDecimal(2));
            precoBasePizza = precoMetade1.add(precoMetade2);
        } else {
            precoBasePizza = BigDecimal.ZERO;
        }

        BigDecimal precoTotalAcompanhamentos = acompanhamentosSelecionados.stream()
                .map(as -> as.getAcompanhamento().getPreco().multiply(new BigDecimal(as.getQuantidadeAcompanhamento())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        this.precoCalculado = (precoBasePizza.add(precoTotalAcompanhamentos)).multiply(new BigDecimal(this.quantidade));
    }
}