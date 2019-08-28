package uren.com.myduties.dbManagement;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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
import uren.com.myduties.models.Group;
import uren.com.myduties.models.Phone;
import uren.com.myduties.models.User;

import static uren.com.myduties.constants.StringConstants.fb_child_adminid;
import static uren.com.myduties.constants.StringConstants.fb_child_countryCode;
import static uren.com.myduties.constants.StringConstants.fb_child_createdat;
import static uren.com.myduties.constants.StringConstants.fb_child_dialCode;
import static uren.com.myduties.constants.StringConstants.fb_child_email;
import static uren.com.myduties.constants.StringConstants.fb_child_groupphotourl;
import static uren.com.myduties.constants.StringConstants.fb_child_groups;
import static uren.com.myduties.constants.StringConstants.fb_child_members;
import static uren.com.myduties.constants.StringConstants.fb_child_name;
import static uren.com.myduties.constants.StringConstants.fb_child_networks;
import static uren.com.myduties.constants.StringConstants.fb_child_phone;
import static uren.com.myduties.constants.StringConstants.fb_child_phoneNumber;
import static uren.com.myduties.constants.StringConstants.fb_child_profilePhotoUrl;
import static uren.com.myduties.constants.StringConstants.fb_child_type;
import static uren.com.myduties.constants.StringConstants.fb_child_username;
import static uren.com.myduties.constants.StringConstants.fb_child_users;

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

        if (group.getType() != null)
            values.put(fb_child_type, group.getType());

        Map<String, Object> membersMap = new HashMap<>();

        if(group.getMemberIdList() != null ){
            for(String userid : group.getMemberIdList()){
                membersMap.put(userid, " ");
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

                if(groupMap != null){
                    List<String> memberList = new ArrayList<>();

                    String adminId = (String) groupMap.get(fb_child_adminid);
                    long createdAt = (long) groupMap.get(fb_child_createdat);
                    String photoUrl = (String) groupMap.get(fb_child_groupphotourl);
                    String name = (String) groupMap.get(fb_child_name);
                    String type = (String) groupMap.get(fb_child_type);

                    Map<String, Object> membersMap = (Map) Objects.requireNonNull(groupMap).get(fb_child_members);
                    if(membersMap != null){
                        for(String memberId: membersMap.keySet()){
                            memberList.add(memberId);
                        }
                    }
                    Group group = new Group(groupId, name, photoUrl, createdAt, adminId, memberList, type);
                    completeCallback.onComplete(group);
                }else
                    completeCallback.onComplete(new Group());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                completeCallback.onFailed(databaseError.toString());
            }
        });
    }
}
