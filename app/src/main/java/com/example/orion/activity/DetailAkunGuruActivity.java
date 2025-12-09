package com.example.orion.activity;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailAkunGuruActivity extends AppCompatActivity {

    ImageView iv_gambar;
    TextView tv_no, tv_nip, tv_nama, tv_jurusan, tv_nomorHp, tv_email, tv_alamat, tv_username;
    LinearLayout ll_progress;
    ScrollView sv_main;
    String ortuDariSiswa = "", ortu_id = null;

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
        tv_jurusan = findViewById(R.id.tv_jurusan);
        tv_nama = findViewById(R.id.tv_nama);
        tv_nomorHp = findViewById(R.id.tv_nomorHp);
        tv_email = findViewById(R.id.tv_email);
        tv_alamat = findViewById(R.id.tv_alamat);

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

                            tv_no.setText(index);
                            tv_username.setText(data.get("username").getAsString());
                            tv_nip.setText(data.get("nip").isJsonNull() ? "-" : data.get("nip").getAsString());
                            if (!data.get("jurusan_id").isJsonNull()) {
                                JsonObject jurusan = data.get("jurusan").getAsJsonObject();
                                String alias = jurusan.get("alias").isJsonNull() ? "" : jurusan.get("alias").getAsString();

                                tv_jurusan.setText(jurusan.get("nama").getAsString() + (alias.equals("") ? "" : " (" + alias + ")"));
                            }
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

                                Glide.with(DetailAkunGuruActivity.this)
                                        .load(gambar)
                                        .placeholder(R.drawable.ic_image_24)
                                        .error(R.drawable.ic_image_24)
                                        .into(iv_gambar);
                            } else {
                                iv_gambar.setImageResource(R.drawable.ic_image_24);
                                iv_gambar.setImageTintList(
                                        ContextCompat.getColorStateList(DetailAkunGuruActivity.this, android.R.color.darker_gray)
                                );
                            }


                            ll_progress.setVisibility(View.GONE);
                            sv_main.setVisibility(View.VISIBLE);
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
}