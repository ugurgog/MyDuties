package uren.com.myduties.dbManagement;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import uren.com.myduties.interfaces.CompleteCallback;
import uren.com.myduties.interfaces.OnCompleteCallback;
import uren.com.myduties.models.Phone;
import uren.com.myduties.models.User;

import static uren.com.myduties.constants.StringConstants.fb_child_countryCode;
import static uren.com.myduties.constants.StringConstants.fb_child_dialCode;
import static uren.com.myduties.constants.StringConstants.fb_child_email;
import static uren.com.myduties.constants.StringConstants.fb_child_groups;
import static uren.com.myduties.constants.StringConstants.fb_child_name;
import static uren.com.myduties.constants.StringConstants.fb_child_networks;
import static uren.com.myduties.constants.StringConstants.fb_child_phone;
import static uren.com.myduties.constants.StringConstants.fb_child_phoneNumber;
import static uren.com.myduties.constants.StringConstants.fb_child_profilePhotoUrl;
import static uren.com.myduties.constants.StringConstants.fb_child_taskdesc;
import static uren.com.myduties.constants.StringConstants.fb_child_username;
import static uren.com.myduties.constants.StringConstants.fb_child_users;

public class UserDBHelper {

    public static void addOrUpdateUser(User user, final OnCompleteCallback onCompleteCallback) {

        if (user == null)
            return;
        if (user.getUserid() == null || user.getUserid().isEmpty())
            return;

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(fb_child_users).child(user.getUserid());

        final Map<String, Object> values = new HashMap<>();

        if (user.getEmail() != null)
            values.put(fb_child_email, user.getEmail());
        if (user.getName() != null)
            values.put(fb_child_name, user.getName());
        if (user.getProfilePhotoUrl() != null)
            values.put(fb_child_profilePhotoUrl, user.getProfilePhotoUrl());
        if (user.getUsername() != null)
            values.put(fb_child_username, user.getUsername());

        //Phone bilgisini ekleyelim
        if (user.getPhone() != null) {
            Map<String, Object> phoneMap = new HashMap<>();

            if (user.getPhone().getCountryCode() != null)
                phoneMap.put(fb_child_countryCode, user.getPhone().getCountryCode());

            if (user.getPhone().getDialCode() != null)
                phoneMap.put(fb_child_dialCode, user.getPhone().getDialCode());

            if (user.getPhone().getPhoneNumber() != 0)
                phoneMap.put(fb_child_phoneNumber, user.getPhone().getPhoneNumber());

            values.put(fb_child_phone, phoneMap);
        }

        //Group map bilgisini ekleyelim
        if(user.getGroupIdList() != null){
            Map<String, Object> groupMap = new HashMap<>();
            for(String groupId : user.getGroupIdList()){
                groupMap.put(groupId, " ");
            }
            values.put(fb_child_groups, groupMap);
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

    public static void getUser(String userid, CompleteCallback completeCallback){

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(fb_child_users).child(userid);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Map<String, Object> userMap = (Map) dataSnapshot.getValue();

                if(userMap != null) {
                    String email = (String) userMap.get(fb_child_email);
                    String name = (String) userMap.get(fb_child_name);
                    String username = (String) userMap.get(fb_child_username);
                    String photoUrl = (String) userMap.get(fb_child_profilePhotoUrl);

                    //Phone number bilgisini alalim
                    Map<String, Object> phoneMap = (Map) Objects.requireNonNull(userMap).get(fb_child_phone);
                    Phone phone = new Phone();
                    if (phoneMap != null) {
                        String countryCode = (String) phoneMap.get(fb_child_countryCode);
                        String dialCode = (String) phoneMap.get(fb_child_dialCode);
                        long phoneNumber = (long) phoneMap.get(fb_child_phoneNumber);
                        phone = new Phone(countryCode, dialCode, phoneNumber);
                    }

                    //Group id listesini alalim
                    Map<String, Object> groupMap = (Map) Objects.requireNonNull(userMap).get(fb_child_groups);
                    List<String> groupList = new ArrayList<>();
                    if(groupMap != null) {
                        for (String groupId : groupMap.keySet()) {
                            groupList.add(groupId);
                        }
                    }

                    User user = new User(userid, name, username, email, photoUrl, phone, groupList);
                    completeCallback.onComplete(user);
                }else
                    completeCallback.onComplete(new User());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                completeCallback.onFailed(databaseError.toString());
            }
        });
    }
}
