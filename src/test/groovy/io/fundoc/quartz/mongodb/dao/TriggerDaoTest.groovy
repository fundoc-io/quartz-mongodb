package io.fundoc.quartz.mongodb.dao

import io.fundoc.quartz.mongodb.MongoHelper
import io.fundoc.quartz.mongodb.trigger.TriggerConverter
import io.fundoc.quartz.mongodb.util.Keys
import io.fundoc.quartz.mongodb.util.QueryHelper
import org.bson.Document
import org.quartz.TriggerKey
import org.quartz.impl.triggers.SimpleTriggerImpl
import spock.lang.Shared
import spock.lang.Specification

import static io.fundoc.quartz.mongodb.Constants.*

class TriggerDaoTest extends Specification {

	@Shared
	triggerKey = new TriggerKey("name", "default")

	def triggerDao = new TriggerDao(MongoHelper.getTriggersColl(),
	new QueryHelper(),
	Mock(TriggerConverter))

	def setup() {
		triggerDao.createIndex()
		MongoHelper.purgeCollections()
	}

	def 'should create index for triggers'() {
		when:
		def indices = MongoHelper.getTriggersColl().listIndexes().into([]).groupBy { it.name }

		then: 'contains default _id index'
		indices.size() == 2 // id and key-group
		indices.containsKey('_id_')

		and: 'has trigger key-group index'
		def idx = indices['keyGroup_1_keyName_1'].first()
		idx['unique'] == true
		idx['key'] == [keyGroup: 1, keyName: 1]
	}

	def "should transfer trigger state if trigger is in specified state"() {
		given:
		insertWaitingTrigger(triggerKey)

		when:
		triggerDao.transferState(triggerKey, STATE_WAITING, STATE_ERROR)

		then:
		triggerDao.getState(triggerKey) == STATE_ERROR
	}

	def "should not transfer state if trigger has different state already"() {
		given:
		insertWaitingTrigger(triggerKey)

		when:
		triggerDao.transferState(triggerKey, STATE_PAUSED, STATE_ERROR)

		then:
		triggerDao.getState(triggerKey) == STATE_WAITING
	}

	def "should create trigger if it not exists"() {
		given:
		def triggerKey = new TriggerKey('test-replace-nonexisting', 'tests')
		def triggerData = createSimpleTriggerData(triggerKey)

		when:
		triggerDao.replace(triggerKey, new Document(triggerData))

		then:
		triggerDao.exists(Keys.toFilter(triggerKey))
	}

	private static Map createSimpleTriggerData(TriggerKey key) {
		return [
			state   : STATE_WAITING,
			keyName : key.name,
			keyGroup: key.group,
			class   : SimpleTriggerImpl.name
		]
	}

	private static insertWaitingTrigger(TriggerKey key) {
		def data = createSimpleTriggerData(key)
		MongoHelper.addTrigger(data)
	}
}

;
