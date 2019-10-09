package uren.com.myduties.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import uren.com.myduties.R;
import uren.com.myduties.models.GroupTask;
import uren.com.myduties.models.Problem;
import uren.com.myduties.models.Task;
import uren.com.myduties.models.TaskType;
import uren.com.myduties.models.User;
import uren.com.myduties.utils.dataModelUtil.UserDataUtil;

public class MyDutiesUtil {

    public static void callAssignedFromTaskUser(Context mContext, Task task, User accountHolderUser) {
        if(task.getAssignedFrom().getUserid().equals(accountHolderUser.getUserid())){
            CommonUtils.showToastShort(mContext, mContext.getResources().getString(R.string.notCallableItsYou));
            return;
        }

        if(task.getAssignedFrom().getPhone() == null || task.getAssignedFrom().getPhone().getPhoneNumber() == 0 ||
                task.getAssignedFrom().getPhone().getDialCode() == null){
            CommonUtils.showToastShort(mContext, UserDataUtil.getNameOrUsernameFromUser(task.getAssignedFrom())  +
                    mContext.getResources().getString(R.string.phone_not_defined));
            return;
        }

        try {
            if (task.getAssignedFrom() != null && task.getAssignedFrom().getPhone() != null) {
                String phoneNumber = task.getAssignedFrom().getPhone().getDialCode() +
                        task.getAssignedFrom().getPhone().getPhoneNumber();
                mContext.startActivity(new Intent(Intent.ACTION_DIAL,
                        Uri.fromParts("tel", phoneNumber, null)));
            }
        } catch (Exception e) {
            CommonUtils.showToastShort(mContext, UserDataUtil.getNameOrUsernameFromUser(task.getAssignedFrom()) +
                    mContext.getResources().getString(R.string.phone_not_defined));
            e.printStackTrace();
        }
    }

    public static void callAssignedToTaskUser(Context mContext, Task task, User accountHolderUser) {
        if(task.getAssignedTo().getUserid().equals(accountHolderUser.getUserid())){
            CommonUtils.showToastShort(mContext, mContext.getResources().getString(R.string.notCallableItsYou));
            return;
        }

        if(task.getAssignedTo().getPhone() == null || task.getAssignedTo().getPhone().getPhoneNumber() == 0 ||
                task.getAssignedTo().getPhone().getDialCode() == null){
            CommonUtils.showToastShort(mContext, UserDataUtil.getNameOrUsernameFromUser(task.getAssignedTo())  +
                    mContext.getResources().getString(R.string.phone_not_defined));
            return;
        }

        try {
            if (task.getAssignedTo() != null && task.getAssignedTo().getPhone() != null) {
                String phoneNumber = task.getAssignedTo().getPhone().getDialCode() +
                        task.getAssignedTo().getPhone().getPhoneNumber();
                mContext.startActivity(new Intent(Intent.ACTION_DIAL,
                        Uri.fromParts("tel", phoneNumber, null)));
            }
        } catch (Exception e) {
            CommonUtils.showToastShort(mContext, UserDataUtil.getNameOrUsernameFromUser(task.getAssignedTo())  +
                    mContext.getResources().getString(R.string.phone_not_defined));
            e.printStackTrace();
        }
    }

    public static void callAssignedFromGroupTaskUser(Context mContext, GroupTask groupTask, User accountHolderUser) {
        if(groupTask.getAssignedFrom().getUserid().equals(accountHolderUser.getUserid())){
            CommonUtils.showToastShort(mContext, mContext.getResources().getString(R.string.notCallableItsYou));
            return;
        }

        if(groupTask.getAssignedFrom().getPhone() == null || groupTask.getAssignedFrom().getPhone().getPhoneNumber() == 0 ||
                groupTask.getAssignedFrom().getPhone().getDialCode() == null){
            CommonUtils.showToastShort(mContext, UserDataUtil.getNameOrUsernameFromUser(groupTask.getAssignedFrom())  +
                    mContext.getResources().getString(R.string.phone_not_defined));
            return;
        }

        try {
            if (groupTask.getAssignedFrom() != null && groupTask.getAssignedFrom().getPhone() != null) {
                String phoneNumber = groupTask.getAssignedFrom().getPhone().getDialCode() +
                        groupTask.getAssignedFrom().getPhone().getPhoneNumber();
                mContext.startActivity(new Intent(Intent.ACTION_DIAL,
                        Uri.fromParts("tel", phoneNumber, null)));
            }
        } catch (Exception e) {
            CommonUtils.showToastShort(mContext, UserDataUtil.getNameOrUsernameFromUser(groupTask.getAssignedFrom())  +
                    mContext.getResources().getString(R.string.phone_not_defined));
            e.printStackTrace();
        }
    }

    public static void setTaskDescription(Task task, TextView txtDetail) {
        if (task.getTaskDesc() != null && !task.getTaskDesc().isEmpty()) {
            txtDetail.setText(task.getTaskDesc());
            txtDetail.setVisibility(View.VISIBLE);
        } else {
            txtDetail.setVisibility(View.GONE);
        }
    }

