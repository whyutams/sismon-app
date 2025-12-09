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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailSiswaTerdaftarActivity extends AppCompatActivity {

    TextView tv_no, tv_nis, tv_nama, tv_nomorHp, tv_jurusan, tv_kelas, tv_ortu;
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

        PreferenceManager pref = new PreferenceManager(this);
        JsonObject user = pref.getUserData();

        String id = getIntent().getStringExtra("id");
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
            apiService.keterlambatanShow(id).enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        JsonObject json = response.body();

                        String status = json.has("status") ? json.get("status").getAsString() : "";

                        if (status.equals("success")) {
                            JsonObject data = json.has("data") ? json.get("data").getAsJsonObject() : null;

                            if (isFinishing() || isDestroyed()) return;

                            if (data.has("siswa")) {
                                JsonObject siswa = data.get("siswa").getAsJsonObject();

                                if (siswa.has("jurusan") && !siswa.get("jurusan").isJsonNull()) {
                                    JsonObject jurusan = siswa.get("jurusan").getAsJsonObject();
                                    String alias = "";

                                    if (jurusan.has("alias"))
                                        alias = jurusan.get("alias").getAsString();

                                    tv_jurusan.setText(jurusan.get("nama").getAsString() + (!alias.equals("") ? " (" + alias + ")" : ""));
                                }

                                if (siswa.has("orangtua")
                                        && !siswa.get("orangtua").isJsonNull()
                                        && siswa.get("orangtua").isJsonObject()) {

                                    JsonObject orangtua = siswa.get("orangtua").getAsJsonObject();

                                    if (orangtua.has("nama") && orangtua.has("username")) {
                                        tv_ortu.setText(orangtua.get("nama").getAsString() + " (" + orangtua.get("username").getAsString() + ")");
                                    }
                                } else {
                                    tv_ortu.setText("-");
                                }

                                tv_nis.setText(siswa.has("nis") ? siswa.get("nis").getAsString() : "-");
                                tv_nama.setText(siswa.get("nama").getAsString());
                                tv_nomorHp.setText(siswa.has("nomor_hp") ? siswa.get("nomor_hp").getAsString() : "-");
                                tv_kelas.setText(siswa.has("kelas") ? siswa.get("kelas").getAsString() : "-");
                            } else {
                                tv_ortu.setText("-");
                                tv_jurusan.setText("-");
                            }

                            tv_no.setText(index);

                            ll_progress.setVisibility(View.GONE);
                            sv_main.setVisibility(View.VISIBLE);
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
}