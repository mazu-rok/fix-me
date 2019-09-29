package com.amazurok.fixme.market.handler;

import com.amazurok.fixme.common.Common;
import com.amazurok.fixme.common.Tags;
import com.amazurok.fixme.common.Result;
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
        sendMessage(clientChannel, fixMessage, message, Result.REJECTED);
    }

    protected void executedMessage(AsynchronousSocketChannel clientChannel, String fixMessage, String message) {
        sendMessage(clientChannel, fixMessage, message, Result.EXECUTED);
    }

    public static String resultFixMessage(String message, String id, String srcName, String targetName, Result result) {
        final StringBuilder builder = new StringBuilder();
        addTag(builder, Tags.ID, id);
        addTag(builder, Tags.SRC_NAME, srcName);
        addTag(builder, Tags.DST_NAME, targetName);
        addTag(builder, Tags.RESULT, result.toString());
        addTag(builder, Tags.MESSAGE, message);
        addTag(builder, Tags.CHECKSUM, calculateChecksum(builder.toString()));
        return builder.toString();
    }

    private void sendMessage(AsynchronousSocketChannel clientChannel, String fixMessage, String message, Result result) {
        final String targetName;
        try {
            targetName = Common.getFixValueByTag(fixMessage, Tags.SRC_NAME);
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

