package com.fatec.Pizzaria_Mario.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
// import org.springframework.data.mongodb.core.mapping.Field; // REMOVIDO IMPORT NÃO USADO

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "pedidos")
@Data
@NoArgsConstructor
public class Pedido {

    @Id
    private String id;
    private Long numeroPedidoExibicao; 
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
            if (item == null) return; // Proteção contra item nulo

            this.nomeExibicao = item.getNomeExibicao();
            this.quantidade = item.getQuantidade();
            
            if (item.getQuantidade() > 0 && item.getPrecoCalculado() != null) {
                this.precoCalculadoItemUnico = item.getPrecoCalculado().divide(new BigDecimal(item.getQuantidade()), 2, RoundingMode.HALF_UP);
            } else {
                this.precoCalculadoItemUnico = item.getPrecoCalculado() != null ? item.getPrecoCalculado() : BigDecimal.ZERO;
            }
            this.precoTotalItem = item.getPrecoCalculado() != null ? item.getPrecoCalculado() : BigDecimal.ZERO; 
            this.observacoesItem = item.getObservacoes(); 
            if (item.getAcompanhamentosSelecionados() != null) {
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
                if (itemCarrinho != null) { // Adicionado null check para itemCarrinho
                    this.itens.add(new ItemPedidoDetalhe(itemCarrinho));
                }
            }
        }
    }
}