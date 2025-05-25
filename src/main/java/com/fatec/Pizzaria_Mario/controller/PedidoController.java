package com.fatec.Pizzaria_Mario.controller;

import com.fatec.Pizzaria_Mario.model.*;
import com.fatec.Pizzaria_Mario.repository.AcompanhamentoRepository;
import com.fatec.Pizzaria_Mario.repository.PedidoRepository;
import com.fatec.Pizzaria_Mario.repository.PizzaRepository;
import com.fatec.Pizzaria_Mario.service.AuthService;
import com.fatec.Pizzaria_Mario.service.ContadorService;
import com.fatec.Pizzaria_Mario.service.EmailService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort; // <<< ADICIONADO IMPORT
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Controller
public class PedidoController {

    @Autowired
    private PizzaRepository pizzaRepository;
    @Autowired
    private AcompanhamentoRepository acompanhamentoRepository;
    @Autowired
    private Carrinho carrinho;
    @Autowired
    private AuthService authService;
    @Autowired
    private PedidoRepository pedidoRepository;
    @Autowired(required = false)
    private EmailService emailService;
    @Autowired
    private ContadorService contadorService;

    @GetMapping("/pedido/montar/{pizzaId}")
    public String montarPizza(@PathVariable("pizzaId") String pizzaId, Model model, HttpSession session) {
        session.removeAttribute("itemPedidoAtual");
        Optional<Pizza> pizzaOpt = pizzaRepository.findById(pizzaId);
        if (pizzaOpt.isEmpty()) {
            model.addAttribute("errorMessage", "Pizza não encontrada.");
            return "redirect:/cardapio?error=PizzaNaoEncontrada";
        }
        List<Pizza> todasAsPizzas = pizzaRepository.findByDisponivelTrue(Sort.by(Sort.Direction.ASC, "nome")); // Uso de Sort
        model.addAttribute("pizzaPrincipal", pizzaOpt.get());
        model.addAttribute("todasAsPizzas", todasAsPizzas);
        return "montar-pizza";
    }

