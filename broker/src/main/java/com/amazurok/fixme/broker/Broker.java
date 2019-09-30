package com.amazurok.fixme.broker;

import com.amazurok.fixme.broker.handler.ExecutionResult;
import com.amazurok.fixme.broker.handler.ResultTagValidator;
import com.amazurok.fixme.common.Client;
import com.amazurok.fixme.common.Common;
import com.amazurok.fixme.common.FIXMessage;
import com.amazurok.fixme.common.handler.MessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;
import java.util.concurrent.Future;

import static com.amazurok.fixme.common.Common.addTag;

public class Broker extends Client{
    private static Logger log = LoggerFactory.getLogger(Broker.class);

    private static final String USER_INPUT_DELIMITER = " ";
    private static final String USER_MESSAGE_FORMAT = "'MARKET_NAME BUY_OR_SELL PRODUCT_NAME AMOUNT PRICE'";

    public static final String NAME_PREFIX = "B_";


    public Broker(String name) {
        super(Common.BROKER_PORT, NAME_PREFIX + name);
    }

    public static String userInputToFixMessage(String input, String id, String name) throws Exception {
        final String[] m = input.split(USER_INPUT_DELIMITER);
        if (m.length != 5) {
            throw new Exception("Wrong input, should be: " + USER_MESSAGE_FORMAT);
        }
        final StringBuilder builder = new StringBuilder();
        addTag(builder, FIXMessage.ID, id);
        addTag(builder, FIXMessage.BROKER, name);
        addTag(builder, FIXMessage.MARKET, m[0]);
        addTag(builder, FIXMessage.DST, m[0]);
        addTag(builder, FIXMessage.ACTION, m[1]);
        addTag(builder, FIXMessage.INSTRUMENT, m[2]);
        addTag(builder, FIXMessage.QUANTITY, m[3]);
        addTag(builder, FIXMessage.PRICE, m[4]);
        addTag(builder, FIXMessage.CHECKSUM, Common.calculateChecksum(builder.toString()));
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
        if (args.length == 1) {
            new Broker(args[0]).start();
        } else {
            log.error("Wrong number of arguments[{}]", args);
            throw new IllegalArgumentException("Only one argument is supported");
        }
    }

}
