package com.fatec.Pizzaria_Mario.service;

import com.fatec.Pizzaria_Mario.model.Contador;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
public class ContadorService {

    @Autowired
    private MongoOperations mongoOperations;

    // O nome do contador que usaremos para os pedidos
    private static final String PEDIDO_COUNTER_ID = "pedido_sequencia";

    public synchronized Long getProximoNumeroPedido() {
        Query query = new Query(Criteria.where("_id").is(PEDIDO_COUNTER_ID));
        Update update = new Update().inc("seq", 1);
        
        // returnNew(true) para retornar o documento atualizado
        // upsert(true) para criar o documento contador se ele não existir na primeira vez
        FindAndModifyOptions options = new FindAndModifyOptions().returnNew(true).upsert(true);

        Contador contador = mongoOperations.findAndModify(query, update, options, Contador.class);

        // Se o contador for nulo (caso de upsert na primeira vez sem valor inicial), inicializa
        if (contador == null) {
            // Tenta criar explicitamente se o upsert não funcionou como esperado ou para garantir o valor inicial
            Contador novoContador = new Contador(PEDIDO_COUNTER_ID, 1L); // Começa em 1
            try {
                 // Tenta salvar. Se já existir devido a uma condição de corrida com upsert, pode falhar,
                 // mas o findAndModify já teria retornado o valor.
                 // Uma alternativa mais robusta seria tentar o findAndModify novamente se nulo.
                mongoOperations.save(novoContador);
                return 1L;
            } catch (Exception e) {
                // Se falhar ao salvar (ex: já existe), tenta buscar novamente o valor atualizado.
                Contador contadorExistente = mongoOperations.findOne(query, Contador.class);
                return contadorExistente != null ? contadorExistente.getSeq() : 1L; // Fallback
            }
        }
        return contador.getSeq();
    }
}