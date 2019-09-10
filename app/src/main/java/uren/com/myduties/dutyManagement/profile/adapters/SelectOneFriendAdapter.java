package uren.com.myduties.dutyManagement.profile.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import uren.com.myduties.R;
import uren.com.myduties.dutyManagement.profile.interfaces.ClickCallback;
import uren.com.myduties.evetBusModels.SelectedUsersBus;
import uren.com.myduties.interfaces.ReturnCallback;
import uren.com.myduties.interfaces.ReturnObjectListener;
import uren.com.myduties.models.User;
import uren.com.myduties.utils.CommonUtils;
import uren.com.myduties.utils.ShapeUtil;
import uren.com.myduties.utils.dataModelUtil.UserDataUtil;

public class SelectOneFriendAdapter extends RecyclerView.Adapter implements Filterable {

    View view;
    private Context context;
    private List<User> friendList;
    private List<User> orginalFriendList;
    private User selectedUser;

    public static final int VIEW_PROG = 0;
    public static final int VIEW_ITEM = 1;
    public static final int VIEW_NULL = 2;

    private int beforeSelectedPosition = -1;
    private ReturnCallback returnCallback;
    private ReturnObjectListener returnObjectListener;

    public SelectOneFriendAdapter(Context context, ReturnCallback returnCallback) {
        this.context = context;
        this.friendList = new ArrayList<>();
        this.orginalFriendList = new ArrayList<>();
        this.returnCallback = returnCallback;
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
                    .inflate(R.layout.one_friend_select_list_item, parent, false);

            viewHolder = new SelectOneFriendAdapter.SelectFriendHolder(itemView);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
            linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.progressbar_item, parent, false);

            viewHolder = new SelectOneFriendAdapter.ProgressViewHolder(v);
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof SelectFriendHolder) {
            User user = friendList.get(position);
            ((SelectFriendHolder) holder).setData(user, position);
        } else {
            ((ProgressViewHolder) holder).progressBar.setIndeterminate(true);
        }
    }

    class SelectFriendHolder extends RecyclerView.ViewHolder {
        AppCompatTextView nameTextView;
        AppCompatTextView usernameTextView;
        TextView shortUserNameTv;
        ImageView profilePicImgView;
        ImageView tickImgv;
        LinearLayout specialListLinearLayout;
        User user;
        int position = 0;

        public SelectFriendHolder(final View itemView) {
            super(itemView);

            view = itemView;
            profilePicImgView = view.findViewById(R.id.profilePicImgView);
            nameTextView = view.findViewById(R.id.nameTextView);
            usernameTextView = view.findViewById(R.id.usernameTextView);
            specialListLinearLayout = view.findViewById(R.id.specialListLinearLayout);
            shortUserNameTv = view.findViewById(R.id.shortUserNameTv);
            tickImgv = view.findViewById(R.id.tickImgv);
            setShapes();

            specialListLinearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CommonUtils.hideKeyBoard(context);
                    manageSelectedItem();
                }
            });
        }

        private void setShapes() {
            profilePicImgView.setBackground(ShapeUtil.getShape(context.getResources().getColor(R.color.DodgerBlue, null),
                    context.getResources().getColor(R.color.Orange, null), GradientDrawable.OVAL, 50, 0));
            tickImgv.setBackground(ShapeUtil.getShape(context.getResources().getColor(R.color.DarkTurquoise, null),
                    context.getResources().getColor(R.color.White, null), GradientDrawable.OVAL, 50, 3));
        }

        public void manageSelectedItem() {
            selectedUser = user;
            notifyItemChanged(position);

            if (beforeSelectedPosition > -1)
                notifyItemChanged(beforeSelectedPosition);

            beforeSelectedPosition = position;
            returnCallback.OnReturn(selectedUser);
        }

        public void setData(User user, int position) {
            this.position = position;
            this.user = user;
            UserDataUtil.setName(user.getName(), nameTextView);
            UserDataUtil.setUsername(user.getUsername(), usernameTextView);
            UserDataUtil.setProfilePicture(context, user.getProfilePhotoUrl(),
                    user.getName(), user.getUsername(), shortUserNameTv, profilePicImgView
                    , false);
            updateTickImgv();
        }


        public void updateTickImgv() {
            if (selectedUser != null && user != null) {
                if (selectedUser.getUserid().equals(user.getUserid()))
                    tickImgv.setVisibility(View.VISIBLE);
                else
                    tickImgv.setVisibility(View.GONE);
            }else
                tickImgv.setVisibility(View.GONE);
        }
    }

    public void updateAdapter(String searchText, ReturnObjectListener returnObjectListener) {
        this.returnObjectListener = returnObjectListener;
        getFilter().filter(searchText);
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                FilterResults filterResults = new FilterResults();

                String searchString = charSequence.toString();
                if (searchString.trim().isEmpty())
                    friendList = orginalFriendList;
                else {
                    List<User> tempuserList = new ArrayList<>();

                    for (User user : orginalFriendList) {
                        if (user.getName() != null &&
                                user.getName().toLowerCase().contains(searchString.toLowerCase()))
                            tempuserList.add(user);
                        else if (user.getUsername() != null &&
                                user.getUsername().toLowerCase().contains(searchString.toLowerCase()))
                            tempuserList.add(user);
                    }
                    friendList = tempuserList;
                }

                filterResults.values = friendList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                friendList = (List<User>) filterResults.values;
                notifyDataSetChanged();

                if (friendList != null && friendList.size() > 0)
                    returnObjectListener.OnReturn(friendList.size());
                else
                    returnObjectListener.OnReturn(0);
            }
        };
    }

    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return ((friendList != null) ? friendList.size() : 0);
    }

    public void addAll(List<User> addedUserList) {
        if (addedUserList != null) {
            friendList.addAll(addedUserList);
            orginalFriendList.addAll(addedUserList);
            notifyItemRangeInserted(friendList.size(), friendList.size() + addedUserList.size());
        }
    }

    public void addUser(User user) {
        if (user != null) {
            friendList.add(user);
            orginalFriendList.add(user);
            notifyItemInserted(friendList.size() - 1);
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

    public boolean isShowingProgressLoading() {
        return getItemViewType(friendList.size() - 1) == VIEW_PROG;
    }

    public static class ProgressViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public ProgressViewHolder(View v) {
            super(v);
            progressBar = v.findViewById(R.id.progressBarLoading);
        }
    }
}