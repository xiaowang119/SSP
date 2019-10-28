package MyUtil;

public class MyMessage {
    private String id;
    private String unit;
    private String remark;
    private String standard;
    private String detection;

    public MyMessage() {
        this.id = "";
        this.detection = "";
        this.remark = "无";
        this.standard = "";
        this.unit = "";
    }
    public MyMessage(String id) {
        this.id = id;

    }
    public MyMessage(String id, String detection) {
        this.id = id;
        this.detection = detection;
        this.remark = "无";
        this.standard = "";
        this.unit = "";
    }
    public MyMessage(String id, String unit, String standard, String detection) {
        this.id = id;
        this.unit = unit;
        this.remark = "无";
        this.standard = standard;
        this.detection = detection;
    }
    public MyMessage(String id, String unit, String standard, String detection, String remark) {
        this.id = id;
        this.unit = unit;
        this.remark = remark;
        this.standard = standard;
        this.detection = detection;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getStandard() {
        return standard;
    }

    public void setStandard(String standard) {
        this.standard = standard;
    }

    public String getDetection() {
        return detection;
    }

    public void setDetection(String detection) {
        this.detection = detection;
    }

}
