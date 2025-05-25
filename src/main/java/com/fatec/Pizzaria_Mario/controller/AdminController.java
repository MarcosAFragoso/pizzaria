package com.fatec.Pizzaria_Mario.controller;

import com.fatec.Pizzaria_Mario.model.Pedido;
import com.fatec.Pizzaria_Mario.model.Pizza; // IMPORT ADICIONADO
import com.fatec.Pizzaria_Mario.repository.PedidoRepository;
import com.fatec.Pizzaria_Mario.repository.PizzaRepository; // IMPORT ADICIONADO
import jakarta.validation.Valid; // IMPORT ADICIONADO
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult; // IMPORT ADICIONADO
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute; // IMPORT ADICIONADO
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList; // IMPORT ADICIONADO
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired // INJEÇÃO DO PIZZA REPOSITORY
    private PizzaRepository pizzaRepository;

    // Constantes de Status (como estavam antes)
    private static final List<String> ORDEM_STATUS_FLUXO = Arrays.asList(
            "RECEBIDO", "AGUARDANDO_PREPARO_MESA", "EM_PREPARO",
            "PRONTO_NA_MESA", "PRONTO_PARA_ENTREGA",
            "SAIU_PARA_ENTREGA", "PAGO_MESA", "ENTREGUE", "CANCELADO"
    );
    private static final List<String> STATUS_COZINHA_ATIVOS = Arrays.asList(
            "RECEBIDO", "AGUARDANDO_PREPARO_MESA", "EM_PREPARO"
    );
    private static final List<String> TODOS_STATUS_POSSIVEIS = Arrays.asList(
            "RECEBIDO", "AGUARDANDO_PREPARO_MESA", "EM_PREPARO", 
            "PRONTO_NA_MESA", "PRONTO_PARA_ENTREGA", 
            "SAIU_PARA_ENTREGA", "ENTREGUE", "PAGO_MESA", "CANCELADO"
    );

    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        model.addAttribute("adminMessage", "Bem-vindo ao Painel do Administrador!");
        try {
            long pedidosPendentes = pedidoRepository.countByStatusIn(
                Arrays.asList("RECEBIDO", "AGUARDANDO_PREPARO_MESA", "EM_PREPARO", 
                              "PRONTO_NA_MESA", "PRONTO_PARA_ENTREGA", "SAIU_PARA_ENTREGA")
            );
            model.addAttribute("pedidosPendentesCount", pedidosPendentes);
        } catch (Exception e) {
            System.err.println("Erro ao contar pedidos pendentes: " + e.getMessage());
            model.addAttribute("pedidosPendentesCount", "N/A");
        }
        return "admin/admin-dashboard";
    }

    // --- MÉTODOS DE GERENCIAMENTO DE PIZZAS ---
    @GetMapping("/pizzas")
    public String listarPizzas(Model model) {
        model.addAttribute("listaPizzas", pizzaRepository.findAll(Sort.by(Sort.Direction.ASC, "nome")));
        return "admin/admin-pizzas";
    }

    @GetMapping("/pizzas/nova")
    public String mostrarFormNovaPizza(Model model) {
        Pizza novaPizza = new Pizza();
        novaPizza.setDisponivel(true); // Padrão para nova pizza
        model.addAttribute("pizza", novaPizza);
        model.addAttribute("tituloForm", "Adicionar Nova Pizza");
        model.addAttribute("acaoForm", "/admin/pizzas/salvar");
        model.addAttribute("ingredientesStr", ""); // Para o campo de texto
        return "admin/admin-form-pizza";
    }

    @GetMapping("/pizzas/editar/{id}")
    public String mostrarFormEditarPizza(@PathVariable("id") String id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Pizza> pizzaOpt = pizzaRepository.findById(id);
        if (pizzaOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Pizza não encontrada com ID: " + id);
            return "redirect:/admin/pizzas";
        }
        Pizza pizza = pizzaOpt.get();
        model.addAttribute("pizza", pizza);
        model.addAttribute("ingredientesStr", pizza.getIngredientes() != null ? String.join(", ", pizza.getIngredientes()) : "");
        model.addAttribute("tituloForm", "Editar Pizza: " + pizza.getNome());
        model.addAttribute("acaoForm", "/admin/pizzas/salvar");
        return "admin/admin-form-pizza";
    }

    @PostMapping("/pizzas/salvar")
    public String salvarPizza(@Valid @ModelAttribute("pizza") Pizza pizza,
                              BindingResult bindingResult,
                              @RequestParam(name = "ingredientesStr", required = false) String ingredientesStr,
                              Model model, // Adicionado Model para retornar ao form em caso de erro
                              RedirectAttributes redirectAttributes) {
        
        // Se houver erros de validação do @Valid nos campos da entidade Pizza
        if (bindingResult.hasErrors()) {
            model.addAttribute("tituloForm", pizza.getId() == null ? "Adicionar Nova Pizza" : "Editar Pizza: " + pizza.getNome());
            model.addAttribute("acaoForm", "/admin/pizzas/salvar");
            model.addAttribute("ingredientesStr", ingredientesStr); // Devolve a string de ingredientes para o formulário
             // Os erros de bindingResult já são adicionados ao model automaticamente pelo Spring
            return "admin/admin-form-pizza";
        }

        if (StringUtils.hasText(ingredientesStr)) {
            pizza.setIngredientes(Arrays.asList(ingredientesStr.trim().split("\\s*,\\s*")));
        } else {
            pizza.setIngredientes(new ArrayList<>());
        }

        try {
            pizzaRepository.save(pizza);
            redirectAttributes.addFlashAttribute("successMessage", "Pizza '" + pizza.getNome() + "' salva com sucesso!");
            return "redirect:/admin/pizzas";
        } catch (Exception e) {
            System.err.println("Erro ao salvar pizza: " + e.getMessage());
            e.printStackTrace();
            // Adiciona o erro ao model para ser exibido no formulário
            model.addAttribute("errorMessage", "Erro ao salvar pizza. Verifique os dados ou tente novamente.");
            model.addAttribute("tituloForm", pizza.getId() == null ? "Adicionar Nova Pizza" : "Editar Pizza: " + pizza.getNome());
            model.addAttribute("acaoForm", "/admin/pizzas/salvar");
            model.addAttribute("ingredientesStr", ingredientesStr);
            model.addAttribute("pizza", pizza); // Devolve o objeto pizza com os dados preenchidos
            return "admin/admin-form-pizza";
        }
    }
    
    @PostMapping("/pizzas/deletar/{id}")
    public String deletarPizza(@PathVariable("id") String id, RedirectAttributes redirectAttributes){
        Optional<Pizza> pizzaOpt = pizzaRepository.findById(id);
        if (pizzaOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Pizza não encontrada com ID: " + id);
            return "redirect:/admin/pizzas";
        }
        try {
            pizzaRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Pizza '" + pizzaOpt.get().getNome() + "' deletada com sucesso!");
        } catch (Exception e) {
            System.err.println("Erro ao deletar pizza: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao deletar pizza.");
        }
        return "redirect:/admin/pizzas";
    }

    // --- MÉTODOS DE GERENCIAMENTO DE PEDIDOS (EXISTENTES, SEM ALTERAÇÃO NESTA ETAPA) ---
    @GetMapping("/pedidos")
    public String listarPedidos(
            @RequestParam(name = "dataSelecionada", required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataSelecionada,
            Model model) {

        LocalDateTime inicioDia;
        LocalDateTime fimDia;
        String tituloVisao = "Pedidos de Hoje";

        if (dataSelecionada == null) {
            dataSelecionada = LocalDate.now(); 
        } else {
            tituloVisao = "Pedidos de " + dataSelecionada.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }

        inicioDia = dataSelecionada.atStartOfDay();         
        fimDia = dataSelecionada.atTime(LocalTime.MAX);     

        Sort sort = Sort.by(Sort.Direction.DESC, "dataHoraPedido");
        
        List<Pedido> pedidosDoDia = pedidoRepository.findByDataHoraPedidoBetween(inicioDia, fimDia, sort);

        List<Pedido> pedidosOrdenados = pedidosDoDia.stream()
            .sorted(Comparator.comparing((Pedido p) -> {
                int index = ORDEM_STATUS_FLUXO.indexOf(p.getStatus());
                return index == -1 ? Integer.MAX_VALUE : index; 
            }).thenComparing(Pedido::getDataHoraPedido, Comparator.nullsLast(Comparator.reverseOrder()))) 
            .collect(Collectors.toList());

        model.addAttribute("listaPedidos", pedidosOrdenados);
        model.addAttribute("tituloVisaoPedidos", tituloVisao);
        model.addAttribute("dataSelecionadaVisao", dataSelecionada.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        model.addAttribute("dataSelecionadaParam", dataSelecionada.toString()); 

        return "admin/admin-pedidos";
    }

    @GetMapping("/cozinha")
    public String painelCozinha(Model model) {
        List<Pedido> pedidosParaCozinha = pedidoRepository.findByStatusInOrderByDataHoraPedidoAsc(STATUS_COZINHA_ATIVOS);
        
        List<Pedido> pedidosCozinhaOrdenados = pedidosParaCozinha.stream()
            .sorted(Comparator.comparing((Pedido p) -> {
                int index = ORDEM_STATUS_FLUXO.indexOf(p.getStatus()); 
                return index == -1 ? Integer.MAX_VALUE : index;
            }).thenComparing(Pedido::getDataHoraPedido, Comparator.nullsLast(Comparator.naturalOrder()))) 
            .collect(Collectors.toList());

        model.addAttribute("pedidosCozinha", pedidosCozinhaOrdenados);
        return "admin/admin-cozinha";
    }

    @GetMapping("/pedidos/detalhes/{id}")
    public String detalhesPedido(@PathVariable("id") String id,
                                 @RequestParam(name = "origem", required = false) String origem,
                                 Model model, 
                                 RedirectAttributes redirectAttributes) {
        Optional<Pedido> pedidoOpt = pedidoRepository.findById(id);
        if (pedidoOpt.isPresent()) {
            model.addAttribute("pedido", pedidoOpt.get());
            model.addAttribute("todosStatus", TODOS_STATUS_POSSIVEIS); 
            if (StringUtils.hasText(origem)) { 
                model.addAttribute("linkVoltar", "/admin/" + origem);
            } else { 
                model.addAttribute("linkVoltar", "/admin/pedidos");
            }
            return "admin/admin-pedido-detalhes";
        } else { 
            redirectAttributes.addFlashAttribute("errorMessage", "Pedido não encontrado com ID: " + id);
            return "redirect:/admin/pedidos";
        }
    }

    @PostMapping("/pedidos/atualizar-status")
    public String atualizarStatusPedido(@RequestParam("pedidoId") String pedidoId,
                                        @RequestParam(name = "novoStatus", required = false) String novoStatus, 
                                        @RequestParam(name = "origem", defaultValue = "") String origem,
                                        RedirectAttributes redirectAttributes) {
        
        System.out.println("--- INICIO ATUALIZAR STATUS ---");
        System.out.println("Pedido ID: " + pedidoId);
        System.out.println("Novo Status Recebido: [" + novoStatus + "]");
        System.out.println("Origem: " + origem);

        if (novoStatus == null || novoStatus.trim().isEmpty()) { 
            System.out.println("VALIDAÇÃO FALHOU: novoStatus é nulo ou vazio.");
            redirectAttributes.addFlashAttribute("errorMessage", "Por favor, selecione um status válido para atualizar.");
            
            if ("cozinha".equals(origem)) {
                System.out.println("Redirecionando para /admin/cozinha (erro de status vazio)");
                return "redirect:/admin/cozinha";
            }
            if (StringUtils.hasText(pedidoId)) {
                System.out.println("Redirecionando para /admin/pedidos/detalhes/" + pedidoId + " (erro de status vazio)");
                return "redirect:/admin/pedidos/detalhes/" + pedidoId;
            } else {
                System.out.println("Redirecionando para /admin/pedidos (erro de status vazio, sem pedidoId)");
                return "redirect:/admin/pedidos"; 
            }
        }
        System.out.println("VALIDAÇÃO OK: novoStatus tem valor: " + novoStatus);

        Optional<Pedido> pedidoOpt = pedidoRepository.findById(pedidoId);
        if (pedidoOpt.isPresent()) {
            Pedido pedido = pedidoOpt.get();
            System.out.println("Pedido encontrado: " + pedido.getId() + " - Status atual: " + pedido.getStatus());
            
            pedido.setStatus(novoStatus);
            pedido.setDataHoraUltimaAtualizacao(LocalDateTime.now());
            try {
                pedidoRepository.save(pedido);
                System.out.println("Pedido salvo com novo status: " + novoStatus);
                redirectAttributes.addFlashAttribute("successMessage", "Status do pedido Nº " + (pedido.getNumeroPedidoExibicao() != null ? pedido.getNumeroPedidoExibicao() : pedido.getId()) + " atualizado para " + novoStatus.replace('_', ' ') + "!");
            } catch (Exception e) {
                System.err.println("ERRO AO SALVAR PEDIDO: " + e.getMessage());
                e.printStackTrace();
                redirectAttributes.addFlashAttribute("errorMessage", "Erro ao salvar atualização do pedido.");
            }
        } else {
            System.out.println("ERRO: Pedido " + pedidoId + " não encontrado.");
            redirectAttributes.addFlashAttribute("errorMessage", "Pedido " + pedidoId + " não encontrado ao tentar atualizar status.");
        }
        
        System.out.println("Redirecionamento final para origem: " + origem);
        if ("cozinha".equals(origem)) {
            return "redirect:/admin/cozinha";
        }
        return "redirect:/admin/pedidos/detalhes/" + pedidoId;
    }
    
    @PostMapping("/pedidos/registrar-pagamento-local")
    public String registrarPagamentoLocal(@RequestParam("pedidoId") String pedidoId,
                                          @RequestParam("adminFormaPagamento") String adminFormaPagamento,
                                          @RequestParam(name = "adminTrocoPara", required = false) String adminTrocoParaStr,
                                          @RequestParam(name = "adminObservacoesPagamento", required = false) String adminObservacoesPagamento,
                                          RedirectAttributes redirectAttributes) {
        Optional<Pedido> pedidoOpt = pedidoRepository.findById(pedidoId);
        if (pedidoOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Pedido " + pedidoId + " não encontrado.");
            return "redirect:/admin/pedidos";
        }

        Pedido pedido = pedidoOpt.get();

        if (!"LOCAL".equals(pedido.getTipoPedido()) || pedido.getFormaPagamento() != null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Este pedido não é elegível para registro de pagamento local ou já foi pago.");
            return "redirect:/admin/pedidos/detalhes/" + pedidoId;
        }

        pedido.setFormaPagamento(adminFormaPagamento);

        String obsPagamentoFinal = StringUtils.hasText(adminObservacoesPagamento) ? adminObservacoesPagamento.trim() : "";

        if ("DINHEIRO_LOCAL".equals(adminFormaPagamento) && StringUtils.hasText(adminTrocoParaStr)) {
            try {
                BigDecimal valorRecebido = new BigDecimal(adminTrocoParaStr.replace(",", "."));
                String obsTroco = "Valor recebido: R$ " + String.format("%.2f", valorRecebido);
                obsPagamentoFinal = StringUtils.hasText(obsPagamentoFinal) ? obsPagamentoFinal + " | " + obsTroco : obsTroco;
            } catch (NumberFormatException e) {
                redirectAttributes.addFlashAttribute("errorMessage", "Valor recebido para troco inválido: " + adminTrocoParaStr);
                return "redirect:/admin/pedidos/detalhes/" + pedidoId;
            }
        }
        pedido.setObservacoesPagamento(obsPagamentoFinal);
        pedido.setStatus("PAGO_MESA"); 
        pedido.setDataHoraUltimaAtualizacao(LocalDateTime.now());

        try {
            pedidoRepository.save(pedido);
            redirectAttributes.addFlashAttribute("successMessage", "Pagamento para o pedido Nº " + (pedido.getNumeroPedidoExibicao() != null ? pedido.getNumeroPedidoExibicao() : pedido.getId()) + " registrado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao salvar o pagamento do pedido: " + e.getMessage());
        }
        
        return "redirect:/admin/pedidos/detalhes/" + pedidoId;
    }
}