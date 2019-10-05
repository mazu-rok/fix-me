package com.amazurok.fixme.router;


import com.amazurok.fixme.common.Common;
import com.amazurok.fixme.common.handler.*;
import com.amazurok.fixme.router.handler.MessageSender;
import com.amazurok.fixme.router.handler.RouterCompletionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Router {
    private static Logger log = LoggerFactory.getLogger(Router.class);

    private static final int INITIAL_ID = 100000;
    private final AtomicInteger ID = new AtomicInteger(INITIAL_ID);

    private final Map<String, AsynchronousSocketChannel> routingTable = new ConcurrentHashMap<>();

    private final Map<String, String> failedMessages = new ConcurrentHashMap<>();
    private static final int EXECUTING_PERIOD_SEC = 15;
    private final ScheduledExecutorService failedMessagesExecutor = Executors.newScheduledThreadPool(1);

    private void start() {
        log.info("The Router starts up ...");

        try {
            final AsynchronousServerSocketChannel brokersListener = AsynchronousServerSocketChannel.open()
                    .bind(new InetSocketAddress(Common.HOST, Common.BROKER_PORT));
            brokersListener.accept(null,
                    new RouterCompletionHandler(brokersListener, routingTable, ID, getMessageHandlerForBroker()));

            final AsynchronousServerSocketChannel marketsListener = AsynchronousServerSocketChannel.open()
                    .bind(new InetSocketAddress(Common.HOST, Common.MARKET_PORT));
            marketsListener.accept(null,
                    new RouterCompletionHandler(marketsListener, routingTable, ID, getMessageHandlerForMarket()));

            failedMessagesExecutor.scheduleAtFixedRate(resendFailedMessages, 0, EXECUTING_PERIOD_SEC, TimeUnit.SECONDS);
            log.info("The Router is running!");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        while (true) {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                log.error(e.getMessage());
                return;
            }
        }

    }

    private Runnable resendFailedMessages = () -> {
        if (!failedMessages.isEmpty()) {
            log.info("Trying to resend failed messages...");
            failedMessages.keySet().removeIf(dstName -> {
                final AsynchronousSocketChannel channel = routingTable.get(dstName);
                if (Objects.nonNull(channel)) {
                    log.info("Resend message to {}", dstName);
                    Common.sendMessage(channel, failedMessages.get(dstName));
                    return true;
                }
                return false;
            });
        }
    };


    private MessageHandler getMessageHandlerForBroker() {
        final MessageHandler messageHandler = new ErrorMessageHandler();
        final MessageHandler mandatoryFieldsValidator = new FIXMessageMandatoryBrokerFieldsValidator();
        final MessageHandler checksumValidator = new ChecksumValidator();
        final MessageHandler messageSender = new MessageSender(routingTable, failedMessages);

        messageHandler.setNext(mandatoryFieldsValidator);
        mandatoryFieldsValidator.setNext(checksumValidator);
        checksumValidator.setNext(messageSender);

        return messageHandler;
    }

    private MessageHandler getMessageHandlerForMarket() {
        final MessageHandler messageHandler = new ErrorMessageHandler();
        final MessageHandler mandatoryFieldsValidator = new FIXMessageMandatoryMarketFieldsValidator();
        final MessageHandler checksumValidator = new ChecksumValidator();
        final MessageHandler messageSender = new MessageSender(routingTable, failedMessages);

        messageHandler.setNext(mandatoryFieldsValidator);
        mandatoryFieldsValidator.setNext(checksumValidator);
        checksumValidator.setNext(messageSender);

        return messageHandler;
    }


    public static void main(String[] args) {
        new Router().start();
    }
}
