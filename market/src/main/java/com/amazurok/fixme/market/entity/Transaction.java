package com.amazurok.fixme.market.entity;

import lombok.Builder;
import lombok.NonNull;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import java.util.UUID;

@NonNull
@Builder
@Entity("transactions")
public class Transaction {

    @Id
    @Builder.Default
    private UUID id = UUID.randomUUID();

    private String marketName;

    private String brokerName;

    private String action;

    private String instrument;

    private String price;

    private Integer quantity;

    private String result;

    private String message;
}
