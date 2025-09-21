package io.fundoc.quartz.mongodb;

import org.quartz.spi.ClassLoadHelper;

import com.mongodb.client.MongoClient;

import io.fundoc.quartz.mongodb.clojure.DynamicClassLoadHelper;

public class DynamicMongoDBJobStore extends MongoDBJobStore {

    public DynamicMongoDBJobStore() {
        super();
    }

    public DynamicMongoDBJobStore(MongoClient mongo) {
        super(mongo);
    }

    public DynamicMongoDBJobStore(String mongoUri, String username, String password) {
        super(mongoUri, username, password);
    }

    @Override
    protected ClassLoadHelper getClassLoaderHelper(ClassLoadHelper original) {
        return new DynamicClassLoadHelper();
    }
}
