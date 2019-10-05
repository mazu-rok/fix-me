package com.amazurok.fixme.market.handler;

import com.amazurok.fixme.common.Common;
import com.amazurok.fixme.common.FIXMessage;
import com.amazurok.fixme.common.exception.NotFoundException;
import com.amazurok.fixme.market.Market;

import java.nio.channels.AsynchronousSocketChannel;
import java.util.Arrays;
import java.util.Collection;

public class FieldsValidator extends MarketMessageHandler {

    public FieldsValidator(String id, String name) {
        super(id, name);
    }

    @Override
    public void handle(AsynchronousSocketChannel clientChannel, String message) {
        try {
            final String instrument = Common.getValueFromFIXMesage(message, FIXMessage.INSTRUMENT);
            final int price = Integer.parseInt(Common.getValueFromFIXMesage(message, FIXMessage.PRICE));
            final int quantity = Integer.parseInt(Common.getValueFromFIXMesage(message, FIXMessage.QUANTITY));
            final String type = Common.getValueFromFIXMesage(message, FIXMessage.ACTION);
            if (!Arrays.asList(Market.INSTRUMENTS_FOR_TRADING).contains(instrument)) {
                rejectedMessage(clientChannel, message,
                        String.format("Instrument not found, please, select from the list: %s",
                                Arrays.asList(Market.INSTRUMENTS_FOR_TRADING)));
                return;
            }
            if (quantity <= 0 || (quantity > Market.MAX_QUANTITY && MessageType.is(type))) {
                rejectedMessage(clientChannel, message,
                        String.format("Unacceptable quantity, it should be in range(1-%d)", Market.MAX_QUANTITY));
                return;
            }
            if (price <= 0 || price > Market.MAX_PRICE) {
                rejectedMessage(clientChannel, message,
                        String.format("Unacceptable price, it should be in range(1-%d)", Market.MAX_PRICE));
                return;
            }
            if (MessageType.is(type)) {
                super.handle(clientChannel, message);
            } else {
                rejectedMessage(clientChannel, message, "Unacceptable operation type, use BUY or SELL");
            }
        } catch (NotFoundException | NumberFormatException e) {
            rejectedMessage(clientChannel, message, e.getMessage());
        }
    }
}

