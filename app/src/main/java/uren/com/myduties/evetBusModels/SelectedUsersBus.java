package uren.com.myduties.evetBusModels;

import java.util.ArrayList;
import java.util.List;

import uren.com.myduties.models.User;

public class SelectedUsersBus {

    List<User> selectedUsers = new ArrayList<>();

    public SelectedUsersBus(List<User> selectedUsers) {
        this.selectedUsers = selectedUsers;
    }

    public List<User> getUsers() {
        return selectedUsers;
    }
}
