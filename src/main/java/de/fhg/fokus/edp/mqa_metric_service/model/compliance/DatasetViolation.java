package de.fhg.fokus.edp.mqa_metric_service.model.compliance;

import de.fhg.fokus.edp.mqa_metric_service.model.ListDocument;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

import java.util.List;

@DataObject
public class DatasetViolation implements ListDocument {

    private String id;
    private String name;
    private List<Violation> violations;

    public DatasetViolation() {
    }

    public DatasetViolation(JsonObject jsonObject) {
        this.id = jsonObject.getString("id");
        this.name = jsonObject.getString("name");
        this.violations = jsonObject.getJsonArray("violations").getList();
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

    public List<Violation> getViolations() {
        return violations;
    }

    public void setViolations(List<Violation> violations) {
        this.violations = violations;
    }
}
