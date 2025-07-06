package com.example.zayn;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RegionDetailFragment extends Fragment {
    private static final String ARG_REGION_NAME = "region_name";
    private String regionName;
    private TextView contentTextView;
    private Button btnHistory, btnKitchen, btnDances;
    private TextView regionNameTextView;
    private Button buttonBack;

    public static RegionDetailFragment newInstance(String regionName) {
        RegionDetailFragment fragment = new RegionDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_REGION_NAME, regionName);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        try {
            android.util.Log.d("RegionDetailFragment", "Creating view...");
            View view = inflater.inflate(R.layout.fragment_region_detail, container, false);
            
            // Find views with null checks
            contentTextView = view.findViewById(R.id.textViewRegionContent);
            btnHistory = view.findViewById(R.id.buttonHistory);
            btnKitchen = view.findViewById(R.id.buttonKitchen);
            btnDances = view.findViewById(R.id.buttonDances);
            regionNameTextView = view.findViewById(R.id.textViewRegionName);
            buttonBack = view.findViewById(R.id.buttonBack);

            android.util.Log.d("RegionDetailFragment", "All views found successfully");

            if (getArguments() != null) {
                regionName = getArguments().getString(ARG_REGION_NAME);
                android.util.Log.d("RegionDetailFragment", "Region name from arguments: " + regionName);
                if (regionName != null) {
                    regionNameTextView.setText(regionName);
                } else {
                    regionNameTextView.setText("Unknown Region");
                }
            } else {
                android.util.Log.w("RegionDetailFragment", "No arguments provided");
                regionNameTextView.setText("Unknown Region");
            }

            buttonBack.setOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            });

            btnHistory.setOnClickListener(v -> loadContent("history"));
            btnKitchen.setOnClickListener(v -> loadContent("kitchen"));
            btnDances.setOnClickListener(v -> loadContent("dances_songs"));

            // Load history by default
            loadContent("history");
            
            android.util.Log.d("RegionDetailFragment", "View created successfully");
            return view;
        } catch (Exception e) {
            android.util.Log.e("RegionDetailFragment", "Error in onCreateView: " + e.getMessage(), e);
            if (getContext() != null) {
                Toast.makeText(getContext(), "Error loading region details: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
            return null;
        }
    }

    private void loadContent(String type) {
        if (regionName == null || regionName.isEmpty()) {
            contentTextView.setText("Region name not available.");
            return;
        }
        
        try {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference ref = database.getReference("regions").child(regionName).child(type);
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (contentTextView != null) {
                        String content = snapshot.getValue(String.class);
                        if (content != null && !content.isEmpty()) {
                            contentTextView.setText(content);
                        } else {
                            contentTextView.setText("No data available for this category.");
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Failed to load data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            if (contentTextView != null) {
                contentTextView.setText("Error loading content.");
            }
            if (getContext() != null) {
                Toast.makeText(getContext(), "Error loading content", Toast.LENGTH_SHORT).show();
            }
        }
    }
} 