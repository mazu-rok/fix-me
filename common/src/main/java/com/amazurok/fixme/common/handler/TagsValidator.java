package com.amazurok.fixme.common.handler;

import com.amazurok.fixme.common.Common;
import com.amazurok.fixme.common.FixTag;

import java.nio.channels.AsynchronousSocketChannel;

public class TagsValidator extends MessageHandler {
    @Override
    public void handle(AsynchronousSocketChannel clientChannel, String message) {
        try {
            final String sourceId;
            sourceId = Common.getFixValueByTag(message, FixTag.ID);
            Common.getFixValueByTag(message, FixTag.SOURCE_NAME);
            Common.getFixValueByTag(message, FixTag.TARGET_NAME);
            final String checksum = Common.getFixValueByTag(message, FixTag.CHECKSUM);

            Integer.parseInt(sourceId);
            Integer.parseInt(checksum);
            super.handle(clientChannel, message);
        } catch (Exception ex) {
            Common.sendInternalMessage(clientChannel, ex.getMessage());
//        } catch (NumberFormatException ex) {
//            Common.sendInternalMessage(clientChannel, "SOURCE_ID, CHECKSUM Tags should be numbers: " + message);
        }
    }
}
