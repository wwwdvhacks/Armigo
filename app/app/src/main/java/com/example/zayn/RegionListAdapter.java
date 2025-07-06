package com.example.zayn;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.zayn.model.Region;
import java.util.ArrayList;
import java.util.List;

public class RegionListAdapter extends RecyclerView.Adapter<RegionListAdapter.RegionViewHolder> {
    public interface OnRegionClickListener {
        void onRegionClick(Region region);
    }

    private List<Region> regionList = new ArrayList<>();
    private final OnRegionClickListener listener;

    public RegionListAdapter(OnRegionClickListener listener) {
        this.listener = listener;
    }

    public void setRegionList(List<Region> regions) {
        this.regionList = regions;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RegionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_region, parent, false);
        return new RegionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RegionViewHolder holder, int position) {
        Region region = regionList.get(position);
        holder.bind(region, listener);
    }

    @Override
    public int getItemCount() {
        return regionList.size();
    }

    static class RegionViewHolder extends RecyclerView.ViewHolder {
        private final TextView regionName;
        public RegionViewHolder(@NonNull View itemView) {
            super(itemView);
            regionName = itemView.findViewById(R.id.textViewRegionName);
        }
        public void bind(final Region region, final OnRegionClickListener listener) {
            regionName.setText(region.getName());
            itemView.setOnClickListener(v -> listener.onRegionClick(region));
        }
    }
} 