// pizzaria_script.js

// Função para alternar a visibilidade das seções de Entrega e Mesa no Checkout
function toggleDadosEntregaMesa() {
    // Verifica se os elementos de rádio para tipo de pedido existem na página
    var tipoEntregaRadio = document.getElementById('tipoEntrega');
    var tipoLocalRadio = document.getElementById('tipoLocal');
    
    if (!tipoEntregaRadio || !tipoLocalRadio) {
        // Se os rádios não existem, provavelmente não estamos na página de checkout, então não faz nada.
        return;
    }

    var tipoPedidoInput = document.querySelector('input[name="tipoPedido"]:checked');
    if (!tipoPedidoInput) {
        // Se nenhum rádio estiver selecionado (o que não deveria acontecer se um for 'checked' por padrão), não faz nada.
        return;
    }
    var tipoPedido = tipoPedidoInput.value;

    var secaoEntrega = document.getElementById('dados-entrega');
    var secaoMesa = document.getElementById('mesa-info-section');
    var selectFormaPagamento = document.getElementById('formaPagamento');

    // Campos que podem ter 'required' alterado na seção de entrega
    var inputsEndereco = [];
    if (secaoEntrega) {
        inputsEndereco = secaoEntrega.querySelectorAll('#cep, #rua, #numero, #bairro, #cidade, #estado');
    }
    var inputTelefone = document.getElementById('telefone'); // Assumindo que o ID é 'telefone'
    var inputNumeroMesa = document.getElementById('numeroMesa');
    var inputNomeClienteLocal = document.getElementById('nomeClienteLocal');


    if (tipoPedido === 'ENTREGA') {
        if (secaoEntrega) secaoEntrega.style.display = 'block';
        if (secaoMesa) secaoMesa.style.display = 'none';
        if (selectFormaPagamento) selectFormaPagamento.required = true;

        if (inputTelefone) inputTelefone.required = true;
        inputsEndereco.forEach(input => input.required = true);
        if (inputNumeroMesa) inputNumeroMesa.required = false; // Mesa não é obrigatória para entrega
        if (inputNomeClienteLocal) inputNomeClienteLocal.required = false; // Nome local não é obrigatório para entrega

    } else if (tipoPedido === 'LOCAL') {
        if (secaoEntrega) secaoEntrega.style.display = 'none';
        if (secaoMesa) secaoMesa.style.display = 'block';
        if (selectFormaPagamento) selectFormaPagamento.required = false; // Forma de pagamento pode ser decidida no caixa
        if (selectFormaPagamento) selectFormaPagamento.value = ""; // Limpa seleção anterior
        
        if (inputTelefone) inputTelefone.required = false;
        inputsEndereco.forEach(input => input.required = false);
        
        // Para consumo local, estes podem ser opcionais ou ter validação no backend
        if (inputNumeroMesa) inputNumeroMesa.required = false; 
        if (inputNomeClienteLocal) inputNomeClienteLocal.required = false; 
    }
    // Atualiza a visibilidade dos detalhes de pagamento com base na nova seleção de forma de pagamento (ou ausência dela)
    mostrarDetalhesPagamento();
}

// Função para mostrar detalhes específicos da forma de pagamento no Checkout
function mostrarDetalhesPagamento() {
    var selectFormaPagamento = document.getElementById('formaPagamento');
    if (!selectFormaPagamento) {
        // Se o select não existe, provavelmente não estamos na página de checkout, então não faz nada.
        return;
    }

    // Esconde todos os divs que contêm detalhes específicos de tipos de pagamento
    var todosDetalhesPagamento = document.querySelectorAll('.detalhes-tipo-pagamento');
    todosDetalhesPagamento.forEach(function(div) {
        div.style.display = 'none';
    });

    var formaPagamentoSelecionada = selectFormaPagamento.value;
    // Verifica o tipo de pedido também, pois as opções de pagamento podem ser diferentes
    var tipoPedidoRadio = document.querySelector('input[name="tipoPedido"]:checked');
    var tipoPedido = tipoPedidoRadio ? tipoPedidoRadio.value : null;


    // Mostra a seção de troco se a forma de pagamento for Dinheiro (para entrega ou local)
    if (formaPagamentoSelecionada.startsWith('DINHEIRO')) { // Ex: DINHEIRO_LOCAL_ENTREGA
        var detalhesDinheiro = document.getElementById('detalhesDinheiro');
        if (detalhesDinheiro) detalhesDinheiro.style.display = 'block';
    }
    // Adicionar lógica para CARTAO_ONLINE se for reabilitado no futuro
    // else if (formaPagamentoSelecionada === 'CARTAO_ONLINE') {
    //     var detalhesCartaoOnline = document.getElementById('detalhesCartaoOnline');
    //     if (detalhesCartaoOnline) detalhesCartaoOnline.style.display = 'block';
    // }
}

