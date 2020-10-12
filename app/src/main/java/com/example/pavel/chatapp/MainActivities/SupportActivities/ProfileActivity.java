package com.example.pavel.chatapp.MainActivities.SupportActivities;

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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.pavel.chatapp.AdaptersAndModulus.Items.MyUser;
import com.example.pavel.chatapp.AdaptersAndModulus.SharedPref;
import com.example.pavel.chatapp.MainActivities.UsersScreens.ActivityUsersContainer;
import com.example.pavel.chatapp.R;
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

import static com.example.pavel.chatapp.MainActivities.ChatWithUserActivity.status;

public class
ProfileActivity extends AppCompatActivity {

    private TextView title, email_tv, username_tv, password_tv;
    private EditText email_name_et, pass_et;
    private CircleImageView circleImageView;
    private Button confirm, cancel;
    private ProgressBar progressBar;
    private Context context;

    private static final int IMAGE_REQUEST = 1;
    private StorageTask uploadTask;
    private SharedPref sharedPref;
    private Uri imageUri;

    private int email_update_task = 1;
    private int name_update_task = 2;
    private int pass_update_task = 3;

    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private FirebaseUser firebaseUser;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(ActivityUsersContainer.setTheme(this, initView()));

        initializeObjects();
        setListeners();
        getUserDataFromFirebase();
    }

    public View initView() {
        return LayoutInflater.from(this).inflate(R.layout.activity_profile, null, false);
    }

    private void initializeObjects() {
        if (context == null)//if called from login fragment
            context = this;

        circleImageView = findViewById(R.id.profile_IV);
        email_tv = findViewById(R.id.profile_TV_email);
        username_tv = findViewById(R.id.profile_TV_name);
        password_tv = findViewById(R.id.profile_TV_pass);
        progressBar = findViewById(R.id.profile_PB);

        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        storageReference = FirebaseStorage.getInstance().getReference("uploads");
    }

    private void setListeners() {
        circleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGalleryOnDevice();
            }
        });

        email_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initAlertDialogForUpdateEmail();
            }
        });

        username_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initAlertDialogForUpdateName();
            }
        });

        password_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initAlertDialogForUpdatePass(context);
            }
        });
    }

    private void getUserDataFromFirebase() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final MyUser myUser = dataSnapshot.getValue(MyUser.class);
                setUserImage(myUser);
                setUserEmail();
                setUserName(myUser);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(context, "connection was lost try again later", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setUserImage(MyUser myUser) {
        if (myUser.getImageURL().equals("default")) {
            circleImageView.setImageResource(R.drawable.ic_user_profile);
        } else {
            Glide.with(getApplicationContext())
                    .load(myUser.getImageURL())
                    .into(circleImageView);
        }
    }

    private void setUserEmail() {
        email_tv.setText(firebaseUser.getEmail());
    }

    private void setUserName(MyUser myUser) {
        String name = myUser.getUsername();
        name = name.substring(0, 1).toUpperCase() + name.substring(1);
        username_tv.setText(name);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void onResume() {
        super.onResume();
        status("online"); //Static method from 'ChatWithUserActivity'
    }

    @Override
    public void onPause() {
        super.onPause();
        status("offline");//Static method from 'ChatWithUserActivity'
    }

    //----------------------------------------Part of updating user data in firebase----------------------------------------
    private View initObjectsForAlertDialog(int task) {
        View view = LayoutInflater.from(context).inflate(R.layout.alert_dialog_update_user_data, null, false);
        title = view.findViewById(R.id.alertDialog_title);
        email_name_et = view.findViewById(R.id.alertDialog_updateEmail);
        pass_et = view.findViewById(R.id.alertDialog_updatePass);
        confirm = view.findViewById(R.id.alertDialog_btnConfirm);
        cancel = view.findViewById(R.id.alertDialog_btnCancel);

        setUpAlertDialogForCurrentTask(task);

        return view;
    }

    private void setUpAlertDialogForCurrentTask(int task) {
        if (task == email_update_task) {
            title.setText("Changing email");
            email_name_et.setHint("Enter new email");
            pass_et.setHint("Enter password");
        } else if (task == name_update_task) {
            title.setText("Changing name");
            email_name_et.setHint("Enter name");
            pass_et.setVisibility(View.GONE);
        } else if (task == pass_update_task) {
            title.setText("Changing password");
            email_name_et.setHint("Enter email for change password");
            pass_et.setVisibility(View.GONE);
        }

    }

    //---------------------Update image---------------------
    private void openGalleryOnDevice() {
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            imageUri = data.getData();

            if (uploadTask != null && uploadTask.isInProgress()) {
                Toast.makeText(context, "Upload in progress", Toast.LENGTH_SHORT).show();
            } else {
                progressBar.setVisibility(View.VISIBLE);
                uploadImage();
            }
        }
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
        progressBar.setVisibility(View.GONE);
    }

    //---------------------Update email---------------------
    private void initAlertDialogForUpdateEmail() {
        final AlertDialog alertDialog = new AlertDialog.Builder(context).setView(initObjectsForAlertDialog(email_update_task)).show();

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                updateEmailInFirebase();
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

    private void updateEmailInFirebase() {
        AuthCredential credential = EmailAuthProvider.getCredential(firebaseUser.getEmail(), pass_et.getText().toString());
        firebaseUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (!task.isSuccessful()) {
                    Toast.makeText(ProfileActivity.this, "password is not correct", Toast.LENGTH_SHORT).show();
                } else {
                    firebaseUser.updateEmail(ProfileActivity.this.email_name_et.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                email_tv.setText(ProfileActivity.this.email_name_et.getText().toString());
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("email", ProfileActivity.this.email_name_et.getText().toString());
                                databaseReference.updateChildren(hashMap);
                                Toast.makeText(ProfileActivity.this, "Email was changed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    //---------------------Update name---------------------
    private void initAlertDialogForUpdateName() {
        final AlertDialog alertDialog = new AlertDialog.Builder(context).setView(initObjectsForAlertDialog(name_update_task)).show();

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                updateNameInFirebase();
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

    private void updateNameInFirebase() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("username", email_name_et.getText().toString());

                databaseReference.updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            email_name_et.setText(email_name_et.getText().toString());
                            Toast.makeText(ProfileActivity.this, "Name was changed successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ProfileActivity.this, "Can't change this name", Toast.LENGTH_SHORT).show();
                        }
                        progressBar.setVisibility(View.GONE);

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);

            }
        });
    }

    //---------------------Update pass---------------------
    public void initAlertDialogForUpdatePass(Context context) {
        this.context = context;
        final AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setView(initObjectsForAlertDialog(pass_update_task))
                .show();

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (progressBar != null)//if called from login fragment
                    progressBar.setVisibility(View.VISIBLE);
                sendEmailForResetPass();
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

    private void sendEmailForResetPass() {
        mAuth = FirebaseAuth.getInstance();
        mAuth.sendPasswordResetEmail(email_name_et.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(context, "Email for reset password was sent to you", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Current email doesn't exist in the system", Toast.LENGTH_SHORT).show();
                }
                if (progressBar != null) //if called from login fragment
                    progressBar.setVisibility(View.GONE);
            }
        });
    }

}

