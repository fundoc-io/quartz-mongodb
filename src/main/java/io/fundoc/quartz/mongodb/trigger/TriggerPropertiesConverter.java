package io.fundoc.quartz.mongodb.trigger;

import java.util.Arrays;
import java.util.List;

import org.bson.Document;
import org.quartz.spi.OperableTrigger;

import io.fundoc.quartz.mongodb.trigger.properties.CalendarIntervalTriggerPropertiesConverter;
import io.fundoc.quartz.mongodb.trigger.properties.CronTriggerPropertiesConverter;
import io.fundoc.quartz.mongodb.trigger.properties.DailyTimeIntervalTriggerPropertiesConverter;
import io.fundoc.quartz.mongodb.trigger.properties.SimpleTriggerPropertiesConverter;

/**
 * Converts trigger type specific properties.
 */
public abstract class TriggerPropertiesConverter {
    private static class Holder {
        private static final List<TriggerPropertiesConverter> propertiesConverters = Arrays.asList(
                new SimpleTriggerPropertiesConverter(),new CalendarIntervalTriggerPropertiesConverter(),
                new CronTriggerPropertiesConverter(),new DailyTimeIntervalTriggerPropertiesConverter());
    }

    /**
     * Returns properties converter for given trigger or null when not found.
     *
     * @param trigger
     *            a trigger instance
     * @return converter or null
     */
    public static TriggerPropertiesConverter getConverterFor(OperableTrigger trigger) {
        for (TriggerPropertiesConverter converter : Holder.propertiesConverters) {
            if (converter.canHandle(trigger)) {
                return converter;
            }
        }
        return null;
    }

    protected abstract boolean canHandle(OperableTrigger trigger);

    public abstract Document injectExtraPropertiesForInsert(OperableTrigger trigger,Document original);

    public abstract void setExtraPropertiesAfterInstantiation(OperableTrigger trigger,Document stored);
}
