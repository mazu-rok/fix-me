package com.amazurok.fixme.router.handler;

import com.amazurok.fixme.common.Common;
import com.amazurok.fixme.common.exception.IllegalInputException;
import com.amazurok.fixme.common.handler.MessageHandler;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class RouterCompletionHandler implements CompletionHandler<AsynchronousSocketChannel, Object> {
    private static Logger log = LoggerFactory.getLogger(RouterCompletionHandler.class);
    private static final int EXECUTOR_THREADS = 5;


    private final ExecutorService executor = Executors.newFixedThreadPool(EXECUTOR_THREADS);
    private final AsynchronousServerSocketChannel listener;
    private final Map<String, AsynchronousSocketChannel> routingTable;
    private final AtomicInteger id;
    private final MessageHandler messageHandler;
    private String clientName;

    public RouterCompletionHandler(AsynchronousServerSocketChannel listener,
                                   Map<String, AsynchronousSocketChannel> routingTable,
                                   AtomicInteger id,
                                   MessageHandler messageHandler) {
        this.listener = listener;
        this.routingTable = routingTable;
        this.id = id;
        this.messageHandler = messageHandler;
    }

    @Override
    public void completed(AsynchronousSocketChannel channel, Object attachment) {
        listener.accept(null, this);

        final ByteBuffer buffer = ByteBuffer.allocate(Common.BUFFER_SIZE);
        try {
            clientName = Common.readMessage(channel, buffer);
        } catch (IllegalInputException | ExecutionException | InterruptedException e) {
            log.error(e.getMessage());
        }

        try {
            saveAndsendClientId(channel, getNextId());
        } catch (IllegalInputException e) {
            log.error(e.getMessage());
            return;
        }

        while (true) {
            try {
                final String message = Common.readMessage(channel, buffer);
                if (Strings.isNullOrEmpty(message)) {
                    break;
                }
                executor.execute(() -> messageHandler.handle(channel, message));
            } catch (IllegalInputException | ExecutionException | InterruptedException e) {
                log.error(e.getMessage());
            }
        }
        closeConnection();
    }

    @Override
    public void failed(Throwable exc, Object attachment) {
        log.warn("Force connection termination");
        closeConnection();
    }

    private String getNextId() {
        return String.valueOf(id.getAndIncrement());
    }

    private void saveAndsendClientId(AsynchronousSocketChannel channel, String currentId) throws IllegalInputException {
        if (routingTable.containsKey(clientName)) {
            Common.sendErrorMessage(channel, String.format("Name '%s' is already exist", clientName));
            throw new IllegalInputException(String.format("Name '%s' is already exist", clientName));
        }
        routingTable.put(clientName, channel);
        log.info(String.format("Client '%s' connected with ID: %s", clientName, currentId));
        Common.sendMessage(channel, currentId);
        log.debug(String.format("\tRouting table\n%s", routingTable.keySet()));
    }

    private void closeConnection() {
        routingTable.remove(clientName);
        log.info(String.format("Client '%s' disconnected", clientName));
        log.debug(String.format("\tRouting table\n%s", routingTable.keySet()));
    }
}
