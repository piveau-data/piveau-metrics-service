package de.fhg.fokus.edp.mqa_metric_service.model.accessibility;

import de.fhg.fokus.edp.mqa_metric_service.model.ListDocument;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

import java.util.List;

@DataObject
public class DatasetAccessibility implements ListDocument {

    private String id;
    private String name;
    private List<Distribution> distributions;

    public DatasetAccessibility() {
    }

    public DatasetAccessibility(JsonObject jsonObject) {
        this.id = jsonObject.getString("id");
        this.name = jsonObject.getString("name");
        this.distributions = jsonObject.getJsonArray("distributions").getList();
    }

    public JsonObject toJson() {
        return JsonObject.mapFrom(this);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Distribution> getDistributions() {
        return distributions;
    }

    public void setDistributions(List<Distribution> distributions) {
        this.distributions = distributions;
    }
}
