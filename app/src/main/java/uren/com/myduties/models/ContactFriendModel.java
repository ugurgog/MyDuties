package uren.com.myduties.models;

public class ContactFriendModel {

    Contact contact;
    User user;

    public ContactFriendModel(Contact contact, User user) {
        this.contact = contact;
        this.user = user;
    }

    public Contact getContact() {
        return contact;
    }

    public void setContact(Contact contact) {
        this.contact = contact;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
