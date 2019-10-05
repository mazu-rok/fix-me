package com.amazurok.fixme.broker;

import com.amazurok.fixme.broker.handler.ExecutionResult;
import com.amazurok.fixme.broker.handler.ResultValidator;
import com.amazurok.fixme.common.Client;
import com.amazurok.fixme.common.Common;
import com.amazurok.fixme.common.FIXMessage;
import com.amazurok.fixme.common.exception.IllegalInputException;
import com.amazurok.fixme.common.handler.MessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

import static com.amazurok.fixme.common.Common.addFieldToMessage;

public class Broker extends Client {
    private static Logger log = LoggerFactory.getLogger(Broker.class);

    private static final String INPUT_MESSAGE_DELIMITER = " ";
    private static final String INPUT_MESSAGE_FORMAT = "'MARKET_NAME BUY_OR_SELL PRODUCT_NAME AMOUNT PRICE'";

    public Broker(String name) {
        super(Common.BROKER_PORT, name);
    }

    private static String createFIXMessage(String input, String id, String name) throws IllegalInputException, NoSuchAlgorithmException {
        final String[] m = input.split(INPUT_MESSAGE_DELIMITER);
        if (m.length != 5) {
            throw new IllegalInputException(String.format("Unacceptable input, example: %s", INPUT_MESSAGE_FORMAT));
        }
        final StringBuilder message = new StringBuilder();
        addFieldToMessage(message, FIXMessage.ID, id);
        addFieldToMessage(message, FIXMessage.BROKER, name);
        addFieldToMessage(message, FIXMessage.MARKET, m[0]);
        addFieldToMessage(message, FIXMessage.DST, m[0]);
        addFieldToMessage(message, FIXMessage.ACTION, m[1]);
        addFieldToMessage(message, FIXMessage.INSTRUMENT, m[2]);
        addFieldToMessage(message, FIXMessage.QUANTITY, m[3]);
        addFieldToMessage(message, FIXMessage.PRICE, m[4]);
        addFieldToMessage(message, FIXMessage.CHECKSUM, Common.getChecksum(message.toString()));
        return message.toString();
    }

    private void start() {
        log.info("The broker starts up ...");
        readFromSocket();

        final Scanner inputScanner = new Scanner(System.in);
        while (true) {
            try {
                final String message = createFIXMessage(inputScanner.nextLine(), getId(), getName()); // waiting for input
                Common.sendMessage(getSocketChannel(), message);
            } catch (IllegalInputException e) {
                log.error("Not valid input, try again: {}", e.getMessage());
            } catch (NoSuchAlgorithmException e) {
                log.error(e.getMessage());
                return;
            }
        }
    }

    @Override
    protected MessageHandler getMessageHandler() {
        final MessageHandler messageHandler = super.getMessageHandler();
        final MessageHandler resultTag = new ResultValidator();
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
