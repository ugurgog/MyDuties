package uren.com.myduties.dutyManagement.tasks.adapters;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import uren.com.myduties.R;
import uren.com.myduties.common.ShowSelectedPhotoFragment;
import uren.com.myduties.dbManagement.UserDBHelper;
import uren.com.myduties.dbManagement.UserTaskDBHelper;
import uren.com.myduties.dutyManagement.BaseFragment;
import uren.com.myduties.evetBusModels.TaskTypeBus;
import uren.com.myduties.interfaces.CompleteCallback;
import uren.com.myduties.interfaces.OnCompleteCallback;
import uren.com.myduties.interfaces.ReturnCallback;
import uren.com.myduties.messaging.NotificationHandler;
import uren.com.myduties.models.Task;
import uren.com.myduties.models.User;
import uren.com.myduties.utils.CommonUtils;
import uren.com.myduties.utils.TaskTypeHelper;
import uren.com.myduties.utils.dataModelUtil.UserDataUtil;

public class WaitingTaskAdapter extends RecyclerView.Adapter {

    public static final int VIEW_PROG = 0;
    public static final int VIEW_ITEM = 1;
    public static final int VIEW_NULL = 2;

    private Activity mActivity;
    private Context mContext;
    private List<Task> taskList;
    private BaseFragment.FragmentNavigation fragmentNavigation;
    private HashMap<String, Integer> taskPositionHashMap;
    private ReturnCallback returnCallback;
    private TaskTypeHelper taskTypeHelper;

    public WaitingTaskAdapter(Activity activity, Context context,
                              BaseFragment.FragmentNavigation fragmentNavigation) {
        this.mActivity = activity;
        this.mContext = context;
        this.fragmentNavigation = fragmentNavigation;
        this.taskList = new ArrayList<>();
        this.taskPositionHashMap = new HashMap<>();
        EventBus.getDefault().register(this);
    }

    @Subscribe(sticky = true)
    public void taskTypeReceived(TaskTypeBus taskTypeBus){
        taskTypeHelper = taskTypeBus.getTypeMap();
    }

    public void setReturnCallback(ReturnCallback returnCallback){
        this.returnCallback = returnCallback;
    }

    @Override
    public int getItemViewType(int position) {
        if (taskList.size() > 0 && position >= 0) {
            return taskList.get(position) != null ? VIEW_ITEM : VIEW_PROG;
        } else {
            return VIEW_NULL;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        RecyclerView.ViewHolder viewHolder;
        if (viewType == VIEW_ITEM) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.task_vert_list_item, parent, false);

            viewHolder = new MyViewHolder(itemView);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.progressbar_item, parent, false);

