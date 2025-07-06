package com.example.zayn;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.zayn.model.Message;
import com.example.zayn.model.ChatSummary;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import de.hdodenhof.circleimageview.CircleImageView;
import org.json.JSONArray;
import org.json.JSONObject;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatActivity extends AppCompatActivity {
    private String chatId, otherUserId, otherUsername, otherProfilePictureUrl, currentUserId;
    private RecyclerView recyclerViewMessages;
    private EditText editTextMessage;
    private ImageButton buttonSend;
    private ImageButton buttonAi;
    private MessageAdapter messageAdapter;
    private final List<Message> messageList = new ArrayList<>();
    private LinearLayoutManager layoutManager;
    // For AI state
    private DatabaseReference aiStateRef;
    private ValueEventListener aiStateListener;
    private String chatUserA = null;
    private String chatUserB = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        chatId = getIntent().getStringExtra("chatId");
        otherUserId = getIntent().getStringExtra("otherUserId");
        otherUsername = getIntent().getStringExtra("otherUsername");
        otherProfilePictureUrl = getIntent().getStringExtra("otherProfilePictureUrl");
        currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        // Always sort to ensure consistency
        if (currentUserId != null && otherUserId != null) {
            if (currentUserId.compareTo(otherUserId) < 0) {
                chatUserA = currentUserId;
                chatUserB = otherUserId;
            } else {
                chatUserA = otherUserId;
                chatUserB = currentUserId;
            }
        }
        // Set current chatId in MainActivity for MapFragment
        if (getParent() instanceof MainActivity) {
            ((MainActivity) getParent()).setCurrentChatId(chatId);
        } else if (getParent() == null && getApplicationContext() instanceof MainActivity) {
            ((MainActivity) getApplicationContext()).setCurrentChatId(chatId);
        }
        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend = findViewById(R.id.buttonSend);
        buttonAi = findViewById(R.id.buttonAi);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerViewMessages.setLayoutManager(layoutManager);
        messageAdapter = new MessageAdapter(messageList, currentUserId);
        recyclerViewMessages.setAdapter(messageAdapter);
        // Toolbar user info
        CircleImageView imageProfile = findViewById(R.id.imageProfile);
        TextView textUsername = findViewById(R.id.textUsername);
        ImageButton buttonBack = findViewById(R.id.buttonBack);
        ImageButton buttonMore = findViewById(R.id.buttonMore);
        
        textUsername.setText(otherUsername);
        Glide.with(this).load(otherProfilePictureUrl).placeholder(R.drawable.user_2).into(imageProfile);
        
        // Back button
        buttonBack.setOnClickListener(v -> onBackPressed());
        
        // More options button
        buttonMore.setOnClickListener(v -> {
            // TODO: Implement more options menu
            Toast.makeText(this, "More options", Toast.LENGTH_SHORT).show();
        });

        aiStateRef = FirebaseDatabase.getInstance()
                .getReference("chats").child(chatId).child("aiState");

        // Send button
        buttonSend.setOnClickListener(v -> sendMessage());
        // AI button logic
        buttonAi.setOnClickListener(v -> activateAiMode());

        // Listen for messages and aiState
        listenForMessages();
        listenForAiState();
    }

    private void activateAiMode() {
        // Write aiState to Firebase
        Map<String, Object> aiState = new HashMap<>();
        aiState.put("active", true);
        aiState.put("msgA", null);
        aiState.put("msgB", null);
        aiState.put("userA", null);
        aiState.put("userB", null);
        aiState.put("processing", false);
        aiStateRef.setValue(aiState);
        Toast.makeText(this, "AI mode activated! Each user must send 1 message.", Toast.LENGTH_SHORT).show();
    }

    private void listenForMessages() {
        DatabaseReference messagesRef = FirebaseDatabase.getInstance()
                .getReference("chats").child(chatId).child("messages");
        messagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageList.clear();
                for (DataSnapshot msgSnap : snapshot.getChildren()) {
                    // Parse timestamp as Long/Double
                    String messageId = msgSnap.child("messageId").getValue(String.class);
                    String senderId = msgSnap.child("senderId").getValue(String.class);
                    String text = msgSnap.child("text").getValue(String.class);
                    Object timestampObj = msgSnap.child("timestamp").getValue();
                    Message msg = new Message(messageId, senderId, text, timestampObj);
                    if (msg != null) {
                        messageList.add(msg);
                    }
                }
                Collections.sort(messageList, Comparator.comparingLong(m -> m.timestamp));
                messageAdapter.notifyDataSetChanged();
                recyclerViewMessages.scrollToPosition(messageList.size() - 1);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void listenForAiState() {
        if (aiStateListener != null) aiStateRef.removeEventListener(aiStateListener);
        aiStateListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean active = snapshot.child("active").getValue(Boolean.class);
                String msgA = snapshot.child("msgA").getValue(String.class);
                String msgB = snapshot.child("msgB").getValue(String.class);
                String userA = snapshot.child("userA").getValue(String.class);
                String userB = snapshot.child("userB").getValue(String.class);
                // Transaction logic for atomic processing
                if (active != null && active && msgA != null && msgB != null && userA != null && userB != null) {
                    aiStateRef.runTransaction(new com.google.firebase.database.Transaction.Handler() {
                        @NonNull
                        @Override
                        public com.google.firebase.database.Transaction.Result doTransaction(@NonNull com.google.firebase.database.MutableData currentData) {
                            Boolean processingVal = currentData.child("processing").getValue(Boolean.class);
                            Boolean activeVal = currentData.child("active").getValue(Boolean.class);
                            String msgAVal = currentData.child("msgA").getValue(String.class);
                            String msgBVal = currentData.child("msgB").getValue(String.class);
                            String userAVal = currentData.child("userA").getValue(String.class);
                            String userBVal = currentData.child("userB").getValue(String.class);
                            if (activeVal != null && activeVal
                                    && processingVal != null && !processingVal
                                    && msgAVal != null && msgBVal != null && userAVal != null && userBVal != null) {
                                currentData.child("processing").setValue(true);
                                return com.google.firebase.database.Transaction.success(currentData);
                            }
                            return com.google.firebase.database.Transaction.abort();
                        }
                        @Override
                        public void onComplete(DatabaseError error, boolean committed, DataSnapshot currentData) {
                            if (committed) {
                                // Only one client will get here!
                                callOpenAiApi(msgA, msgB, new AiCallback() {
                                    @Override
                                    public void onAiResult(String aiReply) {
                                        sendMessageFromAi(aiReply);
                                        saveTourForBothUsers(aiReply, userA, userB);
                                        ChatActivity.saveRouteToFirebase(aiReply, chatId);
                                        // Reset aiState node
                                        Map<String, Object> clear = new HashMap<>();
                                        clear.put("active", false);
                                        clear.put("msgA", null);
                                        clear.put("msgB", null);
                                        clear.put("userA", null);
                                        clear.put("userB", null);
                                        clear.put("processing", false);
                                        aiStateRef.setValue(clear);
                                    }
                                    @Override
                                    public void onAiError(String error) {
                                        Toast.makeText(ChatActivity.this, "AI error: " + error, Toast.LENGTH_SHORT).show();
                                        // Reset aiState node
                                        Map<String, Object> clear = new HashMap<>();
                                        clear.put("active", false);
                                        clear.put("msgA", null);
                                        clear.put("msgB", null);
                                        clear.put("userA", null);
                                        clear.put("userB", null);
                                        clear.put("processing", false);
                                        aiStateRef.setValue(clear);
                                    }
                                });
                            }
                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        aiStateRef.addValueEventListener(aiStateListener);
    }

    private void sendMessage() {
        String text = editTextMessage.getText().toString().trim();
        if (TextUtils.isEmpty(text) || currentUserId == null) return;
        DatabaseReference messagesRef = FirebaseDatabase.getInstance()
                .getReference("chats").child(chatId).child("messages");
        String messageId = messagesRef.push().getKey();
        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put("messageId", messageId);
        messageMap.put("senderId", currentUserId);
        messageMap.put("text", text);
        messageMap.put("timestamp", ServerValue.TIMESTAMP);
        messagesRef.child(messageId).setValue(messageMap);
        editTextMessage.setText("");

        // AI mode logic (sync with aiState in Firebase)
        aiStateRef.get().addOnSuccessListener(snapshot -> {
            Boolean active = snapshot.child("active").getValue(Boolean.class);
            String msgA = snapshot.child("msgA").getValue(String.class);
            String userA = snapshot.child("userA").getValue(String.class);
            String msgB = snapshot.child("msgB").getValue(String.class);
            String userB = snapshot.child("userB").getValue(String.class);

            if (active != null && active) {
                if (msgA == null) {
                    // First message
                    Map<String, Object> update = new HashMap<>();
                    update.put("msgA", text);
                    update.put("userA", currentUserId);
                    aiStateRef.updateChildren(update);
                } else if (msgB == null && userA != null && !userA.equals(currentUserId)) {
                    // Second message from different user
                    Map<String, Object> update = new HashMap<>();
                    update.put("msgB", text);
                    update.put("userB", currentUserId);
                    aiStateRef.updateChildren(update);
                }
                // If same user tries to send both, ignore for AI logic (message is still saved as normal)
            }
        });

        // Update userChats for both users
        long now = System.currentTimeMillis();
        ChatSummary summaryForCurrent = new ChatSummary(otherUserId, text, now);
        ChatSummary summaryForOther = new ChatSummary(currentUserId, text, now);
        DatabaseReference userChatsRef = FirebaseDatabase.getInstance().getReference("userChats");
        userChatsRef.child(currentUserId).child(chatId).setValue(summaryForCurrent);
        userChatsRef.child(otherUserId).child(chatId).setValue(summaryForOther);
    }

    private interface AiCallback {
        void onAiResult(String aiReply);
        void onAiError(String error);
    }

    private void callOpenAiApi(String msg1, String msg2, AiCallback callback) {
        String apiKey = "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";
        String url = "https://openrouter.ai/api/v1/chat/completions";
        OkHttpClient client = new OkHttpClient();
        String jsonBody = "{" +
                "\"model\": \"mistralai/mistral-7b-instruct\"," +
                "\"messages\": [" +
                "{\"role\": \"system\", \"content\": \"You are a detailed AI travel planner.  \n" +
                "You receive two separate messages from two different people about their travel wishes.  \n" +
                "Your task is to create a **single clear travel plan** that suits both users.\n" +
                "\n" +
                "✅ If the requested plan is impossible (for example, if someone wants to reach a place in less time than realistically possible), you must clearly say that it is not possible and explain why.\n" +
                "\n" +
                "✅ Always answer in **this exact structure and style** — do not add or skip anything:\n" +
                "\n" +
                "1. **Travel Route:**  \n" +
                "   Write it exactly like this example: `['Vanadzor', 'Dilijan', 'Yerevan']`  \n" +
                "   It must be a valid Python-style list of place names. These can be cities, neighborhoods, restaurants, churches — whatever makes sense for the route.\n" +
                "\n" +
                "2. **Travel Description:**  \n" +
                "   Write a rich, friendly, detailed text describing the trip step by step.  \n" +
                "   Include approximate timeframes for each place, local tips for what to do, what to eat, where to rest.  \n" +
                "   Make it inspiring and comfortable to read — not short.\n" +
                "\n" +
                "3. **Interesting Facts:**  \n" +
                "   Add 2–4 interesting facts or trivia about the **main places** the users want to visit.  \n" +
                "   Keep it relevant and fun.\n" +
                "\n" +
                "⚠\uFE0F Your answer must **ALWAYS follow this template**, with clear numbering:  \n" +
                "**1. Travel Route:**  \n" +
                "**2. Travel Description:**  \n" +
                "**3. Interesting Facts:**\n" +
                "\n" +
                "Never write anything outside this structure. Never greet the user. Never add extra explanations.\n" +
                "\n" +
                "Be realistic, helpful, creative — and always check if both requests can fit into one plan.\n\"}," +
                "{\"role\": \"user\", \"content\": \"" + msg1.replace("\"", "\\\"") + "\"}," +
                "{\"role\": \"user\", \"content\": \"" + msg2.replace("\"", "\\\"") + "\"}" +
                "]" +
                "}";

        RequestBody body = RequestBody.create(jsonBody, MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("HTTP-Referer", "https://myapp.com")
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();
        new Thread(() -> {
            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    String resp = response.body().string();
                    String aiReply = parseOpenAiResponse(resp);
                    runOnUiThread(() -> callback.onAiResult(aiReply));
                } else {
                    runOnUiThread(() -> callback.onAiError("Code " + response.code()));
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> callback.onAiError(e.getMessage()));
            }
        }).start();
    }

    private String parseOpenAiResponse(String json) {
        try {
            JSONObject obj = new JSONObject(json);
            JSONArray choices = obj.getJSONArray("choices");
            if (choices.length() > 0) {
                JSONObject message = choices.getJSONObject(0).getJSONObject("message");
                return message.getString("content");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "AI error: could not parse response.";
    }

    private void sendMessageFromAi(String text) {
        DatabaseReference messagesRef = FirebaseDatabase.getInstance()
                .getReference("chats").child(chatId).child("messages");
        String messageId = messagesRef.push().getKey();
        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put("messageId", messageId);
        messageMap.put("senderId", "AI");
        messageMap.put("text", text);
        messageMap.put("timestamp", ServerValue.TIMESTAMP);
        messagesRef.child(messageId).setValue(messageMap);
    }

    private void saveTourForBothUsers(String text, String userA, String userB) {
        long timestamp = System.currentTimeMillis();
        String tourId = FirebaseDatabase.getInstance()
                .getReference("users").child(userA).child("tours").push().getKey();
        ToursFragment.Tour tour = new ToursFragment.Tour(tourId, text, timestamp);
        // Save under both users
        DatabaseReference refA = FirebaseDatabase.getInstance()
                .getReference("users").child(userA).child("tours").child(tourId);
        DatabaseReference refB = FirebaseDatabase.getInstance()
                .getReference("users").child(userB).child("tours").child(tourId);
        refA.setValue(tour);
        refB.setValue(tour);
    }

    public static void saveRouteToFirebase(String aiResponse, String chatId) {
        if (aiResponse == null || chatId == null) return;
        // Parse Travel Route
        String routeLine = null;
        for (String line : aiResponse.split("\n")) {
            if (line.trim().startsWith("1. Travel Route:") || line.trim().startsWith("Travel Route:")) {
                routeLine = line.substring(line.indexOf(":") + 1).trim();
                break;
            }
        }
        if (routeLine == null || routeLine.isEmpty()) return;
        // Remove brackets and quotes
        routeLine = routeLine.replace("[", "").replace("]", "").replace("'", "").replace("\"", "");
        String[] parts = routeLine.split(",");
        java.util.List<String> placeNames = new java.util.ArrayList<>();
        for (String part : parts) {
            String name = part.trim();
            if (!name.isEmpty()) placeNames.add(name);
        }
        if (placeNames.size() < 2) return;
        com.google.firebase.database.DatabaseReference ref = com.google.firebase.database.FirebaseDatabase.getInstance()
                .getReference("chats").child(chatId).child("route");
        ref.setValue(placeNames);
    }

    static class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int TYPE_SENT = 1;
        private static final int TYPE_RECEIVED = 2;
        private final List<Message> messages;
        private final String currentUserId;
        MessageAdapter(List<Message> messages, String currentUserId) {
            this.messages = messages;
            this.currentUserId = currentUserId;
        }
        @Override
        public int getItemViewType(int position) {
            return messages.get(position).senderId.equals(currentUserId) ? TYPE_SENT : TYPE_RECEIVED;
        }
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == TYPE_SENT) {
                View view = View.inflate(parent.getContext(), R.layout.item_message_sent, null);
                return new SentViewHolder(view);
            } else {
                View view = View.inflate(parent.getContext(), R.layout.item_message_received, null);
                return new ReceivedViewHolder(view);
            }
        }
        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            Message msg = messages.get(position);
            if (holder instanceof SentViewHolder) {
                ((SentViewHolder) holder).textMessage.setText(msg.text);
            } else if (holder instanceof ReceivedViewHolder) {
                ((ReceivedViewHolder) holder).textMessage.setText(msg.text);
            }
        }
        @Override
        public int getItemCount() { return messages.size(); }
        static class SentViewHolder extends RecyclerView.ViewHolder {
            TextView textMessage;
            SentViewHolder(@NonNull View itemView) {
                super(itemView);
                textMessage = itemView.findViewById(R.id.textMessage);
            }
        }
        static class ReceivedViewHolder extends RecyclerView.ViewHolder {
            TextView textMessage;
            ReceivedViewHolder(@NonNull View itemView) {
                super(itemView);
                textMessage = itemView.findViewById(R.id.textMessage);
            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("chatId", chatId);
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }
}