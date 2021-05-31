//package com.example.photoblog;
//
//import android.Manifest;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.graphics.Bitmap;
//import android.net.Uri;
//import android.os.Build;
//import android.os.Bundle;
//import android.text.TextUtils;
//import android.view.View;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.ImageView;
//import android.widget.ProgressBar;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.appcompat.widget.Toolbar;
//import androidx.core.app.ActivityCompat;
//import androidx.core.content.ContextCompat;
//
//import com.google.android.gms.tasks.OnCompleteListener;
//import com.google.android.gms.tasks.OnFailureListener;
//import com.google.android.gms.tasks.OnSuccessListener;
//import com.google.android.gms.tasks.Task;
//import com.google.firebase.Timestamp;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.firestore.DocumentReference;
//import com.google.firebase.firestore.FieldValue;
//import com.google.firebase.firestore.FirebaseFirestore;
//import com.google.firebase.storage.FirebaseStorage;
//import com.google.firebase.storage.StorageReference;
//import com.google.firebase.storage.UploadTask;
//import com.theartofdev.edmodo.cropper.CropImage;
//import com.theartofdev.edmodo.cropper.CropImageView;
//
//import java.io.ByteArrayOutputStream;
//import java.io.File;
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Objects;
//
//import id.zelory.compressor.Compressor;
//
//public class NewPostActivity extends AppCompatActivity {
//    private Toolbar newPostToolbar;
//    private ImageView newPostImage;
//    private EditText newPostDesc;
//    private Button newPostbtn;
//    private Uri postImageUri = null;
//    private ProgressBar newPostProgressbar;
//
//    private StorageReference storageReference;
//    private FirebaseFirestore firebaseFirestore;
//    private FirebaseAuth Auth;
//    private String currentUserId;
//
//    private Bitmap compressorImageFile;
//
//
//
//
//
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_new_post);
//        newPostToolbar = findViewById(R.id.newPost_toolbar);
//        setSupportActionBar(newPostToolbar);
//        getSupportActionBar().setTitle("Add New Post");
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        newPostImage = findViewById(R.id.new_post_image);
//        newPostDesc = findViewById(R.id.new_post_desc) ;
//        newPostbtn = findViewById(R.id.post_btn);
//        newPostProgressbar = findViewById(R.id.new_post_progressbar);
//
//        storageReference = FirebaseStorage.getInstance().getReference();
//        firebaseFirestore =FirebaseFirestore.getInstance();
//        Auth = FirebaseAuth.getInstance();
//        currentUserId = Auth.getCurrentUser().getUid();
//
//
//
//
//
//
//
//
//        newPostImage.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M){
//                    if (ContextCompat.checkSelfPermission(NewPostActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
//
//                        ActivityCompat.requestPermissions(NewPostActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
//                        Toast.makeText(NewPostActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
//
//
//                    }else{
//                        BringImagePicker();
//
//                    }
//                }else{
//                    BringImagePicker();
//                }
//
//            }
//        });
//
//        newPostbtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                final String desc = newPostDesc.getText().toString();
//                if (!TextUtils.isEmpty(desc) && postImageUri != null){
//                    newPostProgressbar.setVisibility(View.VISIBLE);
//
//                    final StorageReference filepath = storageReference.child("post_images")
//                            .child("My _image "+Timestamp.now().getSeconds()+ ".jpg");
//                    filepath.putFile(postImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                        @Override
//                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//
//                            filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//                                @Override
//                                public void onSuccess(Uri uri) {
//                                    final String imageUri = uri.toString();
//                                    File newImageFile = new File(Objects.requireNonNull(postImageUri.getPath()));
//
//
//                                    try {
//                                        compressorImageFile = new Compressor(NewPostActivity.this)
//                                                .setMaxHeight(100)
//                                                .setMaxWidth(100)
//                                                .setQuality(2)
//                                                .compressToBitmap(newImageFile);
//                                    } catch (IOException e) {
//                                        e.printStackTrace();
//                                    }
//                                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                                    compressorImageFile.compress(Bitmap.CompressFormat.JPEG,100,baos);
//                                    byte[] ThumbData =baos.toByteArray();
//
//                                    final UploadTask uploadTask = storageReference.child("post_images/thumbs")
//                                            .child("My _image "+Timestamp.now().getSeconds()+ ".jpg")
//                                            .putBytes(ThumbData);
//                                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                                        @Override
//                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                                            uploadTask.getResult().getMetadata().getReference().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//                                                @Override
//                                                public void onSuccess(Uri uri) {
//                                                    String downloadThumbUri = uri.toString();
//                                                    BlogPost blogPost = new BlogPost();
//                                                    Map<String, Object> postMap = new HashMap<>();
//                                                    postMap.put("imageUrl",imageUri);
//                                                    postMap.put("thumbUrl",downloadThumbUri);
//                                                    postMap.put("desc",desc);
//                                                    postMap.put("userId",currentUserId);
//
//
//                                                    postMap.put("timeStamp", FieldValue.serverTimestamp());
//
//
//
//                                                    firebaseFirestore.collection("Posts").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
//                                                        @Override
//                                                        public void onComplete(@NonNull Task<DocumentReference> task) {
//                                                            if(task.isSuccessful()){
//                                                                Toast.makeText(NewPostActivity.this, "Successfully added", Toast.LENGTH_SHORT).show();
//                                                                Intent intent = new Intent(NewPostActivity.this,MainActivity.class);
//                                                                startActivity(intent);
//                                                                finish();
//                                                            }else{
//                                                                String error = task.getException().getMessage();
//                                                                Toast.makeText(NewPostActivity.this, "Error :" + error, Toast.LENGTH_SHORT).show();
//                                                            }
//                                                            newPostProgressbar.setVisibility(View.INVISIBLE);
//                                                        }
//                                                    });
//                                                }
//                                            });
//                                        }
//                                    }).addOnFailureListener(new OnFailureListener() {
//                                        @Override
//                                        public void onFailure(@NonNull Exception e) {
//                                            Toast.makeText(NewPostActivity.this, "Error"+e.getMessage(), Toast.LENGTH_SHORT).show();
//                                        }
//                                    });
////                                    thumbFilePath.putFile(compressorImageFile);
//
//
//                                }
//                            });
//
//                        }
//                    }).addOnFailureListener(new OnFailureListener() {
//                        @Override
//                        public void onFailure(@NonNull Exception e) {
//                            newPostProgressbar.setVisibility(View.INVISIBLE);
//                            Toast.makeText(NewPostActivity.this, "Error: "+ e.getMessage(), Toast.LENGTH_SHORT).show();
//                        }
//                    });
//
//
//
//
//
//                }else{
//                    newPostProgressbar.setVisibility(View.INVISIBLE);
//                }
//
//
//            }
//        });
//
//
//    }
//
//    private void BringImagePicker() {
//        CropImage.activity()
//                .setGuidelines(CropImageView.Guidelines.ON)
//                .setMinCropResultSize(512,512)
//                .setAspectRatio(1,1)
//                .start(NewPostActivity.this);
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
//            CropImage.ActivityResult result = CropImage.getActivityResult(data);
//            if (resultCode == RESULT_OK) {
//                postImageUri = result.getUri();
//                newPostImage.setImageURI(postImageUri);
//
//
//
//
//
//            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
//                Exception error = result.getError();
//            }
//        }
//    }
//
//}
//
//
//
//
//
////                    filepath.putFile(postImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
////                        @Override
////                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
////                            if (task.isSuccessful()){
////                                String downloadUri;
////                                downloadUri = Objects.requireNonNull(task.getResult()).getStorage().getDownloadUrl().toString();
////                                Map<String, Object> postMap = new HashMap<>();
////                                postMap.put("imageUrl",downloadUri);
////                                postMap.put("description",desc);
////                                postMap.put("userId",currentUserId);
////                                postMap.put("timeStamp",FieldValue.serverTimestamp());
////
////
////
////                                firebaseFirestore.collection("Posts").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
////                                    @Override
////                                    public void onComplete(@NonNull Task<DocumentReference> task) {
////                                        if(task.isSuccessful()){
////                                            Toast.makeText(NewPostActivity.this, "Successfully added", Toast.LENGTH_SHORT).show();
////                                            Intent intent = new Intent(NewPostActivity.this,MainActivity.class);
////                                            startActivity(intent);
////                                            finish();
////
////                                        }else{
////                                            String error = task.getException().getMessage();
////                                            Toast.makeText(NewPostActivity.this, "Error :" + error, Toast.LENGTH_SHORT).show();
////                                        }
////                                        newPostProgressbar.setVisibility(View.INVISIBLE);
////
////
////                                    }
////                                });
////
////
////
////                            }else{
////                                newPostProgressbar.setVisibility(View.INVISIBLE);
////                                String error = task.getException().getMessage();
////                                Toast.makeText(NewPostActivity.this, "Error :" + error, Toast.LENGTH_SHORT).show();
////                            }
////                        }
////                    });


