package com.example.zayn;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.zayn.model.ChatPreview;
import com.example.zayn.model.ChatSummary;
import com.example.zayn.model.Message;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupsFragment extends Fragment {
    private RecyclerView recyclerViewChats;
    private ChatAdapter chatAdapter;
    private final List<ChatPreview> chatList = new ArrayList<>();
    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_groups, container, false);
        recyclerViewChats = view.findViewById(R.id.recyclerViewChats);
        recyclerViewChats.setLayoutManager(new LinearLayoutManager(getContext()));
        chatAdapter = new ChatAdapter(requireContext(), chat -> {
            Intent intent = new Intent(requireContext(), ChatActivity.class);
            intent.putExtra("chatId", chat.chatId);
            intent.putExtra("otherUserId", chat.otherUserId);
            intent.putExtra("otherUsername", chat.otherUsername);
            intent.putExtra("otherProfilePictureUrl", chat.otherProfilePictureUrl);
            startActivityForResult(intent, 1234);
        });
        recyclerViewChats.setAdapter(chatAdapter);
        FloatingActionButton fabSearch = view.findViewById(R.id.fab_search_user);
        fabSearch.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), SearchUserActivity.class));
        });
        currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (!TextUtils.isEmpty(currentUserId)) {
            listenForChats();
        }
        return view;
    }

    private void listenForChats() {
        DatabaseReference userChatsRef = FirebaseDatabase.getInstance().getReference("userChats").child(currentUserId);
        userChatsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, ChatPreview> chatMap = new HashMap<>();
                for (DataSnapshot chatSnap : snapshot.getChildren()) {
                    String chatId = chatSnap.getKey();
                    ChatSummary summary = chatSnap.getValue(ChatSummary.class);
                    if (chatId == null || summary == null) continue;
                    String otherUserId = summary.otherUserId;
                    String lastMsgText = summary.lastMessage;
                    long lastMsgTime = summary.timestamp;
                    fetchOtherUserInfo(chatId, otherUserId, lastMsgText, lastMsgTime, chatMap);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void fetchOtherUserInfo(String chatId, String otherUserId, String lastMsgText, long lastMsgTime, Map<String, ChatPreview> chatMap) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(otherUserId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String username = snapshot.child("username").getValue(String.class);
                String profileUrl = snapshot.child("profilePictureUrl").getValue(String.class);
                ChatPreview preview = new ChatPreview(chatId, otherUserId, username, profileUrl, lastMsgText, lastMsgTime);
                chatMap.put(chatId, preview);
                updateChatList(chatMap);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void updateChatList(Map<String, ChatPreview> chatMap) {
        chatList.clear();
        chatList.addAll(chatMap.values());
        Collections.sort(chatList, (a, b) -> Long.compare(b.lastMessageTimestamp, a.lastMessageTimestamp));
        chatAdapter.setChatList(chatList);
    }
} 