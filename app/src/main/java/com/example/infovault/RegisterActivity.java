package com.example.infovault;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.infovault.databinding.ActivityRegisterBinding;
import com.example.infovault.interfaces.Collections;
import com.example.infovault.interfaces.LogEvents;
import com.example.infovault.models.LogEvent;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = RegisterActivity.class.getName();
    private ActivityRegisterBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        binding.login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigateToLogin();
            }
        });

        binding.btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isEmailValid = validateEmail();
                if(!isEmailValid) return;
                boolean isPasswordValid = validatePassword();
                if(!isPasswordValid) return;

                firebaseRegister();
            }
        });

    }

    private boolean validateEmail(){
        String email = binding.etEmail.getText().toString();
        if(email.trim().isEmpty()){
            Toast.makeText(this, "Please enter your Email ID.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean validatePassword(){
        String password = binding.etPassword.getText().toString();
        if(password.trim().isEmpty()){
            Toast.makeText(this, "Please enter your Password.", Toast.LENGTH_SHORT).show();
            return false;
        }

        String confirmPassword = binding.etConfirmPassword.getText().toString();
        if(confirmPassword.trim().isEmpty()){
            Toast.makeText(this, "Please enter your Confirm Password.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(!password.equals(confirmPassword)){
            Toast.makeText(this, "Password is not matching. Please try again.", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void firebaseRegister(){
        String email = binding.etEmail.getText().toString();
        String password = binding.etPassword.getText().toString();

        Button registerBtn = binding.btnRegister;
        registerBtn.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            if(user != null){
                                logRegisterEvent(user);
                                verifyEmail(user);
                                navigateToVerifyEmail();
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthUserCollisionException)
                                Toast.makeText(RegisterActivity.this, "User with this email already exist.", Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(RegisterActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                        registerBtn.setEnabled(true);
                    }
                });
    }

    private void verifyEmail(FirebaseUser user) {
        user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(RegisterActivity.this, "Please check your email to verify the email registered with us.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(RegisterActivity.this, "Failed to send verification email to your email registered with us.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void navigateToLogin(){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    private void navigateToVerifyEmail(){
        Intent intent = new Intent(this, VerifyEmailActivity.class);
        startActivity(intent);
    }

    private void logRegisterEvent(FirebaseUser user){
        LogEvent log = new LogEvent();
        log.uuid = user.getUid();
        log.emailId = user.getEmail();
        log.event = LogEvents.REGISTER;
        log.timestamp = Instant.now().toString();

        db.collection(Collections.LOGS)
                .add(log)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if(task.isSuccessful())
                            Log.d(TAG, "register event logged successfully.");
                        else
                            Log.e(TAG, "register event failed to be logged.");
                    }
                });
    }

}