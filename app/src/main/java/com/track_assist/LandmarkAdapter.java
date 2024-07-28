package com.track_assist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LandmarkAdapter extends RecyclerView.Adapter<LandmarkAdapter.LandmarkViewHolder> {

    private final List<Landmark> landmarkList;

    public LandmarkAdapter(List<Landmark> landmarkList) {
        this.landmarkList = landmarkList;
    }

    @NonNull
    @Override
    public LandmarkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.landmark_item, parent, false);
        return new LandmarkViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LandmarkViewHolder holder, int position) {
        Landmark landmark = landmarkList.get(position);
        holder.nameTextView.setText(landmark.getName());
        holder.addressTextView.setText(landmark.getAddress());
    }

    @Override
    public int getItemCount() {
        return landmarkList.size();
    }

    static class LandmarkViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView addressTextView;

        public LandmarkViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.landmark_name);
            addressTextView = itemView.findViewById(R.id.landmark_address);
        }
    }
}
