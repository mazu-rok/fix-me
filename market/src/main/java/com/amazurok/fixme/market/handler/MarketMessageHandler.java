package com.amazurok.fixme.market.handler;

import com.amazurok.fixme.common.Common;
import com.amazurok.fixme.common.FIXMessage;
import com.amazurok.fixme.common.ResultMessage;
import com.amazurok.fixme.common.handler.MessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.channels.AsynchronousSocketChannel;

import static com.amazurok.fixme.common.Common.addTag;
import static com.amazurok.fixme.common.Common.calculateChecksum;

public abstract class MarketMessageHandler extends MessageHandler {
    private static Logger log = LoggerFactory.getLogger(MarketMessageHandler.class);

    private final String id;
    private final String name;

    public MarketMessageHandler(String id, String name) {
        this.id = id;
        this.name = name;
    }

    protected void rejectedMessage(AsynchronousSocketChannel clientChannel, String fixMessage, String message) {
        sendMessage(clientChannel, fixMessage, message, ResultMessage.REJECTED);
    }

    protected void executedMessage(AsynchronousSocketChannel clientChannel, String fixMessage, String message) {
        sendMessage(clientChannel, fixMessage, message, ResultMessage.EXECUTED);
    }

    public static String resultFixMessage(String message, String id, String srcName, String targetName, ResultMessage result) {
        final StringBuilder builder = new StringBuilder();
        addTag(builder, FIXMessage.ID, id);
        addTag(builder, FIXMessage.MARKET, srcName);
        addTag(builder, FIXMessage.BROKER, targetName);
        addTag(builder, FIXMessage.DST, targetName);
        addTag(builder, FIXMessage.RESULT, result.toString());
        addTag(builder, FIXMessage.MESSAGE, message);
        addTag(builder, FIXMessage.CHECKSUM, calculateChecksum(builder.toString()));
        return builder.toString();
    }

    private void sendMessage(AsynchronousSocketChannel clientChannel, String fixMessage, String message, ResultMessage result) {
        final String targetName;
        try {
            targetName = Common.getFixValueByTag(fixMessage, FIXMessage.MARKET);
            Common.sendMessage(clientChannel, resultFixMessage(message, id, name, targetName, result));
//        if (isInsertMessagesToDb()) {
//            Database.insert(
//                    name,
//                    targetName,
//                    Core.getFixValueByTag(fixMessage, Tags.ACTION),
//                    Core.getFixValueByTag(fixMessage, Tags.PRODUCT),
//                    Core.getFixValueByTag(fixMessage, Tags.PRICE),
//                    Core.getFixValueByTag(fixMessage, Tags.AMOUNT),
//                    result.toString(),
//                    message);
//            Database.selectAll();
//        }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    protected boolean isInsertMessagesToDb() {
        return false;
    }
}

