package de.fhg.fokus.edp.mqa_metric_service.metric;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

import java.util.Map;

@DataObject
public class Metric {

    private Map<String, Double> values;

    public Metric(JsonObject jsonObject) {
        Metric metric = jsonObject.mapTo(this.getClass());
        this.values = metric.getValues();
    }

    public Metric(Map<String, Double> values) {
        this.values = values;
    }

    public Map<String, Double> getValues() {
        return values;
    }

    public void setValues(Map<String, Double> values) {
        this.values = values;
    }

    public JsonObject toJson() {
        return new JsonObject(Json.encode(this));
    }
}
