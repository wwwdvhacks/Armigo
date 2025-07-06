package com.example.zayn;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        Toolbar toolbar = view.findViewById(R.id.profileToolbar);
        if (toolbar != null) {
            toolbar.inflateMenu(R.menu.profile_menu);
            toolbar.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.action_logout) {
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    if (getActivity() != null) getActivity().finish();
                    return true;
                }
                return false;
            });
        }
        ImageView imageView = view.findViewById(R.id.imageViewProfile);
        TextView nameView = view.findViewById(R.id.textViewName);
        TextView emailView = view.findViewById(R.id.textViewEmail);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String displayName = user.getDisplayName();
            String email = user.getEmail();
            nameView.setText(displayName != null ? displayName : "No Name");
            emailView.setText(email != null ? email : "No Email");
            if (user.getPhotoUrl() != null) {
                Glide.with(this)
                        .load(user.getPhotoUrl())
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .circleCrop()
                        .into(imageView);
            } else {
                imageView.setImageResource(R.drawable.ic_launcher_foreground);
            }
        } else {
            nameView.setText("No Name");
            emailView.setText("No Email");
            imageView.setImageResource(R.drawable.ic_launcher_foreground);
        }
        return view;
    }
} 