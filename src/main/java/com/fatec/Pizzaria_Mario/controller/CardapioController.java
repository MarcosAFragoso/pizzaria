package com.fatec.Pizzaria_Mario.controller;

import com.fatec.Pizzaria_Mario.model.Pizza;
import com.fatec.Pizzaria_Mario.repository.PizzaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Controller
public class CardapioController {

    @Autowired
    private PizzaRepository pizzaRepository;

    @GetMapping("/cardapio")
    public String mostrarCardapio(Model model) {
        List<Pizza> pizzas = pizzaRepository.findByDisponivelTrue();

        // Se o banco estiver vazio, podemos adicionar algumas pizzas mock para teste inicial
        if (pizzas.isEmpty()) {
            pizzaRepository.saveAll(Arrays.asList(
                new Pizza(null, "Calabresa", "Molho de tomate, calabresa fatiada, cebola e azeitonas.", Arrays.asList("Calabresa", "Cebola", "Azeitona"), new BigDecimal("30.00"), "/images/pizzas/calabresa.jpg", true),
                new Pizza(null, "Mussarela", "Molho de tomate especial coberto com queijo mussarela.", Arrays.asList("Mussarela", "Molho de tomate"), new BigDecimal("28.00"), "/images/pizzas/mussarela.jpg", true),
                new Pizza(null, "Frango com Catupiry", "Frango desfiado temperado com delicioso catupiry.", Arrays.asList("Frango", "Catupiry"), new BigDecimal("35.00"), "/images/pizzas/frango_catupiry.jpg", true)
            ));
            pizzas = pizzaRepository.findByDisponivelTrue(); // Recarrega as pizzas
        }

        model.addAttribute("pizzas", pizzas);
        return "cardapio"; // Nome do arquivo HTML (sem .html) em templates
    }
}