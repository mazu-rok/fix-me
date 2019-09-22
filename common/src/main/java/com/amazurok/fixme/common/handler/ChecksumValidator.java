package com.amazurok.fixme.common.handler;

import com.amazurok.fixme.common.Common;
import com.amazurok.fixme.common.FixTag;

import java.nio.channels.AsynchronousSocketChannel;

public class ChecksumValidator extends MessageHandler{

    @Override
    public void handle(AsynchronousSocketChannel clientChannel, String message) {
        try {
            final String calculatedChecksum = calculateChecksum(getMessageWithoutChecksum(message));
            final String messageChecksum;
            messageChecksum = Common.getFixValueByTag(message, FixTag.CHECKSUM);
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

    private static String calculateChecksum(String message) {
        final byte[] bytes = message.getBytes();
        int sum = 0;
        for (byte aByte : bytes) {
            sum += aByte;
        }
        return String.format("%03d", sum % 256);
    }

    private static String getMessageWithoutChecksum(String fixMessage) {
        final int checksumIndex = fixMessage.lastIndexOf(FixTag.CHECKSUM.getValue() + Common.TAG_VALUE_DELIMITER);
        return fixMessage.substring(0, checksumIndex);
    }

}