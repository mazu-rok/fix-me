package com.amazurok.fixme.market.handler;

import com.amazurok.fixme.common.Common;
import com.amazurok.fixme.common.FIXMessage;
import com.amazurok.fixme.common.ResultMessage;
import com.amazurok.fixme.common.exception.NotFoundException;
import com.amazurok.fixme.market.DBController;
import com.amazurok.fixme.market.entity.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.channels.AsynchronousSocketChannel;
import java.util.Map;

public class MessageExecutor extends MarketMessageHandler {
    private static Logger log = LoggerFactory.getLogger(MessageExecutor.class);

    private final DBController db;

    private final Map<String, Integer> instrumentsFortrading;

    public MessageExecutor(String clientId, String name, Map<String, Integer> instruments, DBController db) {
        super(clientId, name);
        this.instrumentsFortrading = instruments;
        this.db = db;
    }

    @Override
    public void handle(AsynchronousSocketChannel clientChannel, String message) {
        try {
            final String instrument = Common.getValueFromFIXMesage(message, FIXMessage.INSTRUMENT);
            final int quantity = Integer.parseInt(Common.getValueFromFIXMesage(message, FIXMessage.QUANTITY));
            final String type = Common.getValueFromFIXMesage(message, FIXMessage.ACTION);
            int availableQuantity = instrumentsFortrading.get(instrument);
            ResultMessage result = ResultMessage.EXECUTED;
            String resultMessage = "OK";
            switch (MessageType.valueOf(type.toUpperCase())) {
                case BUY:
                    if (quantity > availableQuantity) {
                        resultMessage = "Not enough instruments";
                        rejectedMessage(clientChannel, message, resultMessage);
                        result = ResultMessage.REJECTED;
                    } else {
                        instrumentsFortrading.put(instrument, availableQuantity - quantity);
                        log.info(String.format("%d laptops %s sold", quantity, instrument));
                    }
                    break;
                case SELL:
                    instrumentsFortrading.put(instrument, availableQuantity + quantity);
                    log.info(String.format("%d %s purchased", quantity, instrument));
                    break;
            }
            db.save(Transaction.builder()
                    .marketName(Common.getValueFromFIXMesage(message, FIXMessage.MARKET))
                    .brokerName(Common.getValueFromFIXMesage(message, FIXMessage.BROKER))
                    .action(type.toUpperCase())
                    .instrument(instrument)
                    .price(Common.getValueFromFIXMesage(message, FIXMessage.PRICE))
                    .quantity(quantity)
                    .result(result.name())
                    .message(resultMessage)
                    .build());
            if (result.equals(ResultMessage.EXECUTED)) {
                log.info("Available instruments: " + instrumentsFortrading.toString());
                executedMessage(clientChannel, message, "OK");
            }
        } catch (NotFoundException e) {
            log.error(e.getMessage());
        }
    }
}

