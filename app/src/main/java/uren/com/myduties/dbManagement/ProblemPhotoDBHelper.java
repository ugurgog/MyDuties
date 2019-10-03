package uren.com.myduties.dbManagement;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

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

public class ProblemPhotoDBHelper {

    public static void uploadProblemPhoto(Context context, String problemid, PhotoSelectUtil photoSelectUtil, CompleteCallback completeCallback) {

        if (problemid == null || problemid.isEmpty()) {
            completeCallback.onFailed(context.getResources().getString(R.string.UNEXPECTED_ERROR));
            return;
        }

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("images").child("problems").child(problemid + "/problemphoto.jpg");

        if (photoSelectUtil != null) {

            if(photoSelectUtil.getMediaUri() != null){
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
            }else {
                Bitmap bitmap = null;
                if(photoSelectUtil.getScreeanShotBitmap() != null)
                    bitmap = photoSelectUtil.getScreeanShotBitmap();
                else if(photoSelectUtil.getBitmap() != null)
                    bitmap = photoSelectUtil.getBitmap();

                if(bitmap != null){
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] data = baos.toByteArray();

                    UploadTask uploadTask = storageRef.putBytes(data);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            completeCallback.onFailed("Upload failed: " + exception.getMessage());
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> result = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                            result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String url = uri.toString();
                                    completeCallback.onComplete(url);
                                }
                            });
                        }
                    });
                }else
                    completeCallback.onComplete(null);
            }
        } else
            completeCallback.onComplete(null);
    }

    // TODO: 2019-09-23 - photo silme akisi eklenecek. 
}
