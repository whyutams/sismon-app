package com.example.orion.activity;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import android.view.MenuItem;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.orion.R;
import com.example.orion.utils.Helpers;
import com.example.orion.utils.NetworkUtil;
import com.example.orion.utils.PreferenceManager;
import com.example.orion.utils.api.ApiClient;
import com.example.orion.utils.api.ApiService;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailAkunSatpamActivity extends AppCompatActivity {
    private boolean isDataLoaded = false;
    private String id;

    ImageView iv_gambar;
    TextView tv_no, tv_nama, tv_nomorHp, tv_email, tv_alamat, tv_username;
    LinearLayout ll_progress;
    ScrollView sv_main;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_akun_satpam);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        Drawable navIcon = toolbar.getNavigationIcon();
        if (navIcon != null)
            navIcon.setColorFilter(new PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP));
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        ll_progress = findViewById(R.id.ll_progress);
        sv_main = findViewById(R.id.sv_main);
        iv_gambar = findViewById(R.id.iv_gambar);
        tv_no = findViewById(R.id.tv_no);
        tv_username = findViewById(R.id.tv_username);
        tv_nama = findViewById(R.id.tv_nama);
        tv_nomorHp = findViewById(R.id.tv_nomorHp);
        tv_email = findViewById(R.id.tv_email);
        tv_alamat = findViewById(R.id.tv_alamat);

        PreferenceManager pref = new PreferenceManager(this);
        JsonObject user = pref.getUserData();

        id = getIntent().getStringExtra("id");
        String index = getIntent().getStringExtra("index");
        loadData(id, index);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        finish();
    }

    private void loadData(String id, String index) {
        if (NetworkUtil.isConnected(this)) {
            ApiService apiService = ApiClient.getClient(DetailAkunSatpamActivity.this).create(ApiService.class);
            apiService.userShow(id).enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        JsonObject json = response.body();

                        String status = json.has("status") ? json.get("status").getAsString() : "";

                        if (status.equals("success")) {
                            JsonObject data = json.has("data") ? json.get("data").getAsJsonObject() : null;

                            if (isFinishing() || isDestroyed()) return;

                            tv_no.setText(index);
                            tv_username.setText(data.get("username").getAsString());
                            tv_nama.setText(data.get("nama").getAsString());
                            tv_nomorHp.setText(data.get("nomor_hp").isJsonNull() ? "-" : data.get("nomor_hp").getAsString());
                            tv_email.setText(data.get("email").isJsonNull() ? "-" : data.get("email").getAsString());
                            tv_alamat.setText(data.get("alamat").isJsonNull() ? "-" : data.get("alamat").getAsString());

                            if (data.has("gambar") && !data.get("gambar").isJsonNull()) {
                                String gambar = data.get("gambar").getAsString();

                                if (!gambar.startsWith("http")) {
                                    gambar = ApiClient.base_url + gambar;
                                }

                                iv_gambar.setImageTintList(null);

                                Glide.with(DetailAkunSatpamActivity.this)
                                        .load(gambar)
                                        .placeholder(R.drawable.ic_image_24)
                                        .error(R.drawable.ic_image_24)
                                        .into(iv_gambar);

                    final String previewGambar = gambar;
                    iv_gambar.setOnClickListener(v -> Helpers.showImagePreview(DetailAkunSatpamActivity.this, previewGambar));
                            } else {
                                iv_gambar.setImageResource(R.drawable.ic_image_24);
                                iv_gambar.setImageTintList(
                                        ContextCompat.getColorStateList(DetailAkunSatpamActivity.this, android.R.color.darker_gray)
                                );
                            }

                            ll_progress.setVisibility(View.GONE);
                            sv_main.setVisibility(View.VISIBLE);
                            isDataLoaded = true;
                            invalidateOptionsMenu();
                        } else {
                            Toast.makeText(DetailAkunSatpamActivity.this, "Tidak dapat memuat data. Coba lagi nanti.", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } else {
                        Toast.makeText(DetailAkunSatpamActivity.this, "Tidak dapat memuat data. Coba lagi nanti.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Toast.makeText(DetailAkunSatpamActivity.this, "Terjadi kesalahan: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    finish();
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
    }

    @Override
    public void finish() {
        super.finish();
        Helpers.overridePendingTransition_Exit_v1(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!isDataLoaded) {
            return true;
        }
        PreferenceManager pref = new PreferenceManager(this);
        JsonObject user = pref.getUserData();
        String role = user != null && user.has("role") ? user.get("role").getAsString() : "";
        if (role.equals("superadmin")) {
            getMenuInflater().inflate(R.menu.menu_top_nav_detail_delete, menu);
            for (int i = 0; i < menu.size(); i++) {
                Drawable icon = menu.getItem(i).getIcon();
                if (icon != null) {
                    icon.setTint(ContextCompat.getColor(this, R.color.white));
                }
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.nav_delete) {
            Helpers.showConfirm(this, "Konfirmasi", "Apakah Anda yakin ingin menghapus akun satpam ini?", (dialogInterface, i) -> {
                dialogInterface.dismiss();
                deleteUser();
            });
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteUser() {
        if (!NetworkUtil.isConnected(this)) {
            Toast.makeText(this, "Tidak ada koneksi internet", Toast.LENGTH_SHORT).show();
            return;
        }
        Helpers.LoadingHelper loading = new Helpers.LoadingHelper(this);
        loading.show();
        ApiService apiService = ApiClient.getClient(this).create(ApiService.class);
        apiService.userDelete(id).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                loading.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(DetailAkunSatpamActivity.this, "Data akun satpam berhasil dihapus", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    String msg = "Gagal menghapus data akun satpam";
                    try {
                        if (response.errorBody() != null) {
                            JsonObject err = new com.google.gson.JsonParser().parse(response.errorBody().string()).getAsJsonObject();
                            if (err.has("message")) msg = err.get("message").getAsString();
                        }
                    } catch (Exception ignored) {}
                    Toast.makeText(DetailAkunSatpamActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                loading.dismiss();
                Toast.makeText(DetailAkunSatpamActivity.this, "Terjadi kesalahan: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
