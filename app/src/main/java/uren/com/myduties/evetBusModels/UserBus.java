package uren.com.myduties.evetBusModels;

import uren.com.myduties.models.User;

public class UserBus {

    User user;

    public UserBus(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}
