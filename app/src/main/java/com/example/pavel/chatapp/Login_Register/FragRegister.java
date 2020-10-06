package com.example.pavel.chatapp.Login_Register;

import android.content.Context;
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
import com.example.pavel.chatapp.Adapter_Modul.Items.MyUser;
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

//TODO make toasts for register method, notify user what happen in current time

public class FragRegister extends Fragment {

    private EditText username_et, mail_et, pass1_et, pass2_et;
    private Boolean currentUserExist;
    private Button confirm, cancel;
    private Context context;

    private DatabaseReference databaseReference;
    private FirebaseUser firebaseUser;
    private FirebaseAuth mAuth;

    public FragRegister(Context context){
        this.context = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, null, false);
        setObjectsReferences(view);
        initializeListeners();
        return view;
    }

    private void setObjectsReferences(View view) {
        mAuth = FirebaseAuth.getInstance();

        username_et = view.findViewById(R.id.regETUsername);
        mail_et = view.findViewById(R.id.regETEmail);
        pass1_et = view.findViewById(R.id.regETPass);
        pass2_et = view.findViewById(R.id.regETPass2);

        confirm = view.findViewById(R.id.regBtnConfirm);
        cancel = view.findViewById(R.id.regBtnCancel);
    }

    private void initializeListeners() {
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerNewUser();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearAllFields();
            }
        });
    }

    private void registerNewUser() {
        if (checkCorrectInput()) {
            addUserToFirebaseAuth(mail_et.getText().toString(), pass1_et.getText().toString());
        }
    }

    private void clearAllFields() {
        username_et.setText("");
        mail_et.setText("");
        pass1_et.setText("");
        pass2_et.setText("");
    }

    private boolean checkCorrectInput() {
        if (!checkIfInputFieldsIsEmpty()) {
            Toast.makeText(getView().getContext(), R.string.toast_reg_empty_field, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!checkIfPasswordsIsSame()) {
            Toast.makeText(getView().getContext(), R.string.toast_reg_not_same_passwords, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean checkIfPasswordsIsSame() {
        return pass1_et.getText().toString().equals(pass2_et.getText().toString());
    }

    private boolean checkIfInputFieldsIsEmpty() {
        return !username_et.getText().toString().isEmpty()
                || !mail_et.getText().toString().isEmpty()
                || !pass1_et.getText().toString().isEmpty()
                || !pass2_et.getText().toString().isEmpty();
    }

    private void addUserToFirebaseAuth(final String mail, final String pass) {
        //Check if current email exist in firebase database
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                MyUser myUser = dataSnapshot.getValue(MyUser.class);
                if (myUser != null) {

                    //If email not exist create new account
                    mAuth.createUserWithEmailAndPassword(mail, pass).
                            addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {

                                        firebaseUser = mAuth.getCurrentUser();
                                        //Initialize new user for future settings
                                        HashMap<String, String> hashMap = new HashMap<>();
                                        hashMap.put("id", firebaseUser.getUid());
                                        hashMap.put("email", mail);
                                        hashMap.put("username", username_et.getText().toString());
                                        hashMap.put("imageURL", "default");
                                        hashMap.put("status", "offline");
                                        hashMap.put("search", username_et.getText().toString().toLowerCase());


                                        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
                                        databaseReference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {

                                                    Intent intent = new Intent(context, ChatActivity.class);
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                    startActivity(intent);
                                                    Toast.makeText(context, username_et.getText().toString() + getString(R.string.toast_reg_welcome_to_chat), Toast.LENGTH_SHORT).show();

                                                }
                                            }
                                        });
                                    }
                                }
                            });
                } else {
                    Toast.makeText(context, R.string.toast_reg_user_exist, Toast.LENGTH_SHORT).show();
                    currentUserExist = true;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }


}



