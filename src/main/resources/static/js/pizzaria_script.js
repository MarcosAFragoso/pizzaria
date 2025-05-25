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
    // Seleciona a seção inteira de dados de pagamento
    var secaoPagamento = document.getElementById('dados-pagamento'); // <<< ADICIONADO
    var selectFormaPagamento = document.getElementById('formaPagamento');

    // Campos que podem ter 'required' alterado na seção de entrega
    var inputsEndereco = [];
    if (secaoEntrega) {
        inputsEndereco = secaoEntrega.querySelectorAll('#cep, #rua, #numero, #bairro, #cidade, #estado');
    }
    var inputTelefone = document.getElementById('telefone'); 
    var inputNumeroMesa = document.getElementById('numeroMesa');
    var inputNomeClienteLocal = document.getElementById('nomeClienteLocal');


    if (tipoPedido === 'ENTREGA') {
        if (secaoEntrega) secaoEntrega.style.display = 'block';
        if (secaoMesa) secaoMesa.style.display = 'none';
        if (secaoPagamento) secaoPagamento.style.display = 'block'; // <<< MOSTRAR PAGAMENTO PARA ENTREGA
        if (selectFormaPagamento) selectFormaPagamento.required = true;

        if (inputTelefone) inputTelefone.required = true;
        inputsEndereco.forEach(input => input.required = true);
        if (inputNumeroMesa) inputNumeroMesa.required = false; 
        if (inputNomeClienteLocal) inputNomeClienteLocal.required = false;

    } else if (tipoPedido === 'LOCAL') {
        if (secaoEntrega) secaoEntrega.style.display = 'none';
        if (secaoMesa) secaoMesa.style.display = 'block';
        if (secaoPagamento) secaoPagamento.style.display = 'none'; // <<< ESCONDER PAGAMENTO PARA LOCAL
        if (selectFormaPagamento) selectFormaPagamento.required = false; 
        if (selectFormaPagamento) selectFormaPagamento.value = ""; 
        
        if (inputTelefone) inputTelefone.required = false;
        inputsEndereco.forEach(input => input.required = false);
        
        if (inputNumeroMesa) inputNumeroMesa.required = true; // <<< Tornar número da mesa obrigatório para local
        // Nome do cliente local pode ser opcional ou obrigatório se o usuário não estiver logado
        if (inputNomeClienteLocal) {
             // Você pode adicionar uma lógica aqui para verificar se o usuário está autenticado via Thymeleaf/Spring Security
             // e tornar obrigatório apenas se não estiver autenticado. Por enquanto, deixaremos como opcional.
            inputNomeClienteLocal.required = false; 
        }
    }
    // Atualiza a visibilidade dos detalhes de pagamento com base na nova seleção de forma de pagamento (ou ausência dela)
    // Esta chamada é importante caso a seção de pagamento esteja visível e uma forma de pagamento seja selecionada
    if (secaoPagamento && secaoPagamento.style.display !== 'none') {
        mostrarDetalhesPagamento();
    } else {
        // Se a seção de pagamento estiver escondida, garante que os detalhes também estejam
        var todosDetalhesPagamento = document.querySelectorAll('.detalhes-tipo-pagamento');
        todosDetalhesPagamento.forEach(function(div) {
            div.style.display = 'none';
        });
    }
}

// Função para mostrar detalhes específicos da forma de pagamento no Checkout
function mostrarDetalhesPagamento() {
    var selectFormaPagamento = document.getElementById('formaPagamento');
    if (!selectFormaPagamento) {
        return;
    }

    var todosDetalhesPagamento = document.querySelectorAll('.detalhes-tipo-pagamento');
    todosDetalhesPagamento.forEach(function(div) {
        div.style.display = 'none';
    });

    var formaPagamentoSelecionada = selectFormaPagamento.value;

    if (formaPagamentoSelecionada.startsWith('DINHEIRO')) { 
        var detalhesDinheiro = document.getElementById('detalhesDinheiro');
        if (detalhesDinheiro) detalhesDinheiro.style.display = 'block';
    }
}

