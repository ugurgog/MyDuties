package uren.com.myduties.models;

public class TaskSelectionFilter {

    private String urgencyText;
    private String taskType;

    public TaskSelectionFilter() {
    }

    public TaskSelectionFilter(String urgencyText, String taskType) {
        this.urgencyText = urgencyText;
        this.taskType = taskType;
    }

    public String getUrgencyText() {
        return urgencyText;
    }

    public void setUrgencyText(String urgencyText) {
        this.urgencyText = urgencyText;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }
}
