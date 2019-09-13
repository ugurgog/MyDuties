package uren.com.myduties.dbManagement;

import androidx.annotation.NonNull;

import com.algolia.search.saas.Client;
import com.algolia.search.saas.Index;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import uren.com.myduties.interfaces.CompleteCallback;
import uren.com.myduties.interfaces.OnCompleteCallback;
import uren.com.myduties.models.Phone;
import uren.com.myduties.models.User;

import static uren.com.myduties.constants.StringConstants.ALGOLIA_APP_ID;
import static uren.com.myduties.constants.StringConstants.ALGOLIA_INDEX_NAME;
import static uren.com.myduties.constants.StringConstants.ALGOLIA_SEARCH_API_KEY;
import static uren.com.myduties.constants.StringConstants.fb_child_admin;
import static uren.com.myduties.constants.StringConstants.fb_child_countryCode;
import static uren.com.myduties.constants.StringConstants.fb_child_dialCode;
import static uren.com.myduties.constants.StringConstants.fb_child_email;
import static uren.com.myduties.constants.StringConstants.fb_child_groups;
import static uren.com.myduties.constants.StringConstants.fb_child_name;
import static uren.com.myduties.constants.StringConstants.fb_child_phone;
import static uren.com.myduties.constants.StringConstants.fb_child_phoneNumber;
import static uren.com.myduties.constants.StringConstants.fb_child_profilePhotoUrl;
import static uren.com.myduties.constants.StringConstants.fb_child_username;
import static uren.com.myduties.constants.StringConstants.fb_child_users;

public class UserDBHelper {

    public static void addUser(User user, final OnCompleteCallback onCompleteCallback) {

        if (user == null)
            return;
        if (user.getUserid() == null || user.getUserid().isEmpty())
            return;

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(fb_child_users).child(user.getUserid());

        final Map<String, Object> values = new HashMap<>();

        if (user.getEmail() != null)
            values.put(fb_child_email, user.getEmail());
        if (user.getUsername() != null)
            values.put(fb_child_username, user.getUsername());

        databaseReference.updateChildren(values).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                JSONObject object = null;
                try {
                    object = new JSONObject()
                            .put(fb_child_email, user.getEmail())
                            .put(fb_child_username, user.getUsername());
                    addUserToAlgolia(object, user.getUserid(), "add");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                onCompleteCallback.OnCompleted();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                onCompleteCallback.OnFailed(e.getMessage());
            }
        });
    }

    public static void updateUser(User user, boolean updateAlgolia, final OnCompleteCallback onCompleteCallback) {

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
        if (user.getGroupIdList() != null) {
            Map<String, Object> groupMap = new HashMap<>();
            for (String groupId : user.getGroupIdList()) {
                groupMap.put(groupId, " ");
            }
            values.put(fb_child_groups, groupMap);
        }

        databaseReference.updateChildren(values).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (updateAlgolia) {
                    JSONObject object = null;
                    try {
                        object = new JSONObject()
                                .put(fb_child_email, user.getEmail())
                                .put(fb_child_username, user.getUsername())
                                .put(fb_child_name, user.getName())
                                .put(fb_child_profilePhotoUrl, user.getProfilePhotoUrl());
                        addUserToAlgolia(object, user.getUserid(), "update");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                onCompleteCallback.OnCompleted();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                onCompleteCallback.OnFailed(e.getMessage());
            }
        });
    }

    public static void addUserToAlgolia(JSONObject jsonObject, String userid, String type) {
        Index index;
        Client client = new Client(ALGOLIA_APP_ID, ALGOLIA_SEARCH_API_KEY);
        index = client.getIndex(ALGOLIA_INDEX_NAME);

        if (type.equals("add"))
            index.addObjectAsync(jsonObject, userid, null);
        else if (type.equals("update"))
            index.saveObjectAsync(jsonObject, userid, null);
    }

    public static void getUser(String userid, CompleteCallback completeCallback) {

        if (userid == null) return;

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(fb_child_users).child(userid);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Map<String, Object> userMap = (Map) dataSnapshot.getValue();

                if (userMap != null) {
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
                    if (groupMap != null) {
                        for (String groupId : groupMap.keySet()) {
                            groupList.add(groupId);
                        }
                    }

                    boolean admin = false;
                    try {
                        admin = (boolean) userMap.get(fb_child_admin);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    User user = new User(userid, name, username, email, photoUrl, phone, groupList, admin);
                    user.setAdmin(admin);
                    completeCallback.onComplete(user);
                } else
                    completeCallback.onComplete(new User());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                completeCallback.onFailed(databaseError.toString());
            }
        });
    }
}
