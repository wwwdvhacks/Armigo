package com.example.zayn;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;



public class SearchUserActivity extends AppCompatActivity {
    private EditText editTextSearch;
    private RecyclerView recyclerViewUsers;
    private TextView textNoResults;
    private UserAdapter userAdapter;
    private final List<User> userList = new ArrayList<>();
    private Handler searchHandler = new Handler();
    private Runnable searchRunnable;
    private static final long SEARCH_DEBOUNCE_MS = 300;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_user);
        editTextSearch = findViewById(R.id.editTextSearch);
        recyclerViewUsers = findViewById(R.id.recyclerViewUsers);
        textNoResults = findViewById(R.id.textNoResults);
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));
        userAdapter = new UserAdapter(userList, user -> onUserSelected(user));
        recyclerViewUsers.setAdapter(userAdapter);
        currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
                searchRunnable = () -> searchUsers(s.toString().trim());
                searchHandler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_MS);
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void searchUsers(String query) {
        if (TextUtils.isEmpty(query)) {
            userList.clear();
            userAdapter.notifyDataSetChanged();
            textNoResults.setVisibility(View.GONE);
            return;
        }
        
        // Show loading state
        textNoResults.setText("Searching...");
        textNoResults.setVisibility(View.VISIBLE);
        
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                String lowerQuery = query.toLowerCase().trim();
                
                for (DataSnapshot userSnap : snapshot.getChildren()) {
                    String uid = userSnap.getKey();
                    if (uid == null || uid.equals(currentUserId)) continue;
                    
                    String username = userSnap.child("username").getValue(String.class);
                    String profileUrl = userSnap.child("profilePictureUrl").getValue(String.class);
                    
                    if (username != null && username.toLowerCase().contains(lowerQuery)) {
                        userList.add(new User(uid, username, profileUrl != null ? profileUrl : ""));
                    }
                }
                
                userAdapter.notifyDataSetChanged();
                
                if (userList.isEmpty()) {
                    textNoResults.setText("No users found for '" + query + "'");
                    textNoResults.setVisibility(View.VISIBLE);
                } else {
                    textNoResults.setVisibility(View.GONE);
                }
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                textNoResults.setText("Search failed: " + error.getMessage());
                textNoResults.setVisibility(View.VISIBLE);
            }
        });
    }

    private void onUserSelected(User user) {
        // ChatId is alphabetical order of UIDs
        String chatId = makeChatId(currentUserId, user.uid);
        DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("chats").child(chatId);
        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    // Create chat structure
                    chatRef.child("messages").setValue(""); // create empty node
                }
                openChatActivity(chatId, user);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void openChatActivity(String chatId, User user) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("chatId", chatId);
        intent.putExtra("otherUserId", user.uid);
        intent.putExtra("otherUsername", user.username);
        intent.putExtra("otherProfilePictureUrl", user.profilePictureUrl);
        startActivity(intent);
        finish();
    }

    private String makeChatId(String uid1, String uid2) {
        List<String> uids = new ArrayList<>();
        uids.add(uid1);
        uids.add(uid2);
        Collections.sort(uids);
        return uids.get(0) + "_" + uids.get(1);
    }

    // User model for search
    static class User {
        String uid, username, profilePictureUrl;
        User(String uid, String username, String profilePictureUrl) {
            this.uid = uid;
            this.username = username;
            this.profilePictureUrl = profilePictureUrl;
        }
    }

    // Adapter for user search results
    static class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
        interface OnUserClickListener { void onUserClick(User user); }
        private final List<User> users;
        private final OnUserClickListener listener;
        UserAdapter(List<User> users, OnUserClickListener listener) {
            this.users = users;
            this.listener = listener;
        }
        @NonNull
        @Override
        public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = View.inflate(parent.getContext(), R.layout.item_user_search, null);
            return new UserViewHolder(view);
        }
        @Override
        public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
            User user = users.get(position);
            holder.textUsername.setText(user.username);
            Glide.with(holder.itemView.getContext())
                    .load(user.profilePictureUrl)
                    .placeholder(R.drawable.ic_person_24)
                    .into(holder.imageProfile);
            holder.itemView.setOnClickListener(v -> listener.onUserClick(user));
        }
        @Override
        public int getItemCount() { return users.size(); }
        static class UserViewHolder extends RecyclerView.ViewHolder {
            CircleImageView imageProfile;
            TextView textUsername;
            UserViewHolder(@NonNull View itemView) {
                super(itemView);
                imageProfile = itemView.findViewById(R.id.imageProfile);
                textUsername = itemView.findViewById(R.id.textUsername);
            }
        }
    }
} 