// Função para a página montar-pizza.html
function toggleMetadePizza() {
    var tipoInteiraRadio = document.getElementById('tipoInteira'); 
    if (!tipoInteiraRadio) {
        return; 
    }

    var tipoEscolhaInput = document.querySelector('input[name="tipoEscolha"]:checked');
    if (!tipoEscolhaInput) return; 

    var tipoEscolha = tipoEscolhaInput.value;
    var selectMetade2Div = document.getElementById('selectMetade2');
    var selectMetade2Input = document.getElementById('pizza2Id');

    if (tipoEscolha === 'metade') {
        if (selectMetade2Div) selectMetade2Div.style.display = 'block';
        if (selectMetade2Input) selectMetade2Input.required = true;
    } else { 
        if (selectMetade2Div) selectMetade2Div.style.display = 'none';
        if (selectMetade2Input) {
            selectMetade2Input.required = false;
            selectMetade2Input.value = ""; 
        }
    }
}
// Função para buscar e preencher endereço pelo CEP
function buscarCep() {
    const cepInput = document.getElementById('cep');
    const ruaInput = document.getElementById('rua');
    const bairroInput = document.getElementById('bairro');
    const cidadeInput = document.getElementById('cidade');
    const estadoInput = document.getElementById('estado');

    // Verifica se todos os campos existem antes de prosseguir
    if (!cepInput || !ruaInput || !bairroInput || !cidadeInput || !estadoInput) {
        console.warn("Um ou mais campos de endereço não foram encontrados para o autocompletar do CEP.");
        return;
    }

    let cep = cepInput.value.replace(/\D/g, ''); // Remove caracteres não numéricos

    if (cep.length === 8) { // CEP tem 8 dígitos
        // Mostra algum indicador de carregamento (opcional)
        // ruaInput.value = "Buscando...";
        // bairroInput.value = "Buscando...";
        // cidadeInput.value = "Buscando...";
        // estadoInput.value = "Buscando...";

        fetch(`https://viacep.com.br/ws/${cep}/json/`)
            .then(response => response.json())
            .then(data => {
                if (data.erro) {
                    console.error('CEP não encontrado.');
                    // Limpar campos ou mostrar mensagem de erro
                    // ruaInput.value = ""; // etc.
                    alert("CEP não encontrado. Verifique o número digitado.");
                } else {
                    // Preenche os campos com os dados retornados
                    ruaInput.value = data.logradouro || '';
                    bairroInput.value = data.bairro || '';
                    cidadeInput.value = data.localidade || '';
                    estadoInput.value = data.uf || '';
                    // Você pode querer focar no campo 'número' após o preenchimento
                    const numeroInput = document.getElementById('numero');
                    if(numeroInput) numeroInput.focus();
                }
            })
            .catch(error => {
                console.error('Erro ao buscar CEP:', error);
                // Limpar campos ou mostrar mensagem de erro
                alert("Ocorreu um erro ao buscar o CEP. Tente novamente.");
            });
    } else if (cep.length > 0 && cep.length < 8) {
        // CEP incompleto, pode limpar os campos se quiser ou não fazer nada
        // console.log("CEP incompleto.");
    } else {
        // CEP vazio, pode limpar os campos
        // ruaInput.value = ""; // etc.
    }
}

// Adicionar o event listener ao campo CEP
document.addEventListener('DOMContentLoaded', function() {
    // ... (seu código existente do DOMContentLoaded) ...

    const cepField = document.getElementById('cep');
    if (cepField) {
        cepField.addEventListener('blur', buscarCep); // Busca quando o campo perde o foco
        // Opcional: buscar enquanto digita após um certo número de caracteres
        // cepField.addEventListener('input', function() {
        //     if (this.value.replace(/\D/g, '').length === 8) {
        //         buscarCep();
        //     }
        // });
    }
});


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

    if (window.location.pathname.endsWith('/checkout')) {
        toggleDadosEntregaMesa(); 
        // A chamada mostrarDetalhesPagamento() agora é condicional dentro de toggleDadosEntregaMesa()
    }

    // --- Listeners para Montar Pizza ---
    var radiosTipoEscolhaPizza = document.querySelectorAll('input[name="tipoEscolha"]');
    radiosTipoEscolhaPizza.forEach(function(radio) {
        radio.addEventListener('change', toggleMetadePizza);
    });
    
    if (window.location.pathname.includes('/pedido/montar/')) {
        toggleMetadePizza();
    }

    // --- Scripts para Sidebar (se ainda estiver usando este padrão) ---
    var toggleBtn = document.querySelector('.toggle-btn'); 
    if (toggleBtn) {
        toggleBtn.addEventListener('click', () => {
            var sidebar = document.getElementById('sidebar'); 
            if (sidebar) {
                sidebar.classList.toggle('active'); 
            }
        });
    }
});

// Função para submenu da sidebar (deve estar no escopo global se chamada por onclick no HTML)
function toggleSubmenu(event, element) {
    var parentLi = element.parentElement;
    if (parentLi) {
        parentLi.classList.toggle('open'); 
    }
}