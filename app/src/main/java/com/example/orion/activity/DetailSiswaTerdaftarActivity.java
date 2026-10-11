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

import android.content.Intent;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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

public class DetailSiswaTerdaftarActivity extends AppCompatActivity {
    private boolean isDataLoaded = false;
    private String id;

    TextView tv_no, tv_nis, tv_nama, tv_nomorHp, tv_jurusan, tv_kelas, tv_ortu, tv_dibuat_oleh, tv_dibuat_pada, tv_diperbarui_oleh, tv_diperbarui_pada;
    private ActivityResultLauncher<Intent> editDataLauncher;
    LinearLayout ll_progress;
    ScrollView sv_main;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_siswa_terdaftar);

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
        tv_no = findViewById(R.id.tv_no);
        tv_nis = findViewById(R.id.tv_nis);
        tv_nama = findViewById(R.id.tv_nama);
        tv_nomorHp = findViewById(R.id.tv_nomorHp);
        tv_jurusan = findViewById(R.id.tv_jurusan);
        tv_kelas = findViewById(R.id.tv_kelas);
        tv_ortu = findViewById(R.id.tv_ortu);
        tv_dibuat_oleh = findViewById(R.id.tv_dibuat_oleh);
        tv_dibuat_pada = findViewById(R.id.tv_dibuat_pada);
        tv_diperbarui_oleh = findViewById(R.id.tv_diperbarui_oleh);
        tv_diperbarui_pada = findViewById(R.id.tv_diperbarui_pada);

        editDataLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        loadData(id, getIntent().getStringExtra("index"));
                        setResult(RESULT_OK);
                    }
                }
        );

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
            ApiService apiService = ApiClient.getClient(DetailSiswaTerdaftarActivity.this).create(ApiService.class);
            apiService.siswaShow(id).enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        JsonObject json = response.body();

                        String status = json.has("status") ? json.get("status").getAsString() : "";

                        if (status.equals("success")) {
                            JsonObject data = json.has("data") ? json.get("data").getAsJsonObject() : null;

                            if (isFinishing() || isDestroyed()) return;

                            JsonObject siswa = (data.has("siswa") && !data.get("siswa").isJsonNull() && data.get("siswa").isJsonObject())
                                    ? data.getAsJsonObject("siswa")
                                    : data;

                            // NIS
                            String nis = (siswa.has("nis") && !siswa.get("nis").isJsonNull()) ? siswa.get("nis").getAsString().trim() : "";
                            tv_nis.setText(!nis.isEmpty() && !"null".equalsIgnoreCase(nis) ? nis : "-");

                            // Nama
                            String nama = (siswa.has("nama") && !siswa.get("nama").isJsonNull()) ? siswa.get("nama").getAsString().trim() : "";
                            tv_nama.setText(!nama.isEmpty() && !"null".equalsIgnoreCase(nama) ? nama : "-");

                            // Nomor HP
                            String nomorHp = (siswa.has("nomor_hp") && !siswa.get("nomor_hp").isJsonNull()) ? siswa.get("nomor_hp").getAsString().trim() : "";
                            tv_nomorHp.setText(!nomorHp.isEmpty() && !"null".equalsIgnoreCase(nomorHp) ? nomorHp : "-");

                            // Kelas
                            String kelas = (siswa.has("kelas") && !siswa.get("kelas").isJsonNull()) ? siswa.get("kelas").getAsString().trim() : "";
                            tv_kelas.setText(!kelas.isEmpty() && !"null".equalsIgnoreCase(kelas) ? kelas : "-");

                            // Jurusan
                            if (siswa.has("jurusan") && !siswa.get("jurusan").isJsonNull() && siswa.get("jurusan").isJsonObject()) {
                                JsonObject jurusan = siswa.getAsJsonObject("jurusan");
                                String namaJurusan = (jurusan.has("nama") && !jurusan.get("nama").isJsonNull()) ? jurusan.get("nama").getAsString().trim() : "";
                                String alias = (jurusan.has("alias") && !jurusan.get("alias").isJsonNull()) ? jurusan.get("alias").getAsString().trim() : "";
                                if (!namaJurusan.isEmpty() && !alias.isEmpty()) {
                                    tv_jurusan.setText(namaJurusan + " (" + alias + ")");
                                } else if (!namaJurusan.isEmpty()) {
                                    tv_jurusan.setText(namaJurusan);
                                } else if (!alias.isEmpty()) {
                                    tv_jurusan.setText(alias);
                                } else {
                                    tv_jurusan.setText("-");
                                }
                            } else {
                                tv_jurusan.setText("-");
                            }

                            // Orangtua
                            if (siswa.has("orangtua") && !siswa.get("orangtua").isJsonNull() && siswa.get("orangtua").isJsonObject()) {
                                JsonObject orangtua = siswa.getAsJsonObject("orangtua");
                                String namaOrtu = (orangtua.has("nama") && !orangtua.get("nama").isJsonNull()) ? orangtua.get("nama").getAsString().trim() : "";
                                String userOrtu = (orangtua.has("username") && !orangtua.get("username").isJsonNull()) ? orangtua.get("username").getAsString().trim() : "";
                                if (!namaOrtu.isEmpty() && !userOrtu.isEmpty()) {
                                    tv_ortu.setText(namaOrtu + " (" + userOrtu + ")");
                                } else if (!namaOrtu.isEmpty()) {
                                    tv_ortu.setText(namaOrtu);
                                } else if (!userOrtu.isEmpty()) {
                                    tv_ortu.setText(userOrtu);
                                } else {
                                    tv_ortu.setText("-");
                                }
                            } else {
                                tv_ortu.setText("-");
                            }

                            // Index (No)
                            tv_no.setText(index != null && !index.trim().isEmpty() && !"null".equalsIgnoreCase(index.trim()) ? index.trim() : "-");

                            // Audit trail
                            tv_dibuat_oleh.setText(Helpers.formatUserAudit(siswa.has("created_by") && !siswa.get("created_by").isJsonNull() ? siswa.getAsJsonObject("created_by") : null));
                            tv_dibuat_pada.setText(Helpers.formatDateTimeCombined(siswa.has("created_at") && !siswa.get("created_at").isJsonNull() ? siswa.get("created_at").getAsString() : null));

                            boolean hasUpdatedBy = siswa.has("updated_by") && !siswa.get("updated_by").isJsonNull();
                            tv_diperbarui_oleh.setText(hasUpdatedBy ? Helpers.formatUserAudit(siswa.getAsJsonObject("updated_by")) : "-");
                            tv_diperbarui_pada.setText(hasUpdatedBy && siswa.has("updated_at") && !siswa.get("updated_at").isJsonNull() ? Helpers.formatDateTimeCombined(siswa.get("updated_at").getAsString()) : "-");

                            ll_progress.setVisibility(View.GONE);
                            sv_main.setVisibility(View.VISIBLE);
                            isDataLoaded = true;
                            invalidateOptionsMenu();
                        } else {
                            Toast.makeText(DetailSiswaTerdaftarActivity.this, "Tidak dapat memuat data. Coba lagi nanti.", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } else {
                        Toast.makeText(DetailSiswaTerdaftarActivity.this, "Tidak dapat memuat data. Coba lagi nanti.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Toast.makeText(DetailSiswaTerdaftarActivity.this, "Terjadi kesalahan: " + t.getMessage(), Toast.LENGTH_LONG).show();
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
        if (role.equals("superadmin") || role.equals("kepsek") || role.equals("kesiswaan") || role.equals("bimbingan_konseling")) {
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
            Intent editIntent = new Intent(this, EditSiswaTerdaftarActivity.class);
            editIntent.putExtra("id", id);
            editDataLauncher.launch(editIntent);
            Helpers.overridePendingTransition_Enter_v1(this);
            return true;
        }

        if (item.getItemId() == R.id.nav_delete) {
            Helpers.showConfirm(this, "Konfirmasi", "Apakah Anda yakin ingin menghapus data siswa ini?", (dialogInterface, i) -> {
            dialogInterface.dismiss();
            deleteSiswa();
        });
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteSiswa() {
        if (!NetworkUtil.isConnected(this)) {
            Toast.makeText(this, "Tidak ada koneksi internet", Toast.LENGTH_SHORT).show();
            return;
        }
        Helpers.LoadingHelper loading = new Helpers.LoadingHelper(this);
        loading.show();
        ApiService apiService = ApiClient.getClient(this).create(ApiService.class);
        apiService.siswaDelete(id).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                loading.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(DetailSiswaTerdaftarActivity.this, "Data siswa berhasil dihapus", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    String msg = "Gagal menghapus data siswa";
                    try {
                        if (response.errorBody() != null) {
                            JsonObject err = new com.google.gson.JsonParser().parse(response.errorBody().string()).getAsJsonObject();
                            if (err.has("message")) msg = err.get("message").getAsString();
                        }
                    } catch (Exception ignored) {}
                    Toast.makeText(DetailSiswaTerdaftarActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                loading.dismiss();
                Toast.makeText(DetailSiswaTerdaftarActivity.this, "Terjadi kesalahan: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
