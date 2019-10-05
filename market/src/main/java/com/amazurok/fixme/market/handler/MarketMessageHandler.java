package com.amazurok.fixme.market.handler;

import com.amazurok.fixme.common.Common;
import com.amazurok.fixme.common.FIXMessage;
import com.amazurok.fixme.common.ResultMessage;
import com.amazurok.fixme.common.handler.MessageHandler;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.channels.AsynchronousSocketChannel;
import java.security.NoSuchAlgorithmException;

import static com.amazurok.fixme.common.Common.*;

public abstract class MarketMessageHandler extends MessageHandler {
    private static Logger log = LoggerFactory.getLogger(MarketMessageHandler.class);

    private final String id;
    private final String name;

    public MarketMessageHandler(String id, String name) {
        this.id = id;
        this.name = name;
    }

    protected void rejectedMessage(AsynchronousSocketChannel clientChannel, String fixMessage, String message) {
        log.error(message);
        sendMessage(clientChannel, fixMessage, message, ResultMessage.REJECTED);
    }

    protected void executedMessage(AsynchronousSocketChannel clientChannel, String fixMessage, String message) {
        sendMessage(clientChannel, fixMessage, message, ResultMessage.EXECUTED);
    }

    private String resultFixMessage(String message, String brokerName, ResultMessage result) throws NoSuchAlgorithmException {
        final StringBuilder builder = new StringBuilder();
        addFieldToMessage(builder, FIXMessage.ID, id);
        addFieldToMessage(builder, FIXMessage.MARKET, name);
        addFieldToMessage(builder, FIXMessage.BROKER, brokerName);
        addFieldToMessage(builder, FIXMessage.DST, brokerName);
        addFieldToMessage(builder, FIXMessage.RESULT, result.toString());
        addFieldToMessage(builder, FIXMessage.MESSAGE, message);
        addFieldToMessage(builder, FIXMessage.CHECKSUM, getChecksum(builder.toString()));
        return builder.toString();
    }

    private void sendMessage(AsynchronousSocketChannel clientChannel, String fixMessage, String message, ResultMessage result) {
        try {
            String brokerName = Common.getValueFromFIXMesage(fixMessage, FIXMessage.BROKER);
            Common.sendMessage(clientChannel, resultFixMessage(message, brokerName, result));
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}

