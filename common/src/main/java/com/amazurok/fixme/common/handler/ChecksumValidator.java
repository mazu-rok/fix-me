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

    //TODO: Add regex
    private static String getMessageWithoutChecksum(String fixMessage) {
        final int checksumIndex = fixMessage.lastIndexOf(FIXMessage.CHECKSUM.ordinal() + Common.VALUE_DELIMITER);
        return fixMessage.substring(0, checksumIndex);
    }

}
