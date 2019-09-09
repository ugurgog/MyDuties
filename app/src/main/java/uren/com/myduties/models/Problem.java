package uren.com.myduties.models;

public class Problem {
    User whoOpened;
    String problemid;
    long completedTime;
    long createdAt;
    boolean fixed;
    String platform;
    String problemDesc;
    String type;
    String problemPhotoUrl;

    public Problem() {
    }

    public Problem(User whoOpened, String problemid, long completedTime, long createdAt, boolean fixed, String platform, String problemDesc, String type, String problemPhotoUrl) {
        this.whoOpened = whoOpened;
        this.problemid = problemid;
        this.completedTime = completedTime;
        this.createdAt = createdAt;
        this.fixed = fixed;
        this.platform = platform;
        this.problemDesc = problemDesc;
        this.type = type;
        this.problemPhotoUrl = problemPhotoUrl;
    }

    public User getWhoOpened() {
        return whoOpened;
    }

    public void setWhoOpened(User whoOpened) {
        this.whoOpened = whoOpened;
    }

    public String getProblemid() {
        return problemid;
    }

    public void setProblemid(String problemid) {
        this.problemid = problemid;
    }

    public long getCompletedTime() {
        return completedTime;
    }

    public void setCompletedTime(long completedTime) {
        this.completedTime = completedTime;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isFixed() {
        return fixed;
    }

    public void setFixed(boolean fixed) {
        this.fixed = fixed;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getProblemDesc() {
        return problemDesc;
    }

    public void setProblemDesc(String problemDesc) {
        this.problemDesc = problemDesc;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getProblemPhotoUrl() {
        return problemPhotoUrl;
    }

    public void setProblemPhotoUrl(String problemPhotoUrl) {
        this.problemPhotoUrl = problemPhotoUrl;
    }
}
