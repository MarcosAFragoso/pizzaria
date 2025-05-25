package com.fatec.Pizzaria_Mario.repository;

import com.fatec.Pizzaria_Mario.model.Pizza;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface PizzaRepository extends MongoRepository<Pizza, String> {
    // Método para buscar todas as pizzas disponíveis, ordenadas por nome
    List<Pizza> findByDisponivelTrue(Sort sort);

    // O Spring Data MongoDB já fornece findAll(Sort sort)
    // findAll() já é fornecido
}