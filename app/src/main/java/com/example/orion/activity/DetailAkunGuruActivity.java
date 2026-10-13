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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import android.content.Intent;
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

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailAkunGuruActivity extends AppCompatActivity {
    private boolean isDataLoaded = false;
    private ImageView iv_gambar;
    private TextView tv_no, tv_nip, tv_nama, tv_role, tv_jurusan, tv_nomorHp, tv_email, tv_alamat, tv_username, tv_dibuat_oleh, tv_dibuat_pada, tv_diperbarui_oleh, tv_diperbarui_pada;
    private LinearLayout ll_progress;
    private ScrollView sv_main;
    private String ortuDariSiswa = "", ortu_id = null;

    private final ActivityResultLauncher<Intent> editDataLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    setResult(RESULT_OK);
                    loadData(ortu_id, getIntent().getStringExtra("index"));
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_akun_guru);

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
        tv_nip = findViewById(R.id.tv_nip);
        tv_role = findViewById(R.id.tv_role);
        tv_jurusan = findViewById(R.id.tv_jurusan);
        tv_nama = findViewById(R.id.tv_nama);
        tv_nomorHp = findViewById(R.id.tv_nomorHp);
        tv_email = findViewById(R.id.tv_email);
        tv_alamat = findViewById(R.id.tv_alamat);
        tv_dibuat_oleh = findViewById(R.id.tv_dibuat_oleh);
        tv_dibuat_pada = findViewById(R.id.tv_dibuat_pada);
        tv_diperbarui_oleh = findViewById(R.id.tv_diperbarui_oleh);
        tv_diperbarui_pada = findViewById(R.id.tv_diperbarui_pada);

        PreferenceManager pref = new PreferenceManager(this);
        JsonObject user = pref.getUserData();

        ortu_id = getIntent().getStringExtra("id");
        String index = getIntent().getStringExtra("index");
        loadSiswa();
        loadData(ortu_id, index);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        finish();
    }

    private void loadData(String id, String index) {
        if (NetworkUtil.isConnected(this)) {
            ApiService apiService = ApiClient.getClient(DetailAkunGuruActivity.this).create(ApiService.class);
            apiService.userShow(id).enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        JsonObject json = response.body();

                        String status = json.has("status") ? json.get("status").getAsString() : "";

                        if (status.equals("success")) {
                            JsonObject data = json.has("data") ? json.get("data").getAsJsonObject() : null;

                            if (isFinishing() || isDestroyed()) return;

                            tv_no.setText(index != null && !index.trim().isEmpty() && !"null".equalsIgnoreCase(index.trim()) ? index.trim() : "-");
                            tv_username.setText(data.has("username") && !data.get("username").isJsonNull() ? data.get("username").getAsString() : "-");
                            String nip = data.has("nip") && !data.get("nip").isJsonNull() ? data.get("nip").getAsString().trim() : "";
                            tv_nip.setText(!nip.isEmpty() && !"null".equalsIgnoreCase(nip) ? nip : "-");

                            String role = data.has("role") && !data.get("role").isJsonNull() ? data.get("role").getAsString().trim() : "";
                            if (tv_role != null) tv_role.setText(Helpers.formatRole(role));

                            if (data.has("jurusan_id") && !data.get("jurusan_id").isJsonNull() && data.has("jurusan") && !data.get("jurusan").isJsonNull() && data.get("jurusan").isJsonObject()) {
                                JsonObject jurusan = data.get("jurusan").getAsJsonObject();
                                String namaJ = (jurusan.has("nama") && !jurusan.get("nama").isJsonNull()) ? jurusan.get("nama").getAsString().trim() : "";
                                String alias = (jurusan.has("alias") && !jurusan.get("alias").isJsonNull()) ? jurusan.get("alias").getAsString().trim() : "";
                                if (!namaJ.isEmpty() && !alias.isEmpty()) tv_jurusan.setText(namaJ + " (" + alias + ")");
                                else if (!namaJ.isEmpty()) tv_jurusan.setText(namaJ);
                                else if (!alias.isEmpty()) tv_jurusan.setText(alias);
                                else tv_jurusan.setText("-");
                            } else {
                                tv_jurusan.setText("-");
                            }

                            String nama = data.has("nama") && !data.get("nama").isJsonNull() ? data.get("nama").getAsString().trim() : "";
                            tv_nama.setText(!nama.isEmpty() && !"null".equalsIgnoreCase(nama) ? nama : "-");
                            String nomorHp = data.has("nomor_hp") && !data.get("nomor_hp").isJsonNull() ? data.get("nomor_hp").getAsString().trim() : "";
                            tv_nomorHp.setText(!nomorHp.isEmpty() && !"null".equalsIgnoreCase(nomorHp) ? nomorHp : "-");
                            String email = data.has("email") && !data.get("email").isJsonNull() ? data.get("email").getAsString().trim() : "";
                            tv_email.setText(!email.isEmpty() && !"null".equalsIgnoreCase(email) ? email : "-");
                            String alamat = data.has("alamat") && !data.get("alamat").isJsonNull() ? data.get("alamat").getAsString().trim() : "";
                            tv_alamat.setText(!alamat.isEmpty() && !"null".equalsIgnoreCase(alamat) ? alamat : "-");

                            tv_dibuat_oleh.setText(Helpers.formatUserAudit(data.has("created_by") && !data.get("created_by").isJsonNull() ? data.getAsJsonObject("created_by") : null));
                            tv_dibuat_pada.setText(Helpers.formatDateTimeCombined(data.has("created_at") && !data.get("created_at").isJsonNull() ? data.get("created_at").getAsString() : null));
                            tv_diperbarui_oleh.setText("-");
                            tv_diperbarui_pada.setText(data.has("updated_at") && !data.get("updated_at").isJsonNull() ? Helpers.formatDateTimeCombined(data.get("updated_at").getAsString()) : "-");

                            if (data.has("gambar") && !data.get("gambar").isJsonNull()) {
                                String gambar = data.get("gambar").getAsString();

                                if (!gambar.startsWith("http")) {
                                    gambar = ApiClient.base_url + gambar;
                                }

                                iv_gambar.setImageTintList(null);

                                Glide.with(DetailAkunGuruActivity.this)
                                        .load(gambar)
                                        .placeholder(R.drawable.ic_image_24)
                                        .error(R.drawable.ic_image_24)
                                        .into(iv_gambar);

                    final String previewGambar = gambar;
                    iv_gambar.setOnClickListener(v -> Helpers.showImagePreview(DetailAkunGuruActivity.this, previewGambar));
                            } else {
                                iv_gambar.setImageResource(R.drawable.ic_image_24);
                                iv_gambar.setImageTintList(
                                        ContextCompat.getColorStateList(DetailAkunGuruActivity.this, android.R.color.darker_gray)
                                );
                            }


                            ll_progress.setVisibility(View.GONE);
                            sv_main.setVisibility(View.VISIBLE);
                            isDataLoaded = true;
                            invalidateOptionsMenu();
                        } else {
                            Toast.makeText(DetailAkunGuruActivity.this, "Tidak dapat memuat data. Coba lagi nanti.", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } else {
                        Toast.makeText(DetailAkunGuruActivity.this, "Tidak dapat memuat data. Coba lagi nanti.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Toast.makeText(DetailAkunGuruActivity.this, "Terjadi kesalahan: " + t.getMessage(), Toast.LENGTH_LONG).show();
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

    private void loadSiswa() {
        if (NetworkUtil.isConnected(this)) {
            ApiService apiService = ApiClient.getClient(DetailAkunGuruActivity.this).create(ApiService.class);
            apiService.info().enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        JsonObject json = response.body();

                        String status = json.has("status") ? json.get("status").getAsString() : "";

                        if (status.equals("success")) {
                            JsonObject main_data = json.has("data") ? json.get("data").getAsJsonObject() : null;
                            JsonObject data = main_data.has("data") ? main_data.get("data").getAsJsonObject() : null;
                            JsonArray siswas = data.has("siswa") ? data.get("siswa").getAsJsonArray() : null;

                            if (siswas != null || siswas.size() != 0) {
                                for (int i = 0; i < siswas.size(); i++) {
                                    JsonObject d = siswas.get(i).getAsJsonObject();

                                    if (!d.get("orangtua_id").isJsonNull()) {
                                        if ((d.get("orangtua_id").getAsString()).equals(ortu_id)) {
                                            ortuDariSiswa += (ortuDariSiswa.equals("") ? "" : ",\n") + d.get("nama").getAsString() + " (" + (!d.get("nis").isJsonNull() ? (d.get("nis").getAsString() + "/") : "") + d.get("kelas").getAsString() + ")";
                                        }
                                    }
                                }
                            }
                        } else {
                            Toast.makeText(DetailAkunGuruActivity.this, "Tidak dapat memuat data. Coba lagi nanti.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(DetailAkunGuruActivity.this, "Tidak dapat memuat data. Coba lagi nanti.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Toast.makeText(DetailAkunGuruActivity.this, "Terjadi kesalahan: " + t.getMessage(), Toast.LENGTH_LONG).show();
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
            getMenuInflater().inflate(R.menu.menu_top_nav_detail_edit_delete, menu);
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
        if (item.getItemId() == R.id.nav_edit) {
            Intent intent = new Intent(this, EditAkunGuruActivity.class);
            intent.putExtra("id", ortu_id);
            editDataLauncher.launch(intent);
            Helpers.overridePendingTransition_Enter_v1(this);
            return true;
        }

        if (item.getItemId() == R.id.nav_delete) {
            Helpers.showConfirm(this, "Konfirmasi", "Apakah Anda yakin ingin menghapus akun guru ini?", (dialogInterface, i) -> {
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
        apiService.userDelete(getIntent().getStringExtra("id")).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                loading.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(DetailAkunGuruActivity.this, "Data akun guru berhasil dihapus", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    String msg = "Gagal menghapus data akun guru";
                    try {
                        if (response.errorBody() != null) {
                            JsonObject err = new com.google.gson.JsonParser().parse(response.errorBody().string()).getAsJsonObject();
                            if (err.has("message")) msg = err.get("message").getAsString();
                        }
                    } catch (Exception ignored) {}
                    Toast.makeText(DetailAkunGuruActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                loading.dismiss();
                Toast.makeText(DetailAkunGuruActivity.this, "Terjadi kesalahan: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
