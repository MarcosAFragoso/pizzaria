package com.fatec.Pizzaria_Mario.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "contadores")
@Data
@NoArgsConstructor
public class Contador {

    @Id
    private String id; // Ex: "pedido"
    private long seq;  // O valor sequencial atual

    public Contador(String id, long seq) {
        this.id = id;
        this.seq = seq;
    }
}