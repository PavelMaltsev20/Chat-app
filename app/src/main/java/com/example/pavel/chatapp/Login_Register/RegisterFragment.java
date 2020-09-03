package com.example.pavel.chatapp.Login_Register;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.pavel.chatapp.Adapter_Modul.MyUser;
import com.example.pavel.chatapp.Chat.ChatActivity;
import com.example.pavel.chatapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class RegisterFragment extends Fragment {

    EditText usernameET, mailET, pass1ET, pass2ET;
    Boolean currentUserExists;

    FirebaseAuth mAuth;
    DatabaseReference databaseReference;
    FirebaseUser firebaseUser;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, null, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setPointer();

    }

    private void setPointer() {

        mAuth = FirebaseAuth.getInstance();

        usernameET = getView().findViewById(R.id.regETUsername);
        mailET = getView().findViewById(R.id.regETEmail);
        pass1ET = getView().findViewById(R.id.regETPass);
        pass2ET = getView().findViewById(R.id.regETPass2);

        Button confirm = getView().findViewById(R.id.regBtnConfirm);
        Button cancel = getView().findViewById(R.id.regBtnCancel);

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (checkEnteredFields(mailET.getText().toString())) {
                    //If all ok create new user
                    registerMethod(mailET.getText().toString(), pass1ET.getText().toString());
                }


            }
        });

        //Clear all fields
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                usernameET.setText("");
                mailET.setText("");
                pass1ET.setText("");
                pass2ET.setText("");
            }
        });


    }

    private boolean checkEnteredFields(String email) {

        //Check if all data is  correct and then we create new user

        //Check if all fields not empty
        if (!mailET.getText().toString().isEmpty() || !pass1ET.getText().toString().isEmpty() || !pass2ET.getText().toString().isEmpty()) {

            //Check if password is same
            if (pass1ET.getText().toString().equals(pass2ET.getText().toString())) {

                //Check if user with same email exist
                if (true) {
                    return true;

                } else {
                    Toast.makeText(getView().getContext(), "Another user already exists in the system with the same login name", Toast.LENGTH_SHORT).show();
                    return false;
                }

            } else {
                Toast.makeText(getView().getContext(), "Passwords should be same", Toast.LENGTH_SHORT).show();
                return false;
            }
        } else {
            Toast.makeText(getView().getContext(), "One of fields is empty", Toast.LENGTH_SHORT).show();
            return false;
        }

    }


    private boolean checkEmailOfNewUser(String email) {

        databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                MyUser myUser = dataSnapshot.getValue(MyUser.class);
                if (myUser != null) {
                    currentUserExists = false;
                    Toast.makeText(getView().getContext(), "FIRST", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getView().getContext(), "SECOND", Toast.LENGTH_SHORT).show();
                currentUserExists = true;
            }
        });

        return currentUserExists;

    }


    private void registerMethod(final String mail, String pass1) {

        final ProgressDialog progressDialog = new ProgressDialog(getView().getContext());
        progressDialog.show();
        mAuth.createUserWithEmailAndPassword(mail, pass1).
                addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            firebaseUser = mAuth.getCurrentUser();
                            databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

                            HashMap<String, String> hashMap = new HashMap<>();
                            hashMap.put("id", firebaseUser.getUid());
                            hashMap.put("email", mail);
                            hashMap.put("username", usernameET.getText().toString());
                            hashMap.put("imageURL", "default");
                            hashMap.put("status", "offline");
                            hashMap.put("search", usernameET.getText().toString().toLowerCase());

                            databaseReference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {

                                        Intent intent = new Intent(getView().getContext(), ChatActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        Toast.makeText(getView().getContext(), mailET.getText().toString() + " welcome to chat", Toast.LENGTH_SHORT).show();

                                    }
                                }
                            });
                        } else {
                            Toast.makeText(getView().getContext(), "Check your email and name", Toast.LENGTH_SHORT).show();
                        }
                        progressDialog.dismiss();
                    }
                });
    }

}



