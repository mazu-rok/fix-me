package com.amazurok.fixme.market;

import com.amazurok.fixme.market.entity.Transaction;
import com.mongodb.DuplicateKeyException;
import com.mongodb.MongoClient;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBController {
    private static Logger log = LoggerFactory.getLogger(DBController.class);
    private static String DB_NAME = "fixme";

    private Datastore db = new Morphia().createDatastore(new MongoClient(), DB_NAME);

    public void save(Transaction person) throws DuplicateKeyException {
        db.save(person);
    }

}