//
//
//package com.example.ssipmteducation;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.app.ActivityCompat;
//import androidx.core.content.ContextCompat;
//
//import android.Manifest;
//import android.app.Activity;
//import android.app.Dialog;
//import android.app.ProgressDialog;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.net.Uri;
//import android.os.AsyncTask;
//import android.os.Bundle;
//import android.os.Environment;
//import android.view.View;
//import android.widget.Button;
//import android.widget.TextView;
//import android.widget.Toast;
//import android.widget.VideoView;
//
//import com.google.android.gms.tasks.OnSuccessListener;
//import com.google.firebase.storage.FirebaseStorage;
//import com.google.firebase.storage.OnProgressListener;
//import com.google.firebase.storage.StorageReference;
//import com.google.firebase.storage.UploadTask;
//import com.iceteck.silicompressorr.SiliCompressor;
//
//import java.io.File;
//import java.net.URISyntaxException;
//
//public class MainActivity extends AppCompatActivity {
//    VideoView videoView1,videoView2;
//    TextView  textView1, textView2, textView3;
//    Button selectBtn;
//    StorageReference storageReference;
//    Uri postVideoUri = null;
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//
//        videoView1   = findViewById(R.id.videoView);
//        videoView2 = findViewById(R.id.videoView2);
//        textView1 = findViewById(R.id.originalVideo);
//        textView2 = findViewById(R.id.CompressVideo);
//        textView3 = findViewById(R.id.tv3);
//        storageReference = FirebaseStorage.getInstance().getReference();
//        selectBtn = findViewById(R.id.selectBtn);
//
//        selectBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //check permission
//                startActivity(new Intent(MainActivity.this,AddPost.class));
//                if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
//                    selectVideo();
//                }
//                else{
//                    ActivityCompat.requestPermissions(MainActivity.this,new String[]{
//                            Manifest.permission.WRITE_EXTERNAL_STORAGE
//                    },1);
//                }
//            }
//
//
//
//        });
//
//
//    }
//
//    private void selectVideo() {
//        Intent intent = new Intent(Intent.ACTION_PICK);
//        intent.setType("video/*");
//        intent.setAction(Intent.ACTION_GET_CONTENT);
//        startActivityForResult(Intent.createChooser(intent, "Select Video"),100);
//    }
//
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//
//        if(requestCode ==1 && grantResults.length>0&& grantResults[0] == PackageManager.PERMISSION_GRANTED){
//            selectVideo();
//        }else{
//            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
//        }
//
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if(requestCode ==100 && resultCode ==RESULT_OK &&data!=null){
//            Uri uri = data.getData();
//
//            videoView1.setVideoURI( uri);
//
//            File file = new File(Environment.getExternalStorageDirectory()
//                    .getAbsolutePath());
//
//            new CompressVideo().execute("false",uri.toString(),file.getPath());
//
//
//        }
//    }
//
//    private class CompressVideo extends AsyncTask<String,String,String> {
//        Dialog dialog;
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            dialog = ProgressDialog.show(MainActivity.this,"","Compressing..");
//        }
//
//        @Override
//        protected String doInBackground(String... strings) {
//            String videoPath = null;
//            Uri uri  =Uri.parse(strings[1]);
//
//            try {
//                videoPath = SiliCompressor.with(MainActivity.this).compressVideo(uri,strings[2]);
//            } catch (URISyntaxException e) {
//                e.printStackTrace();
//            }
//            return videoPath;
//        }
//
//        @Override
//        protected void onPostExecute(String s) {
//            super.onPostExecute(s);
//            dialog.dismiss();
//
//            videoView1.setVisibility(View.VISIBLE);
//            videoView2.setVisibility(View.VISIBLE);
//            textView1.setVisibility(View.VISIBLE);
//            textView2.setVisibility(View.VISIBLE);
//            textView3.setVisibility(View.VISIBLE);
//
//            File file = new File(s);
//
//            Uri uri = Uri.fromFile(file);
//            videoView2.setVideoURI(uri);
//            uploadVideotoFirebase(uri);
//
//            videoView1.start();
//            videoView2.start();
//
//            float size = file.length()/1024f;
//            textView3.setText(String.format("Size : % 2f KB",size));
//        }
//
//    }
//
//    private void uploadVideotoFirebase(Uri uri) {
//
//        StorageReference reference = storageReference.child("videos/"+System.currentTimeMillis());
//        reference.putFile(uri)
//                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                    @Override
//                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//                            @Override
//                            public void onSuccess(Uri uri) {
//
//                            }
//                        });
//                    }
//                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
//            @Override
//            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
//
//            }
//        });
//
//
//    }
//}
//
//
