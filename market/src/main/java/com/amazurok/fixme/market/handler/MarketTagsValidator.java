package com.amazurok.fixme.market.handler;

import com.amazurok.fixme.common.Common;
import com.amazurok.fixme.common.Tags;

import java.nio.channels.AsynchronousSocketChannel;

public class MarketTagsValidator extends MarketMessageHandler {

    public MarketTagsValidator(String id, String name) {
        super(id, name);
    }

    @Override
    public void handle(AsynchronousSocketChannel clientChannel, String message) {
        try {
            Common.getFixValueByTag(message, Tags.PRODUCT);
            final int price = Integer.parseInt(Common.getFixValueByTag(message, Tags.PRICE));
            final int quantity = Integer.parseInt(Common.getFixValueByTag(message, Tags.AMOUNT));
            if (quantity <= 0 || quantity > 10000) { //TODO check max val
                rejectedMessage(clientChannel, message, "Wrong quantity(1-10k)");
                return;
            } else if (price <= 0 || price > 10000) {
                rejectedMessage(clientChannel, message, "Wrong price(1-10k");
                return;
            }

            final String type = Common.getFixValueByTag(message, Tags.ACTION);
            if (MessageType.is(type)) {
                super.handle(clientChannel, message);
            } else {
                rejectedMessage(clientChannel, message, "Wrong operation type");
            }
        } catch (NumberFormatException ex) {
            rejectedMessage(clientChannel, message, "Wrong value type");
        } catch (Exception ex) {
            rejectedMessage(clientChannel, message, "Wrong fix tags");
        }
    }
}

