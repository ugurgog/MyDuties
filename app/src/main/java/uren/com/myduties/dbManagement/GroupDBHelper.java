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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import uren.com.myduties.interfaces.CompleteCallback;
import uren.com.myduties.interfaces.OnCompleteCallback;
import uren.com.myduties.interfaces.ReturnCallback;
import uren.com.myduties.models.Friend;
import uren.com.myduties.models.Group;
import uren.com.myduties.models.GroupTask;
import uren.com.myduties.models.Phone;
import uren.com.myduties.models.User;

import static uren.com.myduties.constants.StringConstants.fb_child_adminid;
import static uren.com.myduties.constants.StringConstants.fb_child_assignedfromid;
import static uren.com.myduties.constants.StringConstants.fb_child_assignedtime;
import static uren.com.myduties.constants.StringConstants.fb_child_closed;
import static uren.com.myduties.constants.StringConstants.fb_child_completed;
import static uren.com.myduties.constants.StringConstants.fb_child_completedtime;
import static uren.com.myduties.constants.StringConstants.fb_child_countryCode;
import static uren.com.myduties.constants.StringConstants.fb_child_createdat;
import static uren.com.myduties.constants.StringConstants.fb_child_dialCode;
import static uren.com.myduties.constants.StringConstants.fb_child_email;
import static uren.com.myduties.constants.StringConstants.fb_child_groupphotourl;
import static uren.com.myduties.constants.StringConstants.fb_child_groups;
import static uren.com.myduties.constants.StringConstants.fb_child_grouptask;
import static uren.com.myduties.constants.StringConstants.fb_child_members;
import static uren.com.myduties.constants.StringConstants.fb_child_name;
import static uren.com.myduties.constants.StringConstants.fb_child_networks;
import static uren.com.myduties.constants.StringConstants.fb_child_phone;
import static uren.com.myduties.constants.StringConstants.fb_child_phoneNumber;
import static uren.com.myduties.constants.StringConstants.fb_child_profilePhotoUrl;
import static uren.com.myduties.constants.StringConstants.fb_child_taskdesc;
import static uren.com.myduties.constants.StringConstants.fb_child_type;
import static uren.com.myduties.constants.StringConstants.fb_child_urgency;
import static uren.com.myduties.constants.StringConstants.fb_child_username;
import static uren.com.myduties.constants.StringConstants.fb_child_users;
import static uren.com.myduties.constants.StringConstants.fb_child_whocompletedid;

public class GroupDBHelper {

