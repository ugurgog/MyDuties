package uren.com.myduties.utils.dataModelUtil;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.Calendar;

import uren.com.myduties.R;
import uren.com.myduties.utils.CommonUtils;
import uren.com.myduties.utils.ShapeUtil;

import static uren.com.myduties.constants.StringConstants.CHAR_AMPERSAND;

public class UserDataUtil {

    public static void setNameOrUserName(String name, String username, TextView textView) {
        int nameMaxLen = 25;

        if (name != null && !name.isEmpty()) {
            if (name.length() > nameMaxLen)
                textView.setText(name.trim().substring(0, nameMaxLen) + "...");
            else
                textView.setText(name);
        } else if (username != null && !username.isEmpty()) {
            if (username.length() > nameMaxLen)
                textView.setText(CHAR_AMPERSAND + username.trim().substring(0, nameMaxLen) + "...");
            else
                textView.setText(CHAR_AMPERSAND + username);
        } else
            textView.setVisibility(View.GONE);
    }

    public static String getNameOrUsername(String name, String username) {
        int nameMaxLen = 25;

        if (name != null && !name.isEmpty()) {
            if (name.length() > nameMaxLen)
                return name.trim().substring(0, nameMaxLen) + "...";
            else
                return name;
        } else if (username != null && !username.isEmpty()) {
            if (username.length() > nameMaxLen)
                return CHAR_AMPERSAND + username.trim().substring(0, nameMaxLen) + "...";
            else
                return CHAR_AMPERSAND + username;
        }else
            return "unknown";
    }


    public static void setName(String name, TextView nameTextView) {
        int nameMaxLen = 25;
        if (name != null && nameTextView != null && !name.isEmpty()) {
            nameTextView.setVisibility(View.VISIBLE);
            if (name.length() > nameMaxLen)
                nameTextView.setText(name.trim().substring(0, nameMaxLen) + "...");
            else
                nameTextView.setText(name);
        } else if (nameTextView != null)
            nameTextView.setVisibility(View.GONE);
    }

    public static void setUsername(String username, TextView usernameTextView) {
        int nameMaxLen = 25;
        if (username != null && usernameTextView != null && !username.isEmpty()) {
            usernameTextView.setVisibility(View.VISIBLE);
            if (username.length() > nameMaxLen)
                usernameTextView.setText(CHAR_AMPERSAND + username.trim().substring(0, nameMaxLen) + "...");
            else
                usernameTextView.setText(CHAR_AMPERSAND + username);
        } else if (usernameTextView != null)
            usernameTextView.setVisibility(View.GONE);
    }

    public static String getShortenUserName(String name) {
        StringBuilder returnValue = new StringBuilder();
        if (name != null && !name.trim().isEmpty()) {
            String[] seperatedName = name.trim().split(" ");
            for (String word : seperatedName) {
                if (returnValue.length() < 3)
                    returnValue.append(word.substring(0, 1).toUpperCase());
            }
        }

        return returnValue.toString();
    }

    public static int setProfilePicture(Context context, String url, String name, String username, TextView shortNameTv,
                                        ImageView profilePicImgView, boolean circleColorVal) {
        if (context == null) return 0;

        boolean picExist = false;
        if (url != null && !url.trim().isEmpty()) {
            shortNameTv.setVisibility(View.GONE);
            Glide.with(context)
                    .load(url)
                    .apply(RequestOptions.circleCropTransform())
                    .into(profilePicImgView);
            picExist = true;
            //profilePicImgView.setPadding(1, 1, 1, 1); // degerler asagidaki imageShape strokeWidth ile aynı tutulmalı
        } else {
            if (name != null && !name.trim().isEmpty()) {
                shortNameTv.setVisibility(View.VISIBLE);
                shortNameTv.setText(UserDataUtil.getShortenUserName(name));
                profilePicImgView.setImageDrawable(null);
            } else if (username != null && !username.trim().isEmpty()) {
                shortNameTv.setVisibility(View.VISIBLE);
                shortNameTv.setText(UserDataUtil.getShortenUserName(username));
                profilePicImgView.setImageDrawable(null);
            } else {
                shortNameTv.setVisibility(View.GONE);
                Glide.with(context)
                        .load(R.drawable.ic_person_white_24dp)
                        .apply(RequestOptions.circleCropTransform())
                        .into(profilePicImgView);
            }
        }

        GradientDrawable imageShape;
        int colorCode = CommonUtils.getDarkRandomColor(context);

        if (circleColorVal) {
            if (picExist) {
                imageShape = ShapeUtil.getShape(context.getResources().getColor(R.color.White, null),
                        context.getResources().getColor(R.color.DodgerBlue, null),
                        GradientDrawable.OVAL, 50, 7);
            } else {
                imageShape = ShapeUtil.getShape(context.getResources().getColor(R.color.DodgerBlue, null),
                        context.getResources().getColor(R.color.DodgerBlue, null),
                        GradientDrawable.OVAL, 50, 7);
            }
        } else
            imageShape = ShapeUtil.getShape(context.getResources().getColor(colorCode, null),
                    context.getResources().getColor(R.color.White, null),
                    GradientDrawable.OVAL, 50, 3);

        profilePicImgView.setBackground(imageShape);

        if (!picExist)
            return colorCode;
        else return 0;
    }

}
