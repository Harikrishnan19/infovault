package com.example.infovault.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.infovault.R;
import com.example.infovault.models.LogEvent;
import com.google.type.DateTime;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

public class LogsAdapter extends RecyclerView.Adapter<LogsAdapter.ViewHolder> {

    private static final String TAG = LogsAdapter.class.getName();
    private final Context context;
    private List<LogEvent> list;

    public LogsAdapter(Context context){
        this.context = context;
        this.list = new ArrayList<>();
    }

    public void setList(List<LogEvent> list){
        this.list = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LogsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.log_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LogsAdapter.ViewHolder holder, int position) {
        holder.emailId.setText(list.get(position).emailId);
        holder.uuid.setText(list.get(position).uuid);
        holder.event.setText(list.get(position).event);

        String isoTimestamp = list.get(position).timestamp;
        String displayDateTime = Instant.parse(isoTimestamp)
                .atZone(ZoneId.systemDefault())
                .toOffsetDateTime()
                .format(DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a"));
        holder.timestamp.setText(displayDateTime);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView emailId;
        private final TextView uuid;
        private final TextView event;
        private final TextView timestamp;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            emailId = itemView.findViewById(R.id.email_id);
            uuid = itemView.findViewById(R.id.uuid);
            event = itemView.findViewById(R.id.event_type);
            timestamp = itemView.findViewById(R.id.timestamp);
        }
    }
}
