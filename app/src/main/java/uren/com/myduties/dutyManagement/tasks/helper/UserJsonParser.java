package uren.com.myduties.dutyManagement.tasks.helper;

import org.json.JSONObject;

import uren.com.myduties.models.User;

import static uren.com.myduties.constants.StringConstants.fb_child_email;
import static uren.com.myduties.constants.StringConstants.fb_child_name;
import static uren.com.myduties.constants.StringConstants.fb_child_profilePhotoUrl;
import static uren.com.myduties.constants.StringConstants.fb_child_username;

public class UserJsonParser {

    public User parse(JSONObject jsonObject) {
        if (jsonObject == null) {
            return null;
        }

        String userid = jsonObject.optString("objectID");
        String name = jsonObject.optString(fb_child_name);
        String username = jsonObject.optString(fb_child_username);
        String profilePhotoUrl = jsonObject.optString(fb_child_profilePhotoUrl);
        String email = jsonObject.optString(fb_child_email);

        User user = new User();
        user.setEmail(email);
        user.setProfilePhotoUrl(profilePhotoUrl);
        user.setUsername(username);
        user.setName(name);
        user.setUserid(userid);

        return user;
    }
}
