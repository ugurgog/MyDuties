package uren.com.myduties.dutyManagement.tasks.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import uren.com.myduties.R;
import uren.com.myduties.dbManagement.FriendsDBHelper;
import uren.com.myduties.dutyManagement.profile.interfaces.ListItemClickListener;
import uren.com.myduties.evetBusModels.UserBus;
import uren.com.myduties.interfaces.CompleteCallback;
import uren.com.myduties.interfaces.OnCompleteCallback;
import uren.com.myduties.models.Friend;
import uren.com.myduties.models.User;
import uren.com.myduties.utils.CommonUtils;
import uren.com.myduties.utils.ShapeUtil;
import uren.com.myduties.utils.dataModelUtil.UserDataUtil;
import uren.com.myduties.utils.dialogBoxUtil.CustomDialogBox;
import uren.com.myduties.utils.dialogBoxUtil.DialogBoxUtil;
import uren.com.myduties.utils.dialogBoxUtil.Interfaces.CustomDialogListener;
import uren.com.myduties.utils.dialogBoxUtil.Interfaces.YesNoDialogBoxCallback;

import static uren.com.myduties.constants.StringConstants.fb_child_status_friend;
import static uren.com.myduties.constants.StringConstants.fb_child_status_sendedrequest;
import static uren.com.myduties.constants.StringConstants.fb_child_status_waiting;

public class SearchResultAdapter extends RecyclerView.Adapter {

    public static final int VIEW_PROG = 0;
    public static final int VIEW_ITEM = 1;
    public static final int VIEW_NULL = 2;

    private Context mContext;
    private List<Friend> friendList;
    GradientDrawable imageShape;

    User accountholderUser;

    public SearchResultAdapter(Context context) {
        this.mContext = context;
        this.friendList = new ArrayList<>();
        imageShape = ShapeUtil.getShape(context.getResources().getColor(R.color.DodgerBlue, null),
                0, GradientDrawable.OVAL, 50, 0);
        EventBus.getDefault().register(this);
    }

    @Subscribe(sticky = true)
    public void userReceived(UserBus userBus) {
        accountholderUser = userBus.getUser();
    }

    @Override
    public int getItemViewType(int position) {
        if (friendList.size() > 0 && position >= 0) {
            return friendList.get(position) != null ? VIEW_ITEM : VIEW_PROG;
        } else {
            return VIEW_NULL;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        RecyclerView.ViewHolder viewHolder;

        if (viewType == VIEW_ITEM) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.search_result_list_item, parent, false);

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
            Friend friend = friendList.get(position);
            ((MyViewHolder) holder).setData(friend, position);
        } else {
            ((ProgressViewHolder) holder).progressBar.setIndeterminate(true);
        }
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView profileName;
        TextView profileUserName;
        TextView shortUserNameTv;
        ImageView profileImage;
        Button btnFollowStatus;
        CardView cardView;
        Friend friend;
        int position;

        public MyViewHolder(View view) {
            super(view);

            profileName = view.findViewById(R.id.profile_name);
            profileUserName = view.findViewById(R.id.profile_user_name);
            shortUserNameTv = view.findViewById(R.id.shortUserNameTv);
            profileImage = view.findViewById(R.id.profile_image);
            btnFollowStatus = view.findViewById(R.id.btnFollowStatus);
            cardView = view.findViewById(R.id.card_view);
            profileImage.setBackground(imageShape);
            setListeners();
        }