    @PostMapping("/carrinho/adicionar-pizza")
    public String adicionarPizzaAoCarrinho(@RequestParam("pizza1Id") String pizza1Id,
                                        @RequestParam(value = "pizza2Id", required = false) String pizza2Id,
                                        @RequestParam("tipoEscolha") String tipoEscolha,
                                        @RequestParam(value = "quantidade", defaultValue = "1") int quantidade,
                                        @RequestParam(value = "observacoes", required = false) String observacoes,
                                        HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            Optional<Pizza> pizza1Opt = pizzaRepository.findById(pizza1Id);
            if (pizza1Opt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Pizza principal não encontrada!");
                return "redirect:/cardapio";
            }
            Pizza pizza1 = pizza1Opt.get();
            ItemPedido item;
            if (quantidade < 1) quantidade = 1;

            if ("inteira".equalsIgnoreCase(tipoEscolha)) {
                item = new ItemPedido(pizza1, quantidade);
            } else if ("metade".equalsIgnoreCase(tipoEscolha)) {
                if (!StringUtils.hasText(pizza2Id)) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Para pizza metade/metade, por favor, escolha o segundo sabor.");
                    return "redirect:/pedido/montar/" + pizza1Id;
                }
                Optional<Pizza> pizza2Opt = pizzaRepository.findById(pizza2Id);
                if (pizza2Opt.isEmpty()) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Segunda metade da pizza não encontrada!");
                    return "redirect:/pedido/montar/" + pizza1Id;
                }
                Pizza pizza2 = pizza2Opt.get();
                if (pizza1.getId().equals(pizza2.getId())) {
                     redirectAttributes.addFlashAttribute("errorMessage", "Para pizza metade/metade, escolha dois sabores diferentes.");
                     return "redirect:/pedido/montar/" + pizza1Id;
                }
                item = new ItemPedido(pizza1, pizza2, quantidade);
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Tipo de escolha de pizza inválido.");
                return "redirect:/pedido/montar/" + pizza1Id;
            }
            item.setObservacoes(observacoes);
            session.setAttribute("itemPedidoAtual", item);
            return "redirect:/pedido/acompanhamentos";
        } catch (Exception e) {
            System.err.println("ERRO em /carrinho/adicionar-pizza: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Ocorreu um erro inesperado ao adicionar a pizza.");
            if (StringUtils.hasText(pizza1Id)) {
                return "redirect:/pedido/montar/" + pizza1Id;
            }
            return "redirect:/cardapio";
        }
    }

    @GetMapping("/pedido/acompanhamentos")
    public String mostrarAcompanhamentos(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        Object itemObj = session.getAttribute("itemPedidoAtual");
        ItemPedido itemPedidoAtual = null;
        if (itemObj instanceof ItemPedido) {
            itemPedidoAtual = (ItemPedido) itemObj;
        }
        if (itemPedidoAtual == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Por favor, monte sua pizza primeiro.");
            return "redirect:/cardapio";
        }
        List<Acompanhamento> porcoes = acompanhamentoRepository.findByTipoAndDisponivelTrue("PORCAO");
        List<Acompanhamento> adicionaisPizza = acompanhamentoRepository.findByTipoAndDisponivelTrue("ADICIONAL_PIZZA");
        model.addAttribute("itemPedidoAtual", itemPedidoAtual);
        model.addAttribute("porcoes", porcoes);
        model.addAttribute("adicionaisPizza", adicionaisPizza);
        return "acompanhamentos";
    }

    @PostMapping("/pedido/adicionar-acompanhamentos")
    public String adicionarAcompanhamentos(@RequestParam(value="acompanhamentoIds", required = false) List<String> acompanhamentoIds,
                                           HttpSession session, RedirectAttributes redirectAttributes) {
        Object itemObj = session.getAttribute("itemPedidoAtual");
        ItemPedido itemPedidoAtual = null;
        if (itemObj instanceof ItemPedido) {
            itemPedidoAtual = (ItemPedido) itemObj;
        }
        if (itemPedidoAtual == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Sua sessão expirou ou a pizza não foi montada corretamente.");
            return "redirect:/cardapio";
        }
        itemPedidoAtual.getAcompanhamentosSelecionados().clear();
        if (acompanhamentoIds != null && !acompanhamentoIds.isEmpty()) {
            for (String acompanhamentoId : acompanhamentoIds) {
                Optional<Acompanhamento> acompanhamentoOpt = acompanhamentoRepository.findById(acompanhamentoId);
                if (acompanhamentoOpt.isPresent()) {
                    AcompanhamentoSelecionado as = new AcompanhamentoSelecionado(acompanhamentoOpt.get(), 1);
                    itemPedidoAtual.getAcompanhamentosSelecionados().add(as);
                }
            }
        }
        itemPedidoAtual.recalcularPrecoTotalItem();
        carrinho.adicionarItem(itemPedidoAtual);
        session.removeAttribute("itemPedidoAtual");
        redirectAttributes.addFlashAttribute("successMessage", "'" + itemPedidoAtual.getNomeExibicao() + "' com acompanhamentos foi adicionado ao carrinho!");
        return "redirect:/pedido/bebidas";
    }

    @GetMapping("/pedido/bebidas")
    public String mostrarBebidas(Model model) {
        List<Acompanhamento> refrigerantes = acompanhamentoRepository.findByTipoAndDisponivelTrue("BEBIDA_REFRIGERANTE");
        List<Acompanhamento> vinhos = acompanhamentoRepository.findByTipoAndDisponivelTrue("BEBIDA_VINHO");
        model.addAttribute("refrigerantes", refrigerantes);
        model.addAttribute("vinhos", vinhos);
        model.addAttribute("carrinho", carrinho);
        return "bebidas";
    }

    @PostMapping("/carrinho/adicionar-bebida")
    public String adicionarBebidaAoCarrinho(@RequestParam("bebidaId") String bebidaId,
                                            @RequestParam(value = "quantidade", defaultValue = "1") int quantidade,
                                            RedirectAttributes redirectAttributes) {
        Optional<Acompanhamento> bebidaOpt = acompanhamentoRepository.findById(bebidaId);
        if (bebidaOpt.isEmpty() ||
            !( "BEBIDA_REFRIGERANTE".equals(bebidaOpt.get().getTipo()) ||
               "BEBIDA_VINHO".equals(bebidaOpt.get().getTipo()) )) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bebida não encontrada ou inválida!");
            return "redirect:/pedido/bebidas";
        }
        Acompanhamento bebida = bebidaOpt.get();
        ItemPedido itemBebida = new ItemPedido();
        itemBebida.setTipo("BEBIDA");
        itemBebida.setNomeExibicao(bebida.getNome());
        itemBebida.setQuantidade(quantidade);
        itemBebida.setPrecoCalculado(bebida.getPreco().multiply(new BigDecimal(quantidade)));
        carrinho.adicionarItem(itemBebida);
        redirectAttributes.addFlashAttribute("successMessage", bebida.getNome() + " adicionado ao carrinho!");
        return "redirect:/pedido/bebidas";
    }

    @GetMapping("/carrinho")
    public String mostrarCarrinho(Model model) {
        model.addAttribute("carrinho", carrinho);
        return "carrinho";
    }

    @GetMapping("/carrinho/remover/{index}")
    public String removerDoCarrinho(@PathVariable("index") int index, RedirectAttributes redirectAttributes) {
        carrinho.removerItem(index);
        redirectAttributes.addFlashAttribute("successMessage", "Item removido do carrinho.");
        return "redirect:/carrinho";
    }

    @GetMapping("/checkout")
    public String mostrarCheckout(Model model, Authentication authentication, RedirectAttributes redirectAttributes) {
        if (carrinho == null || carrinho.getItens().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Seu carrinho está vazio.");
            return "redirect:/cardapio";
        }
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal().toString())) {
            String userEmail = authentication.getName();
            authService.findByEmail(userEmail).ifPresent(user -> model.addAttribute("usuario", user));
        }
        model.addAttribute("carrinho", carrinho);
        return "checkout";
    }

    @PostMapping("/pedido/finalizar")
    public String finalizarPedido(
            @RequestParam(value="endereco.cep", required = false) String cep,
            @RequestParam(value="endereco.rua", required = false) String rua,
            @RequestParam(value="endereco.numero", required = false) String numero,
            @RequestParam(value = "endereco.complemento", required = false) String complemento,
            @RequestParam(value="endereco.bairro", required = false) String bairro,
            @RequestParam(value="endereco.cidade", required = false) String cidade,
            @RequestParam(value="endereco.estado", required = false) String estado,
            @RequestParam(value = "contato_telefone", required = false) String clienteTelefone,
            @RequestParam(name = "formaPagamento", required = false) String formaPagamento,
            @RequestParam(value = "trocoPara", required = false) String trocoParaStr,
            @RequestParam(value = "salvarEnderecoPerfil", required = false) boolean salvarEndereco,
            @RequestParam("tipoPedido") String tipoPedido,
            @RequestParam(value = "numeroMesa", required = false) Integer numeroMesa,
            @RequestParam(value = "numeroPessoas", required = false) Integer numeroPessoas,
            @RequestParam(value = "nomeClienteLocal", required = false) String nomeClienteLocal,
            Authentication authentication,
            RedirectAttributes redirectAttributes,
            HttpSession session) {

        if (carrinho == null || carrinho.getItens().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Seu carrinho está vazio.");
            return "redirect:/cardapio";
        }

        Endereco enderecoEntrega = null;
        String formaPagamentoFinal = formaPagamento;

        if ("ENTREGA".equals(tipoPedido)) {
            if (!StringUtils.hasText(cep) || !StringUtils.hasText(rua) ||
                !StringUtils.hasText(numero) || !StringUtils.hasText(bairro) ||
                !StringUtils.hasText(cidade) || !StringUtils.hasText(estado) ||
                !StringUtils.hasText(clienteTelefone) || !StringUtils.hasText(formaPagamentoFinal) ) {
                redirectAttributes.addFlashAttribute("errorMessage", "Para entrega, preencha todo o endereço, telefone e forma de pagamento.");
                return "redirect:/checkout";
            }
            enderecoEntrega = new Endereco(cep, rua, numero, complemento, bairro, cidade, estado);
        } else if ("LOCAL".equals(tipoPedido)) {
            formaPagamentoFinal = null; 
            if (numeroMesa == null || numeroMesa <= 0) {
                 redirectAttributes.addFlashAttribute("errorMessage", "Para consumo no local, por favor, informe o número da mesa.");
                 return "redirect:/checkout";
            }
            if (!StringUtils.hasText(nomeClienteLocal) && (authentication == null || !authentication.isAuthenticated())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Para consumo no local sem login, informe um nome para identificação na mesa.");
                return "redirect:/checkout";
            }
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Tipo de pedido inválido.");
            return "redirect:/checkout";
        }

        String clienteNome = "Cliente Anônimo";
        String clienteEmail = "";
        String clienteId = null;

        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal().toString())) {
            String userEmailAutenticado = authentication.getName();
            Optional<User> userOpt = authService.findByEmail(userEmailAutenticado);
            if (userOpt.isPresent()) {
                User usuarioLogado = userOpt.get();
                clienteNome = usuarioLogado.getNomeCompleto();
                clienteEmail = usuarioLogado.getEmail();
                clienteId = usuarioLogado.getId();
                if ("ENTREGA".equals(tipoPedido)) {
                    if (!StringUtils.hasText(clienteTelefone) && StringUtils.hasText(usuarioLogado.getTelefone())) {
                        clienteTelefone = usuarioLogado.getTelefone();
                    }
                    if (salvarEndereco && enderecoEntrega != null && !enderecoEntrega.isVazio()) {
                        try {
                            authService.atualizarEnderecoEtelefoneUsuario(userEmailAutenticado, enderecoEntrega, clienteTelefone);
                        } catch (Exception e) {
                            System.err.println("Erro ao salvar endereço no perfil: " + e.getMessage());
                        }
                    }
                }
            }
        } else if ("LOCAL".equals(tipoPedido) && StringUtils.hasText(nomeClienteLocal)) {
            clienteNome = nomeClienteLocal;
        }


        String observacoesPagamento = "";
        if ("ENTREGA".equals(tipoPedido) && formaPagamentoFinal != null && formaPagamentoFinal.startsWith("DINHEIRO") && StringUtils.hasText(trocoParaStr)) {
            try {
                BigDecimal trocoValor = new BigDecimal(trocoParaStr);
                 if (trocoValor.compareTo(BigDecimal.ZERO) > 0 && trocoValor.compareTo(carrinho.getTotal()) < 0) {
                     redirectAttributes.addFlashAttribute("errorMessage", "O valor do troco deve ser maior ou igual ao total do pedido.");
                     return "redirect:/checkout";
                }
                observacoesPagamento = "Troco para R$ " + String.format("%.2f", trocoValor);
            } catch (NumberFormatException e) {
                System.err.println("Valor de troco inválido: " + trocoParaStr);
            }
        }

        String statusInicial = "RECEBIDO";
        if ("LOCAL".equals(tipoPedido)) {
            statusInicial = "AGUARDANDO_PREPARO_MESA";
        }

        Pedido novoPedido = new Pedido(
                clienteNome, clienteEmail, clienteTelefone,
                enderecoEntrega, carrinho.getItens(), carrinho.getTotal(),
                formaPagamentoFinal, observacoesPagamento,
                tipoPedido, numeroMesa, numeroPessoas );
        novoPedido.setStatus(statusInicial);
        if (clienteId != null) {
            novoPedido.setClienteId(clienteId);
        }

        Long proximoNumeroExibicao = contadorService.getProximoNumeroPedido();
        novoPedido.setNumeroPedidoExibicao(proximoNumeroExibicao);

        try {
            Pedido pedidoSalvo = pedidoRepository.save(novoPedido);
            carrinho.limparCarrinho();
            session.removeAttribute("itemPedidoAtual"); 

            if (emailService != null && StringUtils.hasText(clienteEmail)) {
                try {
                    System.out.println("SIMULAÇÃO: E-mail de confirmação enviado para " + clienteEmail + " para o pedido #" + pedidoSalvo.getNumeroPedidoExibicao());
                } catch (Exception e) {
                    System.err.println("Falha ao tentar enviar e-mail: " + e.getMessage());
                }
            }
            redirectAttributes.addFlashAttribute("successMessage", "Seu pedido (" + tipoPedido.toLowerCase().replace('_', ' ') + ") nº " + pedidoSalvo.getNumeroPedidoExibicao() + " foi recebido!");
            return "redirect:/pedido/confirmado?pedidoId=" + pedidoSalvo.getId();
        } catch (Exception e) {
            System.err.println("Erro ao processar pedido: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao processar pedido. Tente novamente.");
            return "redirect:/checkout";
        }
    }

    @GetMapping("/pedido/confirmado")
    public String pedidoConfirmado(@RequestParam("pedidoId") String pedidoId, Model model, RedirectAttributes redirectAttributes) {
        Optional<Pedido> pedidoOpt = pedidoRepository.findById(pedidoId);
        if (pedidoOpt.isPresent()){
            Pedido pedidoConfirmado = pedidoOpt.get();
            model.addAttribute("pedidoConfirmado", pedidoConfirmado);
            model.addAttribute("numeroPedidoExibicao", pedidoConfirmado.getNumeroPedidoExibicao());
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Pedido com ID " + pedidoId + " não encontrado.");
            return "redirect:/";
        }
        return "pedido-confirmado";
    }

    @GetMapping("/meus-pedidos")
    public String mostrarMeusPedidos(Model model, Authentication authentication, RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal().toString())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Você precisa estar logado para ver seus pedidos.");
            return "redirect:/login";
        }
        String userEmail = authentication.getName();
        Optional<User> userOpt = authService.findByEmail(userEmail);
        if (userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Usuário não encontrado.");
            return "redirect:/login";
        }
        String clienteId = userOpt.get().getId();
        List<Pedido> meusPedidos = pedidoRepository.findByClienteIdOrderByDataHoraPedidoDesc(clienteId);
        model.addAttribute("listaMeusPedidos", meusPedidos);
        return "meus-pedidos";
    }
}