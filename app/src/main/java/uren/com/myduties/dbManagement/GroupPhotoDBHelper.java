package uren.com.myduties.dbManagement;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import uren.com.myduties.R;
import uren.com.myduties.interfaces.CompleteCallback;
import uren.com.myduties.models.PhotoSelectUtil;

public class GroupPhotoDBHelper {

    public static void uploadGroupPhoto(Context context, String groupid, PhotoSelectUtil photoSelectUtil, CompleteCallback completeCallback){

        if(groupid == null || groupid.isEmpty()){
            completeCallback.onFailed(context.getResources().getString(R.string.UNEXPECTED_ERROR));
            return;
        }

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("images").child(groupid + "/groupphoto.jpg");


        if(photoSelectUtil != null) {
            storageRef.putFile(photoSelectUtil.getMediaUri()).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return storageRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        completeCallback.onComplete(downloadUri.toString());
                    } else {
                        completeCallback.onFailed("Upload failed: " + task.getException().getMessage());
                    }
                }
            });
        }
        else {
            storageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    completeCallback.onComplete(null);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    completeCallback.onFailed("Upload failed: " + exception.getMessage());
                }
            });
        }
    }
}
