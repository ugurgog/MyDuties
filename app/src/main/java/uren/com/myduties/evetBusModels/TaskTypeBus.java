package uren.com.myduties.evetBusModels;

import java.util.HashMap;
import java.util.Map;

import uren.com.myduties.models.User;
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
