package com.amazurok.fixme.market;

import com.amazurok.fixme.common.Client;
import com.amazurok.fixme.common.Common;
import com.amazurok.fixme.common.handler.MessageHandler;
import com.amazurok.fixme.market.handler.FieldsValidator;
import com.amazurok.fixme.market.handler.MessageExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

public class Market extends Client {
    private static Logger log = LoggerFactory.getLogger(Market.class);
    private final DBController db = new DBController();

    public static final String[] INSTRUMENTS_FOR_TRADING = {
            "MacBook", "Acer", "Asus", "Samsung", "Xiaomi", "HP"
    };
    public static final Integer MAX_QUANTITY = 10;
    public static final Integer MAX_PRICE = 10000;

    private final Map<String, Integer> instrumentsForTrading;

    private Market(String name) {
        super(Common.MARKET_PORT, name);
        instrumentsForTrading = generateRandomInstruments();
    }

    private static Map<String, Integer> generateRandomInstruments() {
        final Map<String, Integer> instruments = new HashMap<>();
        final Random random = new Random();
        for(String instrument : INSTRUMENTS_FOR_TRADING) {
            instruments.put(instrument, random.nextInt(MAX_QUANTITY));
        }
        return instruments;
    }

    @Override
    protected MessageHandler getMessageHandler() {
        final MessageHandler messageHandler = super.getMessageHandler();
        final MessageHandler tagsValidator = new FieldsValidator(getId(), getName());
        final MessageHandler messageExecutor = new MessageExecutor(getId(), getName(), instrumentsForTrading, db);

        messageHandler.setNext(tagsValidator);
        tagsValidator.setNext(messageExecutor);
        return messageHandler;
    }

    private void start() {
        readFromSocket();
        log.info("The marker started with next instruments {}", instrumentsForTrading);

        while (true) {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                log.error(e.getMessage());
                return;
            }
        }
    }

    public static void main(String[] args) {
        log.info("The marker starts up ...");
        if (args.length == 1) {
            new Market(args[0]).start();
        } else {
            log.error("Wrong number of arguments[{}]", args);
            throw new IllegalArgumentException("Only one argument is supported");
        }
    }
}
