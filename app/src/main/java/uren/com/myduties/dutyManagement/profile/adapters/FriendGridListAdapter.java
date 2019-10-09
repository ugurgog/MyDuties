package uren.com.myduties.dutyManagement.profile.adapters;


import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;

import uren.com.myduties.R;
import uren.com.myduties.evetBusModels.SelectedUsersBus;
import uren.com.myduties.interfaces.ReturnCallback;
import uren.com.myduties.models.User;
import uren.com.myduties.utils.ShapeUtil;
import uren.com.myduties.utils.dataModelUtil.UserDataUtil;

import static uren.com.myduties.constants.StringConstants.CHAR_AMPERSAND;


public class FriendGridListAdapter extends RecyclerView.Adapter<FriendGridListAdapter.FriendGridListHolder> {
    View view;
    LayoutInflater layoutInflater;
    Context context;
    GradientDrawable imageShape;
    GradientDrawable deleteShape;
    ReturnCallback returnCallback;

    private List<User> selectedUsers;

    public FriendGridListAdapter(Context context, ReturnCallback returnCallback) {
        layoutInflater = LayoutInflater.from(context);
        this.context = context;
        this.returnCallback = returnCallback;
        imageShape = ShapeUtil.getShape(context.getResources().getColor(R.color.DodgerBlue),
                0, GradientDrawable.OVAL, 50, 0);
        deleteShape = ShapeUtil.getShape(context.getResources().getColor(R.color.White),
                0, GradientDrawable.OVAL, 50, 0);
        EventBus.getDefault().register(this);
    }

    @Subscribe(sticky = true)
    public void selectedUsersReceived(SelectedUsersBus selectedUsersBus) {
        selectedUsers = selectedUsersBus.getUsers();
    }

    public Object getItem(int position) {
        return position;
    }

    @Override
    public FriendGridListAdapter.FriendGridListHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        view = layoutInflater.inflate(R.layout.special_grid_list_item, parent, false);
        FriendGridListAdapter.FriendGridListHolder holder = new FriendGridListAdapter.FriendGridListHolder(view);
        return holder;
    }

    class FriendGridListHolder extends RecyclerView.ViewHolder {

        TextView userNameSurname;
        TextView shortUserNameTv;
        User selectedFriend;
        ImageView deletePersonImgv;
        ImageView specialProfileImgView;
        int position = 0;

        public FriendGridListHolder(View itemView) {
            super(itemView);

            specialProfileImgView = view.findViewById(R.id.specialPictureImgView);
            userNameSurname = view.findViewById(R.id.specialNameTextView);
            deletePersonImgv = view.findViewById(R.id.deletePersonImgv);
            shortUserNameTv = view.findViewById(R.id.shortUserNameTv);
            specialProfileImgView.setBackground(imageShape);
            deletePersonImgv.setBackground(deleteShape);

            deletePersonImgv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    removeItem(position);
                    returnCallback.OnReturn(getItemCount());
                }
            });
        }

        private void removeItem(int position) {
            selectedUsers.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, getItemCount());
        }

        public void setData(User selectedFriend, int position) {
            this.position = position;
            this.selectedFriend = selectedFriend;
            setProfileName();
            UserDataUtil.setProfilePicture(context, selectedFriend.getProfilePhotoUrl(),
                    selectedFriend.getName(), selectedFriend.getUsername(),
                    shortUserNameTv, specialProfileImgView, true);
        }

        public void setProfileName() {
            if (selectedFriend.getName() != null && !selectedFriend.getName().isEmpty())
                UserDataUtil.setName(selectedFriend.getName(), userNameSurname);
            else if (selectedFriend.getUsername() != null && !selectedFriend.getUsername().isEmpty())
                UserDataUtil.setName(CHAR_AMPERSAND + selectedFriend.getUsername(), userNameSurname);
        }
    }

    @Override
    public void onBindViewHolder(FriendGridListAdapter.FriendGridListHolder holder, int position) {
        User selectedFriend = selectedUsers.get(position);
        holder.setData(selectedFriend, position);
    }

    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return selectedUsers.size();
    }
}