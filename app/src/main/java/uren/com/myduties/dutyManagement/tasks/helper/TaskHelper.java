package uren.com.myduties.dutyManagement.tasks.helper;


import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import uren.com.myduties.dutyManagement.BaseFragment;
import uren.com.myduties.dutyManagement.profile.OtherProfileFragment;
import uren.com.myduties.dutyManagement.profile.ProfileFragment;
import uren.com.myduties.dutyManagement.tasks.MyTaskFragment;
import uren.com.myduties.dutyManagement.tasks.interfaces.TaskRefreshCallback;
import uren.com.myduties.login.AccountHolderInfo;
import uren.com.myduties.models.User;

import static uren.com.myduties.constants.StringConstants.ANIMATE_RIGHT_TO_LEFT;

public class TaskHelper {

    public static class ProfileClicked {

        static BaseFragment.FragmentNavigation fragmentNavigation;
        static User user;

        public static void startProcess(Context context, BaseFragment.FragmentNavigation fragmNav, User user) {

            fragmentNavigation = fragmNav;
            ProfileClicked.user = user;

            ProfileClicked commentListClicked = new ProfileClicked(context);
        }

        private ProfileClicked(Context context) {
            postProfileClickedProcess(context);
        }

        private void postProfileClickedProcess(Context context) {
            if (fragmentNavigation != null) {
                if (user.getUserid().equals(AccountHolderInfo.getUserID())) {
                    //clicked own profile
                    fragmentNavigation.pushFragment(new ProfileFragment(), ANIMATE_RIGHT_TO_LEFT);
                } else {
                    //clicked others profile
                    fragmentNavigation.pushFragment(new OtherProfileFragment(), ANIMATE_RIGHT_TO_LEFT);
                }
            }
        }

    }

    public static class TaskRefresh {

        private static TaskRefresh instance = null;
        private static List<TaskRefreshCallback> taskRefreshCallbackList;

        public TaskRefresh() {
            taskRefreshCallbackList = new ArrayList<>();
        }

        public static TaskRefresh getInstance() {
            if (instance == null)
                instance = new TaskRefresh();

            return instance;
        }

        public void setTaskRefreshCallback(TaskRefreshCallback taskRefreshCallback) {
            taskRefreshCallbackList.add(taskRefreshCallback);
        }

        public static void taskRefreshStart() {
            if (instance != null) {
                for (int i = 0; i < taskRefreshCallbackList.size(); i++) {
                    taskRefreshCallbackList.get(i).onTaskRefresh();
                }
            }
        }
    }

    public static class InitTask {

        private static MyTaskFragment myTaskFragment = null;

        public InitTask() {
        }

        public static void setTaskFragment(MyTaskFragment fragment) {
            myTaskFragment = fragment;
        }

        public static MyTaskFragment getTaskFragment() {
            return myTaskFragment;
        }

    }

    public static class Utils {

        public static String calculateDistance(Double distance) {
            String distanceValue;
            if (distance < 1) {
                distance = distance * 1000;
                distanceValue = distance.intValue() + "m";
            } else {
                distanceValue = String.format("%.2f", distance) + "km";
            }
            return distanceValue;
        }


    }

}