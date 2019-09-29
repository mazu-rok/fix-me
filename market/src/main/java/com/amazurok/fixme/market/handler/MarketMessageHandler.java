package com.amazurok.fixme.market.handler;

import com.amazurok.fixme.common.Common;
import com.amazurok.fixme.common.FixTag;
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
        sendMessage(clientChannel, fixMessage, message, Result.Rejected);
    }

    protected void executedMessage(AsynchronousSocketChannel clientChannel, String fixMessage, String message) {
        sendMessage(clientChannel, fixMessage, message, Result.Executed);
    }

    public static String resultFixMessage(String message, String id, String srcName, String targetName, Result result) {
        final StringBuilder builder = new StringBuilder();
        addTag(builder, FixTag.ID, id);
        addTag(builder, FixTag.SOURCE_NAME, srcName);
        addTag(builder, FixTag.TARGET_NAME, targetName);
        addTag(builder, FixTag.RESULT, result.toString());
        addTag(builder, FixTag.MESSAGE, message);
        addTag(builder, FixTag.CHECKSUM, calculateChecksum(builder.toString()));
        return builder.toString();
    }

    private void sendMessage(AsynchronousSocketChannel clientChannel, String fixMessage, String message, Result result) {
        final String targetName;
        try {
            targetName = Common.getFixValueByTag(fixMessage, FixTag.SOURCE_NAME);
            Common.sendMessage(clientChannel, resultFixMessage(message, id, name, targetName, result));
//        if (isInsertMessagesToDb()) {
//            Database.insert(
//                    name,
//                    targetName,
//                    Core.getFixValueByTag(fixMessage, FixTag.TYPE),
//                    Core.getFixValueByTag(fixMessage, FixTag.INSTRUMENT),
//                    Core.getFixValueByTag(fixMessage, FixTag.PRICE),
//                    Core.getFixValueByTag(fixMessage, FixTag.QUANTITY),
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

