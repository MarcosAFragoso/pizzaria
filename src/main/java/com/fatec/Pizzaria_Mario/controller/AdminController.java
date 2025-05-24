package com.fatec.Pizzaria_Mario.controller;

import com.fatec.Pizzaria_Mario.model.Pedido;
import com.fatec.Pizzaria_Mario.repository.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam; // Import para @RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes; // Import para RedirectAttributes

import java.time.LocalDateTime; // <<<--- IMPORT ADICIONADO
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin") // Prefixo para todas as rotas neste controller
public class AdminController {

    @Autowired
    private PedidoRepository pedidoRepository;

    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        model.addAttribute("adminMessage", "Bem-vindo ao Painel do Administrador!");
        // Contar pedidos pendentes para o dashboard (exemplo)
        // Certifique-se que o método countByStatusIn existe no PedidoRepository
        try {
            long pedidosPendentes = pedidoRepository.countByStatusIn(List.of("RECEBIDO", "EM_PREPARO", "AGUARDANDO_PREPARO_MESA"));
            model.addAttribute("pedidosPendentesCount", pedidosPendentes);
        } catch (Exception e) {
            System.err.println("Erro ao contar pedidos pendentes: " + e.getMessage());
            model.addAttribute("pedidosPendentesCount", "N/A");
        }
        return "admin/admin-dashboard";
    }

    @GetMapping("/pedidos")
    public String listarPedidos(Model model) {
        // Buscar todos os pedidos, ordenados pela data mais recente primeiro
        List<Pedido> todosOsPedidos = pedidoRepository.findAll(Sort.by(Sort.Direction.DESC, "dataHoraPedido"));
        model.addAttribute("listaPedidos", todosOsPedidos);
        return "admin/admin-pedidos";
    }

    @GetMapping("/pedidos/detalhes/{id}")
    public String detalhesPedido(@PathVariable("id") String id, Model model, RedirectAttributes redirectAttributes) { // Adicionado RedirectAttributes
        Optional<Pedido> pedidoOpt = pedidoRepository.findById(id);
        if (pedidoOpt.isPresent()) {
            model.addAttribute("pedido", pedidoOpt.get());
            // Lista de todos os status possíveis para o dropdown
            model.addAttribute("todosStatus",
                List.of("RECEBIDO", "AGUARDANDO_PREPARO_MESA", "EM_PREPARO", "PRONTO_NA_MESA",
                        "SAIU_PARA_ENTREGA", "ENTREGUE", "PAGO_MESA", "CANCELADO")
            );
            return "admin/admin-pedido-detalhes";
        } else { // Adicionado 'else' para tratar o caso de pedido não encontrado
            redirectAttributes.addFlashAttribute("errorMessage", "Pedido não encontrado com ID: " + id);
            return "redirect:/admin/pedidos";
        }
    } // <<<--- CHAVE DE FECHAMENTO DO MÉTODO detalhesPedido ADICIONADA

    @PostMapping("/pedidos/atualizar-status")
    public String atualizarStatusPedido(@RequestParam("pedidoId") String pedidoId,
                                        @RequestParam("novoStatus") String novoStatus,
                                        RedirectAttributes redirectAttributes) {
        Optional<Pedido> pedidoOpt = pedidoRepository.findById(pedidoId);
        if (pedidoOpt.isPresent()) {
            Pedido pedido = pedidoOpt.get();
            pedido.setStatus(novoStatus);
            pedido.setDataHoraUltimaAtualizacao(LocalDateTime.now()); // Atualiza data da modificação
            pedidoRepository.save(pedido);
            redirectAttributes.addFlashAttribute("successMessage", "Status do pedido " + pedidoId + " atualizado para " + novoStatus.replace('_', ' ') + "!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Pedido " + pedidoId + " não encontrado ao tentar atualizar status.");
        }
        return "redirect:/admin/pedidos/detalhes/" + pedidoId; // Volta para os detalhes do pedido
    }
    
} // <<<--- CHAVE DE FECHAMENTO DA CLASSE AdminController (Removida chave extra depois daqui)