package io.fundoc.quartz.mongodb

import com.mongodb.MongoClientSettings
import com.mongodb.ServerAddress
import com.mongodb.WriteConcern
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import de.flapdoodle.embed.mongo.commands.MongodArguments
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.mongo.transitions.Mongod
import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess
import de.flapdoodle.reverse.TransitionWalker
import de.flapdoodle.reverse.transitions.Start
import org.bson.Document

class MongoHelper {

	static final String testDatabaseName = 'quartz_mongodb_test'
	static int DEFAULT_MONGO_PORT = 27017
	private static final String EMBED_MONGO_PROPERTY = 'embedMongo'

	private static final TransitionWalker.ReachedState<RunningMongodProcess> EMBEDDED_MONGO_STATE = startEmbeddedMongoIfRequested()
	static final MongoClient client = createClient(DEFAULT_MONGO_PORT)
	static final MongoDatabase testDatabase = client.getDatabase(testDatabaseName)

	static final Map<String, MongoCollection<Document>> collections = Collections.unmodifiableMap([
		calendars    : testDatabase.getCollection('quartz_calendars'),
		locks        : testDatabase.getCollection('quartz_locks'),
		jobs         : testDatabase.getCollection('quartz_jobs'),
		jobGroups    : testDatabase.getCollection('quartz_paused_job_groups'),
		schedulers   : testDatabase.getCollection('quartz_schedulers'),
		triggers     : testDatabase.getCollection('quartz_triggers'),
		triggerGroups: testDatabase.getCollection('quartz_paused_trigger_groups')
	])

	private static TransitionWalker.ReachedState<RunningMongodProcess> startEmbeddedMongoIfRequested() {
		if (!Boolean.parseBoolean(System.getProperty(EMBED_MONGO_PROPERTY, 'false'))) {
			return null
		}
		TransitionWalker.ReachedState<RunningMongodProcess> state = null
		try {
			def mongodArguments = MongodArguments.defaults().withUseNoJournal(false)
			def mongod = Mongod.instance().withMongodArguments(
					Start.to(MongodArguments).initializedWith(mongodArguments))
			state = mongod.start(Version.V6_0_5)
			DEFAULT_MONGO_PORT = state.current().getServerAddress().getPort()
			Runtime.getRuntime().addShutdownHook(new Thread({ state.close() }))
			return state
		} catch (Exception ex) {
			if (state != null) {
				try {
					state.close()
				} catch (Exception ignored) {
					// ignore cleanup failure during initialization
				}
			}
			throw new IllegalStateException('Failed to start embedded MongoDB', ex)
		}
	}

	private static MongoClient createClient(int port) {
		MongoClients.create(
				MongoClientSettings.builder()
				.writeConcern(WriteConcern.JOURNALED)
				.applyToClusterSettings { builder ->
					builder.hosts([
						new ServerAddress('localhost', port)
					])
				}
				.build())
	}

	static def dropTestDB() {
		testDatabase.drop()
	}

	static def clearColl(String colKey) {
		collections[colKey].deleteMany(new Document())
	}

	static def purgeCollections() {
		//Remove all data from Quartz MongoDB collections.
		clearColl('triggers')
		clearColl('jobs')
		clearColl('locks')
		clearColl('calendars')
		clearColl('schedulers')
		clearColl('triggerGroups')
		clearColl('jobGroups')
	}

	/**
	 * Adds a new scheduler entry created from given map.
	 */
	static def addScheduler(Map dataMap) {
		collections['schedulers'].insertOne(new Document(dataMap))
	}

	/**
	 * Adds a new Job entry created from given map.
	 */
	static def addJob(Map dataMap) {
		collections['jobs'].insertOne(new Document(dataMap))
	}

	/**
	 * Adds a new Lock entry created from given map.
	 */
	static def addLock(Map dataMap) {
		collections['locks'].insertOne(new Document(dataMap))
	}

	/**
	 * Adds a new Trigger entry created from given map.
	 */
	static def addTrigger(Map dataMap) {
		collections['triggers'].insertOne(new Document(dataMap))
	}

	/**
	 * Return number of elements in a collection.
	 */
	static def getCount(String col) {
		collections[col].countDocuments()
	}

	/**
	 * Return calendars collection as MongoCollection.
	 */
	static def getCalendarsColl() {
		collections['calendars']
	}

	/**
	 * Return locks collection as MongoCollection.
	 */
	static def getLocksColl() {
		collections['locks']
	}

	/**
	 * Return schedulers collection as MongoCollection.
	 */
	static def getSchedulersColl() {
		collections['schedulers']
	}

	static def getTriggersColl() {
		collections['triggers']
	}

	/**
	 * Return the first document from given collection.
	 */
	static def Document getFirst(String col) {
		getFirst(col, [:])
	}

	static def Document getFirst(String col, Map amap) {
		collections[col].find(new Document(amap)).first()
	}

	/**
	 * Return all documents from given collection.
	 */
	static def Collection<Document> findAll(String col) {
		collections[col].find(new Document()).into([])
	}
}
