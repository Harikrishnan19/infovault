package com.example.infovault;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.infovault.databinding.ActivityLoginBinding;
import com.example.infovault.interfaces.Collections;
import com.example.infovault.interfaces.LogEvents;
import com.example.infovault.models.LogEvent;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = LoginActivity.class.getName();
    private ActivityLoginBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            navigateToHome();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        binding.register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigateToRegister();
            }
        });

        binding.forgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isEmailValid = validateEmail();
                if(!isEmailValid) return;

                forgotPassword();
            }
        });

        binding.btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isEmailValid = validateEmail();
                if(!isEmailValid) return;
                boolean isPasswordValid = validatePassword();
                if(!isPasswordValid) return;

                firebaseLogin();
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
        return true;
    }

    private void firebaseLogin(){
        String email = binding.etEmail.getText().toString();
        String password = binding.etPassword.getText().toString();
        Button button = binding.btnLogin;
        button.setEnabled(false);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            if(user != null){
                                logLoginEvent(user);
                                navigateToHome();
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                        }
                        button.setEnabled(true);
                    }
                });
    }

    private void forgotPassword(){
        String email = binding.etEmail.getText().toString();

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "Password reset email sent.");
                            Toast.makeText(LoginActivity.this,
                                    "A password reset email has been sent. Please check your registered email.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(LoginActivity.this, "Forget password failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void navigateToRegister(){
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    private void navigateToHome(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void logLoginEvent(FirebaseUser user){
        LogEvent log = new LogEvent();
        log.uuid = user.getUid();
        log.emailId = user.getEmail();
        log.event = LogEvents.LOG_IN;
        log.timestamp = Instant.now().toString();

        db.collection(Collections.LOGS)
                .add(log)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if(task.isSuccessful())
                            Log.d(TAG, "login event logged successfully.");
                        else
                            Log.e(TAG, "login event failed to be logged.");
                    }
                });
    }
}