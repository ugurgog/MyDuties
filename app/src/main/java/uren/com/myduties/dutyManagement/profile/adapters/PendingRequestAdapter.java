package uren.com.myduties.dutyManagement.profile.adapters;


import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import uren.com.myduties.R;
import uren.com.myduties.dbManagement.FriendsDBHelper;
import uren.com.myduties.dutyManagement.profile.interfaces.ListItemClickListener;
import uren.com.myduties.evetBusModels.UserBus;
import uren.com.myduties.interfaces.OnCompleteCallback;
import uren.com.myduties.interfaces.ReturnCallback;
import uren.com.myduties.models.Friend;
import uren.com.myduties.models.User;
import uren.com.myduties.utils.CommonUtils;
import uren.com.myduties.utils.ShapeUtil;
import uren.com.myduties.utils.dataModelUtil.UserDataUtil;

public class PendingRequestAdapter extends RecyclerView.Adapter<PendingRequestAdapter.PendingRequestHolder> {

    private Context context;
    GradientDrawable imageShape;
    private ListItemClickListener listItemClickListener;
    ReturnCallback returnCallback;
    private List<User> friendList;
    private User accountholderUser;

    public PendingRequestAdapter(Context context, ReturnCallback returnCallback) {
        this.context = context;
        this.friendList = new ArrayList<>();
        this.listItemClickListener = listItemClickListener;
        this.returnCallback = returnCallback;
        imageShape = ShapeUtil.getShape(context.getResources().getColor(R.color.DodgerBlue, null),
                0, GradientDrawable.OVAL, 50, 0);
        EventBus.getDefault().register(this);
    }

    @Subscribe(sticky = true)
    public void userReceived(UserBus userBus) {
        accountholderUser = userBus.getUser();
    }

    @Override
    public PendingRequestHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.pending_request_list_item, parent, false);
        return new PendingRequestHolder(itemView);
    }

    public class PendingRequestHolder extends RecyclerView.ViewHolder {

        AppCompatTextView profileName;
        AppCompatTextView profileUserName;
        TextView shortUserNameTv;
        ImageView profileImage;
        Button btnApprove;
        Button btnReject;
        CardView cardView;
        User user;
        int position;

        public PendingRequestHolder(View view) {
            super(view);

            profileName = view.findViewById(R.id.profile_name);
            profileUserName = view.findViewById(R.id.profile_user_name);
            shortUserNameTv = view.findViewById(R.id.shortUserNameTv);
            profileImage = view.findViewById(R.id.profile_image);
            btnApprove = view.findViewById(R.id.btnApprove);
            btnReject = view.findViewById(R.id.btnReject);
            cardView = view.findViewById(R.id.card_view);
            profileImage.setBackground(imageShape);

            btnApprove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    btnApprove.setEnabled(false);
                    btnApprove.startAnimation(AnimationUtils.loadAnimation(context, R.anim.image_click));
                    managePendingRequest();
                }
            });

            btnReject.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    btnReject.setEnabled(false);
                    btnReject.startAnimation(AnimationUtils.loadAnimation(context, R.anim.image_click));
                    rejectPendingRequest();
                }
            });
        }

        public void managePendingRequest() {

            FriendsDBHelper.acceptFriendRequest(accountholderUser.getUserid(), user.getUserid(), new OnCompleteCallback() {
                @Override
                public void OnCompleted() {
                    friendList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, getItemCount());
                    returnCallback.OnReturn(friendList.size());
                }

                @Override
                public void OnFailed(String message) {
                    CommonUtils.showToastShort(context, message);
                }
            });
        }

        public void rejectPendingRequest() {

            FriendsDBHelper.removeFriend(accountholderUser.getUserid(), user.getUserid(), new OnCompleteCallback() {
                @Override
                public void OnCompleted() {
                    friendList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, getItemCount());
                    returnCallback.OnReturn(friendList.size());
                }

                @Override
                public void OnFailed(String message) {
                    CommonUtils.showToastShort(context, message);
                }
            });
        }

        public void setData(User user, int position) {
            this.user = user;
            this.position = position;
            UserDataUtil.setName(user.getName(), profileName);
            UserDataUtil.setUsername(user.getUsername(), profileUserName);
            UserDataUtil.setProfilePicture(context, user.getProfilePhotoUrl(),
                    user.getName(),
                    user.getUsername(), shortUserNameTv, profileImage, false);
        }
    }

    public void addFriend(User user) {
        if (user != null) {
            friendList.add(user);
            notifyItemInserted(friendList.size() - 1);
            returnCallback.OnReturn(friendList.size());
        }
    }

    public List<User> getFriendList(){
        return friendList;
    }

    @Override
    public void onBindViewHolder(final PendingRequestHolder holder, final int position) {
        User user = friendList.get(position);
        holder.setData(user, position);
    }

    @Override
    public int getItemCount() {
        int size;
        size = friendList.size();
        return size;
    }
}