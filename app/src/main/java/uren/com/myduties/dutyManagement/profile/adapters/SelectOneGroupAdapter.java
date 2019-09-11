package uren.com.myduties.dutyManagement.profile.adapters;


import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import uren.com.myduties.R;
import uren.com.myduties.evetBusModels.UserBus;
import uren.com.myduties.interfaces.ReturnCallback;
import uren.com.myduties.models.Group;
import uren.com.myduties.models.User;
import uren.com.myduties.utils.ShapeUtil;

import static uren.com.myduties.constants.NumericConstants.GROUP_NAME_MAX_LENGTH;

public class SelectOneGroupAdapter extends RecyclerView.Adapter<SelectOneGroupAdapter.UserGroupsListHolder> implements Filterable {

    private View view;
    private LayoutInflater layoutInflater;
    private Context context;
    private List<Group> groupList;
    private List<Group> orgGroupList;
    private String operationType;
    private Group selectedGroup;
    private ReturnCallback returnCallback;
    private ReturnCallback searchResultCallback;
    private User accountholderUser;

    private int beforeSelectedPosition = -1;

    public SelectOneGroupAdapter(Context context, ReturnCallback returnCallback) {
        layoutInflater = LayoutInflater.from(context);
        this.groupList = new ArrayList<>();
        this.orgGroupList = new ArrayList<>();
        this.returnCallback = returnCallback;
        this.context = context;
        this.operationType = operationType;
        EventBus.getDefault().register(this);
    }

    @Subscribe(sticky = true)
    public void userReceived(UserBus userBus) {
        accountholderUser = userBus.getUser();
    }

    @Override
    public SelectOneGroupAdapter.UserGroupsListHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        view = layoutInflater.inflate(R.layout.one_group_select_list_item, parent, false);
        SelectOneGroupAdapter.UserGroupsListHolder holder = new SelectOneGroupAdapter.UserGroupsListHolder(view);
        return holder;
    }

    class UserGroupsListHolder extends RecyclerView.ViewHolder {

        TextView groupnameTextView;
        TextView shortGroupNameTv;
        ImageView groupPicImgView;
        Button adminDisplayButton;
        LinearLayout groupSelectMainLinLay;
        ImageView tickImgv;
        Group group;
        LinearLayout morell;
        int position = 0;

        public UserGroupsListHolder(final View itemView) {
            super(itemView);

            groupPicImgView = view.findViewById(R.id.groupPicImgView);
            groupnameTextView = view.findViewById(R.id.groupnameTextView);
            adminDisplayButton = view.findViewById(R.id.adminDisplayButton);
            groupSelectMainLinLay = view.findViewById(R.id.groupSelectMainLinLay);
            tickImgv = view.findViewById(R.id.tickImgv);
            shortGroupNameTv = view.findViewById(R.id.shortGroupNameTv);
            morell = view.findViewById(R.id.morell);
            setShapes();

            groupSelectMainLinLay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    manageSelectedItem();
                }
            });
        }

        private void setShapes() {
            tickImgv.setBackground(ShapeUtil.getShape(context.getResources().getColor(R.color.DarkTurquoise, null),
                    context.getResources().getColor(R.color.White, null), GradientDrawable.OVAL, 50, 3));
            adminDisplayButton.setBackground(ShapeUtil.getShape(context.getResources().getColor(R.color.White, null),
                    context.getResources().getColor(R.color.MediumSeaGreen, null), GradientDrawable.RECTANGLE, 15, 2));
            groupPicImgView.setBackground(ShapeUtil.getShape(context.getResources().getColor(R.color.DodgerBlue, null),
                    0, GradientDrawable.OVAL, 50, 0));
        }

        public void manageSelectedItem() {
            selectedGroup = group;
            notifyItemChanged(position);

            if (beforeSelectedPosition > -1)
                notifyItemChanged(beforeSelectedPosition);

            beforeSelectedPosition = position;
            returnCallback.OnReturn(group);
        }

        public void setData(Group group, int position) {
            this.group = group;
            this.position = position;
            setGroupName();
            setGroupPhoto();
            setAdminButtonValues();
            updateTickImgv();
        }

        public void setGroupName() {
            if (group.getName() != null && !group.getName().trim().isEmpty()) {
                if (group.getName().trim().length() > GROUP_NAME_MAX_LENGTH)
                    this.groupnameTextView.setText(group.getName().trim().substring(0, GROUP_NAME_MAX_LENGTH) + "...");
                else
                    this.groupnameTextView.setText(group.getName());
            }
        }

        public void setGroupPhoto() {
            if (group.getGroupPhotoUrl() != null && !group.getGroupPhotoUrl().trim().isEmpty()) {
                shortGroupNameTv.setVisibility(View.GONE);
                Glide.with(context)
                        .load(group.getGroupPhotoUrl())
                        .apply(RequestOptions.circleCropTransform())
                        .into(groupPicImgView);
            } else {
                if (group.getName() != null && !group.getName().trim().isEmpty()) {
                    shortGroupNameTv.setVisibility(View.VISIBLE);
                    shortGroupNameTv.setText(getShortGroupName());
                    groupPicImgView.setImageDrawable(null);
                } else {
                    shortGroupNameTv.setVisibility(View.GONE);
                    Glide.with(context)
                            .load(R.drawable.icon_user_groups)
                            .apply(RequestOptions.circleCropTransform())
                            .into(groupPicImgView);
                }
            }
        }

        public String getShortGroupName() {
            String returnValue = "";
            String[] seperatedName = group.getName().trim().split(" ");
            for (String word : seperatedName) {
                if (returnValue.length() < 3)
                    returnValue = returnValue + word.substring(0, 1).toUpperCase();
            }
            return returnValue;
        }

        public void updateTickImgv() {
            if (selectedGroup != null && group != null) {
                if (selectedGroup.getGroupid().equals(group.getGroupid()))
                    tickImgv.setVisibility(View.VISIBLE);
                else
                    tickImgv.setVisibility(View.GONE);
            }
        }

        public void setAdminButtonValues() {
            if (group.getGroupAdmin().equals(accountholderUser.getUserid())) {
                adminDisplayButton.setText(context.getResources().getString(R.string.adminText));
                adminDisplayButton.setVisibility(View.VISIBLE);
            } else
                adminDisplayButton.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBindViewHolder(SelectOneGroupAdapter.UserGroupsListHolder holder, int position) {
        Group group = groupList.get(position);
        holder.setData(group, position);
    }

    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        if (groupList != null)
            return groupList.size();
        else
            return 0;
    }

    public void addGroup(Group group) {
        if (groupList != null) {
            groupList.add(group);
            orgGroupList.add(group);
            notifyItemInserted(groupList.size() - 1);
        }
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

                if (searchString.trim().isEmpty())
                    groupList = orgGroupList;
                else {
                    List<Group> tempList = new ArrayList<>();

                    for (Group item : orgGroupList) {
                        if (item.getName().toLowerCase().contains(searchString.toLowerCase()))
                            tempList.add(item);
                    }
                    groupList = tempList;
                }

                filterResults.values = groupList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                groupList = (List<Group>) filterResults.values;
                notifyDataSetChanged();

                if (groupList != null  && groupList.size() > 0)
                    searchResultCallback.OnReturn(groupList.size());
                else
                    searchResultCallback.OnReturn(0);
            }
        };
    }
}