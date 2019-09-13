package uren.com.myduties.dutyManagement.assignTask.controller;

import android.content.Context;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import uren.com.myduties.R;
import uren.com.myduties.dbManagement.GroupTaskDBHelper;
import uren.com.myduties.dbManagement.UserTaskDBHelper;
import uren.com.myduties.interfaces.OnCompleteCallback;
import uren.com.myduties.messaging.NotificationHandler;
import uren.com.myduties.models.Group;
import uren.com.myduties.models.GroupTask;
import uren.com.myduties.models.Task;
import uren.com.myduties.models.TaskType;
import uren.com.myduties.models.User;
import uren.com.myduties.utils.dataModelUtil.UserDataUtil;

import static uren.com.myduties.constants.StringConstants.fb_child_grouptask;
import static uren.com.myduties.constants.StringConstants.fb_child_usertask;

public class CheckTaskItems {

    Context context;
    User assignedFrom;
    User assignedTo;
    Group group;
    TaskType taskType;
    boolean urgency;
    String taskDesc;
    OnCompleteCallback onCompleteCallback;

    public CheckTaskItems(Context context, User assignedFrom, User assignedTo, Group group, TaskType taskType, boolean urgency, String taskDesc,
                          OnCompleteCallback onCompleteCallback) {
        this.context = context;
        this.assignedFrom = assignedFrom;
        this.assignedTo = assignedTo;
        this.group = group;
        this.taskType = taskType;
        this.urgency = urgency;
        this.taskDesc = taskDesc;
        this.onCompleteCallback = onCompleteCallback;
    }

    public void checkTaskThenAssign() {

        if (assignedTo == null && group == null) {
            onCompleteCallback.OnFailed(context.getResources().getString(R.string.selectUserOrGroupToAssignTask));
            return;
        }

        if (taskDesc == null || taskDesc.trim().isEmpty()) {
            onCompleteCallback.OnFailed(context.getResources().getString(R.string.pleaseEnterTaskDesc));
            return;
        }

        if (assignedTo != null && group == null)
            pushUserTask();
        else if (assignedTo == null && group != null)
            pushGroupTask();
    }

    public void pushUserTask() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(fb_child_usertask).child(assignedTo.getUserid());
        String taskid = databaseReference.push().getKey();

        Task task = new Task();
        task.setTaskId(taskid);
        task.setAssignedFrom(assignedFrom);
        task.setAssignedTo(assignedTo);
        task.setTaskDesc(taskDesc);
        task.setUrgency(urgency);
        task.setClosed(false);
        task.setClosed(false);
        task.setType(taskType.getKey());

        UserTaskDBHelper.addUserTask(task, new OnCompleteCallback() {
            @Override
            public void OnCompleted() {
                NotificationHandler.sendUserNotification(context, assignedFrom, assignedTo,
                        UserDataUtil.getNameOrUsernameFromUser(assignedFrom) + " " + context.getResources().getString(R.string.assignedTaskToYou),
                        taskDesc);
                onCompleteCallback.OnCompleted();
            }

            @Override
            public void OnFailed(String message) {
                onCompleteCallback.OnFailed(message);
            }
        });
    }

    private void pushGroupTask(){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(fb_child_grouptask).child(group.getGroupid());
        String taskid = databaseReference.push().getKey();

        GroupTask groupTask = new GroupTask();
        groupTask.setTaskId(taskid);
        groupTask.setAssignedFrom(assignedFrom);
        groupTask.setTaskDesc(taskDesc);
        groupTask.setUrgency(urgency);
        groupTask.setClosed(false);
        groupTask.setClosed(false);
        groupTask.setType(taskType.getKey());
        groupTask.setGroup(group);

        GroupTaskDBHelper.addGroupTask(groupTask, new OnCompleteCallback() {
            @Override
            public void OnCompleted() {
                NotificationHandler.sendNotificationToGroupParticipants(context, assignedFrom, group,
                        UserDataUtil.getNameOrUsernameFromUser(assignedFrom) + " " + context.getResources().getString(R.string.assignedTaskTo) + " " +
                        groupTask.getGroup().getName(), taskDesc);
                onCompleteCallback.OnCompleted();
            }

            @Override
            public void OnFailed(String message) {
                onCompleteCallback.OnFailed(message);
            }
        });
    }
}
