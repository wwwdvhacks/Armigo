package com.example.zayn;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.zayn.model.Region;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements RegionListAdapter.OnRegionClickListener {
    private RecyclerView recyclerView;
    private RegionListAdapter adapter;
    private final List<Region> regionList = new ArrayList<>();
    private static final String TAG = "HomeFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewRegions);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new RegionListAdapter(this);
        recyclerView.setAdapter(adapter);
        loadRegionsFromFirebase();
        return view;
    }

    private void loadRegionsFromFirebase() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("regions");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                regionList.clear();
                if (!snapshot.exists()) {
                    Toast.makeText(getContext(), "No regions found in database.", Toast.LENGTH_SHORT).show();
                    Log.w(TAG, "No regions found in database.");
                } else {
                    for (DataSnapshot regionSnap : snapshot.getChildren()) {
                        Region region = regionSnap.getValue(Region.class);
                        if (region != null) {
                            region.setName(regionSnap.getKey()); // Set name from key if not present
                            regionList.add(region);
                        } else {
                            Log.w(TAG, "Null region object for key: " + regionSnap.getKey());
                        }
                    }
                    Log.d(TAG, "Loaded regions: " + regionList.size());
                }
                adapter.setRegionList(new ArrayList<>(regionList));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load regions: " + error.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "Firebase error: " + error.getMessage());
            }
        });
    }

    @Override
    public void onRegionClick(Region region) {
        try {
            Log.d(TAG, "Region clicked: " + region.getName());
            
            if (region == null) {
                Log.e(TAG, "Region is null");
                Toast.makeText(getContext(), "Region data is invalid", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (region.getName() == null || region.getName().isEmpty()) {
                Log.e(TAG, "Region name is null or empty");
                Toast.makeText(getContext(), "Region name is invalid", Toast.LENGTH_SHORT).show();
                return;
            }
            
            Log.d(TAG, "Creating RegionDetailFragment for: " + region.getName());
            RegionDetailFragment fragment = RegionDetailFragment.newInstance(region.getName());
            
            if (fragment == null) {
                Log.e(TAG, "Failed to create RegionDetailFragment");
                Toast.makeText(getContext(), "Failed to create region detail view", Toast.LENGTH_SHORT).show();
                return;
            }
            
            Log.d(TAG, "Starting fragment transaction");
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
            
            Log.d(TAG, "Fragment transaction completed");
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to region detail: " + e.getMessage(), e);
            Toast.makeText(getContext(), "Error opening region details: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
} 