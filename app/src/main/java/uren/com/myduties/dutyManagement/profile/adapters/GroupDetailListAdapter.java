package uren.com.myduties.dutyManagement.profile.adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import uren.com.myduties.R;
import uren.com.myduties.dbManagement.GroupDBHelper;
import uren.com.myduties.evetBusModels.UserBus;
import uren.com.myduties.interfaces.CompleteCallback;
import uren.com.myduties.interfaces.ItemClickListener;
import uren.com.myduties.interfaces.OnCompleteCallback;
import uren.com.myduties.models.Group;
import uren.com.myduties.models.User;
import uren.com.myduties.utils.CommonUtils;
import uren.com.myduties.utils.ShapeUtil;
import uren.com.myduties.utils.dataModelUtil.UserDataUtil;

import static uren.com.myduties.constants.StringConstants.CHAR_AMPERSAND;

public class GroupDetailListAdapter extends RecyclerView.Adapter<GroupDetailListAdapter.GroupDetailListHolder> {

    View view;
    LinearLayout specialListLinearLayout;
    LayoutInflater layoutInflater;
    List<User> groupParticipantList;
    Group group;
    ItemClickListener itemClickListener;

    Context context;
    Activity activity;

    public static final int CODE_REMOVE_FROM_GROUP = 0;
    public static final int CODE_CHANGE_AS_ADMIN = 1;

    TextView textview;
    CardView addFriendCardView;

    int groupAdminPosition = 0;
    GradientDrawable imageShape;
    GradientDrawable adminButtonShape;
    User accountholderUser;

    public GroupDetailListAdapter(Context context, Group group,
                                  ItemClickListener itemClickListener) {
        layoutInflater = LayoutInflater.from(context);
        initVariables();
        this.group = group;
        this.groupParticipantList = new ArrayList<>();
        this.itemClickListener = itemClickListener;
        this.context = context;
        activity = (Activity) context;
        imageShape = ShapeUtil.getShape(context.getResources().getColor(R.color.DodgerBlue),
                0, GradientDrawable.OVAL, 50, 0);
        adminButtonShape = ShapeUtil.getShape(context.getResources().getColor(R.color.White),
                context.getResources().getColor(R.color.MediumSeaGreen), GradientDrawable.RECTANGLE, 15, 2);
        EventBus.getDefault().register(this);
    }

    @Subscribe(sticky = true)
    public void userReceived(UserBus userBus) {
        accountholderUser = userBus.getUser();
    }

    public void initVariables() {
        this.groupParticipantList = new ArrayList<>();
    }

    @Override
    public GroupDetailListAdapter.GroupDetailListHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        GroupDetailListHolder holder;
        view = layoutInflater.inflate(R.layout.group_detail_list, parent, false);
        holder = new GroupDetailListHolder(view);

        textview = activity.findViewById(R.id.personCntTv);
        textview.setText(Integer.toString(groupParticipantList.size()));

        addFriendCardView = activity.findViewById(R.id.addFriendCardView);

