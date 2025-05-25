package com.fatec.Pizzaria_Mario.repository;

import com.fatec.Pizzaria_Mario.model.Pedido;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.time.LocalDateTime;
import java.util.Collection; // Importar Collection para o In
import java.util.List;

public interface PedidoRepository extends MongoRepository<Pedido, String> {
    List<Pedido> findByClienteIdOrderByDataHoraPedidoDesc(String clienteId);
    long countByStatusIn(List<String> statuses);
    List<Pedido> findByDataHoraPedidoBetween(LocalDateTime start, LocalDateTime end, Sort sort);

    // NOVO MÉTODO PARA O PAINEL DA COZINHA
    List<Pedido> findByStatusInOrderByDataHoraPedidoAsc(Collection<String> statuses);

}