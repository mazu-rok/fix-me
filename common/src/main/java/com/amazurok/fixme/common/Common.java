package com.amazurok.fixme.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

public class Common {
    private static Logger log = LoggerFactory.getLogger(Common.class);
    public static final String HOST = "127.0.0.1";
    public static final int BROKER_PORT = 5000;
    public static final int MARKET_PORT = 5001;
    public static final int BUFFER_SIZE = 4096;
    public static final String TAG_VALUE_DELIMITER = "=";
    public static final String INTERNAL_MESSAGE = "INTERNAL_MESSAGE:";

    private static final String USER_INPUT_DELIMITER = " ";
    private static final String FIELD_DELIMITER = "|";


    public static Future<Integer> sendMessage(AsynchronousSocketChannel channel, String message) {
        log.error(String.format("Send message: %s", message));
        return channel.write(ByteBuffer.wrap(message.getBytes()));
    }

    public static String getFixValueByTag(String fixMessage, FixTag tag) throws Exception {
        final String[] tagValues = fixMessage.split(Pattern.quote(FIELD_DELIMITER));
        final String searchPattern = tag.getValue() + TAG_VALUE_DELIMITER;
        for (String tagValue : tagValues) {
            if (tagValue.startsWith(searchPattern)) {
                return tagValue.substring(searchPattern.length());
            }
        }
        throw new Exception("No '" + tag + "' tag in message + '" + fixMessage + "'");
    }

    public static Future<Integer> sendInternalMessage(AsynchronousSocketChannel channel, String message) {
        log.info("Send internal: " + message);
        final String internalMessage = INTERNAL_MESSAGE + message;
        return channel.write(ByteBuffer.wrap(internalMessage.getBytes()));
    }
}
