package com.fatec.Pizzaria_Mario.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.math.RoundingMode; // IMPORT NECESSÁRIO
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class ItemPedido {
    private Pizza pizza1;
    private Pizza pizza2;
    private String tipo; 
    private int quantidade;
    private BigDecimal precoCalculado;
    private String observacoes;
    private String nomeExibicao; 
    private List<AcompanhamentoSelecionado> acompanhamentosSelecionados = new ArrayList<>();

    public ItemPedido(Pizza pizza, int quantidade) {
        this.pizza1 = pizza;
        this.tipo = "INTEIRA"; 
        this.quantidade = Math.max(1, quantidade); // Garante que quantidade seja pelo menos 1
        if (pizza != null && pizza.getPreco() != null) {
            this.precoCalculado = pizza.getPreco().multiply(new BigDecimal(this.quantidade));
        } else {
            this.precoCalculado = BigDecimal.ZERO;
        }
    }

    public ItemPedido(Pizza pizza1, Pizza pizza2, int quantidade) {
        this.pizza1 = pizza1;
        this.pizza2 = pizza2;
        this.tipo = "METADE_METADE";
        this.quantidade = Math.max(1, quantidade); // Garante que quantidade seja pelo menos 1
        if (pizza1 != null && pizza1.getPreco() != null && pizza2 != null && pizza2.getPreco() != null) {
            BigDecimal precoMetade1 = pizza1.getPreco().divide(new BigDecimal(2), 2, RoundingMode.HALF_UP); // Adicionado scale e rounding
            BigDecimal precoMetade2 = pizza2.getPreco().divide(new BigDecimal(2), 2, RoundingMode.HALF_UP); // Adicionado scale e rounding
            this.precoCalculado = (precoMetade1.add(precoMetade2)).multiply(new BigDecimal(this.quantidade));
        } else {
            this.precoCalculado = BigDecimal.ZERO;
        }
    }

    public String getNomeExibicao() {
        if (this.nomeExibicao != null && !this.nomeExibicao.isEmpty()) {
            return this.nomeExibicao;
        }
        if ("INTEIRA".equals(tipo) && pizza1 != null) {
            return pizza1.getNome();
        } else if ("METADE_METADE".equals(tipo) && pizza1 != null && pizza2 != null) {
            return "Metade " + pizza1.getNome() + ", Metade " + pizza2.getNome();
        }
        if ("BEBIDA".equals(tipo)) { // Se nomeExibicao não foi setado, mas é uma bebida
            return "Bebida"; // Fallback genérico
        }
        return "Item"; // Fallback mais genérico ainda
    }

    public void recalcularPrecoTotalItem() {
        BigDecimal precoBase;
        int qtd = Math.max(1, this.quantidade); // Garante que qtd seja pelo menos 1

        if ("INTEIRA".equals(tipo) && pizza1 != null && pizza1.getPreco() != null) {
            precoBase = pizza1.getPreco();
        } else if ("METADE_METADE".equals(tipo) && pizza1 != null && pizza1.getPreco() != null && pizza2 != null && pizza2.getPreco() != null) {
            BigDecimal precoMetade1 = pizza1.getPreco().divide(new BigDecimal(2), 2, RoundingMode.HALF_UP);
            BigDecimal precoMetade2 = pizza2.getPreco().divide(new BigDecimal(2), 2, RoundingMode.HALF_UP);
            precoBase = precoMetade1.add(precoMetade2);
        } else if ("BEBIDA".equals(tipo) || "ACOMPANHAMENTO".equals(tipo)) {
            // Se precoCalculado já foi o total (precoUnidade * qtd), precisamos do preço unitário para 'precoBase'
            if (this.precoCalculado != null) {
                precoBase = this.precoCalculado.divide(new BigDecimal(qtd), 2, RoundingMode.HALF_UP);
            } else {
                precoBase = BigDecimal.ZERO; // Ou buscar o preço unitário do Acompanhamento original se tiver referência
            }
        } else {
            precoBase = BigDecimal.ZERO;
        }

        BigDecimal precoTotalAcompanhamentos = BigDecimal.ZERO;
        if (acompanhamentosSelecionados != null) {
            precoTotalAcompanhamentos = acompanhamentosSelecionados.stream()
                    .filter(as -> as != null && as.getAcompanhamento() != null && as.getAcompanhamento().getPreco() != null)
                    .map(as -> as.getAcompanhamento().getPreco().multiply(new BigDecimal(as.getQuantidadeAcompanhamento())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        
        if ("BEBIDA".equals(tipo)) {
            // Para bebida, precoCalculado já deve ser o total (precoBase * qtd)
            // Não somamos acompanhamentos a uma bebida neste modelo.
            this.precoCalculado = precoBase.multiply(new BigDecimal(qtd));
        } else {
            // Para pizzas, soma o preço base da pizza com os acompanhamentos e multiplica pela quantidade da pizza
            this.precoCalculado = (precoBase.add(precoTotalAcompanhamentos)).multiply(new BigDecimal(qtd));
        }
    }
}