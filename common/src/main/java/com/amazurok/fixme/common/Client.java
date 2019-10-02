package com.amazurok.fixme.common;

import com.amazurok.fixme.common.handler.ChecksumValidator;
import com.amazurok.fixme.common.handler.ErrorMessageHandler;
import com.amazurok.fixme.common.handler.FIXMessageMandatoryFieldsValidator;
import com.amazurok.fixme.common.handler.MessageHandler;
import com.google.common.base.Strings;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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

    private AsynchronousSocketChannel connectToRouter() {
        final AsynchronousSocketChannel socketChannel;
        try {
            socketChannel = AsynchronousSocketChannel.open();
            final Future future = socketChannel.connect(new InetSocketAddress(Common.HOST, port));
            future.get();
        } catch (IOException | InterruptedException | ExecutionException e) {
            log.error("Could not connect to Router, trying to reconnect...");
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException ex) {
                log.error(ex.getMessage());
            }
            return connectToRouter();
        }
        return socketChannel;
    }

    protected AsynchronousSocketChannel getSocketChannel() {
        if (Objects.isNull(socketChannel)) {
            socketChannel = connectToRouter();
            Common.sendMessage(socketChannel, name);
            id = Common.readMessage(socketChannel, buffer);
            log.info("Connected to Router with name '{}' and ID '{}'", name, id);
            return socketChannel;
        }
        return socketChannel;
    }

    protected MessageHandler getMessageHandler() {
        final MessageHandler messageHandler = new ErrorMessageHandler();
        final MessageHandler mandatoryFieldsValidator = new FIXMessageMandatoryFieldsValidator();
        final MessageHandler checksumValidator = new ChecksumValidator();

        messageHandler.setNext(mandatoryFieldsValidator);
        mandatoryFieldsValidator.setNext(checksumValidator);

        return messageHandler;
    }

    protected void readFromSocket() {
        getSocketChannel().read(buffer, null, new CompletionHandler<Integer, Object>() {
            @Override
            public void completed(Integer result, Object attachment) {
                final String message = Common.read(result, buffer);
                if (!Strings.isNullOrEmpty(message)) {
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
                log.warn("Message router doesn't exist! Trying to reconnect");
                socketChannel = null;
                getSocketChannel().read(buffer, null, this);
            }
        });
    }
}
