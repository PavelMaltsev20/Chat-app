package com.example.pavel.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import de.hdodenhof.circleimageview.CircleImageView;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.pavel.chatapp.Adapter_Modul.Items.MyUser;
import com.example.pavel.chatapp.Adapter_Modul.SharedPref;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class
ProfileActivity extends AppCompatActivity {

    CircleImageView circleImageView;
    TextView email, username, password;
    Context context;

    FirebaseAuth mAuth;
    FirebaseUser firebaseUser;
    DatabaseReference databaseReference;

    StorageReference storageReference;
    private static final int IMAGE_REQUEST = 1;
    private Uri imageUri;
    private StorageTask uploadTask;
    SharedPref sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        updateNewTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        setPointer();
    }

    private void updateNewTheme() {
        sharedPref = new SharedPref(this);
        if (sharedPref.loadNightModeState()==true) {
            setTheme(R.style.AppTheme2);
        } else {
            setTheme(R.style.AppTheme);
        }
    }

    private void setPointer() {
        context = this;

        circleImageView = findViewById(R.id.profileIV);
        email = findViewById(R.id.profileTVEmail2);
        username = findViewById(R.id.profileTVName2);
        password = findViewById(R.id.profileTVPassword2);

        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        storageReference = FirebaseStorage.getInstance().getReference("uploads");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                final MyUser myUser = dataSnapshot.getValue(MyUser.class);

                if (myUser.getImageURL().equals("default")) {
                    circleImageView.setImageResource(R.mipmap.ic_launcher);
                } else {
                    Glide.with(getApplicationContext()).load(myUser.getImageURL()).into(circleImageView);
                }

                email.setText(firebaseUser.getEmail());
                email.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        updateEmail(email);
                    }
                });

                username.setText(myUser.getUsername());
                username.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        updateUserName(myUser);
                    }
                });

                password.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        updatePassword(context, firebaseUser, mAuth);
                    }
                });


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(context, "connection was lost try again later", Toast.LENGTH_SHORT).show();
            }
        });

        circleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImage();
            }
        });
    }

    private void openImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_REQUEST);
    }


    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = context.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage() {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Uploading");
        pd.show();

        if (imageUri != null) {
            final StorageReference fileReference = storageReference.child(System.currentTimeMillis()
                    + "." + getFileExtension(imageUri));

            uploadTask = fileReference.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        String mUri = downloadUri.toString();

                        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("imageURL", "" + mUri);
                        databaseReference.updateChildren(map);

                        pd.dismiss();
                    } else {
                        Toast.makeText(context, "Failed!", Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                }
            });
        } else {
            Toast.makeText(context, "No image selected", Toast.LENGTH_SHORT).show();
            pd.dismiss();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            imageUri = data.getData();

            if (uploadTask != null && uploadTask.isInProgress()) {
                Toast.makeText(context, "Upload in progress", Toast.LENGTH_SHORT).show();
            } else {
                uploadImage();
            }
        }
    }

    //Method of email update
    private void updateEmail(final TextView emailTV) {

        View view = LayoutInflater.from(context).inflate(R.layout.alert_dialog_email_updater, null, false);
        final AlertDialog alertDialog = new AlertDialog.Builder(context).setView(view).show();

        final EditText email = view.findViewById(R.id.emailUpET);
        final EditText passConfirm = view.findViewById(R.id.emailUpPassConfirm);

        Button confirm = view.findViewById(R.id.emailUpBtnConfirm);
        Button cancel = view.findViewById(R.id.emailUpBtnCancel);

        confirm.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {

                AuthCredential credential = EmailAuthProvider.getCredential(firebaseUser.getEmail(), passConfirm.getText().toString());
                firebaseUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(ProfileActivity.this, "password is not correct", Toast.LENGTH_SHORT).show();
                        } else {
                            firebaseUser.updateEmail(email.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        emailTV.setText(email.getText().toString());
                                        HashMap<String, Object> hashMap = new HashMap<>();
                                        hashMap.put("email", email.getText().toString());
                                        databaseReference.updateChildren(hashMap);
                                        Toast.makeText(ProfileActivity.this, "Email was changed", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }
                });

                alertDialog.dismiss();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

    }

    //Method of username update
    private void updateUserName(final MyUser myUser) {

        View view = LayoutInflater.from(context).inflate(R.layout.alert_dialog_name_updater, null, false);
        final AlertDialog alertDialog = new AlertDialog.Builder(context).setView(view).show();

        final EditText username = view.findViewById(R.id.nameUpET);

        Button confirm = view.findViewById(R.id.nameUpBtnConfirm);
        final Button cancel = view.findViewById(R.id.nameUpBtnCancel);

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("username", username.getText().toString());


                        databaseReference.updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    username.setText(myUser.getUsername());
                                    Toast.makeText(ProfileActivity.this, "Name was change successfully", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(ProfileActivity.this, "Can't change this name", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        alertDialog.dismiss();


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
    }

    //Method of password update
    public void updatePassword(final Context context, final FirebaseUser firebaseUser, final FirebaseAuth mAuth) {

        View view = LayoutInflater.from(context).inflate(R.layout.alert_dialog_password_updater, null, false);
        final AlertDialog alertDialog = new AlertDialog.Builder(context).setView(view).show();

        final EditText email = view.findViewById(R.id.passUpETEmail);

        Button confirm = view.findViewById(R.id.passUpBtnConfirm);
        Button cancel = view.findViewById(R.id.passUpBtnCancel);

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mAuth.sendPasswordResetEmail(email.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(context, "Email for reset password was sending to you", Toast.LENGTH_SHORT).show();
                            alertDialog.dismiss();
                        } else {
                            Toast.makeText(context, "Current email doesn't exist in the system", Toast.LENGTH_SHORT).show();
                        }
                    }
                });


            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
    }


}

