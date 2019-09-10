package uren.com.myduties.models;

public class TaskType {
    String key;
    int imgId;
    String desc;

    public TaskType(String key, int imgId, String desc) {
        this.key = key;
        this.imgId = imgId;
        this.desc = desc;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getImgId() {
        return imgId;
    }

    public void setImgId(int imgId) {
        this.imgId = imgId;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
