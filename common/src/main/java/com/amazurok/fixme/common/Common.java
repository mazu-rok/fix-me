package com.amazurok.fixme.common;

import com.amazurok.fixme.common.exception.IllegalInputException;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Common {
    private static Logger log = LoggerFactory.getLogger(Common.class);

    public static final String HOST = "127.0.0.1";
    public static final int BROKER_PORT = 5000;
    public static final int MARKET_PORT = 5001;
    public static final int BUFFER_SIZE = 4096;
    public static final String ERROR_MESSAGE = "ERROR_MESSAGE:";

    public static final String VALUES_DELIMITER = "=";
    private static final String FIELDS_DELIMITER = "|";

    public static void sendMessage(AsynchronousSocketChannel channel, String message) {
        log.info(String.format("Send message: %s", message));
        channel.write(ByteBuffer.wrap(message.getBytes()));
    }

    public static String readMessage(AsynchronousSocketChannel channel, ByteBuffer readBuffer)
            throws IllegalInputException, ExecutionException, InterruptedException {
        String message = read(channel.read(readBuffer).get(), readBuffer);
        if (message.startsWith(Common.ERROR_MESSAGE)) {
            throw new IllegalInputException(message);
        }
        return message;
    }

    public static String read(int bytesRead, ByteBuffer readBuffer) {
        if (bytesRead != -1) {
            readBuffer.flip();
            byte[] bytes = new byte[bytesRead];
            readBuffer.get(bytes, 0, bytesRead);
            readBuffer.clear();
            String message = new String(bytes);
            log.info("Read message: {}", message);
            return message;
        }
        return "";
    }

    public static String getChecksum(String message) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(message.getBytes(StandardCharsets.UTF_8));
        return DatatypeConverter.printHexBinary(hash).toLowerCase();
    }

    public static void addFieldToMessage(StringBuilder builder, FIXMessage field, String value) {
        builder.append(field.ordinal())
                .append(VALUES_DELIMITER)
                .append(value)
                .append(FIELDS_DELIMITER);
    }

    public static String getValueFromFIXMesage(String fixMessage, FIXMessage field) throws NotFoundException {
        String regex = String.format("(?<=(%d)%s)(.*?)(?=\\%s)", field.ordinal(), VALUES_DELIMITER, FIELDS_DELIMITER);
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(fixMessage);
        if (matcher.find()) {
            return matcher.group(0);
        }

        throw new NotFoundException(String.format("Field value '%s' not found in message '%s'", field, fixMessage));
    }

    public static void sendErrorMessage(AsynchronousSocketChannel channel, String message) {
        log.error("Send error message: " + message);
        final String internalMessage = ERROR_MESSAGE + message;
        channel.write(ByteBuffer.wrap(internalMessage.getBytes()));
    }
}
