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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uren.com.myduties.interfaces.CompleteCallback;
import uren.com.myduties.interfaces.OnCompleteCallback;
import uren.com.myduties.models.Group;
import uren.com.myduties.models.GroupTask;
import uren.com.myduties.models.Task;
import uren.com.myduties.models.User;

import static uren.com.myduties.constants.StringConstants.fb_child_assignedfromid;
import static uren.com.myduties.constants.StringConstants.fb_child_assignedtime;
import static uren.com.myduties.constants.StringConstants.fb_child_closed;
import static uren.com.myduties.constants.StringConstants.fb_child_completed;
import static uren.com.myduties.constants.StringConstants.fb_child_completedtime;
import static uren.com.myduties.constants.StringConstants.fb_child_grouptask;
import static uren.com.myduties.constants.StringConstants.fb_child_taskdesc;
import static uren.com.myduties.constants.StringConstants.fb_child_type;
import static uren.com.myduties.constants.StringConstants.fb_child_urgency;
import static uren.com.myduties.constants.StringConstants.fb_child_usertask;
import static uren.com.myduties.constants.StringConstants.fb_child_whocompletedid;

public class GroupTaskDBHelper {

    public static void getGroupAllTasks(User user,
                                        final CompleteCallback completeCallback) {
        final List<GroupTask> taskList = new ArrayList<>();

        if (user.getGroupIdList() == null || user.getGroupIdList().size() == 0)
            return;

        for (String groupId : user.getGroupIdList()) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                    .getReference(fb_child_grouptask).child(groupId);

            Query query = databaseReference
                    .orderByChild(groupId + "/" + fb_child_assignedtime);

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    for (DataSnapshot outboundSnapshot : dataSnapshot.getChildren()) {

                        String taskId = outboundSnapshot.getKey();

                        Map<String, Object> map = (Map) outboundSnapshot.getValue();

                        boolean closedVal = (boolean) map.get(fb_child_closed);

                        if (closedVal == false) {
                            String taskDesc = (String) map.get(fb_child_taskdesc);
                            long assignedTime = (long) map.get(fb_child_assignedtime);
                            boolean completedVal = (boolean) map.get(fb_child_completed);
                            boolean urgency = (boolean) map.get(fb_child_urgency);
                            String type = (String) map.get(fb_child_type);

                            String whoCompId = (String) map.get(fb_child_whocompletedid);

                            String assignedFromId = (String) map.get(fb_child_assignedfromid);
                            User assignedFrom = new User();
                            assignedFrom.setUserid(assignedFromId);

                            long completedTime = 0;
                            if (map.get(fb_child_completedtime) != null)
                                completedTime = (long) map.get(fb_child_completedtime);

                            GroupTask groupTask = new GroupTask(taskId, taskDesc,
                                    new Group(groupId), assignedFrom, completedVal,
                                    new User(whoCompId), assignedTime, completedTime, false, type, urgency);
                            completeCallback.onComplete(groupTask);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    completeCallback.onFailed(databaseError.getMessage());
                }
            });
        }
    }

    public static void getSingleGroupAllTasks(String groupId, CompleteCallback completeCallback) {
        if (groupId == null) return;

        List<GroupTask> groupTasks = new ArrayList<>();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference(fb_child_grouptask).child(groupId);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot outboundSnapshot : dataSnapshot.getChildren()) {

                    String taskId = outboundSnapshot.getKey();

                    Map<String, Object> map = (Map) outboundSnapshot.getValue();

                    boolean closedVal = (boolean) map.get(fb_child_closed);
                    String taskDesc = (String) map.get(fb_child_taskdesc);
                    long assignedTime = (long) map.get(fb_child_assignedtime);
                    boolean completedVal = (boolean) map.get(fb_child_completed);
                    String whoCompId = (String) map.get(fb_child_whocompletedid);
                    String type = (String) map.get(fb_child_type);
                    boolean urgency = (boolean) map.get(fb_child_urgency);

                    String assignedFromId = (String) map.get(fb_child_assignedfromid);
                    User assignedFrom = new User();
                    assignedFrom.setUserid(assignedFromId);

                    long completedTime = 0;
                    if (map.get(fb_child_completedtime) != null)
                        completedTime = (long) map.get(fb_child_completedtime);

                    GroupTask groupTask = new GroupTask(taskId, taskDesc,
                            new Group(groupId), assignedFrom, completedVal,
                            new User(whoCompId), assignedTime, completedTime, closedVal, type, urgency);
                    groupTasks.add(groupTask);
                }
                completeCallback.onComplete(groupTasks);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                completeCallback.onFailed(databaseError.getMessage());
            }
        });
    }

    public static void getGroupTaskWithId(String taskid, String groupId, CompleteCallback completeCallback) {

        if (taskid == null || groupId == null) return;

        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference(fb_child_grouptask).child(groupId).child(taskid);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //String taskId = dataSnapshot.getKey();

                Map<String, Object> map = (Map) dataSnapshot.getValue();

                boolean closedVal = (boolean) map.get(fb_child_closed);
                String taskDesc = (String) map.get(fb_child_taskdesc);
                long assignedTime = (long) map.get(fb_child_assignedtime);
                boolean completedVal = (boolean) map.get(fb_child_completed);
                String whoCompId = (String) map.get(fb_child_whocompletedid);
                String type = (String) map.get(fb_child_type);
                boolean urgency = (boolean) map.get(fb_child_urgency);

                String assignedFromId = (String) map.get(fb_child_assignedfromid);
                User assignedFrom = new User();
                assignedFrom.setUserid(assignedFromId);

                long completedTime = 0;
                if (map.get(fb_child_completedtime) != null)
                    completedTime = (long) map.get(fb_child_completedtime);

                GroupTask groupTask = new GroupTask(taskid, taskDesc,
                        new Group(groupId), assignedFrom, completedVal,
                        new User(whoCompId), assignedTime, completedTime, closedVal, type, urgency);
                completeCallback.onComplete(groupTask);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                completeCallback.onFailed(databaseError.getMessage());
            }
        });

    }

    public static void getGroupWaitingTasks(User user, int limitValue,
                                            final CompleteCallback completeCallback) {
        final List<GroupTask> taskList = new ArrayList<>();

        if (user.getGroupIdList() == null || user.getGroupIdList().size() == 0)
            completeCallback.onComplete(null);

        for (String groupId : user.getGroupIdList()) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                    .getReference(fb_child_grouptask).child(groupId);

            Query query = databaseReference
                    .orderByChild(groupId + "/" + fb_child_assignedtime).limitToLast(limitValue);

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    for (DataSnapshot outboundSnapshot : dataSnapshot.getChildren()) {

                        String taskId = outboundSnapshot.getKey();

                        Map<String, Object> map = (Map) outboundSnapshot.getValue();

                        boolean completedVal = (boolean) map.get(fb_child_completed);
                        boolean closedVal = (boolean) map.get(fb_child_closed);

                        if (!completedVal && !closedVal) {
                            String taskDesc = (String) map.get(fb_child_taskdesc);
                            long assignedTime = (long) map.get(fb_child_assignedtime);
                            String whoCompId = (String) map.get(fb_child_whocompletedid);
                            String type = (String) map.get(fb_child_type);
                            boolean urgency = (boolean) map.get(fb_child_urgency);

                            String assignedFromId = (String) map.get(fb_child_assignedfromid);
                            User assignedFrom = new User();
                            assignedFrom.setUserid(assignedFromId);

                            long completedTime = 0;
                            if (map.get(fb_child_completedtime) != null)
                                completedTime = (long) map.get(fb_child_completedtime);

                            GroupTask groupTask = new GroupTask(taskId, taskDesc,
                                    new Group(groupId), assignedFrom, completedVal,
                                    new User(whoCompId), assignedTime, completedTime, closedVal, type, urgency);
                            completeCallback.onComplete(groupTask);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    completeCallback.onFailed(databaseError.getMessage());
                }
            });
        }
    }

    public static void getGroupCompletedTasks(User assignedTo, int limitValue,
                                              final CompleteCallback completeCallback) {
        final List<GroupTask> taskList = new ArrayList<>();

        if (assignedTo.getGroupIdList() == null || assignedTo.getGroupIdList().size() == 0)
            completeCallback.onComplete(null);

        for (String groupId : assignedTo.getGroupIdList()) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                    .getReference(fb_child_grouptask).child(groupId);

            Query query = databaseReference
                    .orderByChild(groupId + "/" + fb_child_assignedtime).limitToLast(limitValue);

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    for (DataSnapshot outboundSnapshot : dataSnapshot.getChildren()) {

                        String taskId = outboundSnapshot.getKey();

                        Map<String, Object> map = (Map) outboundSnapshot.getValue();

                        boolean completedVal = (boolean) map.get(fb_child_completed);
                        boolean closedVal = (boolean) map.get(fb_child_closed);

                        if (completedVal && !closedVal) {
                            String taskDesc = (String) map.get(fb_child_taskdesc);
                            long assignedTime = (long) map.get(fb_child_assignedtime);

                            String assignedFromId = (String) map.get(fb_child_assignedfromid);
                            String whoCompletedId = (String) map.get(fb_child_whocompletedid);
                            String type = (String) map.get(fb_child_type);
                            boolean urgency = (boolean) map.get(fb_child_urgency);

                            long completedTime = 0;
                            if (map.get(fb_child_completedtime) != null)
                                completedTime = (long) map.get(fb_child_completedtime);

                            GroupTask groupTask = new GroupTask(taskId, taskDesc,
                                    new Group(groupId), new User(assignedFromId), completedVal,
                                    new User(whoCompletedId), assignedTime, completedTime, closedVal,type, urgency);
                            completeCallback.onComplete(groupTask);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    completeCallback.onFailed(databaseError.getMessage());
                }
            });
        }
    }

    public static void addOrUpdateGroupTask(GroupTask groupTask, boolean completedOk,
                                            final OnCompleteCallback onCompleteCallback) {

        if (groupTask == null) return;
        if (groupTask.getTaskId() == null || groupTask.getTaskId().isEmpty()) return;
        if (groupTask.getGroup() == null || groupTask.getGroup().getGroupid().isEmpty()) return;

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(fb_child_grouptask).
                child(groupTask.getGroup().getGroupid()).child(groupTask.getTaskId());

        final Map<String, Object> values = new HashMap<>();

        if (groupTask.getTaskDesc() != null)
            values.put(fb_child_taskdesc, groupTask.getTaskDesc());
        if (groupTask.getAssignedFrom().getUserid() != null)
            values.put(fb_child_assignedfromid, groupTask.getAssignedFrom().getUserid());
        if (groupTask.getAssignedTime() != 0)
            values.put(fb_child_assignedtime, groupTask.getAssignedTime());

        Log.i("ServerValue.TIMESTAMP:", ServerValue.TIMESTAMP.toString());

        if (completedOk)
            values.put(fb_child_completedtime, ServerValue.TIMESTAMP);

        else if (groupTask.getCompletedTime() != 0)
            values.put(fb_child_completedtime, groupTask.getCompletedTime());

        values.put(fb_child_completed, groupTask.isCompleted());
        values.put(fb_child_closed, groupTask.isClosed());
        values.put(fb_child_whocompletedid, groupTask.getWhoCompleted().getUserid());
        values.put(fb_child_type, groupTask.getType());
        values.put(fb_child_urgency, groupTask.isUrgency());

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

    public static void deleteGroupTask(String userid, String groupid, String taskid, final OnCompleteCallback onCompleteCallback) {

        FirebaseDatabase.getInstance().getReference(fb_child_grouptask).child(groupid).child(taskid)
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