            viewHolder = new ProgressViewHolder(v);
        }
        return viewHolder;

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof MyViewHolder) {
            Task task = taskList.get(position);
            ((MyViewHolder) holder).setData(task, position);
        } else {
            ((ProgressViewHolder) holder).progressBar.setIndeterminate(true);
        }
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        View mView;
        ImageView imgProfilePic;
        ImageView moreImgv;
        ImageView taskTypeImgv;
        TextView txtProfilePic;
        TextView txtUserName;
        TextView txtDetail;
        CardView cardView;
        Task task;
        int position;
        LinearLayout profileMainLayout;
        TextView txtCreateAt;
        PopupMenu popupMenu = null;
        User assignedFrom = null;

        public MyViewHolder(View view) {
            super(view);

            mView = view;
            cardView = view.findViewById(R.id.card_view);
            imgProfilePic = view.findViewById(R.id.imgProfilePic);
            moreImgv = view.findViewById(R.id.moreImgv);
            txtProfilePic = view.findViewById(R.id.txtProfilePic);
            txtUserName = view.findViewById(R.id.txtUserName);
            txtDetail = view.findViewById(R.id.txtDetail);
            profileMainLayout = view.findViewById(R.id.profileMainLayout);
            txtCreateAt = view.findViewById(R.id.txtCreateAt);
            taskTypeImgv = view.findViewById(R.id.taskTypeImgv);
            setListeners();
            setPopupMenu();
        }

        private void setPopupMenu() {
            popupMenu = new PopupMenu(mContext, moreImgv);
            popupMenu.inflate(R.menu.menu_waiting_task_item);
        }

        private void setListeners() {
            imgProfilePic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (assignedFrom != null && assignedFrom.getProfilePhotoUrl() != null &&
                            !assignedFrom.getProfilePhotoUrl().isEmpty()) {
                        fragmentNavigation.pushFragment(new ShowSelectedPhotoFragment(assignedFrom.getProfilePhotoUrl()));
                    }
                }
            });

            moreImgv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.changeAsCompleted:
                                    task.setCompleted(true);

                                    UserTaskDBHelper.updateUserTask(task, true, new OnCompleteCallback() {
                                        @Override
                                        public void OnCompleted() {
                                            taskList.remove(position);
                                            notifyItemRemoved(position);
                                            notifyItemRangeChanged(position, getItemCount());
                                            returnCallback.OnReturn(taskList);
                                            NotificationHandler.sendUserNotification(mContext, task.getAssignedTo(), task.getAssignedFrom(),
                                                    task.getAssignedTo().getUserid() + " " + mContext.getResources().getString(R.string.completedThisTask),
                                                    task.getTaskDesc());
                                        }

                                        @Override
                                        public void OnFailed(String message) {
                                            CommonUtils.showToastShort(mContext, message);
                                        }
                                    });

                                    break;
                                case R.id.callUser:

                                    UserDBHelper.getUser(task.getAssignedFrom().getUserid(), new CompleteCallback() {
                                        @Override
                                        public void onComplete(Object object) {
                                            User user = (User) object;

                                            try {
                                                if(user != null && user.getPhone() != null){
                                                    String phoneNumber = user.getPhone().getDialCode() + user.getPhone().getPhoneNumber();
                                                    mContext.startActivity(new Intent(Intent.ACTION_DIAL,
                                                            Uri.fromParts("tel", phoneNumber, null)));
                                                }
                                            } catch (Exception e) {
                                                CommonUtils.showToastShort(mContext, mContext.getResources().getString(R.string.users_phone_not_defined));
                                                e.printStackTrace();
                                            }
                                        }

                                        @Override
                                        public void onFailed(String message) {
                                            CommonUtils.showToastShort(mContext, message);
                                        }
                                    });

                                    break;
                            }
                            return false;
                        }
                    });
                    popupMenu.show();
                }
            });
        }

        public void setData(Task task, int position) {

            //her postID bir position ile entegre halde...
            this.task = task;
            this.position = position;
            taskPositionHashMap.put(task.getTaskId(), position);
            setTaskTypeImage();
            setUrgency();

            //Task Description
            if (task.getTaskDesc() != null && !task.getTaskDesc().isEmpty()) {
                txtDetail.setText(task.getTaskDesc());
                txtDetail.setVisibility(View.VISIBLE);
            } else {
                txtDetail.setVisibility(View.GONE);
            }

            //Create at
            if (task.getAssignedTime() != 0)
                txtCreateAt.setText(CommonUtils.getMessageTime(mContext, task.getAssignedTime()));

            UserDBHelper.getUser(task.getAssignedFrom().getUserid(), new CompleteCallback() {
                @Override
                public void onComplete(Object object) {
                    assignedFrom = (User) object;

                    //profile picture
                    UserDataUtil.setProfilePicture(mContext, assignedFrom.getProfilePhotoUrl(), assignedFrom.getName(), assignedFrom.getUsername()
                            , txtProfilePic, imgProfilePic, true);

                    //username of user who assigned the task
                    txtUserName.setText(UserDataUtil.getNameOrUsername(assignedFrom.getName(), assignedFrom.getUsername()));
                }

                @Override
                public void onFailed(String message) {

                }
            });
        }

        private void setUrgency() {
            CommonUtils.setUrgencyColor(mContext, task.isUrgency(), cardView, null);
        }

        private void setTaskTypeImage() {
            CommonUtils.setTaskTypeImage(mContext, taskTypeImgv, task.getType(), taskTypeHelper);
        }
    }

    @Override
    public int getItemCount() {
        return (taskList != null ? taskList.size() : 0);
    }

    public void addAll(List<Task> addedTaskList) {
        if (addedTaskList != null) {
            taskList.addAll(addedTaskList);
            notifyItemRangeInserted(taskList.size(), taskList.size() + taskList.size());
        }
    }

    public void addProgressLoading() {
        if (getItemViewType(taskList.size() - 1) != VIEW_PROG) {
            taskList.add(null);
            notifyItemInserted(taskList.size() - 1);
        }
    }

    public void removeProgressLoading() {
        if (getItemViewType(taskList.size() - 1) == VIEW_PROG) {
            taskList.remove(taskList.size() - 1);
            notifyItemRemoved(taskList.size());
        }
    }

    public void updatePostListItems(List<Task> newTaskList) {
        this.taskList.clear();
        this.taskList.addAll(newTaskList);
        notifyDataSetChanged();
    }

    public boolean isShowingProgressLoading() {
        return getItemViewType(taskList.size() - 1) == VIEW_PROG;
    }

    public static class ProgressViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public ProgressViewHolder(View v) {
            super(v);
            progressBar = v.findViewById(R.id.progressBarLoading);
        }
    }

}