package uren.com.myduties.utils.dataModelUtil;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import uren.com.myduties.R;
import uren.com.myduties.models.Group;
import uren.com.myduties.utils.CommonUtils;
import uren.com.myduties.utils.ShapeUtil;

import static uren.com.myduties.constants.NumericConstants.GROUP_NAME_MAX_LENGTH;

public class GroupDataUtil {

    public static void setGroupPicture(Context context, String url, String name, TextView shortNameTv, ImageView groupPicImgView) {
        if (url != null && !url.trim().isEmpty()) {
            shortNameTv.setVisibility(View.GONE);
            Glide.with(context)
                    .load(url)
                    .apply(RequestOptions.circleCropTransform())
                    .into(groupPicImgView);
            groupPicImgView.setPadding(1, 1, 1, 1); // degerler asagidaki imageShape strokeWidth ile aynı tutulmalı
        } else {
            if (name != null && !name.trim().isEmpty()) {
                shortNameTv.setVisibility(View.VISIBLE);
                shortNameTv.setText(UserDataUtil.getShortenUserName(name));
                groupPicImgView.setImageDrawable(null);
            } else {
                shortNameTv.setVisibility(View.GONE);
                Glide.with(context)
                        .load(R.drawable.ic_group_white_24dp)
                        .apply(RequestOptions.centerInsideTransform())
                        .into(groupPicImgView);
            }
        }

        GradientDrawable imageShape = ShapeUtil.getShape(context.getResources().getColor(CommonUtils.getDarkRandomColor(context)),
                context.getResources().getColor(R.color.White),
                GradientDrawable.OVAL, 50, 3);
        groupPicImgView.setBackground(imageShape);
    }

    public static void setGroupName(Group group, TextView groupnameTextView) {
        if(group != null) {
            if (group.getName() != null && !group.getName().trim().isEmpty()) {
                if (group.getName().trim().length() > GROUP_NAME_MAX_LENGTH)
                    groupnameTextView.setText(group.getName().trim().substring(0, GROUP_NAME_MAX_LENGTH) + "...");
                else
                    groupnameTextView.setText(group.getName());
            }
        }
    }
}
