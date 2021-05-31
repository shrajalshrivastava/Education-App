package com.example.ssipmteducation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import id.zelory.compressor.Compressor;

public class AddPost extends AppCompatActivity {

    TextInputLayout CourseTitle, CourseInstructor,CourseDuration,CourseDesc,CourseSem;
    Button Next;

    ImageView CourseImage;
    Uri postImageUri= null;
    StorageReference storageReference ;
    FirebaseFirestore db;
    private Bitmap compressorImageFile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        CourseTitle = findViewById(R.id.CourseTitle);
        CourseDuration = findViewById(R.id.Duration);
        CourseInstructor = findViewById(R.id.CourseInstructor);
        CourseDesc = findViewById(R.id.CourseDesc);
        CourseSem = findViewById(R.id.CourseSem);
        Next = findViewById(R.id.NextBtn);
        storageReference  = FirebaseStorage.getInstance().getReference();
        db = FirebaseFirestore.getInstance();

        CourseImage  = findViewById(R.id.courseImage);


        CourseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M){
                    if (ContextCompat.checkSelfPermission(AddPost.this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){

                        ActivityCompat.requestPermissions(AddPost.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
                        Toast.makeText(AddPost.this, "Permission Granted", Toast.LENGTH_SHORT).show();


                    }else{
                        BringImagePicker();

                    }
                }else{
                    BringImagePicker();
                }
//                startActivity(new Intent(AddPost.this,AddNotes.class));
//                finish();
            }
        });

        Next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String title = CourseTitle.getEditText().getText().toString();
                String instructor =  CourseInstructor.getEditText().getText().toString();
                String duration = CourseDuration.getEditText().getText().toString();
                String desc = CourseDesc.getEditText().getText().toString();
                String semester = CourseSem.getEditText().getText().toString();

                if (!TextUtils.isEmpty(title) &&!TextUtils.isEmpty(instructor)&&!TextUtils.isEmpty(duration)&&!TextUtils.isEmpty(desc)&&!TextUtils.isEmpty(semester)&& postImageUri != null){
                    Dialog dialog;
                    dialog = ProgressDialog.show(AddPost.this,"","Creating Course.... ");
                    final StorageReference reference = storageReference.child("CourseImage").child("Image "+ Timestamp.now().getSeconds()+ ".jpg");
                    reference.putFile(postImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    final String imageUri = uri.toString();
                                    File newImageFile = new File(Objects.requireNonNull(postImageUri.getPath()));
                                    try {
                                        compressorImageFile = new Compressor(AddPost.this)
                                                .setMaxWidth(100)
                                                .setQuality(2)
                                                .compressToBitmap(newImageFile);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                    compressorImageFile.compress(Bitmap.CompressFormat.JPEG,100,baos);
                                    byte[] ThumbData =baos.toByteArray();
                                    final UploadTask uploadTask = storageReference.child("thumbs")
                                            .child("My _image "+Timestamp.now().getSeconds()+ ".jpg")
                                            .putBytes(ThumbData);

                                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                            uploadTask.getResult().getMetadata().getReference().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {

                                                    UUID uuid = UUID.randomUUID();
                                                    String uuidAsString = uuid.toString();
                                                    String ThumbUri  = uri.toString();
                                                    Map<String,Object> data = new HashMap<>();
                                                    data.put("Image",imageUri);
                                                    data.put("Thumbnail", ThumbUri);
                                                    data.put("Description", desc);
                                                    data.put("Instructor",instructor);
                                                    data.put("Duration",duration);
                                                    data.put("Title",title);
                                                    data.put("id", uuidAsString);
                                                    data.put("timeStamp", FieldValue.serverTimestamp());

                                                    db.collection("Courses").document(uuidAsString).set(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if(task.isSuccessful()){
                                                                Toast.makeText(AddPost.this, "Successfully Created", Toast.LENGTH_SHORT).show();
                                                                dialog.dismiss();
                                                                Intent intent = new Intent(AddPost.this,MainActivity.class);
                                                                intent.putExtra("id", uuidAsString);
                                                                startActivity(intent);
                                                                finish();

                                                            }
                                                            else{
                                                                String err = task.getException().getMessage();
                                                                Toast.makeText(AddPost.this, "Err: "+ err, Toast.LENGTH_SHORT);
                                                                dialog.dismiss();

                                                            }
                                                        }
                                                    });

                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    dialog.dismiss();
                                                    Toast.makeText(AddPost.this, "Err :" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    });
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            dialog.dismiss();
                            Toast.makeText(AddPost.this, "Error: "+ e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }else{

                    Toast.makeText(AddPost.this, "Empty Field are not allowed", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    private void BringImagePicker() {
        CropImage.activity().setGuidelines(CropImageView.Guidelines.ON)
                .setMinCropResultSize(512,512)
                .setAspectRatio(1,1)
                .start(AddPost.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                postImageUri = result.getUri();
                CourseImage.setImageURI(postImageUri);





            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(this, "Error "+ error, Toast.LENGTH_SHORT).show();
            }
        }
    }
}