package com.amazurok.fixme.broker.handler;

import com.amazurok.fixme.common.Common;
import com.amazurok.fixme.common.FIXMessage;
import com.amazurok.fixme.common.ResultMessage;
import com.amazurok.fixme.common.handler.MessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.channels.AsynchronousSocketChannel;

public class ResultValidator extends MessageHandler {
    private static Logger log = LoggerFactory.getLogger(ResultValidator.class);

    @Override
    public void handle(AsynchronousSocketChannel clientChannel, String message) {
        try {
            final String result = Common.getValueFromFIXMesage(message, FIXMessage.RESULT);
            if (ResultMessage.is(result)) {
                super.handle(clientChannel, message);
            } else {
                log.error("Wrong result type in message: {}", message);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
