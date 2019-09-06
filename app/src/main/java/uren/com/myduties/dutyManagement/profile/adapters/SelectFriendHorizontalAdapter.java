package uren.com.myduties.dutyManagement.profile.adapters;


import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;

import uren.com.myduties.R;
import uren.com.myduties.dutyManagement.profile.interfaces.ClickCallback;
import uren.com.myduties.evetBusModels.SelectedUsersBus;
import uren.com.myduties.models.User;
import uren.com.myduties.utils.ShapeUtil;
import uren.com.myduties.utils.dataModelUtil.UserDataUtil;

import static uren.com.myduties.constants.StringConstants.CHAR_AMPERSAND;


public class SelectFriendHorizontalAdapter extends RecyclerView.Adapter<SelectFriendHorizontalAdapter.SelectFriendHorizontalHolder> {

    View view;
    LayoutInflater layoutInflater;
    Context context;
    Activity activity;
    ClickCallback clickCallback;
    GradientDrawable imageShape;
    GradientDrawable deleteImgvShape;
    private List<User> selectedUsers;

    public SelectFriendHorizontalAdapter(Context context, ClickCallback clickCallback) {
            layoutInflater = LayoutInflater.from(context);
            this.context = context;
            this.clickCallback = clickCallback;
            activity = (Activity) context;
            imageShape = ShapeUtil.getShape(context.getResources().getColor(R.color.DodgerBlue, null),
                    0, GradientDrawable.OVAL, 50, 0);
            deleteImgvShape = ShapeUtil.getShape(context.getResources().getColor(R.color.White, null),
                    context.getResources().getColor(R.color.White, null), GradientDrawable.OVAL, 50, 0);
        EventBus.getDefault().register(this);
    }

    @Subscribe(sticky = true)
    public void selectedUsersReceived(SelectedUsersBus selectedUsersBus){
        selectedUsers = selectedUsersBus.getUsers();
    }

    @NonNull
    @Override
    public SelectFriendHorizontalAdapter.SelectFriendHorizontalHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        view = layoutInflater.inflate(R.layout.grid_list_item_small, viewGroup, false);
        final SelectFriendHorizontalAdapter.SelectFriendHorizontalHolder holder = new SelectFriendHorizontalAdapter.SelectFriendHorizontalHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull SelectFriendHorizontalAdapter.SelectFriendHorizontalHolder myViewHolder, int position) {

        User user = selectedUsers.get(position);
        myViewHolder.setData(user, position);
    }

    class SelectFriendHorizontalHolder extends RecyclerView.ViewHolder {

        ImageView specialPictureImgView;
        ImageView deletePersonImgv;
        TextView specialNameTextView;
        TextView shortenTextView;
        User user;
        int position = 0;

        public SelectFriendHorizontalHolder(final View itemView) {
            super(itemView);

                specialPictureImgView = view.findViewById(R.id.specialPictureImgView);
                deletePersonImgv = view.findViewById(R.id.deletePersonImgv);
                specialNameTextView = view.findViewById(R.id.specialNameTextView);
                shortenTextView = view.findViewById(R.id.shortenTextView);
                specialPictureImgView.setBackground(imageShape);
                deletePersonImgv.setBackground(deleteImgvShape);

                specialPictureImgView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        removeItem(position);
                        clickCallback.onItemClick();
                    }
                });

                deletePersonImgv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        removeItem(position);
                        clickCallback.onItemClick();
                    }
                });
        }

        private void removeItem(int position) {
                selectedUsers.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, getItemCount());
        }

        public void setData(User user, int position) {
                this.position = position;
                this.user = user;
                setProfileName();
                UserDataUtil.setProfilePicture(context, user.getProfilePhotoUrl(),
                        user.getName(), user.getUsername(),
                        shortenTextView, specialPictureImgView, false);
        }

        public void setProfileName(){
                if(user.getName() != null && !user.getName().isEmpty())
                    UserDataUtil.setName(user.getName(), specialNameTextView);
                else if(user.getUsername() != null && !user.getUsername().isEmpty())
                    UserDataUtil.setName(CHAR_AMPERSAND + user.getUsername(), specialNameTextView);
        }
    }

    @Override
    public int getItemCount() {
        int size = 0;
        try {
            size = selectedUsers.size();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return size;
    }
}