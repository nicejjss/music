package com.example.music;


import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.music.Fragment.HomeFragment;
import com.example.music.Fragment.SearchFragment;
import com.example.music.Fragment.SettingFragment;
import com.example.music.Fragment.UploadFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    HomeFragment homeFragment = new HomeFragment();
    SettingFragment settingsFragment = new SettingFragment();
    SearchFragment searchFragment = new SearchFragment();

    UploadFragment uploadFragment = new UploadFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_nav);

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, homeFragment).commit();

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
               int id = menuItem.getItemId();

               if (id == R.id.nav_home) {
                   getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, homeFragment).commit();
                   return true;
               } else if (id == R.id.nav_search) {
                   getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, searchFragment).commit();
                   return true;
               } else if (id == R.id.nav_settings) {
                   getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, settingsFragment).commit();
                   return true;
               } else if (id == R.id.nav_upload) {
                   getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, uploadFragment).commit();
                   return true;
               }

               return false;
            }
        });
    }
}