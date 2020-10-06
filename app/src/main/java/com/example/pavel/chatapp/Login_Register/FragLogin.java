package com.example.pavel.chatapp.Login_Register;

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

public class FragLogin extends Fragment {

    private FirebaseAuth mAuth;
    private EditText email_et, pass_et;
    private Button confirmLogin, cancelLogin;
    private TextView resetPassword;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, null, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initializeObjects();
        initializeListeners();
    }

    private void initializeListeners() {
        resetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initializeAlertDialogForResetPassword();
            }
        });

        confirmLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkIfCurrentUserCreated();

            }
        });

        cancelLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetEditTextObjects();
            }
        });
    }

    private void checkIfCurrentUserCreated() {
        if (checkIfFieldsIsNotEmpty()) {
            Toast.makeText(getView().getContext(), R.string.toast_login_required_fields, Toast.LENGTH_SHORT).show();
        } else {
            mAuth.signInWithEmailAndPassword(email_et.getText().toString(), pass_et.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        startMainActivity();
                    } else {
                        Toast.makeText(getView().getContext(), "Error while entering please try again later", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
    }

    private boolean checkIfFieldsIsNotEmpty() {
        return email_et.getText().toString().isEmpty() || pass_et.getText().toString().isEmpty();
    }

    private void initializeObjects() {
        email_et = getView().findViewById(R.id.loginETLogin);
        pass_et = getView().findViewById(R.id.loginETPass);
        mAuth = FirebaseAuth.getInstance();

        resetPassword = getView().findViewById(R.id.loginTVResetPass);

        confirmLogin = getView().findViewById(R.id.loginBtnConfirm);
        cancelLogin = getView().findViewById(R.id.login_Btn_cancel);
    }

    private void initializeAlertDialogForResetPassword() {
        ProfileActivity profileActivity = new ProfileActivity();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        profileActivity.updatePassword(getView().getContext(), firebaseUser, mAuth);
    }

    private void startMainActivity() {
        Intent intent = new Intent(getView().getContext(), ChatActivity.class);
        startActivity(intent);
        Toast.makeText(getView().getContext(), "Welcome", Toast.LENGTH_SHORT).show();
    }

    private void resetEditTextObjects() {
        email_et.setText("");
        pass_et.setText("");
    }

}