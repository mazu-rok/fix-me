package com.amazurok.fixme.router;


import com.amazurok.fixme.common.Common;
import com.amazurok.fixme.common.handler.ChecksumValidator;
import com.amazurok.fixme.common.handler.ErrorMessageHandler;
import com.amazurok.fixme.common.handler.FIXMessageMandatoryFieldsValidator;
import com.amazurok.fixme.common.handler.MessageHandler;
import com.amazurok.fixme.router.handler.MessageProcessor;
import com.amazurok.fixme.router.handler.RouterCompletionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Router {
    private Logger log = LoggerFactory.getLogger(Router.class);

    private static final int INITIAL_ID = 100000;
    private final AtomicInteger ID = new AtomicInteger(INITIAL_ID);

    private final Map<String, AsynchronousSocketChannel> routingTable = new ConcurrentHashMap<>();
    private final Map<String, String> failedToSendMessages = new ConcurrentHashMap<>();


    private void start() {
        log.info("The Router starts up ...");

        try {
            final MessageHandler messageHandler = getMessageHandler();

            final AsynchronousServerSocketChannel brokersListener = AsynchronousServerSocketChannel.open()
                    .bind(new InetSocketAddress(Common.HOST, Common.BROKER_PORT));
            brokersListener.accept(null,
                    new RouterCompletionHandler(brokersListener, routingTable, ID, messageHandler));

            final AsynchronousServerSocketChannel marketsListener = AsynchronousServerSocketChannel.open()
                    .bind(new InetSocketAddress(Common.HOST, Common.MARKET_PORT));
            marketsListener.accept(null,
                    new RouterCompletionHandler(marketsListener, routingTable, ID, messageHandler));
            log.info("The Router is running!");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.error(e.getMessage());
            }
//            tryToSendFailedMessages();
        }

    }

//    private void tryToSendFailedMessages() {
//        if (!failedToSendMessages.isEmpty()) {
//            System.out.println("Trying to send failed messages...");
//            failedToSendMessages.keySet().removeIf(targetName -> {
//                final AsynchronousSocketChannel targetChannel = routingTable.get(targetName);
//                if (targetChannel != null) {
//                    System.out.println("Found message to resend " + targetName + ", sending message");
//                    Utils.sendMessage(targetChannel, failedToSendMessages.get(targetName));
//                    return true;
//                }
//                return false;
//            });
//        }
//    }

    private MessageHandler getMessageHandler() {
        final MessageHandler messageHandler = new ErrorMessageHandler();
        final MessageHandler mandatoryFieldsValidator = new FIXMessageMandatoryFieldsValidator();
        final MessageHandler checksumValidator = new ChecksumValidator();
        final MessageHandler messageParser = new MessageProcessor(routingTable, failedToSendMessages);
        messageHandler.setNext(mandatoryFieldsValidator);
        mandatoryFieldsValidator.setNext(checksumValidator);
        checksumValidator.setNext(messageParser);
        return messageHandler;
    }

    public static void main(String[] args) {
        new Router().start();
    }
}
