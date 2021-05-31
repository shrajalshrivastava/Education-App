package com.example.ssipmteducation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.iceteck.silicompressorr.SiliCompressor;

import java.io.File;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.internal.functions.ObjectHelper;

public class MainActivity extends AppCompatActivity {
    VideoView videoView2;
    Button selectBtn,uploadBtn;
    StorageReference storageReference;
    Uri postVideoUri = null;
    TextInputLayout VideoTitle;
    FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        videoView2 = findViewById(R.id.videoView2);
        storageReference = FirebaseStorage.getInstance().getReference();
        selectBtn = findViewById(R.id.selectBtn);
        uploadBtn = findViewById(R.id.upload);
        VideoTitle = findViewById(R.id.videoTitle);
        db = FirebaseFirestore.getInstance();
        Bundle bundle = getIntent().getExtras();
        String id = bundle.getString("id");
        selectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check permission

                if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                    selectVideo();
                }
                else{
                    ActivityCompat.requestPermissions(MainActivity.this,new String[]{
                           Manifest.permission.WRITE_EXTERNAL_STORAGE
                    },1);
                }
            }



        });

        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = VideoTitle.getEditText().getText().toString();
                if (!TextUtils.isEmpty(title ) && postVideoUri!=null){
                    uploadVideoToFirebase(postVideoUri,title,id);
                }
            }
        });


    }

    private void selectVideo() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Video"),100);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode ==1 && grantResults.length>0&& grantResults[0] == PackageManager.PERMISSION_GRANTED){
            selectVideo();
        }else{
            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode ==100 && resultCode ==RESULT_OK &&data!=null){
            Uri uri = data.getData();


            File file = new File(Environment.getExternalStorageDirectory()
            .getAbsolutePath());

            new CompressVideo().execute("false",uri.toString(),file.getPath());


        }
    }

    private class CompressVideo extends AsyncTask<String,String,String> {
        Dialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = ProgressDialog.show(MainActivity.this,"","Compressing..");
        }

        @Override
        protected String doInBackground(String... strings) {
            String videoPath = null;
            Uri uri  =Uri.parse(strings[1]);

            try {
                videoPath = SiliCompressor.with(MainActivity.this).compressVideo(uri,strings[2]);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            return videoPath;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            dialog.dismiss();


            videoView2.setVisibility(View.VISIBLE);


            File file = new File(s);

            Uri uri = Uri.fromFile(file);
            videoView2.setVideoURI(uri);
            postVideoUri = uri;


        videoView2.start();
        }

    }

    private void uploadVideoToFirebase(Uri uri, String title,String id) {
        Dialog dialog;
        dialog = ProgressDialog.show(MainActivity.this,"","Uploading Video ");
        StorageReference reference = storageReference.child("videos/"+System.currentTimeMillis());
        reference.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String VideoUri  = uri.toString();
                                Map<String, Object> data  = new HashMap<>();
                                data.put("Title", title);
                                data.put("VideoUrl",VideoUri);
                                db.collection("Courses/"+id +"/videos").add(data).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentReference> task) {
                                        if(task.isSuccessful()){
                                            Toast.makeText(MainActivity.this, "Successfully Created", Toast.LENGTH_SHORT).show();
                                            dialog.dismiss();
                                            postVideoUri = null;
                                            videoView2.setVisibility(View.GONE);
                                            VideoTitle.getEditText().setText("");
                                        }
                                        else{
                                            String err = task.getException().getMessage();
                                            Toast.makeText(MainActivity.this, "Err: "+ err, Toast.LENGTH_SHORT);
                                            dialog.dismiss();

                                        }
                                    }
                                });

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                dialog.dismiss();
                                Toast.makeText(MainActivity.this, "Error: "+ e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {

            }
        });


    }
}


