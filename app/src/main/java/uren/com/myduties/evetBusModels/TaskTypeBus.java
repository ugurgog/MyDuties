package uren.com.myduties.evetBusModels;

import uren.com.myduties.utils.TaskTypeHelper;

public class TaskTypeBus {

    TaskTypeHelper taskTypeHelper;

    public TaskTypeBus(TaskTypeHelper taskTypeHelper) {
        this.taskTypeHelper = taskTypeHelper;
    }

    public TaskTypeHelper getTypeMap() {
        return taskTypeHelper;
    }
}
