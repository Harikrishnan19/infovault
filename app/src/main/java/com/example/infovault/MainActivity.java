package com.example.infovault;

import static com.example.infovault.interfaces.Constants.GENDER_ITEMS;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.infovault.databinding.ActivityMainBinding;
import com.example.infovault.interfaces.Collections;
import com.example.infovault.interfaces.LogEvents;
import com.example.infovault.interfaces.Roles;
import com.example.infovault.models.LogEvent;
import com.example.infovault.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.Instant;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();
    private ActivityMainBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = getCurrentUser();
        if(currentUser == null){
            navigateToLogin();
        } else {
            binding.email.setText(getCurrentUser().getEmail());
            enableAdminFn();
            fetchData();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        Toolbar toolbar = binding.toolbar.getRoot();
        setSupportActionBar(toolbar);

        binding.email.setEnabled(false);
        initializeSpinner();

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        binding.saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveData();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.log_out){
            FirebaseUser user = getCurrentUser();
            if(user != null){
                logLogoutEvent(user);
                firebaseLogout();
            }
            navigateToLogin();
        } else if(item.getItemId() == R.id.view_logs){
            navigateToLogs();
        }

        return super.onOptionsItemSelected(item);
    }

    private void saveData(){
        binding.saveBtn.setEnabled(false);
        User user = captureValues();
        String userId = mAuth.getCurrentUser().getUid();
        DocumentReference docRef = db.collection(Collections.USERS).document(userId);
                docRef.set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                        Toast.makeText(MainActivity.this, "Saved successfully.", Toast.LENGTH_SHORT).show();
                        binding.saveBtn.setEnabled(true);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                        Toast.makeText(MainActivity.this, "Failed to save.", Toast.LENGTH_SHORT).show();
                        binding.saveBtn.setEnabled(true);
                    }
                });
    }

    private void enableAdminFn(){
        String userId = mAuth.getCurrentUser().getUid();
        db.collection(Collections.ROLE_USER_MT)
                .whereEqualTo("uuid", userId)
                .whereEqualTo("role", Roles.ADMIN)
                .limit(1)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful() && !task.getResult().isEmpty()){
                            MenuItem menuItem = binding.toolbar.getRoot().getMenu().findItem(R.id.view_logs);
                            menuItem.setVisible(true);
                        }
                    }
                });
    }

    private User captureValues(){
        User user = new User();
        user.firstName = binding.firstName.getText().toString();
        user.lastName = binding.lastName.getText().toString();
        user.gender = binding.gender.getSelectedItem().toString();
        user.mobile = binding.mobile.getText().toString();
        user.email = binding.email.getText().toString();
        user.address = binding.address.getText().toString();
        user.city = binding.city.getText().toString();
        user.pincode = binding.pincode.getText().toString();
        return user;
    }

    private void fetchData(){
        String userId = mAuth.getCurrentUser().getUid();
        DocumentReference docRef = db.collection(Collections.USERS).document(userId);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        User user = document.toObject(User.class);
                        initializeViewsData(user);
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    private void initializeViewsData(User data){
        if(!TextUtils.isEmpty(data.firstName)) binding.firstName.setText(data.firstName);
        if(!TextUtils.isEmpty(data.lastName)) binding.lastName.setText(data.lastName);
        if(!TextUtils.isEmpty(data.gender)) {
            ArrayAdapter<String> adapter = (ArrayAdapter<String>) binding.gender.getAdapter();
            binding.gender.setSelection(adapter.getPosition(data.gender));
        }
        if(!TextUtils.isEmpty(data.mobile)) binding.mobile.setText(data.mobile);
        if(!TextUtils.isEmpty(data.address)) binding.address.setText(data.address);
        if(!TextUtils.isEmpty(data.city)) binding.city.setText(data.city);
        if(!TextUtils.isEmpty(data.pincode)) binding.pincode.setText(data.pincode);
    }

    private void initializeSpinner(){
        Spinner spinner = binding.gender;
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, GENDER_ITEMS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
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

    private void navigateToLogs(){
        Intent intent = new Intent(this, LogsActivity.class);
        startActivity(intent);
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