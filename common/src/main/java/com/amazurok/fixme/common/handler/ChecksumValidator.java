package com.amazurok.fixme.common.handler;

import com.amazurok.fixme.common.Common;
import com.amazurok.fixme.common.FIXMessage;
import com.amazurok.fixme.common.exception.NotFoundException;

import java.nio.channels.AsynchronousSocketChannel;
import java.security.NoSuchAlgorithmException;

public class ChecksumValidator extends MessageHandler{

    @Override
    public void handle(AsynchronousSocketChannel clientChannel, String message) {
        try {
            final String calculatedChecksum = Common.getChecksum(getMessageWithoutChecksum(message));
            final String messageChecksum = Common.getValueFromFIXMesage(message, FIXMessage.CHECKSUM);
            if (calculatedChecksum.equals(messageChecksum)) {
               super.handle(clientChannel, message);
            } else {
                Common.sendErrorMessage(clientChannel, "Invalid checksum for message: " + message);
            }
        } catch (NoSuchAlgorithmException | NotFoundException e) {
            Common.sendErrorMessage(clientChannel, e.getMessage());
        }
    }

    private static String getMessageWithoutChecksum(String fixMessage) {
        String regex = String.format("(%d)%s.*$", FIXMessage.CHECKSUM.ordinal(), Common.VALUES_DELIMITER);
        return fixMessage.replaceAll(regex, "");
    }

}