    public static void setGroupTaskDescription(GroupTask groupTask, TextView txtDetail) {
        if (groupTask.getTaskDesc() != null && !groupTask.getTaskDesc().isEmpty()) {
            txtDetail.setText(groupTask.getTaskDesc());
            txtDetail.setVisibility(View.VISIBLE);
        } else {
            txtDetail.setVisibility(View.GONE);
        }
    }

    public static void setProblemDescription(Problem problem, TextView txtDetail) {
        if (problem.getProblemDesc() != null && !problem.getProblemDesc().isEmpty()) {
            txtDetail.setText(problem.getProblemDesc());
            txtDetail.setVisibility(View.VISIBLE);
        } else {
            txtDetail.setVisibility(View.GONE);
        }
    }

    public static void setTaskCompletedAtValue(Context mContext, Task task, TextView txtCompletedAt) {
        if (task.getCompletedTime() != 0)
            txtCompletedAt.setText(CommonUtils.getMessageTime(mContext, task.getCompletedTime()));
    }

    public static void setGroupTaskCompletedAtValue(Context mContext, GroupTask groupTask, TextView txtCompletedAt) {
        if (groupTask.getCompletedTime() != 0)
            txtCompletedAt.setText(CommonUtils.getMessageTime(mContext, groupTask.getCompletedTime()));
    }

    public static void setTaskCompletedTimeValueAndSetLayoutVisibility(Task task, View llcompleted) {
        if (task.getCompletedTime() != 0)
            llcompleted.setVisibility(View.VISIBLE);
        else
            llcompleted.setVisibility(View.GONE);
    }

    public static void setGroupTaskCompletedTimeValueAndSetLayoutVisibility(GroupTask groupTask, View llcompleted) {
        if (groupTask.getCompletedTime() != 0)
            llcompleted.setVisibility(View.VISIBLE);
        else
            llcompleted.setVisibility(View.GONE);
    }

    public static void setProblemCompletedTimeValueAndSetLayoutVisibility(Problem problem, View llcompleted, Context mContext,
                                                                          TextView txtCompletedTime) {
        if (problem.getCompletedTime() != 0) {
            llcompleted.setVisibility(View.VISIBLE);
            txtCompletedTime.setText(CommonUtils.getMessageTime(mContext, problem.getCompletedTime()));
        }else
            llcompleted.setVisibility(View.GONE);
    }

    public static void setTaskCreatedAtValue(Context mContext, Task task, TextView txtCreateAt) {
        if (task.getAssignedTime() != 0)
            txtCreateAt.setText(CommonUtils.getMessageTime(mContext, task.getAssignedTime()));
    }

    public static void setGroupTaskCreatedAtValue(Context mContext, GroupTask groupTask, TextView txtCreateAt) {
        if (groupTask.getAssignedTime() != 0)
            txtCreateAt.setText(CommonUtils.getMessageTime(mContext, groupTask.getAssignedTime()));
    }

    public static void setProblemCreatedAtValue(Context mContext, Problem problem, TextView txtCreateAt) {
        if (problem.getCreatedAt() != 0)
            txtCreateAt.setText(CommonUtils.getMessageTime(mContext, problem.getCreatedAt()));
    }

    public static void setTaskTypeImage(Context context, ImageView taskTypeImgv, String type, TaskTypeHelper taskTypeHelper) {
        if (type == null || type.isEmpty()) return;
        int typeVal = 0;

        for (TaskType taskType : taskTypeHelper.getTypes())
            if (taskType.getKey().equals(type)) {
                typeVal = taskType.getImgId();
                break;
            }

        Glide.with(context)
                .load(typeVal)
                .apply(RequestOptions.centerInsideTransform())
                .into(taskTypeImgv);
    }

    public static void setUrgency(Context mContext, boolean urgencyVal, TextView tvUrgency, View view) {
        if (tvUrgency != null) {
            if (urgencyVal)
                tvUrgency.setVisibility(View.VISIBLE);
            else
                tvUrgency.setVisibility(View.GONE);
        }

        if(urgencyVal)
            view.setBackgroundColor(mContext.getResources().getColor(R.color.urgentColor));
        else
            view.setBackgroundColor(mContext.getResources().getColor(R.color.White));
    }

    public static void setClosedTv(boolean closed, TextView tvClosed) {
        if (tvClosed != null) {
            if (closed)
                tvClosed.setVisibility(View.VISIBLE);
            else
                tvClosed.setVisibility(View.GONE);
        }
    }

    public static void setCompletedImgv(Context context, boolean completed, ImageView completedImgv) {
        if (completed)
            completedImgv.setColorFilter(context.getResources().getColor(R.color.Green), PorterDuff.Mode.SRC_IN);
        else
            completedImgv.setColorFilter(context.getResources().getColor(R.color.Red), PorterDuff.Mode.SRC_IN);
    }

    public static void setProblemPicture(Context context, String url, ImageView imageView) {
        if (context == null) return;

        if (url != null && !url.trim().isEmpty()) {
            imageView.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(url)
                    .apply(RequestOptions.centerInsideTransform())
                    .into(imageView);
        }else
            imageView.setVisibility(View.GONE);
    }
}
