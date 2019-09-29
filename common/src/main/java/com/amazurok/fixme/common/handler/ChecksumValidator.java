package com.amazurok.fixme.common.handler;

import com.amazurok.fixme.common.Common;
import com.amazurok.fixme.common.Tags;

import java.nio.channels.AsynchronousSocketChannel;

public class ChecksumValidator extends MessageHandler{

    @Override
    public void handle(AsynchronousSocketChannel clientChannel, String message) {
        try {
            final String calculatedChecksum = Common.calculateChecksum(getMessageWithoutChecksum(message));
            final String messageChecksum;
            messageChecksum = Common.getFixValueByTag(message, Tags.CHECKSUM);
            final boolean isValidChecksum = calculatedChecksum.equals(messageChecksum);
            if (isValidChecksum) {
               super.handle(clientChannel, message);
            } else {
                Common.sendInternalMessage(clientChannel, "Invalid checksum for message: " + message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getMessageWithoutChecksum(String fixMessage) {
        final int checksumIndex = fixMessage.lastIndexOf(Tags.CHECKSUM.ordinal() + Common.TAG_VALUE_DELIMITER);
        return fixMessage.substring(0, checksumIndex);
    }

}
