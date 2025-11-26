package com.example.orion.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.orion.R;
import com.example.orion.fragment.BerandaFragment;
import com.example.orion.fragment.ProfileFragment;
import com.example.orion.utils.Helpers;
import com.example.orion.utils.NetworkUtil;
import com.example.orion.utils.PreferenceManager;
import com.example.orion.utils.api.ApiClient;
import com.example.orion.utils.api.ApiService;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.JsonObject;

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

        bottomTab = findViewById(R.id.bottom_tab);

        if (savedInstanceState == null) {
            berandaFragment = new BerandaFragment();
            profileFragment = new ProfileFragment();
            activeFragment = berandaFragment;

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, profileFragment, "PROFILE")
                    .hide(profileFragment)
                    .add(R.id.fragment_container, berandaFragment, "BERANDA")
                    .commit();
        } else {
            berandaFragment = getSupportFragmentManager().findFragmentByTag("BERANDA");
            profileFragment = getSupportFragmentManager().findFragmentByTag("PROFILE");

            if (berandaFragment == null) berandaFragment = new BerandaFragment();
            if (profileFragment == null) profileFragment = new ProfileFragment();

            if (profileFragment.isVisible()) {
                activeFragment = profileFragment;
            } else {
                activeFragment = berandaFragment;
            }
        }

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
                                        Helpers.showAlert(MainActivity.this, null, "Sesi Anda telah berakhir! Silahkan login kembali.", "OK", (dialogInterface, i) -> {
                                            Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            pref.clear();
                                            startActivity(intent);
                                            Helpers.overridePendingTransition_Enter_v1(MainActivity.this);
                                            finish();
                                        });
                                        return;
                                    }
                                }

                                pref.saveUserData(data);

                                Helpers.delay(1000, () -> {
                                    if (berandaFragment instanceof BerandaFragment) {
                                        ((BerandaFragment) berandaFragment).removeShimmers();
                                    }
                                    if (profileFragment instanceof ProfileFragment) {
                                        ((ProfileFragment) profileFragment).removeShimmers();
                                    }
                                });
                            }
                            loading.dismiss();
                        } else {
                            loading.dismiss();
                        }
                    } else {
                        Helpers.showAlert(MainActivity.this, null, "Sesi Anda telah berakhir! Silahkan login kembali.", "OK", (dialogInterface, i) -> {
                            Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            pref.clear();
                            startActivity(intent);
                            Helpers.overridePendingTransition_Enter_v1(MainActivity.this);
                            finish();
                        });
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Helpers.showAlert(MainActivity.this, null, "Gagal terhubung ke server! Silahkan login kembali.", "OK", (dialogInterface, i) -> {
                        Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        pref.clear();
                        startActivity(intent);
                        Helpers.overridePendingTransition_Enter_v1(MainActivity.this);
                        finish();
                    });
                }
            });
        } else {
            Helpers.showAlert(this, null, "Tidak ada koneksi internet! Coba lagi nanti.", "OK", (dialogInterface, i) -> {
                dialogInterface.dismiss();
                finish();
            });
        }

        bottomTab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Fragment selectedFragment = null;
                switch (tab.getPosition()) {
                    case 0:
                        selectedFragment = berandaFragment;
                        break;
                    case 1:
                        selectedFragment = profileFragment;
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

    public void openEditProfile() {
        Intent intent = new Intent(this, EditProfileActivity.class);
        editProfileLauncher.launch(intent);
    }

    @Override
    public void finish() {
        super.finish();
        Helpers.overridePendingTransition_Exit_v1(this);
    }
}