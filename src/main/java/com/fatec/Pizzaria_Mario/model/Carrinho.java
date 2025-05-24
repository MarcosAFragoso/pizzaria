package com.fatec.Pizzaria_Mario.model;

import lombok.Data;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
@SessionScope // Este bean viverá durante a sessão HTTP do usuário
@Data
public class Carrinho implements Serializable { // Serializable é importante para SessionScope
    private static final long serialVersionUID = 1L;

    private List<ItemPedido> itens = new ArrayList<>();
    private BigDecimal total = BigDecimal.ZERO;

    
    public void adicionarItem(ItemPedido item) {
    this.itens.add(item);
    recalcularTotal(); // Certifique-se que este método está funcionando corretamente
    System.out.println("ItemPedido adicionado ao Carrinho (dentro do bean): " + item.getNomeExibicao() + " - Total atual do carrinho: " + this.total); // DEBUG
}

    public void removerItem(int index) {
        if (index >= 0 && index < itens.size()) {
            this.itens.remove(index);
            recalcularTotal();
        }
    }

    public void limparCarrinho() {
        this.itens.clear();
        this.total = BigDecimal.ZERO;
    }

    private void recalcularTotal() {
        this.total = itens.stream()
                          .map(ItemPedido::getPrecoCalculado)
                          .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}