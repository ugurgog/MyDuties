package uren.com.myduties.dutyManagement.profile;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import uren.com.myduties.R;
import uren.com.myduties.dutyManagement.BaseFragment;
import uren.com.myduties.dutyManagement.NextActivity;
import uren.com.myduties.dutyManagement.profile.adapters.ContactsAdapter;
import uren.com.myduties.dutyManagement.profile.helper.ContactListHelper;
import uren.com.myduties.dutyManagement.profile.helper.PhoneNumberFormatUtil;
import uren.com.myduties.dutyManagement.profile.interfaces.ContactFriendSelectCallback;
import uren.com.myduties.interfaces.CompleteCallback;
import uren.com.myduties.interfaces.ReturnCallback;
import uren.com.myduties.models.Contact;
import uren.com.myduties.utils.CommonUtils;
import uren.com.myduties.utils.DynamicLinkUtil;
import uren.com.myduties.utils.PermissionModule;

import static uren.com.myduties.constants.StringConstants.ANIMATE_RIGHT_TO_LEFT;


@SuppressLint("ValidFragment")
public class ContactsFragment extends BaseFragment {

    View mView;

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.toolbarLayout)
    LinearLayout toolbarLayout;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.editTextSearch)
    EditText editTextSearch;
    @BindView(R.id.imgCancelSearch)
    ImageView imgCancelSearch;
    @BindView(R.id.searchToolbarBackImgv)
    ImageView searchToolbarBackImgv;
    @BindView(R.id.warningMsgLayout)
    LinearLayout warningMsgLayout;
    @BindView(R.id.warningMsgTv)
    AppCompatTextView warningMsgTv;
    @BindView(R.id.searchToolbarAddItemImgv)
    ImageView searchToolbarAddItemImgv;

    PermissionModule permissionModule;
    ContactsAdapter contactsAdapter;
    List<Contact> reformedContactList;
    List<Contact> inviteContactsList;

    boolean showTollbar;
    boolean edittextFocused = false;
    Fragment fragment;

    public ContactsFragment(boolean showTollbar) {
        this.showTollbar = showTollbar;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((NextActivity) Objects.requireNonNull(getActivity())).ANIMATION_TAG = ANIMATE_RIGHT_TO_LEFT;
        if (mView == null) {
            mView = inflater.inflate(R.layout.fragment_contacts, container, false);
            ButterKnife.bind(this, mView);
            initVariables();
            checkToolbarVisibility();
            addListeners();
            getContactList();
        }
        return mView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

    }

    public void initVariables() {
        searchToolbarAddItemImgv.setVisibility(View.GONE);
        permissionModule = new PermissionModule(getActivity());
        reformedContactList = new ArrayList<>();
        inviteContactsList = new ArrayList<>();
        fragment = this;
    }

    private void checkToolbarVisibility() {
        if (showTollbar)
            toolbarLayout.setVisibility(View.VISIBLE);
    }

    private void addListeners() {
        searchToolbarBackImgv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edittextFocused) {
                    CommonUtils.hideKeyBoard(Objects.requireNonNull(getContext()));
                    searchToolbarBackImgv.setVisibility(View.GONE);
                    if (editTextSearch != null)
                        editTextSearch.setText("");
                } else {
                    Objects.requireNonNull(getActivity()).onBackPressed();
                }
            }
        });

        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && !s.toString().trim().isEmpty()) {
                    updateAdapter(s.toString());
                    imgCancelSearch.setVisibility(View.VISIBLE);
                } else {
                    updateAdapter("");
                    imgCancelSearch.setVisibility(View.GONE);
                }
            }
        });

        editTextSearch.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    searchToolbarBackImgv.setVisibility(View.VISIBLE);
                    edittextFocused = true;
                } else {
                    searchToolbarBackImgv.setVisibility(View.GONE);
                    edittextFocused = false;
                }
            }
        });

        editTextSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchToolbarBackImgv.setVisibility(View.VISIBLE);
            }
        });

        imgCancelSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editTextSearch != null)
                    editTextSearch.setText("");
                imgCancelSearch.setVisibility(View.GONE);
                CommonUtils.hideKeyBoard(Objects.requireNonNull(getContext()));
                searchToolbarBackImgv.setVisibility(View.GONE);
            }
        });
    }

    public void getContactList() {
        if (!permissionModule.checkReadContactsPermission()) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PermissionModule.PERMISSION_READ_CONTACTS);
        } else {
            startGetContactList();
        }
    }

    public void startGetContactList() {
        progressBar.setVisibility(View.VISIBLE);
        ContactListHelper.getContactList(Objects.requireNonNull(getActivity()), new CompleteCallback() {
            @Override
            public void onComplete(Object object) {
                List<Contact> contactList = (List<Contact>) object;

                PhoneNumberFormatUtil.reformPhoneList(contactList, getActivity(), new CompleteCallback() {
                    @Override
                    public void onComplete(Object object) {
                        clearDuplicateNumbers((List<Contact>) object);
                        setInviteFriendsAdapter();
                    }

                    @Override
                    public void onFailed(String message) {
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void onFailed(String message) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    public void clearDuplicateNumbers(List<Contact> tempContactList) {
        for (Contact contact : tempContactList) {

            boolean isExist = false;

            for (Contact tempContact : reformedContactList) {
                if (tempContact != null && tempContact.getPhoneNumber() != null) {
                    if (contact.getPhoneNumber().trim().equals(tempContact.getPhoneNumber().trim())) {
                        isExist = true;
                        break;
                    }
                }
            }

            if (!isExist)
                reformedContactList.add(contact);
        }
    }

    public void setInviteFriendsAdapter() {

        if (reformedContactList != null && reformedContactList.size() == 0) {
            warningMsgLayout.setVisibility(View.VISIBLE);
            warningMsgTv.setText(Objects.requireNonNull(getActivity()).getResources().getString(R.string.THERE_IS_NO_SEARCH_RESULT));
        } else {
            contactsAdapter = new ContactsAdapter(getContext(), reformedContactList, new ContactFriendSelectCallback() {
                @Override
                public void contactSelected(Contact contact) {
                    DynamicLinkUtil.setAppInvitationLinkForSms(getContext(), contact, fragment);
                }
            });
            recyclerView.setAdapter(contactsAdapter);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
            linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
            recyclerView.setLayoutManager(linearLayoutManager);
        }

        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PermissionModule.PERMISSION_READ_CONTACTS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startGetContactList();
            } else {
                warningMsgLayout.setVisibility(View.VISIBLE);
                warningMsgTv.setText(Objects.requireNonNull(getActivity()).getResources().getString(R.string.GIVE_PERMISSION_TO_SEE_CONTACT_FRIENDS));
            }
        }
    }

    public void updateAdapter(String searchText) {
        if (searchText != null && contactsAdapter != null) {
            contactsAdapter.updateAdapter(searchText, new ReturnCallback() {
                @Override
                public void OnReturn(Object object) {
                    int itemSize = (int) object;

                    if (itemSize == 0) {
                        warningMsgTv.setText(Objects.requireNonNull(getActivity()).getResources().getString(R.string.THERE_IS_NO_SEARCH_RESULT));
                        warningMsgLayout.setVisibility(View.VISIBLE);
                    } else
                        warningMsgLayout.setVisibility(View.GONE);
                }
            });
        }
    }
}