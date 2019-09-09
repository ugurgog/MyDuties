package uren.com.myduties.dbManagement;

import android.content.Context;
import android.graphics.Bitmap;
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

import java.io.ByteArrayOutputStream;

import uren.com.myduties.R;
import uren.com.myduties.interfaces.CompleteCallback;
import uren.com.myduties.models.PhotoSelectUtil;
import uren.com.myduties.models.User;

public class UserPhotoDBHelper {

    public static void uploadUserPhoto(Context context,String userid, PhotoSelectUtil photoSelectUtil, CompleteCallback completeCallback){

        if(userid == null || userid.isEmpty()){
            completeCallback.onFailed(context.getResources().getString(R.string.UNEXPECTED_ERROR));
            return;
        }

        if(photoSelectUtil == null || photoSelectUtil.getBitmap() == null){
            completeCallback.onFailed(context.getResources().getString(R.string.UNEXPECTED_ERROR));
            return;
        }

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("images").child("users").child(userid + "/profilephoto.jpg");

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
                    Log.i("Uri:", downloadUri.toString());
                    completeCallback.onComplete(downloadUri.toString());
                } else {
                    completeCallback.onFailed("Upload failed: " + task.getException().getMessage());
                }
            }
        });
    }


}
