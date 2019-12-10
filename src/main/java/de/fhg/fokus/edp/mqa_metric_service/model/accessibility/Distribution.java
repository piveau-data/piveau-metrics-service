package de.fhg.fokus.edp.mqa_metric_service.model.accessibility;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

@DataObject
public class Distribution {

    private String id;
    private String format;
    private String metaDataMediaType;
    private String checkedMediaType;
    private String accessUrl;
    private Integer statusAccessUrl;
    private String downloadUrl;
    private Integer statusDownloadUrl;

    public Distribution() {
    }

    public Distribution(JsonObject jsonObject) {
        this.id = jsonObject.getString("id");
        this.format = jsonObject.getString("format");
        this.metaDataMediaType = jsonObject.getString("metaDataMediaType");
        this.checkedMediaType = jsonObject.getString("checkedMediaType");
        this.accessUrl = jsonObject.getString("accessUrl");
        this.statusAccessUrl = jsonObject.getInteger("statusAccessUrl");
        this.downloadUrl = jsonObject.getString("downloadUrl");
        this.statusDownloadUrl = jsonObject.getInteger("statusDownloadUrl");
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

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getMetaDataMediaType() {
        return metaDataMediaType;
    }

    public void setMetaDataMediaType(String metaDataMediaType) {
        this.metaDataMediaType = metaDataMediaType;
    }

    public String getCheckedMediaType() {
        return checkedMediaType;
    }

    public void setCheckedMediaType(String checkedMediaType) {
        this.checkedMediaType = checkedMediaType;
    }

    public String getAccessUrl() {
        return accessUrl;
    }

    public void setAccessUrl(String accessUrl) {
        this.accessUrl = accessUrl;
    }

    public Integer getStatusAccessUrl() {
        return statusAccessUrl;
    }

    public void setStatusAccessUrl(Integer statusAccessUrl) {
        this.statusAccessUrl = statusAccessUrl;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public Integer getStatusDownloadUrl() {
        return statusDownloadUrl;
    }

    public void setStatusDownloadUrl(Integer statusDownloadUrl) {
        this.statusDownloadUrl = statusDownloadUrl;
    }
}
