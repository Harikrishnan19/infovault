package com.example.infovault;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.infovault.adapters.LogsAdapter;
import com.example.infovault.databinding.ActivityLogsBinding;
import com.example.infovault.interfaces.Collections;
import com.example.infovault.models.LogEvent;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class LogsActivity extends AppCompatActivity {

    ActivityLogsBinding binding;
    FirebaseFirestore db;

    @Override
    protected void onStart() {
        super.onStart();
        fetchLogs();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLogsBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        initiateToolbar();
        initiateRecyclerView();

        db = FirebaseFirestore.getInstance();
    }

    private void initiateToolbar(){
        Toolbar toolbar = binding.logsToolbar.getRoot();
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void initiateRecyclerView(){
        RecyclerView recyclerView = binding.logList;
        LogsAdapter adapter = new LogsAdapter(getApplicationContext());
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void fetchLogs(){
        db.collection(Collections.LOGS).orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(!task.isSuccessful()){
                            Toast.makeText(LogsActivity.this, "Failed to fetch logs", Toast.LENGTH_SHORT).show();;
                        } else if(task.getResult().isEmpty()) {
                            Toast.makeText(LogsActivity.this, "No logs found.", Toast.LENGTH_SHORT).show();
                        } else {
                            List<LogEvent> data = new ArrayList<>();
                            for( DocumentSnapshot snapshot : task.getResult().getDocuments()){
                                data.add(snapshot.toObject(LogEvent.class));
                            }
                            ((LogsAdapter) binding.logList.getAdapter()).setList(data);
                        }
                    }
                });
    }
}