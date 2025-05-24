package com.fatec.Pizzaria_Mario.repository;

import com.fatec.Pizzaria_Mario.model.Acompanhamento;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface AcompanhamentoRepository extends MongoRepository<Acompanhamento, String> {
    List<Acompanhamento> findByDisponivelTrue();
    List<Acompanhamento> findByTipoAndDisponivelTrue(String tipo);
}