        private void setListeners() {

            btnFollowStatus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    btnFollowStatus.setEnabled(false);
                    btnFollowStatus.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.image_click));
                    manageFollowStatus();
                }
            });
        }

        public void manageFollowStatus() {
            
            switch (friend.getFriendStatus()) {
                case fb_child_status_friend:
                    
                    break;
                case fb_child_status_waiting:
                    FriendsDBHelper.acceptFriendRequest(accountholderUser.getUserid(), friend.getUser().getUserid(), new OnCompleteCallback() {
                        @Override
                        public void OnCompleted() {
                            friend.setFriendStatus(fb_child_status_friend);
                            friendList.set(position, friend);
                            notifyItemChanged(position);
                        }

                        @Override
                        public void OnFailed(String message) {
                            CommonUtils.showToastShort(mContext, message);
                        }
                    });
                    break;
                case fb_child_status_sendedrequest:
                    cancelRequestFriendDialog();
                    break;
                default:
                    sendFriendRequest();
                    break;
            }
        }

        public void sendFriendRequest(){
            FriendsDBHelper.sendFriendRequest(accountholderUser.getUserid(), friend.getUser().getUserid(), new OnCompleteCallback() {
                @Override
                public void OnCompleted() {
                    friend.setFriendStatus(fb_child_status_friend);
                    friendList.set(position, friend);
                    notifyItemChanged(position);
                }

                @Override
                public void OnFailed(String message) {

                }
            });
        }

        public void cancelRequestFriendDialog() {

            new CustomDialogBox.Builder((Activity) mContext)
                    .setMessage(mContext.getResources().getString(R.string.cancelSendedRequest))
                    .setUser(friend.getUser())
                    .setNegativeBtnVisibility(View.VISIBLE)
                    .setNegativeBtnText(mContext.getResources().getString(R.string.cancel))
                    .setNegativeBtnBackground(mContext.getResources().getColor(R.color.Silver, null))
                    .setPositiveBtnVisibility(View.VISIBLE)
                    .setPositiveBtnText(mContext.getResources().getString(R.string.lowerYes))
                    .setPositiveBtnBackground(mContext.getResources().getColor(R.color.bg_screen1, null))
                    .setDurationTime(0)
                    .isCancellable(true)
                    .OnPositiveClicked(new CustomDialogListener() {
                        @Override
                        public void OnClick() {
                            removeFriend();
                        }
                    })
                    .OnNegativeClicked(new CustomDialogListener() {
                        @Override
                        public void OnClick() {

                        }
                    }).build();
        }

        private void removeFriend() {
            FriendsDBHelper.removeFriend(accountholderUser.getUserid(), friend.getUser().getUserid(), new OnCompleteCallback() {
                @Override
                public void OnCompleted() {
                    friend.setFriendStatus("");
                    friendList.set(position, friend);
                    notifyItemChanged(position);
                }

                @Override
                public void OnFailed(String message) {
                    CommonUtils.showToastShort(mContext, message);
                }
            });
        }

        public void setData(Friend friend, int position) {
            this.friend = friend;
            this.position = position;
            UserDataUtil.setName(friend.getUser().getName(), profileName);
            UserDataUtil.setUsername(friend.getUser().getUsername(), profileUserName);
            UserDataUtil.setProfilePicture(mContext, friend.getUser().getProfilePhotoUrl(),
                    friend.getUser().getName(), friend.getUser().getUsername(), shortUserNameTv, profileImage, false);
            setFriendStatus();
            UserDataUtil.updateFriendButton(mContext, friend.getFriendStatus(), btnFollowStatus, true);
            setButtonEnabled();
        }

        private void setFriendStatus() {
            if(friend.getFriendStatus() == null || friend.getFriendStatus().isEmpty()){
                FriendsDBHelper.getFriendStatus(accountholderUser.getUserid(), friend.getUser().getUserid(), new CompleteCallback() {
                    @Override
                    public void onComplete(Object object) {
                        String status = (String) object;
                        friend.setFriendStatus(status);
                        friendList.set(position, friend);
                        notifyItemChanged(position);
                    }

                    @Override
                    public void onFailed(String message) {

                    }
                });
            }
        }

        private void setButtonEnabled() {
            if(friend.getFriendStatus().equals(fb_child_status_friend))
                btnFollowStatus.setEnabled(false);
            else
                btnFollowStatus.setEnabled(true);
        }
    }

    public static class ProgressViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public ProgressViewHolder(View v) {
            super(v);
            progressBar = v.findViewById(R.id.progressBarLoading);
        }
    }

    public void addProgressLoading() {
        if (getItemViewType(friendList.size() - 1) != VIEW_PROG) {
            friendList.add(null);
            notifyItemInserted(friendList.size() - 1);
        }
    }

    public void removeProgressLoading() {
        if (getItemViewType(friendList.size() - 1) == VIEW_PROG) {
            friendList.remove(friendList.size() - 1);
            notifyItemRemoved(friendList.size());
        }
    }

    @Override
    public int getItemCount() {
        return friendList.size();
    }

    public void updateAdapterWithPosition(int position) {
        notifyItemChanged(position);
    }

    public List<Friend> getPersonList() {
        return friendList;
    }

    public void addAll(List<Friend> friends) {
        if (friends != null) {
            friendList.addAll(friends);
            notifyItemRangeInserted(friendList.size(), friendList.size() + friends.size());
        }
    }

    public void updateListItems(List<Friend> newUserList) {
        this.friendList.clear();
        this.friendList.addAll(newUserList);
    }

    public void clearList() {
        friendList.clear();
        notifyDataSetChanged();
    }
}


