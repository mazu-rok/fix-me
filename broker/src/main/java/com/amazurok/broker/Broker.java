package com.amazurok.broker;

import com.amazurok.broker.handler.ExecutionResult;
import com.amazurok.broker.handler.ResultTagValidator;
import com.amazurok.fixme.common.Client;
import com.amazurok.fixme.common.Common;
import com.amazurok.fixme.common.FixTag;
import com.amazurok.fixme.common.handler.MessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Broker extends Client{
    private static Logger log = LoggerFactory.getLogger(Broker.class);

    private static final String USER_INPUT_DELIMITER = " ";
    private static final String USER_MESSAGE_FORMAT = "'MARKET_NAME BUY_OR_SELL INSTRUMENT_NAME QUANTITY PRICE'";

    private static final String TAG_VALUE_DELIMITER = "=";
    private static final String FIELD_DELIMITER = "|";
    public static final String NAME_PREFIX = "B. ";


    public Broker(String name) {
        super(Common.BROKER_PORT, NAME_PREFIX + name);
    }

    private static void addTag(StringBuilder builder, FixTag tag, String value) {
        builder.append(tag.getValue())
                .append(TAG_VALUE_DELIMITER)
                .append(value)
                .append(FIELD_DELIMITER);
    }

    public static String userInputToFixMessage(String input, String id, String name) throws Exception {
        final String[] m = input.split(USER_INPUT_DELIMITER);
        if (m.length != 5) {
            throw new Exception("Wrong input, should be: " + USER_MESSAGE_FORMAT);
        }
        final StringBuilder builder = new StringBuilder();
        addTag(builder, FixTag.ID, id);
        addTag(builder, FixTag.SOURCE_NAME, name);
        addTag(builder, FixTag.TARGET_NAME, m[0]);
        addTag(builder, FixTag.TYPE, m[1]);
        addTag(builder, FixTag.INSTRUMENT, m[2]);
        addTag(builder, FixTag.QUANTITY, m[3]);
        addTag(builder, FixTag.PRICE, m[4]);
        addTag(builder, FixTag.CHECKSUM, Common.calculateChecksum(builder.toString()));
        return builder.toString();
    }

    private void start() {
        try {
            log.info("The broker starts up ...");
            readFromSocket();

            final Scanner scanner = new Scanner(System.in);
            log.info("Message to send " + USER_MESSAGE_FORMAT + ":");
            while (true) {
                try {
                    final String message = userInputToFixMessage(scanner.nextLine(), getId(), getName());
                    final Future<Integer> result = Common.sendMessage(getSocketChannel(), message);
                    result.get();
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    @Override
    protected MessageHandler getMessageHandler() {
        final MessageHandler messageHandler = super.getMessageHandler();
        final MessageHandler resultTag = new ResultTagValidator();
        final MessageHandler executionResult = new ExecutionResult();
        messageHandler.setNext(resultTag);
        resultTag.setNext(executionResult);
        return messageHandler;
    }


    public static void main(String[] args) {
        if (args.length == 2) {
            new Broker(args[1]).start();
        } else {
            log.error("Wrong number of arguments[{}]", args);
            throw new IllegalArgumentException("Only one argument is supported");
        }
    }

}
