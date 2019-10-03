package com.amazurok.fixme.market.handler;

import com.amazurok.fixme.common.Common;
import com.amazurok.fixme.common.FIXMessage;
import com.amazurok.fixme.common.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.channels.AsynchronousSocketChannel;
import java.util.Map;

public class MessageExecutor extends MarketMessageHandler {
    private static Logger log = LoggerFactory.getLogger(MessageExecutor.class);

    private final Map<String, Integer> instrumentsFortrading;

    public MessageExecutor(String clientId, String name, Map<String, Integer> instruments) {
        super(clientId, name);
        this.instrumentsFortrading = instruments;
    }

    @Override
    public void handle(AsynchronousSocketChannel clientChannel, String message) {
        try {
            final String instrument = Common.getValueFromFIXMesage(message, FIXMessage.INSTRUMENT);
            final int quantity = Integer.parseInt(Common.getValueFromFIXMesage(message, FIXMessage.QUANTITY));
            final String type = Common.getValueFromFIXMesage(message, FIXMessage.ACTION);
            int availableQuantity = instrumentsFortrading.get(instrument);
            switch (MessageType.valueOf(type)) {
                case BUY:
                    if (availableQuantity <= quantity) {
                        rejectedMessage(clientChannel, message, "Not enough instruments");
                    } else {
                        instrumentsFortrading.put(instrument, availableQuantity - quantity);
                        log.info(String.format("%d laptops %s sold", quantity, instrument));
                    }
                    break;
                case SELL:
                    instrumentsFortrading.put(instrument, availableQuantity + quantity);
                    log.info(String.format("%d %s purchased", quantity, instrument));
            }
            log.info("Available instruments: " + instrumentsFortrading.toString());
            executedMessage(clientChannel, message, "OK");
        } catch (NotFoundException e) {
            log.error(e.getMessage());
        }
    }

//    @Override
//    protected boolean isInsertMessagesToDb() {
//        return true;
//    }
}

