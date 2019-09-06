package uren.com.myduties.dutyManagement.profile.interfaces;

import android.view.View;

import uren.com.myduties.models.User;


public interface ListItemClickListener {

    void onClick(View view, User user, int clickedPosition);
}