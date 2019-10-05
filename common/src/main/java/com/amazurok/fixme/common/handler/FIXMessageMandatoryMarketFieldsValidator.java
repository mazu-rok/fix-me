package com.amazurok.fixme.common.handler;

import com.amazurok.fixme.common.Common;
import com.amazurok.fixme.common.FIXMessage;
import com.amazurok.fixme.common.exception.NotFoundException;

import java.nio.channels.AsynchronousSocketChannel;

public class FIXMessageMandatoryMarketFieldsValidator extends MessageHandler {
    @Override
    public void handle(AsynchronousSocketChannel clientChannel, String message) {
        try {
            Common.getValueFromFIXMesage(message, FIXMessage.ID);
            Common.getValueFromFIXMesage(message, FIXMessage.BROKER);
            Common.getValueFromFIXMesage(message, FIXMessage.MARKET);
            Common.getValueFromFIXMesage(message, FIXMessage.RESULT);
            Common.getValueFromFIXMesage(message, FIXMessage.MESSAGE);
            Common.getValueFromFIXMesage(message, FIXMessage.CHECKSUM);

            super.handle(clientChannel, message);
        } catch (NotFoundException ex) {
            Common.sendErrorMessage(clientChannel, ex.getMessage());
        }
    }
}
