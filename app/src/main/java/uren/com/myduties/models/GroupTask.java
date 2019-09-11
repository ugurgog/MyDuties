package uren.com.myduties.models;


public class GroupTask {
    String taskId;
    String taskDesc;
    Group group;
    User assignedFrom;
    boolean completed;
    boolean closed;
    boolean urgency;
    User whoCompleted;
    long assignedTime;
    long completedTime;
    String type;

    public GroupTask() {
    }

    public GroupTask(String taskId, String taskDesc, Group group, User assignedFrom, boolean completed, User whoCompleted,
                     long assignedTime, long completedTime, boolean closed, String type, boolean urgency) {
        this.taskId = taskId;
        this.taskDesc = taskDesc;
        this.group = group;
        this.assignedFrom = assignedFrom;
        this.completed = completed;
        this.whoCompleted = whoCompleted;
        this.assignedTime = assignedTime;
        this.completedTime = completedTime;
        this.closed = closed;
        this.type = type;
        this.urgency = urgency;
    }

    public GroupTask(Group group) {
        this.group = group;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getTaskDesc() {
        return taskDesc;
    }

    public void setTaskDesc(String taskDesc) {
        this.taskDesc = taskDesc;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public User getAssignedFrom() {
        return assignedFrom;
    }

    public void setAssignedFrom(User assignedFrom) {
        this.assignedFrom = assignedFrom;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public User getWhoCompleted() {
        return whoCompleted;
    }

    public void setWhoCompleted(User whoCompleted) {
        this.whoCompleted = whoCompleted;
    }

    public long getAssignedTime() {
        return assignedTime;
    }

    public void setAssignedTime(long assignedTime) {
        this.assignedTime = assignedTime;
    }

    public long getCompletedTime() {
        return completedTime;
    }

    public void setCompletedTime(long completedTime) {
        this.completedTime = completedTime;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isUrgency() {
        return urgency;
    }

    public void setUrgency(boolean urgency) {
        this.urgency = urgency;
    }
}