    public static void addOrUpdateGroup(Group group, final OnCompleteCallback onCompleteCallback) {

        if (group == null)
            return;
        if (group.getGroupid() == null || group.getGroupid().isEmpty())
            return;

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(fb_child_groups)
                .child(group.getGroupid());

        final Map<String, Object> values = new HashMap<>();

        if (group.getGroupAdmin() != null)
            values.put(fb_child_adminid, group.getGroupAdmin());

        values.put(fb_child_createdat, group.getCreateAt());

        if (group.getGroupPhotoUrl() != null)
            values.put(fb_child_groupphotourl, group.getGroupPhotoUrl());

        if (group.getName() != null)
            values.put(fb_child_name, group.getName());

        Map<String, Object> membersMap = new HashMap<>();

        if (group.getMemberList() != null) {
            for (User user : group.getMemberList()) {
                membersMap.put(user.getUserid(), " ");
            }
            values.put(fb_child_members, membersMap);
        }

        databaseReference.updateChildren(values).addOnCompleteListener(new OnCompleteListener<Void>() {
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

    public static void getGroup(String groupId, CompleteCallback completeCallback) {

        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference(fb_child_groups).child(groupId);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Map<String, Object> groupMap = (Map) dataSnapshot.getValue();

                if (groupMap != null) {
                    List<User> memberList = new ArrayList<>();

                    String adminId = (String) groupMap.get(fb_child_adminid);
                    long createdAt = (long) groupMap.get(fb_child_createdat);
                    String photoUrl = (String) groupMap.get(fb_child_groupphotourl);
                    String name = (String) groupMap.get(fb_child_name);

                    Map<String, Object> membersMap = (Map) Objects.requireNonNull(groupMap).get(fb_child_members);
                    if (membersMap != null) {
                        for (String memberId : membersMap.keySet()) {
                            memberList.add(new User(memberId));
                        }
                    }
                    Group group = new Group(groupId, name, photoUrl, createdAt, adminId, memberList);
                    completeCallback.onComplete(group);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                completeCallback.onFailed(databaseError.toString());
            }
        });
    }


    public static void getUserGroupsCount(String userid, ReturnCallback returnCallback) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference(fb_child_users).child(userid).child(fb_child_groups);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                int count = 0;

                for (DataSnapshot outboundSnapshot : dataSnapshot.getChildren())
                    count++;

                returnCallback.OnReturn(count);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public static void getUserGroups(User user, CompleteCallback completeCallback) {

        if (user.getGroupIdList() != null && user.getGroupIdList().size() > 0) {
            readUserGroups(user.getGroupIdList(), completeCallback);
        } else
            readFirstUserGroups(user.getUserid(), completeCallback);
    }

    public static void readFirstUserGroups(String userid, CompleteCallback completeCallback) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference(fb_child_users).child(userid).child(fb_child_groups);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                List<String> groupIdList = new ArrayList<>();

                for (DataSnapshot outboundSnapshot : dataSnapshot.getChildren()) {

                    String groupid = outboundSnapshot.getKey();
                    groupIdList.add(groupid);
                }
                readUserGroups(groupIdList, completeCallback);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                completeCallback.onFailed(databaseError.toString());
            }
        });
    }

    public static void readUserGroups(List<String> groupIdList, CompleteCallback completeCallback) {
        for (String groupId : groupIdList) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                    .getReference(fb_child_groups).child(groupId);

            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    Map<String, Object> map = (Map) dataSnapshot.getValue();

                    String adminid = (String) map.get(fb_child_adminid);
                    long createdat = (long) map.get(fb_child_createdat);
                    String groupPhotoUrl = (String) map.get(fb_child_groupphotourl);
                    String name = (String) map.get(fb_child_name);

                    Map<String, Object> members = (Map) Objects.requireNonNull(map).get(fb_child_members);
                    List<User> memberUserList = new ArrayList<>();
                    if (members != null) {
                        for (String userid : members.keySet()) {
                            memberUserList.add(new User(userid));
                        }
                    }
                    Group group = new Group(groupId, name, groupPhotoUrl, createdat, adminid, memberUserList);
                    completeCallback.onComplete(group);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    completeCallback.onFailed(databaseError.getMessage());
                }
            });
        }
    }

    public static void exitUserFromGroup(String userid, String groupid, CompleteCallback completeCallback) {

        FirebaseDatabase.getInstance().getReference(fb_child_users).child(userid).child(fb_child_groups).child(groupid)
                .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                FirebaseDatabase.getInstance().getReference(fb_child_groups).child(groupid).child(fb_child_members).child(userid)
                        .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        completeCallback.onComplete(null);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        completeCallback.onFailed(e.getMessage());
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                completeCallback.onFailed(e.getMessage());
            }
        });
    }

    public static void updateGroupPhoto(String groupid, String photoUrl, OnCompleteCallback onCompleteCallback) {

        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference(fb_child_groups).child(groupid);

        if (photoUrl == null) {
            databaseReference.child(fb_child_groupphotourl).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
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
        } else {
            final Map<String, Object> values = new HashMap<>();
            values.put(fb_child_groupphotourl, photoUrl);

            databaseReference.updateChildren(values).addOnCompleteListener(new OnCompleteListener<Void>() {
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
    }

    public static void changeAdministrator(String userid, String groupid, OnCompleteCallback completeCallback) {

        FirebaseDatabase.getInstance()
                .getReference(fb_child_groups)
                .child(groupid)
                .child(fb_child_adminid).setValue(userid)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        completeCallback.OnCompleted();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                completeCallback.OnFailed(e.getMessage());
            }
        });
    }

    public static void addParticipantsToGroup(String groupid, List<User> participants, OnCompleteCallback onCompleteCallback) {

        for (User user : participants) {
            final Map<String, Object> values = new HashMap<>();
            values.put(groupid, " ");

            FirebaseDatabase.getInstance()
                    .getReference(fb_child_users).child(user.getUserid()).child(fb_child_groups)
                    .updateChildren(values).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    final Map<String, Object> map = new HashMap<>();
                    map.put(user.getUserid(), " ");

                    FirebaseDatabase.getInstance()
                            .getReference(fb_child_groups).child(groupid).child(fb_child_members)
                            .updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
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
    }


}
