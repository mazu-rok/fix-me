package com.amazurok.fixme.common;

import com.amazurok.fixme.common.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.DatatypeConverter;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

public class Common {
    private static Logger log = LoggerFactory.getLogger(Common.class);

    public static final String HOST = "127.0.0.1";
    public static final int BROKER_PORT = 5000;
    public static final int MARKET_PORT = 5001;
    public static final int BUFFER_SIZE = 4096;
    public static final String VALUE_DELIMITER = "=";
    public static final String ERROR_MESSAGE = "ERROR_MESSAGE:";
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

    public static String getChecksum(String message) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(message.getBytes(StandardCharsets.UTF_8));
        return DatatypeConverter.printHexBinary(hash).toLowerCase();
    }

    public static void addTag(StringBuilder builder, FIXMessage tag, String value) {
        builder.append(tag.ordinal())
                .append(VALUE_DELIMITER)
                .append(value)
                .append(FIELD_DELIMITER);
    }

    //TODO: Add regex
    public static String getValueFromFIXMesage(String fixMessage, FIXMessage field) throws NotFoundException {
        final String[] tagValues = fixMessage.split(Pattern.quote(FIELD_DELIMITER));
        final String searchPattern = field.ordinal() + VALUE_DELIMITER;
        for (String tagValue : tagValues) {
            if (tagValue.startsWith(searchPattern)) {
                return tagValue.substring(searchPattern.length());
            }
        }
        throw new NotFoundException(String.format("Field value '%s' not found in message '%s'", field, fixMessage));
    }

    public static void sendErrorMessage(AsynchronousSocketChannel channel, String message) {
        log.error("Send error message: " + message);
        final String internalMessage = ERROR_MESSAGE + message;
        channel.write(ByteBuffer.wrap(internalMessage.getBytes()));
    }
}
