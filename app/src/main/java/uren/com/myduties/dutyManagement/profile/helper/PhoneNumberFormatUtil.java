package uren.com.myduties.dutyManagement.profile.helper;

import android.content.Context;
import android.os.AsyncTask;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import uren.com.myduties.interfaces.CompleteCallback;
import uren.com.myduties.models.Contact;
import uren.com.myduties.models.Country;
import uren.com.myduties.utils.CommonUtils;

public class PhoneNumberFormatUtil {

    public static void reformPhoneList(final List<Contact> contactList, final Context context, final CompleteCallback completeCallback) {

        String countryCodesStr = CommonUtils.readCountryCodes(context);
        Gson gson = new Gson();
        Country[] countries = gson.fromJson(countryCodesStr, Country[].class);

        String locale = context.getResources().getConfiguration().locale.getCountry();

        for (Country country : countries) {
            if (country != null && country.getCode() != null && !country.getCode().trim().isEmpty() &&
                    country.getDialCode() != null && !country.getDialCode().isEmpty()) {
                if (country.getCode().trim().equals(locale)) {
                    formatNumbersWithDialCode(country.getDialCode(), locale, contactList,
                            completeCallback, context);
                    break;
                }
            }
        }
    }

    public static void formatNumbersWithDialCode(String dialCode, String locale, final List<Contact> contactList,
                                                 CompleteCallback completeCallback, Context context) {
        List<Contact> reformedContactList = new ArrayList<>();

        if (locale.equals("TR")) {
            for (Contact contact : contactList) {
                if (contact != null && contact.getPhoneNumber() != null && !contact.getPhoneNumber().isEmpty()) {

                    try {
                        String completeNumber;
                        String reverseText = new StringBuilder(contact.getPhoneNumber().trim()).reverse().toString();
                        completeNumber = dialCode.trim() + new StringBuilder(reverseText.substring(0, 10)).reverse().toString();

                        Contact contactTemp = new Contact(contact.getName(), completeNumber);

                        reformedContactList.add(contactTemp);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        completeCallback.onComplete(reformedContactList);
    }
}
