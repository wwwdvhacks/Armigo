package com.example.zayn;

import static java.security.AccessController.getContext;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private String currentChatId = null; // Store the current chatId

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(this::onNavigationItemSelected);

        // Устанавливаем стартовый фрагмент
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null && data.hasExtra("chatId")) {
            String chatId = data.getStringExtra("chatId");
            setCurrentChatId(chatId);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted - notify the MapFragment if it's currently active
                Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                if (currentFragment instanceof MapFragment) {
                    ((MapFragment) currentFragment).onLocationPermissionGranted();
                }
            } else {
                Toast.makeText(this, "Разрешение на геолокацию не выдано", Toast.LENGTH_SHORT).show();
            }
        }
    }



    // Call this method from ChatActivity or when opening a chat
    public void setCurrentChatId(String chatId) {
        this.currentChatId = chatId;
    }

    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment selectedFragment;

        int itemId = item.getItemId();

        if (itemId == R.id.nav_home) {
            selectedFragment = new HomeFragment();
        } else if (itemId == R.id.nav_map) {
            // Pass chatId to MapFragment if available
            if (currentChatId != null) {
                selectedFragment = MapFragment.newInstance(currentChatId);
            } else {
                selectedFragment = new MapFragment();
            }
        } else if (itemId == R.id.nav_profile) {
            selectedFragment = new ProfileFragment();
        } else if (itemId == R.id.nav_tours) {
            selectedFragment = new ToursFragment();
        } else if (itemId == R.id.nav_groups) {
            selectedFragment = new GroupsFragment();
        } else {
            return false;
        }

        return loadFragment(selectedFragment);
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }
}

//
//import static com.example.zayn.R.*;
//
//import android.os.Bundle;
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.fragment.app.Fragment;
//import com.google.android.material.bottomnavigation.BottomNavigationView;
//import android.view.MenuItem;
//
//public class MainActivity extends AppCompatActivity {
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
//        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);
//
//        // Set default fragment
//        if (savedInstanceState == null) {
//            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
//        }
//    }
//
//    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
//            new BottomNavigationView.OnNavigationItemSelectedListener() {
//                @Override
//                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//                    Fragment selectedFragment = null;
//                    switch (item.getItemId()) {
//                        case R.id.nav_home:
//                            selectedFragment = new HomeFragment();
//                            break;
//                        case R.id.nav_map:
//                            selectedFragment = new MapFragment();
//                            break;
//                        case R.id.nav_profile:
//                            selectedFragment = new ProfileFragment();
//                            break;
//                    }
//                    if (selectedFragment != null) {
//                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
//                        return true;
//                    }
//                    return false;
//                }
//            };
//}


//public class MainActivity extends AppCompatActivity {
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
//        setContentView(R.layout.activity_main);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
//    }
//}