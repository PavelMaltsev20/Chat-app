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
import android.widget.TextView;
import android.widget.Toast;

import com.example.pavel.chatapp.Chat.ChatActivity;
import com.example.pavel.chatapp.ProfileActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.example.pavel.chatapp.R;


public class LoginFragment extends Fragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, null, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        final FirebaseAuth mAuth = FirebaseAuth.getInstance();

        final EditText mailET = getView().findViewById(R.id.loginETLogin);
        final EditText passET = getView().findViewById(R.id.loginETPass);

        Button confirmLogin = getView().findViewById(R.id.loginBtnConfirm);
        Button cancelLogin = getView().findViewById(R.id.loginBtnCancel);

        TextView resetPassword = getView().findViewById(R.id.loginTVResetPass);

        confirmLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog progressDialog = new ProgressDialog(getView().getContext());
                progressDialog.show();
                if (mailET.getText().toString().isEmpty() || passET.getText().toString().isEmpty()) {
                    Toast.makeText(getView().getContext(), "Fields are required", Toast.LENGTH_SHORT).show();
                } else {
                    mAuth.signInWithEmailAndPassword(mailET.getText().toString(), passET.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                mailET.setText("");
                                passET.setText("");
                                Intent intent = new Intent(getView().getContext(), ChatActivity.class);
                                progressDialog.dismiss();
                                startActivity(intent);
                                Toast.makeText(getView().getContext(), "Welcome", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getView().getContext(), "Check your connection", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    progressDialog.dismiss();
                }

            }
        });


        cancelLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mailET.setText("");
                passET.setText("");
            }
        });

        resetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ProfileActivity profileActivity = new ProfileActivity();
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                profileActivity.updatePassword(getView().getContext(), firebaseUser, mAuth);

            }
        });

    }
}
