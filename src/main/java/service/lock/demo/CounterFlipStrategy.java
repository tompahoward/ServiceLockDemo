package service.lock.demo;

import java.util.Map;

import org.ff4j.FF4j;
import org.ff4j.core.Feature;
import org.ff4j.core.FeatureStore;
import org.ff4j.core.FlippingExecutionContext;
import org.ff4j.property.AbstractProperty;
import org.ff4j.property.PropertyInt;
import org.ff4j.strategy.AbstractFlipStrategy;

public class CounterFlipStrategy extends AbstractFlipStrategy {

    private static final String COUNT_KEY = "count";

    private int max;

    @Override
    public boolean evaluate(String featureName, FeatureStore store,
            FlippingExecutionContext executionContext) {
        Integer count = incrementCount(featureName, store);
        assertRequiredParameter("max");
        return count.intValue() >= Integer.parseInt(getInitParams().get("max"));
    }

    Integer incrementCount(String featureName, FeatureStore store) {
        Feature feature = store.read(featureName);

        Map<String, AbstractProperty<?>> properties = feature
                .getCustomProperties();
        PropertyInt requestCount = (PropertyInt) properties.get(COUNT_KEY);
        if (requestCount == null) {
            requestCount = new PropertyInt(COUNT_KEY, "0");
            properties.put(COUNT_KEY, requestCount);
        }
        requestCount.setValue(requestCount.getValue() + 1);
        store.update(feature);
        return requestCount.getValue();
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    static void createCounter(FF4j ff4j, String featureName, int max) {
        if (!ff4j.exist(featureName)) {
            Feature feature = new Feature(featureName, true);
            CounterFlipStrategy flippingStrategy = new CounterFlipStrategy();
            flippingStrategy.getInitParams().put("max", Integer.toString(max));
            feature.setFlippingStrategy(flippingStrategy);
            ff4j.getFeatureStore().create(feature);
        }
    }

}