        return holder;
    }

    class GroupDetailListHolder extends RecyclerView.ViewHolder {

        AppCompatTextView profileName;
        AppCompatTextView profileUserName;
        TextView shortUsernameTv;
        User user;
        Button adminDisplayBtn;
        ImageView specialProfileImgView;
        int position = 0;

        public GroupDetailListHolder(View itemView) {
            super(itemView);

            specialProfileImgView = view.findViewById(R.id.specialPictureImgView);
            profileName = view.findViewById(R.id.profileName);
            shortUsernameTv = view.findViewById(R.id.shortUsernameTv);
            profileUserName = view.findViewById(R.id.profileUserName);
            specialListLinearLayout = view.findViewById(R.id.specialListLinearLayout);
            adminDisplayBtn = view.findViewById(R.id.adminDisplayBtn);
            specialProfileImgView.setBackground(imageShape);
            adminDisplayBtn.setBackground(adminButtonShape);

            specialListLinearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(group.getGroupAdmin().equals(accountholderUser.getUserid()) &&
                        !accountholderUser.getUserid().equals(user.getUserid())){
                        final ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1);

                        if (accountholderUser.getUserid().equals(group.getGroupAdmin()))
                            adapter.add(context.getResources().getString(R.string.removeFromGroup));

                        if (accountholderUser.getUserid().equals(group.getGroupAdmin()))
                            adapter.add(context.getResources().getString(R.string.changeAsAdmin));

                        AlertDialog.Builder builder = new AlertDialog.Builder(context);

                        builder.setTitle(getAlertDialogTitle());

                        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int item) {

                                if (item == CODE_REMOVE_FROM_GROUP)
                                    exitFromGroup(user.getUserid());
                                else if (item == CODE_CHANGE_AS_ADMIN)
                                    changeAdministrator(user.getUserid());
                            }
                        });

                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                }
            });
        }

        public String getAlertDialogTitle() {
            String title = "";
            if (user.getName() != null && !user.getName().isEmpty())
                title = user.getName();
            else if (user.getUsername() != null && !user.getUsername().isEmpty())
                title = CHAR_AMPERSAND + user.getUsername();
            return title;
        }


        public void setData(User user, int position) {
            this.position = position;
            this.user = user;
            UserDataUtil.setName(user.getName(), profileName);
            UserDataUtil.setUsername(user.getUsername(), profileUserName);
            setGroupAdmin();
            UserDataUtil.setProfilePicture(context, user.getProfilePhotoUrl(),
                    user.getName(), user.getUsername(),
                    shortUsernameTv, specialProfileImgView, false);
        }

        public void setGroupAdmin() {
            if (group.getGroupAdmin() != null && !group.getGroupAdmin().trim().isEmpty() &&
                    user.getUserid() != null && !user.getUserid().trim().isEmpty()) {
                if (group.getGroupAdmin().equals(user.getUserid())) {
                    adminDisplayBtn.setVisibility(View.VISIBLE);
                    groupAdminPosition = position;
                } else
                    adminDisplayBtn.setVisibility(View.GONE);
            } else
                adminDisplayBtn.setVisibility(View.GONE);
        }

        public void exitFromGroup(final String userid) {

            GroupDBHelper.exitUserFromGroup(userid, group.getGroupid(), new CompleteCallback() {
                @Override
                public void onComplete(Object object) {
                    groupParticipantList.remove(position);
                    itemClickListener.onClick(groupParticipantList, CODE_REMOVE_FROM_GROUP);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, getItemCount());
                    textview.setText(Integer.toString(getItemCount()));
                }

                @Override
                public void onFailed(String message) {
                    CommonUtils.showToastShort(context, message);
                }
            });
        }

        public void changeAdministrator(final String userid) {

            GroupDBHelper.changeAdministrator(userid, group.getGroupid(), new OnCompleteCallback() {
                @Override
                public void OnCompleted() {
                    addFriendCardView.setVisibility(View.GONE);
                    group.setGroupAdmin(userid);
                    notifyItemChanged(position);
                    notifyItemChanged(groupAdminPosition);
                    itemClickListener.onClick(group, CODE_CHANGE_AS_ADMIN);
                }

                @Override
                public void OnFailed(String message) {
                    CommonUtils.showToastShort(context, message);
                }
            });
        }
    }

    public List<User> getGroupParticipantList(){
        return groupParticipantList;
    }

    public void addFriend(User user) {
        if (user != null) {
            groupParticipantList.add(user);
            notifyItemInserted(groupParticipantList.size() - 1);
        }
    }

    @Override
    public void onBindViewHolder(GroupDetailListAdapter.GroupDetailListHolder holder, int position) {
        User user = groupParticipantList.get(position);
        holder.setData(user, position);
    }

    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return groupParticipantList.size();
    }

}