package com.example.takeit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class Home extends AppCompatActivity {
    Button buttonLogout;
    FirebaseAuth fAuth;

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        fAuth = FirebaseAuth.getInstance();

        if (fAuth.getCurrentUser()==null){
            startActivity(new Intent(getApplicationContext(),MainActivity.class));
            finish();
        }

        buttonLogout = findViewById(R.id.buttonLogout);

        bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new HomeFragment()).commit();

    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            Fragment selectedFragment = null;

            switch (item.getItemId()){
                case R.id.nav_home:
                    selectedFragment = new HomeFragment();
                    break;
                case R.id.nav_bookmark:
                    selectedFragment = new BookmarksFragment();
                    break;
                case R.id.nav_upload:
                    selectedFragment = new UploadFragment();
                    break;
                case R.id.nav_profile:
                    selectedFragment = new ProfileFragment();
                    break;
                case R.id.nav_setting:
                    selectedFragment = new SettingsFragment();
                    break;
            }

            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,selectedFragment).addToBackStack(null).commit();

            return true;
        }
    };
}