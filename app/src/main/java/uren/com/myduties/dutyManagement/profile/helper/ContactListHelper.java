package uren.com.myduties.dutyManagement.profile.helper;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import uren.com.myduties.interfaces.CompleteCallback;
import uren.com.myduties.models.Contact;

public class ContactListHelper {
    public static void getContactList(Context context, CompleteCallback completeCallback) {
        List<Contact> contactList = new ArrayList<>();
        String previousPhoneNum = "";

        Cursor phones = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        while (Objects.requireNonNull(phones).moveToNext()) {

            String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            if(!phoneNumber.equals(previousPhoneNum)) {
                StringBuilder clearPhoneNum = new StringBuilder();

                for (int i = 0; i < phoneNumber.length(); i++) {
                    char ch = phoneNumber.charAt(i);
                    if (Character.isDigit(ch)) {
                        clearPhoneNum.append(ch);
                    }
                }
                Contact contact = new Contact(name,clearPhoneNum.toString() );
                contactList.add(contact);
            }
            previousPhoneNum = phoneNumber;
        }
        phones.close();
        Collections.sort(contactList, new CustomComparator());
        completeCallback.onComplete(contactList);
    }

    public static class CustomComparator implements Comparator<Contact> {
        @Override
        public int compare(Contact o1, Contact o2) {
            return o1.getName().compareToIgnoreCase(o2.getName());
        }
    }
}
