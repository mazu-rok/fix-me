package com.amazurok.fixme.broker.handler;

import com.amazurok.fixme.common.Common;
import com.amazurok.fixme.common.FIXMessage;
import com.amazurok.fixme.common.ResultMessage;
import com.amazurok.fixme.common.handler.MessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.channels.AsynchronousSocketChannel;

public class ResultTagValidator extends MessageHandler {
    private static Logger log = LoggerFactory.getLogger(ResultTagValidator.class);

    @Override
    public void handle(AsynchronousSocketChannel clientChannel, String message) {
        final String result;
        try {
            result = Common.getFixValueByTag(message, FIXMessage.RESULT);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return;
        }
        if (ResultMessage.is(result)) {
            super.handle(clientChannel, message);
        } else {
            System.out.println("Wrong result type in message: " + message);
        }
    }
}
