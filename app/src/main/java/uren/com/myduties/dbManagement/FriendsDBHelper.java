package uren.com.myduties.dbManagement;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uren.com.myduties.interfaces.CompleteCallback;
import uren.com.myduties.interfaces.OnCompleteCallback;
import uren.com.myduties.models.Friend;
import uren.com.myduties.models.User;

import static uren.com.myduties.constants.StringConstants.fb_child_friends;
import static uren.com.myduties.constants.StringConstants.fb_child_status_all;
import static uren.com.myduties.constants.StringConstants.fb_child_status_friend;
import static uren.com.myduties.constants.StringConstants.fb_child_status_sendedrequest;
import static uren.com.myduties.constants.StringConstants.fb_child_status_waiting;

public class FriendsDBHelper {

    public static void getFriendsByStatus(String userid, String statusVal, CompleteCallback completeCallback){

        if(userid == null || userid.isEmpty()){
            completeCallback.onFailed("Kullanıcı id boş olamaz");
            return;
        }

        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference(fb_child_friends).child(userid);

        //Query query = databaseReference.limitToFirst(limit);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot outboundSnapshot : dataSnapshot.getChildren()) {

                    String friendUserid = outboundSnapshot.getKey();
                    String status = (String) outboundSnapshot.getValue();

                    if(statusVal.equals(fb_child_status_all)) {
                        UserDBHelper.getUser(friendUserid, new CompleteCallback() {
                            @Override
                            public void onComplete(Object object) {
                                User user = (User) object;
                                completeCallback.onComplete(new Friend(user, status));
                            }

                            @Override
                            public void onFailed(String message) {
                                completeCallback.onFailed(message);
                            }
                        });
                    }else {
                        assert status != null;
                        if(status.equals(statusVal)){
                            UserDBHelper.getUser(friendUserid, new CompleteCallback() {
                                @Override
                                public void onComplete(Object object) {
                                    User user = (User) object;
                                    completeCallback.onComplete(new Friend(user, status));
                                }

                                @Override
                                public void onFailed(String message) {
                                    completeCallback.onFailed(message);
                                }
                            });
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                completeCallback.onFailed(databaseError.toString());
            }
        });
    }

    public static void getAllFriendsByStatus(String userid, String statusVal, CompleteCallback completeCallback){

        if(userid == null || userid.isEmpty()){
            completeCallback.onFailed("Kullanıcı id boş olamaz");
            return;
        }

        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference(fb_child_friends).child(userid);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot outboundSnapshot : dataSnapshot.getChildren()) {

                    String friendUserid = outboundSnapshot.getKey();
                    String status = (String) outboundSnapshot.getValue();

                    if(statusVal.equals(fb_child_status_all)) {
                        UserDBHelper.getUser(friendUserid, new CompleteCallback() {
                            @Override
                            public void onComplete(Object object) {
                                User user = (User) object;
                                completeCallback.onComplete(new Friend(user, status));
                            }

                            @Override
                            public void onFailed(String message) {
                                completeCallback.onFailed(message);
                            }
                        });
                    }else {
                        assert status != null;
                        if(status.equals(statusVal)){
                            UserDBHelper.getUser(friendUserid, new CompleteCallback() {
                                @Override
                                public void onComplete(Object object) {
                                    User user = (User) object;
                                    completeCallback.onComplete(new Friend(user, status));
                                }

                                @Override
                                public void onFailed(String message) {
                                    completeCallback.onFailed(message);
                                }
                            });
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                completeCallback.onFailed(databaseError.toString());
            }
        });
    }

    public static void getFriendsByStatusList(String userid, List<String> statusList, CompleteCallback completeCallback){

        if(userid == null || userid.isEmpty()){
            completeCallback.onFailed("Kullanıcı id boş olamaz");
            return;
        }

        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference(fb_child_friends).child(userid);

        //Query query = databaseReference.limitToFirst(limit);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot outboundSnapshot : dataSnapshot.getChildren()) {

                    String friendUserid = outboundSnapshot.getKey();
                    String status = (String) outboundSnapshot.getValue();

                    if(statusList.contains(status)){
                        UserDBHelper.getUser(friendUserid, new CompleteCallback() {
                            @Override
                            public void onComplete(Object object) {
                                User user = (User) object;
                                completeCallback.onComplete(new Friend(user, status));
                            }

                            @Override
                            public void onFailed(String message) {
                                completeCallback.onFailed(message);
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                completeCallback.onFailed(databaseError.toString());
            }
        });
    }

    public static void getFriendCountByStatus(String userid, String statusVal, CompleteCallback completeCallback){

        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference(fb_child_friends).child(userid);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                int count = 0;

                for (DataSnapshot outboundSnapshot : dataSnapshot.getChildren()) {

                    String friendUserid = outboundSnapshot.getKey();
                    String status = (String) outboundSnapshot.getValue();

                    if(statusVal.equals(status))
                        count ++;
                }
                completeCallback.onComplete(count);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                completeCallback.onFailed(databaseError.toString());
            }
        });
    }

    public static void acceptFriendRequest(String whoAcceptedId, String friendUserid, OnCompleteCallback onCompleteCallback){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference(fb_child_friends).child(whoAcceptedId);

        final Map<String, Object> values = new HashMap<>();
        values.put(friendUserid, fb_child_status_friend);

        databaseReference.updateChildren(values).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                DatabaseReference reference = FirebaseDatabase.getInstance()
                        .getReference(fb_child_friends).child(friendUserid);

                final Map<String, Object> values = new HashMap<>();
                values.put(whoAcceptedId, fb_child_status_friend);

                reference.updateChildren(values).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
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

    public static void removeFriend(String userid, String friendUserid, OnCompleteCallback onCompleteCallback){

        FirebaseDatabase.getInstance().getReference(fb_child_friends).child(userid).child(friendUserid)
                .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                FirebaseDatabase.getInstance().getReference(fb_child_friends).child(friendUserid).child(userid)
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
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                onCompleteCallback.OnFailed(e.getMessage());
            }
        });
    }

    public static void sendFriendRequest(String whoSendedId, String whoReceiveId, OnCompleteCallback onCompleteCallback){

        final Map<String, Object> values = new HashMap<>();
        values.put(whoReceiveId, fb_child_status_sendedrequest);

        FirebaseDatabase.getInstance().getReference(fb_child_friends).child(whoSendedId).updateChildren(values)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                final Map<String, Object> temp = new HashMap<>();
                temp.put(whoSendedId, fb_child_status_waiting);

                FirebaseDatabase.getInstance().getReference(fb_child_friends).child(whoReceiveId).updateChildren(temp)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
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
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                onCompleteCallback.OnFailed(e.getMessage());
            }
        });
    }

    public static void getFriendStatus(String whoDemands, String friendUserId, CompleteCallback completeCallback){

        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference(fb_child_friends).child(whoDemands).child(friendUserId);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                try {
                    String status = (String) dataSnapshot.getValue();
                    completeCallback.onComplete(status);
                } catch (Exception e) {
                    completeCallback.onComplete(null);
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                completeCallback.onFailed(databaseError.toString());
            }
        });
    }
}
