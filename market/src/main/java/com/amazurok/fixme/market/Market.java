package com.amazurok.fixme.market;

import com.amazurok.fixme.common.Client;
import com.amazurok.fixme.common.Common;
import com.amazurok.fixme.common.handler.MessageHandler;
import com.amazurok.fixme.market.handler.MarketTagsValidator;
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

    private static final String[] INSTRUMENTS = {
            "bolt", "nail", "screwdriver", "screw",
            "hammer", "saw", "drill", "wrench", "knife",
            "scissors", "toolbox", "tape", "needle"
    };

    private static final String NAME_PREFIX = "M";
    private final Map<String, Integer> instruments;

    private Market(String name) {
        super(Common.MARKET_PORT, NAME_PREFIX + name);
        instruments = getRandomInstruments();

    }

    private static Map<String, Integer> getRandomInstruments() {
        final Map<String, Integer> instruments = new HashMap<String, Integer>();
        final Random random = new Random();
        for(String instrument : INSTRUMENTS) {
            if (random.nextBoolean()) {
                instruments.put(instrument, random.nextInt(9) + 1);
            }
        }
        return instruments;
    }

    @Override
    protected MessageHandler getMessageHandler() {
        final MessageHandler messageHandler = super.getMessageHandler();
        final MessageHandler tagsValidator = new MarketTagsValidator(getId(), getName());
        final MessageHandler messageExecutor = new MessageExecutor(getId(), getName(), instruments);
        messageHandler.setNext(tagsValidator);
        tagsValidator.setNext(messageExecutor);
        return messageHandler;
    }

    private void start() {
        log.info("The marker starts up ...");

        readFromSocket();

        while (true) {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                log.error(e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        if (args.length == 1) {
            new Market(args[0]).start();
        } else {
            log.error("Wrong number of arguments[{}]", args);
            throw new IllegalArgumentException("Only one argument is supported");
        }
    }
}
