package uren.com.myduties.dbManagement;


import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import uren.com.myduties.interfaces.CompleteCallback;
import uren.com.myduties.interfaces.OnCompleteCallback;
import uren.com.myduties.models.Task;
import uren.com.myduties.models.User;

import static uren.com.myduties.constants.StringConstants.fb_child_assignedfromid;
import static uren.com.myduties.constants.StringConstants.fb_child_assignedtime;
import static uren.com.myduties.constants.StringConstants.fb_child_closed;
import static uren.com.myduties.constants.StringConstants.fb_child_completed;
import static uren.com.myduties.constants.StringConstants.fb_child_completedtime;
import static uren.com.myduties.constants.StringConstants.fb_child_email;
import static uren.com.myduties.constants.StringConstants.fb_child_taskdesc;
import static uren.com.myduties.constants.StringConstants.fb_child_users;
import static uren.com.myduties.constants.StringConstants.fb_child_usertask;

public class UserTaskDBHelper {

    public static void getUserWaitingTasks(User assignedTo, int limitValue,
                                           final CompleteCallback completeCallback) {
        final List<Task> taskList = new ArrayList<>();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference(fb_child_usertask).child(assignedTo.getUserid());

        Query query = databaseReference
                .orderByChild(assignedTo.getUserid() + "/" + fb_child_assignedtime).limitToLast(limitValue);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot outboundSnapshot : dataSnapshot.getChildren()) {

                    String taskId = outboundSnapshot.getKey();

                    Map<String, Object> map = (Map) outboundSnapshot.getValue();

                    boolean completedVal = (boolean) map.get(fb_child_completed);

                    if (completedVal == false) {
                        String taskDesc = (String) map.get(fb_child_taskdesc);
                        long assignedTime = (long) map.get(fb_child_assignedtime);
                        boolean closedVal = (boolean) map.get(fb_child_closed);

                        long completedTime = 0;
                        if (map.get(fb_child_completedtime) != null)
                            completedTime = (long) map.get(fb_child_completedtime);

                        String assignedFromId = (String) map.get(fb_child_assignedfromid);

                        User assignedFrom = new User();
                        assignedFrom.setUserid(assignedFromId);

                        Task task = new Task(taskId, taskDesc, assignedFrom, completedVal,
                                assignedTo, assignedTime, completedTime, closedVal);
                        taskList.add(task);
                    }
                }

                completeCallback.onComplete(taskList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                completeCallback.onFailed(databaseError.getMessage());
            }
        });
    }

    public static void getUserCompletedTasks(User assignedTo, int limitValue,
                                             final CompleteCallback completeCallback) {
        final List<Task> taskList = new ArrayList<>();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference(fb_child_usertask).child(assignedTo.getUserid());

        Query query = databaseReference
                .orderByChild(assignedTo.getUserid() + "/" + fb_child_assignedtime).limitToLast(limitValue);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot outboundSnapshot : dataSnapshot.getChildren()) {

                    String taskId = outboundSnapshot.getKey();

                    Map<String, Object> map = (Map) outboundSnapshot.getValue();

                    boolean completedVal = (boolean) map.get(fb_child_completed);
                    boolean closedVal = (boolean) map.get(fb_child_closed);

                    if (completedVal == true && closedVal == false) {
                        String taskDesc = (String) map.get(fb_child_taskdesc);
                        long assignedTime = (long) map.get(fb_child_assignedtime);

                        long completedTime = 0;
                        if (map.get(fb_child_completedtime) != null)
                            completedTime = (long) map.get(fb_child_completedtime);

                        String assignedFromId = (String) map.get(fb_child_assignedfromid);

                        User assignedFrom = new User();
                        assignedFrom.setUserid(assignedFromId);

                        Task task = new Task(taskId, taskDesc, assignedFrom, completedVal,
                                assignedTo, assignedTime, completedTime, closedVal);
                        taskList.add(task);
                    }
                }

                completeCallback.onComplete(taskList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                completeCallback.onFailed(databaseError.getMessage());
            }
        });
    }

    public static void addOrUpdateUserTask(Task task, boolean completedOk, final OnCompleteCallback onCompleteCallback) {

        if (task == null) return;
        if (task.getTaskId() == null || task.getTaskId().isEmpty()) return;

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(fb_child_usertask).
                child(task.getAssignedTo().getUserid()).child(task.getTaskId());

        final Map<String, Object> values = new HashMap<>();

        if (task.getTaskDesc() != null)
            values.put(fb_child_taskdesc, task.getTaskDesc());
        if (task.getAssignedTime() != 0)
            values.put(fb_child_assignedtime, task.getAssignedTime());
        if (task.getAssignedFrom().getUserid() != null)
            values.put(fb_child_assignedfromid, task.getAssignedFrom().getUserid());

        if (completedOk)
            values.put(fb_child_completedtime, ServerValue.TIMESTAMP);
        else if (task.getCompletedTime() != 0)
            values.put(fb_child_completedtime, task.getCompletedTime());

        values.put(fb_child_completed, task.isCompleted());
        values.put(fb_child_closed, task.isClosed());

        databaseReference.updateChildren(values).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> task) {
                onCompleteCallback.OnCompleted();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                onCompleteCallback.OnFailed(e.getMessage());
            }
        });
    }

    public static void deleteUserTask(String userid, String taskid, final OnCompleteCallback onCompleteCallback) {

        FirebaseDatabase.getInstance().getReference(fb_child_usertask).child(userid).child(taskid)
                .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                onCompleteCallback.OnCompleted();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                onCompleteCallback.OnFailed(e.getMessage());
            }
        });
    }
}