// Função para a página montar-pizza.html
function toggleMetadePizza() {
    var tipoInteiraRadio = document.getElementById('tipoInteira'); // Usado para verificar se estamos na página correta
    if (!tipoInteiraRadio) {
        return; // Não estamos na página de montar pizza
    }

    var tipoEscolhaInput = document.querySelector('input[name="tipoEscolha"]:checked');
    if (!tipoEscolhaInput) return; // Nenhum selecionado

    var tipoEscolha = tipoEscolhaInput.value;
    var selectMetade2Div = document.getElementById('selectMetade2');
    var selectMetade2Input = document.getElementById('pizza2Id');

    if (tipoEscolha === 'metade') {
        if (selectMetade2Div) selectMetade2Div.style.display = 'block';
        if (selectMetade2Input) selectMetade2Input.required = true;
    } else { // tipoEscolha === 'inteira'
        if (selectMetade2Div) selectMetade2Div.style.display = 'none';
        if (selectMetade2Input) {
            selectMetade2Input.required = false;
            selectMetade2Input.value = ""; // Limpa a seleção da segunda metade
        }
    }
}


// Adiciona os event listeners quando o DOM estiver carregado
document.addEventListener('DOMContentLoaded', function () {
    console.log("Pizzaria Mario JS carregado e DOM pronto!");

    // --- Listeners para Checkout ---
    var radiosTipoPedido = document.querySelectorAll('input[name="tipoPedido"]');
    radiosTipoPedido.forEach(function(radio) {
        radio.addEventListener('change', toggleDadosEntregaMesa);
    });

    var selectFormaPagamento = document.getElementById('formaPagamento');
    if (selectFormaPagamento) {
        selectFormaPagamento.addEventListener('change', mostrarDetalhesPagamento);
    }

    // Chama as funções na carga inicial da página de checkout para configurar o estado correto
    if (window.location.pathname.endsWith('/checkout')) {
        toggleDadosEntregaMesa(); // Para mostrar/esconder seções de endereço/mesa
        mostrarDetalhesPagamento(); // Para mostrar/esconder detalhes de pagamento
    }

    // --- Listeners para Montar Pizza ---
    var radiosTipoEscolhaPizza = document.querySelectorAll('input[name="tipoEscolha"]');
    radiosTipoEscolhaPizza.forEach(function(radio) {
        radio.addEventListener('change', toggleMetadePizza);
    });
    
    // Chama na carga inicial da página de montar pizza
    if (window.location.pathname.includes('/pedido/montar/')) {
        toggleMetadePizza();
    }


    // --- Scripts para Sidebar (se ainda estiver usando este padrão) ---
    var toggleBtn = document.querySelector('.toggle-btn'); // Classe do botão de toggle da sidebar
    if (toggleBtn) {
        toggleBtn.addEventListener('click', () => {
            var sidebar = document.getElementById('sidebar'); // ID da sua sidebar
            if (sidebar) {
                sidebar.classList.toggle('active'); // 'active' seria a classe que mostra/esconde a sidebar
            }
        });
    }
});

// Função para submenu da sidebar (deve estar no escopo global se chamada por onclick no HTML)
function toggleSubmenu(event, element) {
    // event.preventDefault(); // Descomente se 'element' for um link <a> e você não quer que ele navegue
    var parentLi = element.parentElement;
    if (parentLi) {
        parentLi.classList.toggle('open'); // 'open' seria a classe que expande o submenu
    }
}