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
import uren.com.myduties.models.User;
import uren.com.myduties.utils.CommonUtils;
import uren.com.myduties.utils.ShapeUtil;
import uren.com.myduties.utils.dataModelUtil.UserDataUtil;


public class FriendVerticalListAdapter extends RecyclerView.Adapter implements Filterable {

    View view;
    private Context context;
    private List<User> friendList;
    private List<User> orginalFriendList;
    private RecyclerView horRecyclerView;
    private boolean horAdapterUpdateChk = false;
    private SelectFriendHorizontalAdapter selectFriendHorizontalAdapter = null;
    private List<User> groupParticipantList;
    private List<User> selectedUsers;

    public static final int VIEW_PROG = 0;
    public static final int VIEW_ITEM = 1;
    public static final int VIEW_NULL = 2;

    public static final int CODE_ADD_ITEM = 0;
    public static final int CODE_REMOVE_ITEM = 1;

    public FriendVerticalListAdapter(Context context, List<User> groupParticipantList) {
        this.context = context;
        this.friendList = new ArrayList<>();
        this.orginalFriendList = new ArrayList<>();
        this.groupParticipantList = groupParticipantList;
        EventBus.getDefault().register(this);
    }

    @Subscribe(sticky = true)
    public void selectedUsersReceived(SelectedUsersBus selectedUsersBus) {
        selectedUsers = selectedUsersBus.getUsers();
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
                .inflate(R.layout.friend_vert_list_item, parent, false);

        viewHolder = new FriendVerticalListAdapter.SelectFriendHolder(itemView);
        horRecyclerView = ((Activity) context).findViewById(R.id.horRecyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        horRecyclerView.setLayoutManager(linearLayoutManager);
        setHorizontalAdapter();

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        User user = friendList.get(position);
        ((SelectFriendHolder) holder).setData(user, position);
    }

    private void setHorizontalAdapter() {
        selectFriendHorizontalAdapter = new SelectFriendHorizontalAdapter(context, new ClickCallback() {
            @Override
            public void onItemClick() {
                FriendVerticalListAdapter.this.notifyDataSetChanged();

                if (selectedUsers.size() == 0)
                    horRecyclerView.setVisibility(View.GONE);
            }
        });
    }

    class SelectFriendHolder extends RecyclerView.ViewHolder {
        AppCompatTextView nameTextView;
        AppCompatTextView usernameTextView;
        TextView shortUserNameTv;
        ImageView profilePicImgView;
        ImageView tickImgv;
        LinearLayout specialListLinearLayout;
        User user;
        AppCompatTextView inGroupTv;
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
            inGroupTv = view.findViewById(R.id.inGroupTv);
            setShapes();

            specialListLinearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CommonUtils.hideKeyBoard(context);
                    if (tickImgv.getVisibility() == View.VISIBLE) {
                        tickImgv.setVisibility(View.GONE);
                        selectedUsers.remove(user);
                        checkHorizontalAdapter(CODE_REMOVE_ITEM);
                    } else {
                        tickImgv.setVisibility(View.VISIBLE);
                        selectedUsers.add(user);
                        checkHorizontalAdapter(CODE_ADD_ITEM);
                    }
                }
            });
        }

        private void setShapes() {
            profilePicImgView.setBackground(ShapeUtil.getShape(context.getResources().getColor(R.color.DodgerBlue),
                    context.getResources().getColor(R.color.Orange), GradientDrawable.OVAL, 50, 0));
            tickImgv.setBackground(ShapeUtil.getShape(context.getResources().getColor(R.color.DarkTurquoise),
                    context.getResources().getColor(R.color.White), GradientDrawable.OVAL, 50, 3));
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
            updateItemEnableValue();
        }

        private void updateItemEnableValue() {
            if (isInParticipantList(user)) {
                specialListLinearLayout.setEnabled(false);
                specialListLinearLayout.setBackgroundColor(context.getResources().getColor(R.color.AliceBlue));
                inGroupTv.setVisibility(View.VISIBLE);
            } else {
                specialListLinearLayout.setEnabled(true);
                specialListLinearLayout.setBackgroundColor(context.getResources().getColor(R.color.White));
                inGroupTv.setVisibility(View.GONE);
            }
        }

        public boolean isUserInList(String userid) {
            if (selectedUsers != null)
                for (User user : selectedUsers)
                    if (user.getUserid().equals(userid))
                        return true;

            return false;
        }

        public void updateTickImgv() {
            if (isUserInList(user.getUserid()))
                tickImgv.setVisibility(View.VISIBLE);
            else
                tickImgv.setVisibility(View.GONE);
        }

        public void checkHorizontalAdapter(int type) {
            if (horAdapterUpdateChk == false) {
                horRecyclerView.setVisibility(View.VISIBLE);
                horRecyclerView.setAdapter(selectFriendHorizontalAdapter);
                horAdapterUpdateChk = true;
            } else {
                horRecyclerView.setAdapter(selectFriendHorizontalAdapter);

                if (selectedUsers.size() == 0) {
                    horRecyclerView.setVisibility(View.GONE);
                } else if (horRecyclerView.getVisibility() == View.GONE) {
                    horRecyclerView.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    public boolean isInParticipantList(User user) {
        if (groupParticipantList != null) {
            for (User user1 : groupParticipantList) {
                if (user.getUserid().equals(user1.getUserid()))
                    return true;
            }
        }
        return false;
    }

    public void updateAdapter(String searchText) {
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

 /*   public void addAll(List<User> addedUserList) {
        if (addedUserList != null) {
            friendList.addAll(addedUserList);
            orginalFriendList.addAll(addedUserList);
            notifyItemRangeInserted(friendList.size(), friendList.size() + addedUserList.size());
        }
    }*/

    public void addUser(User user) {
        if (user != null) {
            friendList.add(user);
            orginalFriendList.add(user);
            notifyItemInserted(friendList.size() - 1);
        }
    }
}