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

import uren.com.myduties.interfaces.CompleteCallback;
import uren.com.myduties.interfaces.OnCompleteCallback;
import uren.com.myduties.interfaces.ReturnCallback;
import uren.com.myduties.models.Task;
import uren.com.myduties.models.TaskSelectionFilter;
import uren.com.myduties.models.User;

import static uren.com.myduties.constants.StringConstants.ALL_URGENT;
import static uren.com.myduties.constants.StringConstants.NOT_URGENT;
import static uren.com.myduties.constants.StringConstants.URGENT;
import static uren.com.myduties.constants.StringConstants.fb_child_assignedfrom;
import static uren.com.myduties.constants.StringConstants.fb_child_assignedfromid;
import static uren.com.myduties.constants.StringConstants.fb_child_assignedtime;
import static uren.com.myduties.constants.StringConstants.fb_child_closed;
import static uren.com.myduties.constants.StringConstants.fb_child_completed;
import static uren.com.myduties.constants.StringConstants.fb_child_completedtime;
import static uren.com.myduties.constants.StringConstants.fb_child_taskdesc;
import static uren.com.myduties.constants.StringConstants.fb_child_type;
import static uren.com.myduties.constants.StringConstants.fb_child_urgency;
import static uren.com.myduties.constants.StringConstants.fb_child_users;
import static uren.com.myduties.constants.StringConstants.fb_child_usertask;

public class UserTaskDBHelper {

