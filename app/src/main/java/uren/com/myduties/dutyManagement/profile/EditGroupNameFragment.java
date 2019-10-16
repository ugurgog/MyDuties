package uren.com.myduties.dutyManagement.profile;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import uren.com.myduties.R;
import uren.com.myduties.dbManagement.GroupDBHelper;
import uren.com.myduties.dutyManagement.BaseFragment;
import uren.com.myduties.dutyManagement.NextActivity;
import uren.com.myduties.interfaces.CompleteCallback;
import uren.com.myduties.interfaces.OnCompleteCallback;
import uren.com.myduties.models.Group;
import uren.com.myduties.utils.ClickableImage.ClickableImageView;
import uren.com.myduties.utils.CommonUtils;
import uren.com.myduties.utils.ShapeUtil;
import uren.com.myduties.utils.dialogBoxUtil.DialogBoxUtil;
import uren.com.myduties.utils.dialogBoxUtil.Interfaces.InfoDialogBoxCallback;

import static uren.com.myduties.constants.NumericConstants.GROUP_NAME_MAX_LENGTH;
import static uren.com.myduties.constants.StringConstants.ANIMATE_RIGHT_TO_LEFT;

public class EditGroupNameFragment extends BaseFragment {

    View mView;

    @BindView(R.id.commonToolbarbackImgv)
    ClickableImageView commonToolbarbackImgv;
    @BindView(R.id.toolbarTitleTv)
    AppCompatTextView toolbarTitleTv;
    @BindView(R.id.groupNameEditText)
    EditText groupNameEditText;
    @BindView(R.id.textSizeCntTv)
    TextView textSizeCntTv;
    @BindView(R.id.cancelButton)
    Button cancelButton;
    @BindView(R.id.approveButton)
    Button approveButton;
    @BindView(R.id.relLayout)
    RelativeLayout relLayout;

    CompleteCallback completeCallback;

    int groupNameSize = 0;
    GradientDrawable buttonShape;
    Group groupRequestResultResultArrayItem;

    public EditGroupNameFragment(Group groupRequestResultResultArrayItem, CompleteCallback completeCallback) {
        this.groupRequestResultResultArrayItem = groupRequestResultResultArrayItem;
        this.completeCallback = completeCallback;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (mView == null) {
            mView = inflater.inflate(R.layout.fragment_edit_group_name, container, false);
            ButterKnife.bind(this, mView);
            addListeners();
            setGroupVariables();
            toolbarTitleTv.setText(getContext().getResources().getString(R.string.giveNewName));
            setButtonShapes();
        }
        return mView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((NextActivity) Objects.requireNonNull(getActivity())).ANIMATION_TAG = ANIMATE_RIGHT_TO_LEFT;
    }

    private void setGroupVariables() {
        groupNameEditText.setText(groupRequestResultResultArrayItem.getName());
        groupNameSize = GROUP_NAME_MAX_LENGTH - groupRequestResultResultArrayItem.getName().length();
        textSizeCntTv.setText(Integer.toString(groupNameSize));
    }

    private void setButtonShapes() {
        buttonShape = ShapeUtil.getShape(getContext().getResources().getColor(R.color.White),
                getContext().getResources().getColor(R.color.Gray), GradientDrawable.RECTANGLE, 15, 2);
        cancelButton.setBackground(buttonShape);
        approveButton.setBackground(buttonShape);
    }

    public void addListeners() {
        relLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommonUtils.hideKeyBoard(Objects.requireNonNull(getContext()));
            }
        });

        commonToolbarbackImgv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Objects.requireNonNull(getActivity()).onBackPressed();
            }
        });

        groupNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                groupNameSize = GROUP_NAME_MAX_LENGTH - s.toString().length();

                if (groupNameSize >= 0)
                    textSizeCntTv.setText(Integer.toString(groupNameSize));
                else
                    textSizeCntTv.setText(Integer.toString(0));
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelButton.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.image_click));
                Objects.requireNonNull(getActivity()).onBackPressed();
            }
        });

        approveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommonUtils.hideKeyBoard(Objects.requireNonNull(getContext()));
                approveButton.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.image_click));
                if (groupNameEditText.getText() != null && !groupNameEditText.getText().toString().trim().isEmpty())
                    updateGroup();
                else {
                    DialogBoxUtil.showInfoDialogBox(getContext(), getContext().getResources().getString(R.string.pleaseWriteGroupName), null, new InfoDialogBoxCallback() {
                        @Override
                        public void okClick() {

                        }
                    });
                }
            }
        });
    }

    public void updateGroup() {
        groupRequestResultResultArrayItem.setName(groupNameEditText.getText().toString());

        GroupDBHelper.updateGroup(groupRequestResultResultArrayItem, new OnCompleteCallback() {
            @Override
            public void OnCompleted() {
                completeCallback.onComplete(groupNameEditText.getText().toString());
                Objects.requireNonNull(getActivity()).onBackPressed();
            }

            @Override
            public void OnFailed(String message) {
                CommonUtils.showToastShort(getContext(), message);
            }
        });
    }

}