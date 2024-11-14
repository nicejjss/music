package com.example.music.utils;

import android.net.Uri;
import android.webkit.MimeTypeMap;

import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class FirebaseUtils {
    private static final FirebaseStorage storage = FirebaseStorage.getInstance();

    public static String getFileExtension(Uri uri) {
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(MimeTypeMap.getFileExtensionFromUrl(uri.toString()));
    }

    public static Task<Uri> uploadFile(Uri fileUri, String folderName, String fileName) {
        StorageReference fileRef = storage.getReference()
                .child(folderName)
                .child(fileName + "." + getFileExtension(fileUri));

        UploadTask uploadTask = fileRef.putFile(fileUri);

        return uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }
            return fileRef.getDownloadUrl();
        });
    }
}
