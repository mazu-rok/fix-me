package com.amazurok.fixme.market.handler;

import com.amazurok.fixme.common.Common;
import com.amazurok.fixme.common.FixTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.channels.AsynchronousSocketChannel;
import java.util.Map;

public class MessageExecutor extends MarketMessageHandler {
    private static Logger log = LoggerFactory.getLogger(MessageExecutor.class);

    private final Map<String, Integer> instruments;

    public MessageExecutor(String clientId, String name, Map<String, Integer> instruments) {
        super(clientId, name);
        this.instruments = instruments;
    }

    @Override
    public void handle(AsynchronousSocketChannel clientChannel, String message) {
        try {
            final String instrument = Common.getFixValueByTag(message, FixTag.INSTRUMENT);
            if (instruments.containsKey(instrument)) {
                final int quantity = Integer.parseInt(Common.getFixValueByTag(message, FixTag.QUANTITY));
                final int marketQuantity = instruments.get(instrument);
                final String type = Common.getFixValueByTag(message, FixTag.TYPE);
                if (type.equals(MessageType.Buy.toString())) {
                    if (marketQuantity < quantity) {
                        rejectedMessage(clientChannel, message, "Not enough instruments");
                        return;
                    } else {
                        instruments.put(instrument, marketQuantity - quantity);
                    }
                } else {
                    instruments.put(instrument, marketQuantity + quantity);
                }
                log.info("Market instruments: " + instruments.toString());
                executedMessage(clientChannel, message, "OK");
            } else {
                rejectedMessage(clientChannel, message, instrument + " instrument is not traded on the market");
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    @Override
    protected boolean isInsertMessagesToDb() {
        return true;
    }
}

