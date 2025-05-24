package com.fatec.Pizzaria_Mario.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.math.RoundingMode; // IMPORT ADICIONADO
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "pedidos")
@Data
@NoArgsConstructor
public class Pedido {

    @Id
    private String id;

    private String clienteId;
    private String clienteNome;
    private String clienteEmail;
    private String clienteTelefone;
    private Endereco enderecoEntrega;
    private List<ItemPedidoDetalhe> itens = new ArrayList<>();
    private BigDecimal valorTotal;
    private String formaPagamento;
    private String observacoesPagamento;
    private String tipoPedido;
    private Integer numeroMesa;
    private Integer numeroPessoas;
    private String status;
    private LocalDateTime dataHoraPedido;
    private LocalDateTime dataHoraUltimaAtualizacao;

    @Data
    @NoArgsConstructor
    public static class ItemPedidoDetalhe {
        private String nomeExibicao;
        private int quantidade;
        private BigDecimal precoCalculadoItemUnico;
        private BigDecimal precoTotalItem;
        private String observacoesItem;
        private List<String> acompanhamentosDoItem = new ArrayList<>();

        public ItemPedidoDetalhe(ItemPedido item) {
            this.nomeExibicao = item.getNomeExibicao();
            this.quantidade = item.getQuantidade();
            if (item != null && item.getQuantidade() > 0 && item.getPrecoCalculado() != null) {
                // DIVISÃO CORRIGIDA COM RoundingMode.HALF_UP
                this.precoCalculadoItemUnico = item.getPrecoCalculado().divide(new BigDecimal(item.getQuantidade()), 2, RoundingMode.HALF_UP);
            } else {
                this.precoCalculadoItemUnico = (item != null && item.getPrecoCalculado() != null) ? item.getPrecoCalculado() : BigDecimal.ZERO;
            }
            this.precoTotalItem = item != null ? item.getPrecoCalculado() : BigDecimal.ZERO; // Adicionado null check
            this.observacoesItem = item != null ? item.getObservacoes() : null; // Adicionado null check
            if (item != null && item.getAcompanhamentosSelecionados() != null) {
                for (AcompanhamentoSelecionado as : item.getAcompanhamentosSelecionados()) {
                    if (as != null && as.getAcompanhamento() != null) {
                       this.acompanhamentosDoItem.add(as.getAcompanhamento().getNome() + " (x" + as.getQuantidadeAcompanhamento() + ")");
                    }
                }
            }
        }
    }

    public Pedido(String clienteNome, String clienteEmail, String clienteTelefone, Endereco enderecoEntrega,
                  List<ItemPedido> itensDoCarrinho, BigDecimal valorTotal, String formaPagamento, String observacoesPagamento,
                  String tipoPedido, Integer numeroMesa, Integer numeroPessoas) {
        this.clienteNome = clienteNome;
        this.clienteEmail = clienteEmail;
        this.clienteTelefone = clienteTelefone;
        this.tipoPedido = tipoPedido;

        if ("ENTREGA".equals(tipoPedido)) {
            this.enderecoEntrega = enderecoEntrega;
        } else {
            this.enderecoEntrega = null;
            this.numeroMesa = numeroMesa;
            this.numeroPessoas = numeroPessoas;
        }

        this.valorTotal = valorTotal;
        this.formaPagamento = formaPagamento;
        this.observacoesPagamento = observacoesPagamento;
        this.status = "RECEBIDO";
        if ("LOCAL".equals(tipoPedido)) {
            this.status = "AGUARDANDO_PREPARO_MESA";
        }
        this.dataHoraPedido = LocalDateTime.now();
        this.dataHoraUltimaAtualizacao = LocalDateTime.now();

        if (itensDoCarrinho != null) {
            for (ItemPedido itemCarrinho : itensDoCarrinho) {
                if (itemCarrinho != null) {
                    this.itens.add(new ItemPedidoDetalhe(itemCarrinho));
                }
            }
        }
    }
}