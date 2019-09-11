package uren.com.myduties.dbManagement;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

import uren.com.myduties.interfaces.CompleteCallback;
import uren.com.myduties.interfaces.OnCompleteCallback;
import uren.com.myduties.models.Problem;

import static uren.com.myduties.constants.StringConstants.fb_child_completedtime;
import static uren.com.myduties.constants.StringConstants.fb_child_createdat;
import static uren.com.myduties.constants.StringConstants.fb_child_fixed;
import static uren.com.myduties.constants.StringConstants.fb_child_platform;
import static uren.com.myduties.constants.StringConstants.fb_child_problemdesc;
import static uren.com.myduties.constants.StringConstants.fb_child_problemphotourl;
import static uren.com.myduties.constants.StringConstants.fb_child_problems;
import static uren.com.myduties.constants.StringConstants.fb_child_type;
import static uren.com.myduties.constants.StringConstants.fb_child_whoopened;

public class ProblemDBHelper {

    public static void addProblem(Problem problem, final CompleteCallback completeCallback) {

        if (problem == null)
            return;

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(fb_child_problems).child(problem.getProblemid());

        final Map<String, Object> values = new HashMap<>();

        values.put(fb_child_createdat, ServerValue.TIMESTAMP);
        values.put(fb_child_fixed, problem.isFixed());

        if(problem.getPlatform() != null)
            values.put(fb_child_platform, problem.getPlatform());

        if(problem.getProblemDesc() != null)
            values.put(fb_child_problemdesc, problem.getProblemDesc());

        if(problem.getProblemPhotoUrl() != null)
            values.put(fb_child_problemphotourl, problem.getProblemPhotoUrl());

        if(problem.getType() != null)
            values.put(fb_child_type, problem.getType());

        if(problem.getWhoOpened() != null && problem.getWhoOpened().getUserid() != null)
            values.put(fb_child_whoopened, problem.getWhoOpened().getUserid());

        databaseReference.updateChildren(values).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                completeCallback.onComplete(null);
            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                completeCallback.onFailed(e.getMessage());
            }
        });
    }

    public static void updateProblem(Problem problem, final CompleteCallback completeCallback) {

        if (problem == null)
            return;

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(fb_child_problems).child(problem.getProblemid());

        final Map<String, Object> values = new HashMap<>();

        values.put(fb_child_createdat, problem.getCreatedAt());
        values.put(fb_child_fixed, problem.isFixed());
        values.put(fb_child_completedtime, ServerValue.TIMESTAMP);

        if(problem.getPlatform() != null)
            values.put(fb_child_platform, problem.getPlatform());

        if(problem.getProblemDesc() != null)
            values.put(fb_child_problemdesc, problem.getProblemDesc());

        if(problem.getProblemPhotoUrl() != null)
            values.put(fb_child_problemphotourl, problem.getProblemPhotoUrl());

        if(problem.getType() != null)
            values.put(fb_child_type, problem.getType());

        if(problem.getWhoOpened() != null && problem.getWhoOpened().getUserid() != null)
            values.put(fb_child_whoopened, problem.getWhoOpened().getUserid());

        databaseReference.updateChildren(values).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                completeCallback.onComplete(null);
            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                completeCallback.onFailed(e.getMessage());
            }
        });
    }

    public static void deleteProblem(String problemid, final OnCompleteCallback onCompleteCallback) {

        FirebaseDatabase.getInstance().getReference(fb_child_problems).child(problemid)
                .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                onCompleteCallback.OnCompleted();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                onCompleteCallback.OnFailed(e.getMessage());
            }
        });
    }
}
