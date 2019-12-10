package de.fhg.fokus.edp.mqa_metric_service.model.compliance;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

@DataObject
public class Violation {

    private String name;
    private String message;

    public Violation() {
    }

    public Violation(JsonObject jsonObject) {
        this.name = jsonObject.getString("name");
        this.message = jsonObject.getString("message");
    }

    public JsonObject toJson() {
        return JsonObject.mapFrom(this);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
