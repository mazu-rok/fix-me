package com.amazurok.fixme.router.handler;

import com.amazurok.fixme.common.Common;
import com.amazurok.fixme.common.handler.MessageHandler;
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
    private Logger log = LoggerFactory.getLogger(RouterCompletionHandler.class);
    private static final int EXECUTOR_THREADS = 5;
    private static final String EMPTY_MESSAGE = "";


    private final ExecutorService executor = Executors.newFixedThreadPool(EXECUTOR_THREADS);
    private final AsynchronousServerSocketChannel listener;
    private final Map<String, AsynchronousSocketChannel> routingTable;
    private final AtomicInteger id;
    private final MessageHandler messageHandler;
    private String clientName;

    public RouterCompletionHandler(AsynchronousServerSocketChannel listener, Map<String, AsynchronousSocketChannel> routingTable,
                            AtomicInteger id, MessageHandler messageHandler) {
        this.listener = listener;
        this.routingTable = routingTable;
        this.id = id;
        this.messageHandler = messageHandler;
    }

    private static String readMessage(AsynchronousSocketChannel channel, ByteBuffer readBuffer) {
        try {
            int bytesRead = channel.read(readBuffer).get();
            if (bytesRead != -1) {
                readBuffer.flip();
                byte[] bytes = new byte[bytesRead];
                readBuffer.get(bytes, 0, bytesRead);
                readBuffer.clear();
                String message = new String(bytes);
                System.out.println("Got: " + message);
                return message;
            }
            return EMPTY_MESSAGE;
        } catch (InterruptedException | ExecutionException e1) {
            e1.printStackTrace();
            return EMPTY_MESSAGE;
        }
    }

    @Override
    public void completed(AsynchronousSocketChannel channel, Object attachment) {
        listener.accept(null, this);
        final ByteBuffer buffer = ByteBuffer.allocate(Common.BUFFER_SIZE);
        clientName = readMessage(channel, buffer);

        sendClientId(channel, getNextId());

        while (true) {
            final String message = readMessage(channel, buffer);
            if (EMPTY_MESSAGE.equals(message)) {
                break;
            }
            executor.execute(() -> messageHandler.handle(channel, message));
        }
        endConnection();
    }

    @Override
    public void failed(Throwable exc, Object attachment) {
        log.warn("Force connection termination");
        endConnection();
    }



    private void sendClientId(AsynchronousSocketChannel channel, String currentId) {
        log.info(String.format("Client '%s' connected with ID: %s", clientName, currentId));
        Common.sendMessage(channel, currentId);
        routingTable.put(clientName, channel);
//        printRoutingTable();
    }

    private void endConnection() {
        routingTable.remove(clientName);
        log.info(String.format("Client '%s' disconnected", clientName));
//        printRoutingTable();
    }

//    private void printRoutingTable() {
//        System.out.println("Routing table: " + routingTable.keySet().toString());
//    }

    private String getNextId() {
        return String.format("%d", id.getAndIncrement());
    }
}
