package com.fatec.Pizzaria_Mario.repository;

import com.fatec.Pizzaria_Mario.model.Pizza;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface PizzaRepository extends MongoRepository<Pizza, String> {
    // MongoRepository já fornece métodos como findAll(), findById(), save(), deleteById()
    // Podemos adicionar métodos de busca personalizados aqui se necessário
    List<Pizza> findByDisponivelTrue();
}
