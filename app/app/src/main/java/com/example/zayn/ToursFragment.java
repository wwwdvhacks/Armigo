package com.example.zayn;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class ToursFragment extends Fragment {
    private RecyclerView recyclerView;
    private TourAdapter adapter;
    private List<Tour> tourList = new ArrayList<>();
    private DatabaseReference toursRef;
    private String userId;

    // Model for a tour/note
    public static class Tour {
        public String id;
        public String text;
        public long timestamp;
        public Tour() {}
        public Tour(String id, String text, long timestamp) {
            this.id = id;
            this.text = text;
            this.timestamp = timestamp;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tours, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewTours);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TourAdapter();
        recyclerView.setAdapter(adapter);
        FloatingActionButton fab = view.findViewById(R.id.fabAddTour);
        fab.setOnClickListener(v -> showAddTourDialog());
        userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (userId != null) {
            toursRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("tours");
            listenForTours();
        } else {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
        }
        return view;
    }

    // Listen for real-time updates to tours
    private void listenForTours() {
        toursRef.orderByChild("timestamp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tourList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Tour tour = snap.getValue(Tour.class);
                    if (tour != null) {
                        tour.id = snap.getKey();
                        tourList.add(tour);
                    }
                }
                // Sort by timestamp descending
                Collections.sort(tourList, (a, b) -> Long.compare(b.timestamp, a.timestamp));
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load tours", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Show dialog to add a new tour/note
    private void showAddTourDialog() {
        final EditText input = new EditText(getContext());
        input.setHint("Enter tour note...");
        new AlertDialog.Builder(getContext())
                .setTitle("Add New Tour")
                .setView(input)
                .setPositiveButton("Add", (dialog, which) -> {
                    String text = input.getText().toString().trim();
                    if (!TextUtils.isEmpty(text)) {
                        addTour(text);
                    } else {
                        Toast.makeText(getContext(), "Tour text cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // Add a new tour to Firebase with robust error handling
    private void addTour(String text) {
        if (userId == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }
        if (toursRef == null) {
            Toast.makeText(getContext(), "Tours reference is null", Toast.LENGTH_SHORT).show();
            return;
        }
        String tourId = toursRef.push().getKey();
        if (tourId == null) {
            Toast.makeText(getContext(), "Failed to generate tour ID", Toast.LENGTH_SHORT).show();
            return;
        }
        long timestamp = System.currentTimeMillis();
        Tour tour = new Tour(tourId, text, timestamp);
        toursRef.child(tourId).setValue(tour)
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Tour added", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to add tour: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                });
    }

    // Show dialog to edit a tour/note
    private void showEditTourDialog(Tour tour) {
        final EditText input = new EditText(getContext());
        input.setText(tour.text);
        new AlertDialog.Builder(getContext())
                .setTitle("Edit Tour")
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newText = input.getText().toString().trim();
                    if (!TextUtils.isEmpty(newText)) {
                        updateTour(tour.id, newText);
                    } else {
                        Toast.makeText(getContext(), "Tour text cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // Update a tour in Firebase
    private void updateTour(String tourId, String newText) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("text", newText);
        toursRef.child(tourId).updateChildren(updates)
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Tour updated", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update tour", Toast.LENGTH_SHORT).show());
    }

    // Delete a tour from Firebase
    private void deleteTour(String tourId) {
        toursRef.child(tourId).removeValue()
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Tour deleted", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to delete tour", Toast.LENGTH_SHORT).show());
    }

    // RecyclerView Adapter for tours/notes
    private class TourAdapter extends RecyclerView.Adapter<TourAdapter.TourViewHolder> {
        @NonNull
        @Override
        public TourViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tour, parent, false);
            return new TourViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull TourViewHolder holder, int position) {
            Tour tour = tourList.get(position);
            holder.textTour.setText(tour.text);
            holder.textTimestamp.setText(formatTimestamp(tour.timestamp));
            holder.buttonEdit.setOnClickListener(v -> showEditTourDialog(tour));
            holder.buttonDelete.setOnClickListener(v -> {
                new AlertDialog.Builder(getContext())
                        .setTitle("Delete Tour")
                        .setMessage("Are you sure you want to delete this tour?")
                        .setPositiveButton("Delete", (dialog, which) -> deleteTour(tour.id))
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        }

        @Override
        public int getItemCount() {
            return tourList.size();
        }

        class TourViewHolder extends RecyclerView.ViewHolder {
            TextView textTour, textTimestamp;
            ImageButton buttonEdit, buttonDelete;
            TourViewHolder(@NonNull View itemView) {
                super(itemView);
                textTour = itemView.findViewById(R.id.textTour);
                textTimestamp = itemView.findViewById(R.id.textTimestamp);
                buttonEdit = itemView.findViewById(R.id.buttonEdit);
                buttonDelete = itemView.findViewById(R.id.buttonDelete);
            }
        }
    }

    // Format timestamp to readable date/time
    private String formatTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
} 