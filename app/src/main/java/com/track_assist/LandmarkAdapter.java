package com.track_assist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class LandmarkAdapter extends RecyclerView.Adapter<LandmarkAdapter.LandmarkViewHolder> {

    private List<Landmark> landmarks;

    public LandmarkAdapter(List<Landmark> landmarks) {
        this.landmarks = landmarks;
    }

    @NonNull
    @Override
    public LandmarkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.landmark_item, parent, false);
        return new LandmarkViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LandmarkViewHolder holder, int position) {
        Landmark landmark = landmarks.get(position);
        holder.name.setText(landmark.getName());
        holder.address.setText(landmark.getAddress());
        holder.timeAgo.setText(getTimeAgo(landmark.getTimestamp()));
    }

    @Override
    public int getItemCount() {
        return landmarks.size();
    }

    public static class LandmarkViewHolder extends RecyclerView.ViewHolder {

        TextView name, address, timeAgo;

        public LandmarkViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.landmarkName);
            address = itemView.findViewById(R.id.landmarkAddress);
            timeAgo = itemView.findViewById(R.id.timeAgo);
        }
    }

    private String getTimeAgo(long time) {
        long currentTime = System.currentTimeMillis();
        long diff = currentTime - time;

        long seconds = TimeUnit.MILLISECONDS.toSeconds(diff);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
        long hours = TimeUnit.MILLISECONDS.toHours(diff);
        long days = TimeUnit.MILLISECONDS.toDays(diff);

        if (seconds < 60) {
            return "just now";
        } else if (minutes < 60) {
            return minutes + " min ago";
        } else if (hours < 24) {
            return hours + " hrs ago";
        } else {
            return days + " days ago";
        }
    }
}
