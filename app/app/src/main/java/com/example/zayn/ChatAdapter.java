package com.example.zayn;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.zayn.model.ChatPreview;
import java.util.ArrayList;
import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
    public interface OnChatClickListener {
        void onChatClick(ChatPreview chat);
    }

    private List<ChatPreview> chatList = new ArrayList<>();
    private final OnChatClickListener listener;
    private final Context context;

    public ChatAdapter(Context context, OnChatClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setChatList(List<ChatPreview> chats) {
        this.chatList = chats;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatPreview chat = chatList.get(position);
        holder.textUsername.setText(chat.otherUsername);
        holder.textLastMessage.setText(chat.lastMessageText != null ? chat.lastMessageText : "");
        Glide.with(context)
                .load(chat.otherProfilePictureUrl)
                .placeholder(R.drawable.ic_person_24)
                .into(holder.imageProfile);
        holder.itemView.setOnClickListener(v -> listener.onChatClick(chat));
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        CircleImageView imageProfile;
        TextView textUsername, textLastMessage;
        ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            imageProfile = itemView.findViewById(R.id.imageProfile);
            textUsername = itemView.findViewById(R.id.textUsername);
            textLastMessage = itemView.findViewById(R.id.textLastMessage);
        }
    }
} 