package com.fatec.Pizzaria_Mario.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PizzariaHomeController {

    @GetMapping("/")
    public String homePage() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null &&
            authentication.isAuthenticated() &&
            !(authentication.getPrincipal() instanceof String &&
              authentication.getPrincipal().equals("anonymousUser"))) {
            
            return "redirect:/cardapio"; // Redireciona para o cardápio se estiver logado
        }
        
        return "index_pizzaria"; // Mostra a página inicial normal para não logados
    }
}