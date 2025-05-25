package com.fatec.Pizzaria_Mario.controller;

import com.fatec.Pizzaria_Mario.model.Pizza;
import com.fatec.Pizzaria_Mario.repository.PizzaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort; // IMPORT CORRIGIDO/ADICIONADO
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
// import java.util.ArrayList; // Removido se não usado diretamente para new ArrayList<>()
import java.util.Arrays;
import java.util.List;

@Controller
public class CardapioController {

    @Autowired
    private PizzaRepository pizzaRepository;

    // Método para popular dados iniciais se o banco estiver vazio
    // Considere usar um CommandLineRunner para esta lógica em vez de dentro do controller.
    private void popularPizzasIniciaisSeVazio() {
        if (pizzaRepository.count() == 0) { 
            Pizza p1 = new Pizza();
            p1.setNome("Calabresa");
            p1.setDescricao("Molho de tomate, calabresa fatiada, cebola e azeitonas.");
            p1.setIngredientes(Arrays.asList("Calabresa", "Cebola", "Azeitona"));
            p1.setPreco(new BigDecimal("30.00"));
            p1.setImagemUrl("/images/pizzas/calabresa.jpg");
            p1.setDisponivel(true);

            Pizza p2 = new Pizza();
            p2.setNome("Mussarela");
            p2.setDescricao("Molho de tomate especial coberto com queijo mussarela.");
            p2.setIngredientes(Arrays.asList("Mussarela", "Molho de tomate"));
            p2.setPreco(new BigDecimal("28.00"));
            p2.setImagemUrl("/images/pizzas/mussarela.jpg");
            p2.setDisponivel(true);

            Pizza p3 = new Pizza();
            p3.setNome("Frango com Catupiry");
            p3.setDescricao("Frango desfiado temperado com delicioso catupiry.");
            p3.setIngredientes(Arrays.asList("Frango", "Catupiry"));
            p3.setPreco(new BigDecimal("35.00"));
            p3.setImagemUrl("/images/pizzas/frango_catupiry.jpg");
            p3.setDisponivel(true);

            pizzaRepository.saveAll(Arrays.asList(p1, p2, p3));
            System.out.println(">>> Pizzas iniciais populadas no banco de dados.");
        }
    }

    @GetMapping("/cardapio")
    public String mostrarCardapio(Model model) {
        popularPizzasIniciaisSeVazio(); 

        List<Pizza> pizzas = pizzaRepository.findByDisponivelTrue(Sort.by(Sort.Direction.ASC, "nome"));
        
        model.addAttribute("pizzas", pizzas);
        return "cardapio"; 
    }
}