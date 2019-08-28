package uren.com.myduties.dutyManagement.tasks.adapters;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import uren.com.myduties.dutyManagement.tasks.CompletedTaskFragment;
import uren.com.myduties.dutyManagement.tasks.GroupTaskFragment;
import uren.com.myduties.dutyManagement.tasks.WaitingTaskFragment;


public class FeedPagerAdapter extends FragmentPagerAdapter {

    private int numOfTabs;

    public FeedPagerAdapter(FragmentManager fm, int numOfTabs) {
        super(fm);
        this.numOfTabs = numOfTabs;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new WaitingTaskFragment();
            case 1:
                return new CompletedTaskFragment();
            case 2:
                return new GroupTaskFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return numOfTabs;
    }

}