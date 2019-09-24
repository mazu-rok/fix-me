package com.amazurok.fixme.common;

import com.amazurok.fixme.common.handler.ChecksumValidator;
import com.amazurok.fixme.common.handler.InternalMessageHandler;
import com.amazurok.fixme.common.handler.MessageHandler;
import com.amazurok.fixme.common.handler.TagsValidator;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Client {
    private static Logger log = LoggerFactory.getLogger(Client.class);

    private final ByteBuffer buffer = ByteBuffer.allocate(Common.BUFFER_SIZE);

    private final int port;
    @Getter
    private final String name;
    @Getter
    private String id = "";

    private AsynchronousSocketChannel socketChannel;

    public Client(int port, String name) {
        this.port = port;
        this.name = name;
    }

    private AsynchronousSocketChannel connectToMessageRouter() {
        final AsynchronousSocketChannel socketChannel;
        try {
            socketChannel = AsynchronousSocketChannel.open();
            final Future future = socketChannel.connect(new InetSocketAddress(Common.HOST, port));
            future.get();
        } catch (IOException | InterruptedException | ExecutionException e) {
            System.out.println("Could not connect to Message Router, reconnecting...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
            return connectToMessageRouter();
        }
        return socketChannel;
    }

    protected AsynchronousSocketChannel getSocketChannel() {
        if (socketChannel == null) {
            socketChannel = connectToMessageRouter();
            Common.sendMessage(socketChannel, name);
            id = Common.readMessage(socketChannel, buffer);
            System.out.println(name + " ID: " + id);
            return socketChannel;
        }
        return socketChannel;
    }

    protected MessageHandler getMessageHandler() {
        final MessageHandler messageHandler = new InternalMessageHandler();
        final MessageHandler mandatoryTagsValidator = new TagsValidator();
        final MessageHandler checksumValidator = new ChecksumValidator();
        messageHandler.setNext(mandatoryTagsValidator);
        mandatoryTagsValidator.setNext(checksumValidator);
        return messageHandler;
    }

    protected void readFromSocket() {
        getSocketChannel().read(buffer, null, new CompletionHandler<Integer, Object>() {
            @Override
            public void completed(Integer result, Object attachment) {
                final String message = Common.read(result, buffer);
                if (!Common.EMPTY_MESSAGE.equals(message)) {
                    getMessageHandler().handle(getSocketChannel(), message);
                    getSocketChannel().read(buffer, null, this);
                } else {
                    reconnect();
                }
            }

            @Override
            public void failed(Throwable exc, Object attachment) {
                reconnect();
            }

            private void reconnect() {
                log.warn("Message router died! Have to reconnect");
                socketChannel = null;
                getSocketChannel().read(buffer, null, this);
            }
        });
    }
}
