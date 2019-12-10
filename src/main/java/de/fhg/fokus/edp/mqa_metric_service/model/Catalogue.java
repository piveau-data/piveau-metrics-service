package de.fhg.fokus.edp.mqa_metric_service.model;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

@DataObject
public class Catalogue implements ListDocument {

    private String id;
    private String name;
    private String title;
    private String description;
    private String spatial;
    private Boolean isDcat;
    private Double rating;

    public Catalogue() {    }

    public Catalogue(JsonObject jsonObject) {
        this.id = jsonObject.getString("id");
        this.name = jsonObject.getString("name");
        this.title = jsonObject.getString("title");
        this.description = jsonObject.getString("description");
        this.spatial = jsonObject.getString("spatial");
        this.isDcat = jsonObject.getBoolean("isDcat");
        this.rating = jsonObject.getDouble("rating");
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSpatial() {
        return spatial;
    }

    public void setSpatial(String spatial) {
        this.spatial = spatial;
    }

    public Boolean getDcat() {
        return isDcat;
    }

    public void setDcat(Boolean dcat) {
        isDcat = dcat;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    @Override
    public String toString() {
        return "Catalogue{" +
            "id='" + id + '\'' +
            ", name='" + name + '\'' +
            ", title='" + title + '\'' +
            ", description='" + description + '\'' +
            ", spatial='" + spatial + '\'' +
            ", isDcat=" + isDcat +
            ", rating=" + rating +
            '}';
    }
}
