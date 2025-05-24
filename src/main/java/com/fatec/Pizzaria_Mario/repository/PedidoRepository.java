package com.fatec.Pizzaria_Mario.repository;

import com.fatec.Pizzaria_Mario.model.Pedido;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface PedidoRepository extends MongoRepository<Pedido, String> {
    List<Pedido> findByClienteIdOrderByDataHoraPedidoDesc(String clienteId);
    List<Pedido> findByClienteEmailOrderByDataHoraPedidoDesc(String clienteEmail); // Se o cliente não estiver logado
     long countByStatusIn(List<String> statuses);
    // Adicionar outros métodos de busca conforme necessário (ex: por status)
}