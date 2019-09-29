package com.amazurok.fixme.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
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
    public static final String EMPTY_MESSAGE = "";


    private static final String FIELD_DELIMITER = "|";


    public static Future<Integer> sendMessage(AsynchronousSocketChannel channel, String message) {
        log.info(String.format("Send message: %s", message));
        return channel.write(ByteBuffer.wrap(message.getBytes()));
    }

    public static String readMessage(AsynchronousSocketChannel channel, ByteBuffer readBuffer) {
        try {
            return read(channel.read(readBuffer).get(), readBuffer);
        } catch (InterruptedException | ExecutionException e) {
            return EMPTY_MESSAGE;
        }
    }

    public static String read(int bytesRead, ByteBuffer readBuffer) {
        if (bytesRead != -1) {
            readBuffer.flip();
            byte[] bytes = new byte[bytesRead];
            readBuffer.get(bytes, 0, bytesRead);
            readBuffer.clear();
            String message = new String(bytes);
            System.out.println("Got: " + message);
            return message;
        }
        return EMPTY_MESSAGE;
    }

    public static String calculateChecksum(String message) {
        final byte[] bytes = message.getBytes();
        int sum = 0;
        for (byte aByte : bytes) {
            sum += aByte;
        }
        return String.format("%03d", sum % 256);
    }

    public static void addTag(StringBuilder builder, FixTag tag, String value) {
        builder.append(tag.getValue())
                .append(TAG_VALUE_DELIMITER)
                .append(value)
                .append(FIELD_DELIMITER);
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