    public static void getUserWaitingTasks(User assignedTo, final CompleteCallback completeCallback) {
        final List<Task> taskList = new ArrayList<>();

        if (assignedTo == null || assignedTo.getUserid() == null) {
            completeCallback.onComplete(null);
            return;
        }

        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference(fb_child_usertask).child(assignedTo.getUserid());

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
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
                        boolean urgency = (boolean) map.get(fb_child_urgency);

                        long completedTime = 0;
                        if (map.get(fb_child_completedtime) != null)
                            completedTime = (long) map.get(fb_child_completedtime);

                        String assignedFromId = (String) map.get(fb_child_assignedfromid);
                        String type = (String) map.get(fb_child_type);

                        User assignedFrom = new User();
                        assignedFrom.setUserid(assignedFromId);

                        Task task = new Task(taskId, taskDesc, assignedFrom, completedVal,
                                assignedTo, assignedTime, completedTime, closedVal, type, urgency);
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

    /*public static void getUserWaitingTasksWithFilterValues(User assignedTo, TaskSelectionFilter taskSelectionFilter,
                                                           final CompleteCallback completeCallback) {
        final List<Task> taskList = new ArrayList<>();

        if (assignedTo == null || assignedTo.getUserid() == null) {
            completeCallback.onComplete(null);
            return;
        }

        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference(fb_child_usertask).child(assignedTo.getUserid());

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
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
                        boolean urgency = (boolean) map.get(fb_child_urgency);
                        String taskType = (String) map.get(fb_child_type);

                        long completedTime = 0;
                        if (map.get(fb_child_completedtime) != null)
                            completedTime = (long) map.get(fb_child_completedtime);

                        String assignedFromId = (String) map.get(fb_child_assignedfromid);
                        User assignedFrom = new User();
                        assignedFrom.setUserid(assignedFromId);

                        if((taskSelectionFilter.getTaskType() == null || (taskSelectionFilter.getTaskType() != null && taskSelectionFilter.getTaskType().equals(taskType))) &&
                                ((taskSelectionFilter.getUrgencyText().equals(URGENT) && urgency) ||
                                        ((taskSelectionFilter.getUrgencyText().equals(NOT_URGENT) && !urgency)) ||
                                        (taskSelectionFilter.getUrgencyText().equals(ALL_URGENT)))) {
                            Task task = new Task(taskId, taskDesc, assignedFrom, completedVal,
                                    assignedTo, assignedTime, completedTime, closedVal, taskType, urgency);
                            taskList.add(task);
                        }
                    }
                }

                completeCallback.onComplete(taskList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                completeCallback.onFailed(databaseError.getMessage());
            }
        });
    }*/

    public static void getUserCompletedTasks(User assignedTo, final CompleteCallback completeCallback) {
        final List<Task> taskList = new ArrayList<>();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference(fb_child_usertask).child(assignedTo.getUserid());

        Query query = databaseReference
                .orderByChild(assignedTo.getUserid() + "/" + fb_child_assignedtime);

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
                        boolean urgency = (boolean) map.get(fb_child_urgency);

                        long completedTime = 0;
                        if (map.get(fb_child_completedtime) != null)
                            completedTime = (long) map.get(fb_child_completedtime);

                        String assignedFromId = (String) map.get(fb_child_assignedfromid);
                        String type = (String) map.get(fb_child_type);

                        User assignedFrom = new User();
                        assignedFrom.setUserid(assignedFromId);

                        Task task = new Task(taskId, taskDesc, assignedFrom, completedVal,
                                assignedTo, assignedTime, completedTime, closedVal, type, urgency);
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

    public static void getUserTaskById(User assignedTo, User assignedFrom, String taskid, final CompleteCallback completeCallback) {

        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference(fb_child_usertask).child(assignedTo.getUserid()).child(taskid);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Map<String, Object> map = (Map) dataSnapshot.getValue();

                boolean completedVal = false;
                try {
                    completedVal = (boolean) map.get(fb_child_completed);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                boolean closedVal = false;
                try {
                    closedVal = (boolean) map.get(fb_child_closed);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                String taskDesc = null;
                try {
                    taskDesc = (String) map.get(fb_child_taskdesc);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                long assignedTime = 0;
                try {
                    assignedTime = (long) map.get(fb_child_assignedtime);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                boolean urgency = false;
                try {
                    urgency = (boolean) map.get(fb_child_urgency);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                long completedTime = 0;
                try {
                    completedTime = (long) map.get(fb_child_completedtime);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                String type = null;
                try {
                    type = (String) map.get(fb_child_type);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Task task = new Task(taskid, taskDesc, assignedFrom, completedVal,
                        assignedTo, assignedTime, completedTime, closedVal, type, urgency);

                completeCallback.onComplete(task);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                completeCallback.onFailed(databaseError.getMessage());
            }
        });
    }

    public static void updateUserTask(Task task, boolean completedOk, final OnCompleteCallback onCompleteCallback) {

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
        values.put(fb_child_type, task.getType());
        values.put(fb_child_urgency, task.isUrgency());

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

    public static void addUserTask(Task userTask, final OnCompleteCallback onCompleteCallback) {

        if (userTask == null) return;
        if (userTask.getTaskId() == null || userTask.getTaskId().isEmpty()) return;

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(fb_child_usertask).
                child(userTask.getAssignedTo().getUserid()).child(userTask.getTaskId());

        final Map<String, Object> values = new HashMap<>();

        if (userTask.getTaskDesc() != null)
            values.put(fb_child_taskdesc, userTask.getTaskDesc());

        if (userTask.getAssignedFrom().getUserid() != null)
            values.put(fb_child_assignedfromid, userTask.getAssignedFrom().getUserid());

        values.put(fb_child_completed, userTask.isCompleted());
        values.put(fb_child_closed, userTask.isClosed());
        values.put(fb_child_type, userTask.getType());
        values.put(fb_child_urgency, userTask.isUrgency());
        values.put(fb_child_assignedtime, ServerValue.TIMESTAMP);

        databaseReference.updateChildren(values).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> task) {

                final Map<String, Object> map = new HashMap<>();
                map.put(userTask.getTaskId(), " ");

                FirebaseDatabase.getInstance().getReference(fb_child_assignedfrom).
                        child(userTask.getAssignedFrom().getUserid())
                        .child(fb_child_users)
                        .child(userTask.getAssignedTo().getUserid())
                        .updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
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
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                onCompleteCallback.OnFailed(e.getMessage());
            }
        });
    }

    public static void getIAssignedTasksToUsersCount(String userid, ReturnCallback returnCallback) {

        if (userid == null || userid.isEmpty()) {
            returnCallback.OnReturn(0);
            return;
        }

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(fb_child_assignedfrom).
                child(userid).child(fb_child_users);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                int count = 0;

                for (DataSnapshot outboundSnapshot : dataSnapshot.getChildren())
                    for (DataSnapshot temp : outboundSnapshot.getChildren())
                        count++;


                returnCallback.OnReturn(count);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public static void getIAssignedTasksToUsers(User assignedFrom, CompleteCallback completeCallback) {

        if (assignedFrom == null || assignedFrom.getUserid() == null || assignedFrom.getUserid().isEmpty()) {
            return;
        }

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(fb_child_assignedfrom).
                child(assignedFrom.getUserid()).child(fb_child_users);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                for (DataSnapshot outboundSnapshot : dataSnapshot.getChildren()) {
                    Log.i("outboundSnapshot:", outboundSnapshot.toString());

                    String assignedToId = outboundSnapshot.getKey();

                    for (DataSnapshot temp : outboundSnapshot.getChildren()) {
                        Log.i("temp:", outboundSnapshot.toString());

                        UserDBHelper.getUser(assignedToId, new CompleteCallback() {
                            @Override
                            public void onComplete(Object object) {
                                getUserTaskById((User) object, assignedFrom, temp.getKey(), completeCallback);
                            }

                            @Override
                            public void onFailed(String message) {

                            }
                        });


                    }
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                completeCallback.onFailed(databaseError.getMessage());
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
