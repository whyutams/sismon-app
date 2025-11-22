package com.example.orion.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.orion.R;
import com.example.orion.fragment.BerandaFragment;
import com.example.orion.fragment.ProfileFragment;
import com.example.orion.utils.Helpers;
import com.example.orion.utils.NetworkUtil;
import com.example.orion.utils.PreferenceManager;
import com.example.orion.utils.api.ApiClient;
import com.example.orion.utils.api.ApiService;
import com.facebook.shimmer.Shimmer;
import com.facebook.shimmer.ShimmerDrawable;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.JsonObject;

import java.util.Objects;

import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.Color;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private TabLayout bottomTab;
    private Fragment berandaFragment;
    private Fragment profileFragment;
    private Fragment activeFragment;
    private ActivityResultLauncher<Intent> editProfileLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Helpers.LoadingHelper loading = new Helpers.LoadingHelper(this);
        loading.show();

        PreferenceManager pref = new PreferenceManager(this);

        setContentView(R.layout.activity_main);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        Drawable overflowIcon = toolbar.getOverflowIcon();

        if (overflowIcon != null) {
            overflowIcon.setColorFilter(new PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP));
            toolbar.setOverflowIcon(overflowIcon);
        }
        setSupportActionBar(toolbar);

        bottomTab = findViewById(R.id.bottom_tab);

        berandaFragment = new BerandaFragment();
        profileFragment = new ProfileFragment();
        activeFragment = berandaFragment;

        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, profileFragment).hide(profileFragment)
                .add(R.id.fragment_container, berandaFragment)
                .commit();

        editProfileLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        if (berandaFragment instanceof BerandaFragment) {
                            ((BerandaFragment) berandaFragment).refreshUserData();
                        }
                        if (profileFragment instanceof ProfileFragment) {
                            ((ProfileFragment) profileFragment).refreshUserData();
                        }
                    }
                }
        );

        if (NetworkUtil.isConnected(this)) {
            ApiService apiService = ApiClient.getClient(MainActivity.this).create(ApiService.class);
            apiService.whoami().enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        JsonObject json = response.body();

                        String status = json.has("status") ? json.get("status").getAsString() : "";

                        if (status.equals("success")) {
                            JsonObject data = json.has("user") ? json.getAsJsonObject("user") : null;

                            PreferenceManager pref = new PreferenceManager(MainActivity.this);
                            JsonObject user = pref.getUserData();

                            if (data != null) {
                                if (user != null) {
                                    if (!user.get("role").getAsString().equals(data.get("role").getAsString())) {
                                        Helpers.showAlert(MainActivity.this, null, "Sesi Anda telah berakhir! Silahkan login kembali.", "OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                pref.clear();
                                                startActivity(intent);
                                                finish();
                                            }
                                        });

                                        return;
                                    }
                                }

                                pref.saveUserData(data);

                                Helpers.delay(1000, new Runnable() {
                                    @Override
                                    public void run() {
                                        if (berandaFragment instanceof BerandaFragment) {
                                            ((BerandaFragment) berandaFragment).removeShimmers();
                                        }

                                        if (profileFragment instanceof ProfileFragment) {
                                            ((ProfileFragment) profileFragment).removeShimmers();
                                        }
                                    }
                                });
                            }

                            loading.dismiss();
                        } else {
                            loading.dismiss();
                        }
                    } else {
                        Helpers.showAlert(MainActivity.this, null, "Sesi Anda telah berakhir! Silahkan login kembali.", "OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                pref.clear();
                                startActivity(intent);
                                finish();
                            }
                        });
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Helpers.showAlert(MainActivity.this, null, "Sesi Anda telah berakhir! Silahkan login kembali.", "OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            pref.clear();
                            startActivity(intent);
                            finish();
                        }
                    });
                }
            });
        } else {
            Helpers.showAlert(this, null, "Tidak ada koneksi internet! Coba lagi nanti.", "OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    finish();
                }
            });
        }

        bottomTab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Fragment selectedFragment = null;
                switch (tab.getPosition()) {
                    case 0:
                        selectedFragment = berandaFragment;
                        getSupportActionBar().setTitle("Selamat Datang");
                        break;
                    case 1:
                        selectedFragment = profileFragment;
                        getSupportActionBar().setTitle("Profil");
                        break;
                }

                if (selectedFragment != null && selectedFragment != activeFragment) {
                    getSupportFragmentManager().beginTransaction()
                            .hide(activeFragment)
                            .show(selectedFragment)
                            .commit();
                    activeFragment = selectedFragment;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_top_nav, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.nav_about) {
            Toast.makeText(this, "About clicked", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void openEditProfile() {
        Intent intent = new Intent(this, EditProfileActivity.class);
        editProfileLauncher.launch(intent);
    }
}