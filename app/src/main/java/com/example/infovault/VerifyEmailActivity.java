package com.example.infovault;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.infovault.databinding.ActivityVerifyEmailBinding;
import com.example.infovault.interfaces.Collections;
import com.example.infovault.interfaces.LogEvents;
import com.example.infovault.models.LogEvent;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.Instant;

public class VerifyEmailActivity extends AppCompatActivity {

    private static final String TAG = VerifyEmailActivity.class.getName();
    private ActivityVerifyEmailBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = getCurrentUser();
        if(currentUser == null){
            navigateToLogin();
        } else if(currentUser.isEmailVerified()) {
            navigateToHome();
        } else {
            String text = "Please verify the email ( " + getCurrentUser().getEmail() + " ) registered with us.";
            binding.verifyEmailTitle.setText(text);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVerifyEmailBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        binding.logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseUser user = getCurrentUser();
                firebaseLogout();
                logLogoutEvent(user);
                navigateToLogin();
            }
        });

        binding.resendVerificationEmailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseUser user = getCurrentUser();
                resendEmailVerificationLink(user);
            }
        });

        mAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null && user.isEmailVerified()){
                    navigateToHome();
                }
            }
        });
    }



    private FirebaseUser getCurrentUser(){
        return mAuth.getCurrentUser();
    }

    private void firebaseLogout() {
        mAuth.signOut();
    }

    private void navigateToLogin(){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    private void navigateToHome(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void resendEmailVerificationLink(FirebaseUser user){
        user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(VerifyEmailActivity.this, "Please check your email to verify the email registered with us.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(VerifyEmailActivity.this, "Failed to send verification email to your email registered with us.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void logLogoutEvent(FirebaseUser user){
        LogEvent log = new LogEvent();
        log.uuid = user.getUid();
        log.emailId = user.getEmail();
        log.event = LogEvents.LOG_OUT;
        log.timestamp = Instant.now().toString();

        db.collection(Collections.LOGS)
                .add(log)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if(task.isSuccessful())
                            Log.d(TAG, "logout event logged successfully.");
                        else
                            Log.e(TAG, "logout event failed to be logged.");
                    }
                });
    }
}