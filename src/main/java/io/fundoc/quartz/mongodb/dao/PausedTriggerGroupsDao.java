package io.fundoc.quartz.mongodb.dao;

import static io.fundoc.quartz.mongodb.util.Keys.KEY_GROUP;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;

public class PausedTriggerGroupsDao {

    private final MongoCollection<Document> triggerGroupsCollection;

    public PausedTriggerGroupsDao(MongoCollection<Document> triggerGroupsCollection) {
        this.triggerGroupsCollection = triggerGroupsCollection;
    }

    public HashSet<String> getPausedGroups() {
        return triggerGroupsCollection.distinct(KEY_GROUP,String.class).into(new HashSet<String>());
    }

    public void pauseGroups(Collection<String> groups) {
        List<Document> list = new ArrayList<Document>();
        for (String s : groups) {
            list.add(new Document(KEY_GROUP, s));
        }
        triggerGroupsCollection.insertMany(list);
    }

    public void remove() {
        triggerGroupsCollection.deleteMany(new Document());
    }

    public void unpauseGroups(Collection<String> groups) {
        triggerGroupsCollection.deleteMany(Filters.in(KEY_GROUP,groups));
    }
}
