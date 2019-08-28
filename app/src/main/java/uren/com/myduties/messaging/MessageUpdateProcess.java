package uren.com.myduties.messaging;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;

import static uren.com.myduties.constants.StringConstants.FB_CHILD_DEVICE_TOKEN;
import static uren.com.myduties.constants.StringConstants.FB_CHILD_SIGNIN;

public class MessageUpdateProcess {

    public static void updateTokenSigninValue(String userid, String value) {
        FirebaseDatabase.getInstance().getReference(FB_CHILD_DEVICE_TOKEN)
                .child(userid).child(FB_CHILD_SIGNIN).setValue(value)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }
}
