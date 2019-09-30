package com.amazurok.fixme.broker.handler;

import com.amazurok.fixme.common.Common;
import com.amazurok.fixme.common.FIXMessage;
import com.amazurok.fixme.common.handler.MessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.channels.AsynchronousSocketChannel;

public class ExecutionResult extends MessageHandler {
    private static Logger log = LoggerFactory.getLogger(ExecutionResult.class);

    @Override
    public void handle(AsynchronousSocketChannel clientChannel, String message) {
        try {
            final String result = Common.getFixValueByTag(message, FIXMessage.RESULT);
            final String resultMessage = Common.getFixValueByTag(message, FIXMessage.MESSAGE);

            log.info("Operation result: " + result + " - " + resultMessage);
            super.handle(clientChannel, message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
