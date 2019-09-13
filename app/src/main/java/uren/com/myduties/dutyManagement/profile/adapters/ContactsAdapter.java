package uren.com.myduties.dutyManagement.profile.adapters;


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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import uren.com.myduties.R;
import uren.com.myduties.dutyManagement.profile.interfaces.ContactFriendSelectCallback;
import uren.com.myduties.evetBusModels.UserBus;
import uren.com.myduties.interfaces.ReturnCallback;
import uren.com.myduties.models.Contact;
import uren.com.myduties.models.User;
import uren.com.myduties.utils.ShapeUtil;
import uren.com.myduties.utils.dataModelUtil.UserDataUtil;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactsHolder> implements Filterable {


    private View view;
    private LayoutInflater layoutInflater;
    private Context context;
    private GradientDrawable imageShape;
    private GradientDrawable buttonShape;
    private ReturnCallback returnCallback;
    private List<Contact> contactFriendModelList;
    private List<Contact> orgContactFriendModelList;
    private ContactFriendSelectCallback contactFriendSelectCallback;
    private User accountholderUser;

    public ContactsAdapter(Context context, List<Contact> contactFriendModelList, ContactFriendSelectCallback contactFriendSelectCallback) {
        layoutInflater = LayoutInflater.from(context);
        this.context = context;
        this.contactFriendModelList = contactFriendModelList;
        this.orgContactFriendModelList = contactFriendModelList;
        this.contactFriendSelectCallback = contactFriendSelectCallback;
        imageShape = ShapeUtil.getShape(context.getResources().getColor(R.color.DodgerBlue, null),
                0, GradientDrawable.OVAL, 50, 0);
        EventBus.getDefault().register(this);
    }

    @Subscribe(sticky = true)
    public void userReceived(UserBus userBus) {
        accountholderUser = userBus.getUser();
    }

    @NonNull
    @Override
    public ContactsAdapter.ContactsHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        ContactsAdapter.ContactsHolder holder;
        view = layoutInflater.inflate(R.layout.person_vert_list_item, viewGroup, false);
        holder = new ContactsHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ContactsAdapter.ContactsHolder myViewHolder, int position) {
        Contact contact = contactFriendModelList.get(position);
        myViewHolder.setData(contact, position);
    }


    class ContactsHolder extends RecyclerView.ViewHolder {
        AppCompatTextView nameTextView;
        TextView shortenTextView;
        AppCompatTextView phoneNumTextView;
        ImageView profilePicImgView;
        Contact contactFriendModel;
        Button statuDisplayBtn;

        int position = 0;

        public ContactsHolder(final View itemView) {
            super(itemView);

            profilePicImgView = view.findViewById(R.id.profilePicImgView);
            nameTextView = view.findViewById(R.id.nameTextView);
            phoneNumTextView = view.findViewById(R.id.phoneNumTextView);
            statuDisplayBtn = view.findViewById(R.id.statuDisplayBtn);
            shortenTextView = view.findViewById(R.id.shortenTextView);
            phoneNumTextView.setVisibility(View.VISIBLE);
            profilePicImgView.setBackground(imageShape);
            statuDisplayBtn.setBackground(buttonShape);

            statuDisplayBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    statuDisplayBtn.setEnabled(false);
                    statuDisplayBtn.startAnimation(AnimationUtils.loadAnimation(context, R.anim.image_click));
                    contactFriendSelectCallback.contactSelected(contactFriendModel);
                }
            });
        }

        public void setData(Contact contactFriendModel, int position) {
            this.position = position;
            this.contactFriendModel = contactFriendModel;
            setPhoneNum();
            setNameAndProfilePicture();
            setDisplayButton();
        }

        public void setPhoneNum() {
            if (contactFriendModel != null &&
                    contactFriendModel.getPhoneNumber() != null && !contactFriendModel.getPhoneNumber().isEmpty())
                this.phoneNumTextView.setText(contactFriendModel.getPhoneNumber());
        }

        public void setNameAndProfilePicture() {
            if (contactFriendModel != null) {
                UserDataUtil.setName(contactFriendModel.getName(), nameTextView);
                UserDataUtil.setProfilePicture(context, null, contactFriendModel.getName(),
                        contactFriendModel.getName(), shortenTextView, profilePicImgView, false);
            }
        }

        public void setDisplayButton() {
            if (contactFriendModel != null) {
                UserDataUtil.updateInviteButton(context, statuDisplayBtn, false);
            }
        }
    }

    public long getItemId(int position) {
        return position;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {

                FilterResults filterResults;
                String searchString = charSequence.toString();

                if (searchString.trim().isEmpty())
                    contactFriendModelList = orgContactFriendModelList;
                else {
                    List<Contact> tempList = new ArrayList<>();

                    for (Contact contactFriendModel : orgContactFriendModelList) {
                        if (contactFriendModel != null) {
                            if (contactFriendModel.getName() != null &&
                                    contactFriendModel.getName().toLowerCase().contains(searchString.toLowerCase())) {
                                tempList.add(contactFriendModel);
                            }
                        }
                    }
                    contactFriendModelList = tempList;
                }

                filterResults = new FilterResults();
                filterResults.values = contactFriendModelList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                contactFriendModelList = (List<Contact>) filterResults.values;
                notifyDataSetChanged();

                if (contactFriendModelList != null && contactFriendModelList.size() > 0)
                    returnCallback.OnReturn(contactFriendModelList.size());
                else
                    returnCallback.OnReturn(0);
            }
        };
    }

    public void updateAdapter(String searchText, ReturnCallback returnCallback) {
        this.returnCallback = returnCallback;
        getFilter().filter(searchText);
    }

    @Override
    public int getItemCount() {
        int size;
        size = contactFriendModelList.size();
        return size;
    }
}