package uren.com.myduties.dutyManagement.profile.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import uren.com.myduties.R;
import uren.com.myduties.dbManagement.FriendsDBHelper;
import uren.com.myduties.dbManagement.UserDBHelper;
import uren.com.myduties.evetBusModels.UserBus;
import uren.com.myduties.interfaces.CompleteCallback;
import uren.com.myduties.interfaces.OnCompleteCallback;
import uren.com.myduties.interfaces.ReturnCallback;
import uren.com.myduties.models.Friend;
import uren.com.myduties.models.User;
import uren.com.myduties.utils.CommonUtils;
import uren.com.myduties.utils.ShapeUtil;
import uren.com.myduties.utils.dataModelUtil.UserDataUtil;
import uren.com.myduties.utils.dialogBoxUtil.CustomDialogBox;
import uren.com.myduties.utils.dialogBoxUtil.Interfaces.CustomDialogListener;

import static uren.com.myduties.constants.StringConstants.fb_child_status_friend;
import static uren.com.myduties.constants.StringConstants.fb_child_status_sendedrequest;

public class FriendsAdapter extends RecyclerView.Adapter implements Filterable {

    private View mView;
    private Context mContext;
    private List<Friend> friendList;
    private List<Friend> orgFriendList;
    private ReturnCallback searchResultCallback;

    public static final int VIEW_PROG = 0;
    public static final int VIEW_ITEM = 1;
    public static final int VIEW_NULL = 2;

    User accountholderUser;

    public FriendsAdapter(Context context) {
        this.mContext = context;
        this.friendList = new ArrayList<>();
        this.orgFriendList = new ArrayList<>();
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
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.friends_vert_list_item, parent, false);

        viewHolder = new FriendsAdapter.FriendViewHolder(itemView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Friend friend = friendList.get(position);
        ((FriendsAdapter.FriendViewHolder) holder).setData(friend, position);
    }

    public class FriendViewHolder extends RecyclerView.ViewHolder {

        AppCompatTextView profileName;
        AppCompatTextView profileUserName;
        AppCompatTextView shortUserNameTv;
        ImageView profileImage;
        Button btnFollowStatus;
        CardView cardView;
        ImageView settingsImgv;
        Friend friend;
        int position;

        public FriendViewHolder(View view) {
            super(view);

            mView = view;
            profileName = mView.findViewById(R.id.profile_name);
            profileUserName = mView.findViewById(R.id.profile_user_name);
            shortUserNameTv = mView.findViewById(R.id.shortUserNameTv);
            profileImage = mView.findViewById(R.id.profile_image);
            btnFollowStatus = mView.findViewById(R.id.btnFollowStatus);
            cardView = mView.findViewById(R.id.card_view);
            settingsImgv = mView.findViewById(R.id.settingsImgv);
            setShapes();

            btnFollowStatus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    btnFollowStatus.setEnabled(false);
                    btnFollowStatus.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.image_click));
                }
            });

            settingsImgv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (friend.getFriendStatus().equals(fb_child_status_friend))
                        removeFriendDialog();
                    else if (friend.getFriendStatus().equals(fb_child_status_sendedrequest))
                        cancelRequestFriendDialog();
                }
            });
        }

        public void removeFriendDialog() {

            new CustomDialogBox.Builder((Activity) mContext)
                    .setMessage(mContext.getResources().getString(R.string.REMOVE_FOLLOWER_MESSAGE))
                    .setTitle(mContext.getResources().getString(R.string.REMOVE_FOLLOWER_TITLE))
                    .setUser(friend.getUser())
                    .setNegativeBtnVisibility(View.VISIBLE)
                    .setNegativeBtnText(mContext.getResources().getString(R.string.cancel))
                    .setNegativeBtnBackground(mContext.getResources().getColor(R.color.Silver, null))
                    .setPositiveBtnVisibility(View.VISIBLE)
                    .setPositiveBtnText(mContext.getResources().getString(R.string.REMOVE))
                    .setPositiveBtnBackground(mContext.getResources().getColor(R.color.bg_screen1, null))
                    .setDurationTime(0)
                    .isCancellable(true)
                    .setEditTextVisibility(View.GONE)
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
                    .setEditTextVisibility(View.GONE)
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

        public void setShapes() {
            profileImage.setBackground(ShapeUtil.getShape(mContext.getResources().getColor(R.color.DodgerBlue, null),
                    0, GradientDrawable.OVAL, 50, 0));
        }

        private void removeFriend() {

            FriendsDBHelper.removeFriend(accountholderUser.getUserid(), friend.getUser().getUserid(), new OnCompleteCallback() {
                @Override
                public void OnCompleted() {
                    friendList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, getItemCount());
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
            setFriendInformation();
            UserDataUtil.updateFriendButton(mContext, friend.getFriendStatus(), btnFollowStatus, false);
            btnFollowStatus.setEnabled(false);
        }

        private void setFriendInformation() {
            UserDBHelper.getUser(friend.getUser().getUserid(), new CompleteCallback() {
                @Override
                public void onComplete(Object object) {
                    User user = (User) object;
                    UserDataUtil.setName(user.getName(), profileName);
                    UserDataUtil.setUsername(user.getUsername(), profileUserName);
                    UserDataUtil.setProfilePicture(mContext, user.getProfilePhotoUrl(),
                            user.getName(), user.getUsername(), shortUserNameTv, profileImage, false);
                }

                @Override
                public void onFailed(String message) {

                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return ((friendList != null) ? friendList.size() : 0);
    }


    public void addFriend(Friend friend) {
        if (friend != null) {
            friendList.add(friend);
            orgFriendList.add(friend);
            notifyItemInserted(friendList.size() - 1);
        }
    }

    public long getItemId(int position) {
        return position;
    }

    public void updateAdapter(String searchText, ReturnCallback searchResultCallback) {
        this.searchResultCallback = searchResultCallback;
        getFilter().filter(searchText);
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                FilterResults filterResults = new FilterResults();

                String searchString = charSequence.toString();

                if (searchString.trim().isEmpty()) {
                    friendList.clear();
                    friendList.addAll(orgFriendList);
                } else {
                    List<Friend> tempUserList = new ArrayList<>();

                    for (Friend friend : orgFriendList) {
                        if (friend.getUser().getName() != null &&
                                friend.getUser().getName().toLowerCase().contains(searchString.toLowerCase()))
                            tempUserList.add(friend);
                        else if (friend.getUser().getUsername() != null &&
                                friend.getUser().getUsername().toLowerCase().contains(searchString.toLowerCase()))
                            tempUserList.add(friend);
                    }
                    friendList.clear();
                    friendList.addAll(tempUserList);
                }

                filterResults.values = friendList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                friendList = (ArrayList<Friend>) filterResults.values;
                notifyDataSetChanged();

                if (friendList != null && friendList.size() > 0)
                    searchResultCallback.OnReturn(friendList.size());
                else
                    searchResultCallback.OnReturn(0);
            }
        };
    }

}
