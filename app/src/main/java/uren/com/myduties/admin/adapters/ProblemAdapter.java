package uren.com.myduties.admin.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import uren.com.myduties.R;
import uren.com.myduties.common.ShowSelectedPhotoFragment;
import uren.com.myduties.dbManagement.ProblemDBHelper;
import uren.com.myduties.dutyManagement.BaseFragment;
import uren.com.myduties.evetBusModels.UserBus;
import uren.com.myduties.interfaces.CompleteCallback;
import uren.com.myduties.interfaces.OnCompleteCallback;
import uren.com.myduties.models.Problem;
import uren.com.myduties.models.User;
import uren.com.myduties.utils.CommonUtils;
import uren.com.myduties.utils.MyDutiesUtil;
import uren.com.myduties.utils.dataModelUtil.UserDataUtil;

public class ProblemAdapter extends RecyclerView.Adapter {

    private Context mContext;
    private List<Problem> problemList;
    private BaseFragment.FragmentNavigation fragmentNavigation;
    private User accountholderUser;

    public ProblemAdapter(Context context, BaseFragment.FragmentNavigation fragmentNavigation) {
        this.mContext = context;
        this.fragmentNavigation = fragmentNavigation;
        this.problemList = new ArrayList<>();
        EventBus.getDefault().register(this);
    }

    @Subscribe(sticky = true)
    public void userReceived(UserBus userBus) {
        accountholderUser = userBus.getUser();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        RecyclerView.ViewHolder viewHolder;
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.problem_item, parent, false);

        viewHolder = new ProblemAdapter.MyViewHolder(itemView);
        return viewHolder;

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Problem problem = problemList.get(position);
        ((ProblemAdapter.MyViewHolder) holder).setData(problem, position);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        View mView;
        ImageView imgProfilePic;
        ImageView moreImgv;
        AppCompatTextView txtUserName;
        AppCompatTextView txtDetail;
        CardView cardView;
        int position;
        LinearLayout llcompleted;
        TextView txtCreateAt;
        TextView txtCompletedAt;
        ImageView problemImgv;
        TextView txtProfilePic;

        PopupMenu popupMenu = null;
        Problem problem;

        public MyViewHolder(View view) {
            super(view);

            mView = view;
            cardView = view.findViewById(R.id.card_view);
            imgProfilePic = view.findViewById(R.id.imgProfilePic);
            moreImgv = view.findViewById(R.id.moreImgv);
            txtUserName = view.findViewById(R.id.txtUserName);
            txtDetail = view.findViewById(R.id.txtDetail);
            txtCreateAt = view.findViewById(R.id.txtCreateAt);
            txtCompletedAt = view.findViewById(R.id.txtCompletedAt);
            llcompleted = view.findViewById(R.id.llcompleted);
            problemImgv = view.findViewById(R.id.problemImgv);
            txtProfilePic = view.findViewById(R.id.txtProfilePic);
            setListeners();
            setPopupMenu();
        }

        private void setPopupMenu() {
            popupMenu = new PopupMenu(mContext, moreImgv);
            popupMenu.inflate(R.menu.menu_problems);
        }

        private void setListeners() {

            problemImgv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (problem.getProblemPhotoUrl() != null && !problem.getProblemPhotoUrl().isEmpty()) {
                        fragmentNavigation.pushFragment(new ShowSelectedPhotoFragment(problem.getProblemPhotoUrl()));
                    }
                }
            });

            moreImgv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.fixed:
                                    if (problem.isFixed()) {
                                        CommonUtils.showToastShort(mContext, "Problem is already fixed");
                                        return false;
                                    }

                                    problem.setFixed(true);

                                    ProblemDBHelper.updateProblem(problem, new CompleteCallback() {
                                        @Override
                                        public void onComplete(Object object) {
                                            problemList.remove(position);
                                            notifyItemRemoved(position);
                                            notifyItemRangeChanged(position, getItemCount());
                                        }

                                        @Override
                                        public void onFailed(String message) {

                                        }
                                    });

                                    break;

                                case R.id.delete:

                                    ProblemDBHelper.deleteProblem(problem.getProblemid(), new OnCompleteCallback() {
                                        @Override
                                        public void OnCompleted() {
                                            problemList.remove(position);
                                            notifyItemRemoved(position);
                                            notifyItemRangeChanged(position, getItemCount());
                                        }

                                        @Override
                                        public void OnFailed(String message) {

                                        }
                                    });
                                    break;
                            }
                            return false;
                        }
                    });
                    popupMenu.show();
                }
            });
        }

        public void setData(Problem problem, int position) {
            this.problem = problem;
            this.position = position;
            setAssignedFromValues();
            MyDutiesUtil.setProblemDescription(problem, txtDetail);
            MyDutiesUtil.setProblemCreatedAtValue(mContext, problem, txtCreateAt);
            MyDutiesUtil.setProblemCompletedTimeValueAndSetLayoutVisibility(problem, llcompleted, mContext, txtCompletedAt);
            MyDutiesUtil.setProblemPicture(mContext, problem.getProblemPhotoUrl(), problemImgv);
        }

        private void setAssignedFromValues() {
            UserDataUtil.setProfilePicture(mContext, problem.getWhoOpened().getProfilePhotoUrl(),
                    problem.getWhoOpened().getName(), problem.getWhoOpened().getUsername(), txtProfilePic, imgProfilePic, true);
            txtUserName.setText(UserDataUtil.getNameOrUsername(problem.getWhoOpened().getName(), problem.getWhoOpened().getUsername()));
        }
    }

    @Override
    public int getItemCount() {
        return (problemList != null ? problemList.size() : 0);
    }

    public void addProblem(Problem problem) {
        if (problemList != null) {
            problemList.add(problem);
            notifyItemInserted(problemList.size() - 1);
        }
    }

    public void updatePostListItems() {
        this.problemList.clear();
        notifyDataSetChanged();
    }
}