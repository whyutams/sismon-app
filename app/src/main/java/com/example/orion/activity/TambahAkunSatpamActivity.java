package com.example.orion.activity;

import static com.example.orion.utils.Helpers.hideKeyboard;

import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import com.example.orion.R;
import com.example.orion.utils.Helpers;
import com.example.orion.utils.NetworkUtil;
import com.example.orion.utils.PreferenceManager;
import com.example.orion.utils.api.ApiClient;
import com.example.orion.utils.api.ApiService;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TambahAkunSatpamActivity extends AppCompatActivity {

    private TextInputEditText et_jurusan, et_alias, et_deskripsi;
    private TextInputLayout til_jurusan, til_alias, til_deskripsi;
    private Button btn_simpan;
    private List<Map<String, @Nullable Object>> jurusanList = new ArrayList<>();
    private JsonObject user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tambah_jurusan_terdaftar);

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

        PreferenceManager pref = new PreferenceManager(this);
        user = pref.getUserData();

        til_jurusan = findViewById(R.id.til_jurusan);
        til_alias = findViewById(R.id.til_alias);
        til_deskripsi = findViewById(R.id.til_deskripsi);
        et_jurusan = findViewById(R.id.et_jurusan);
        et_alias = findViewById(R.id.et_alias);
        et_deskripsi = findViewById(R.id.et_deskripsi);
        btn_simpan = findViewById(R.id.btn_simpan);

        loadJurusan();

        Helpers.LoadingHelper loading = new Helpers.LoadingHelper(this);

        btn_simpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loading.show();

                Helpers.delay(250, new Runnable() {
                    @Override
                    public void run() {
                        if (Helpers.validateRequired(til_jurusan) && Helpers.validateRequired(til_alias) && Helpers.validateRequired(til_deskripsi)) {
                            boolean is_sim_jurusan = false, is_sim_alias = false;

                            for (Map<String, @Nullable Object> jurusan : jurusanList) {
                                String nama = jurusan.get("nama").toString();
                                String alias = jurusan.get("alias").toString();

                                if (nama.equalsIgnoreCase(et_jurusan.getText().toString()))
                                    is_sim_jurusan = true;
                                if (alias.equalsIgnoreCase(et_alias.getText().toString()))
                                    is_sim_alias = true;
                            }

                            if(is_sim_jurusan || is_sim_alias) {
                                if(is_sim_jurusan) {
                                    til_jurusan.setErrorEnabled(true);
                                    til_jurusan.setError("Nama jurusan sudah digunakan.");
                                }
                                if(is_sim_alias) {
                                    til_alias.setErrorEnabled(true);
                                    til_alias.setError("Alias jurusan sudah digunakan.");
                                }

                                loading.dismiss();

                                return;
                            } else {
                                til_jurusan.setErrorEnabled(true);
                                til_jurusan.setError(null);
                                til_alias.setErrorEnabled(true);
                                til_alias.setError(null);
                            }

                            if (NetworkUtil.isConnected(TambahAkunSatpamActivity.this)) {
                                RequestBody jurusan = RequestBody.create(MultipartBody.FORM, et_jurusan.getText().toString());
                                RequestBody alias = RequestBody.create(MultipartBody.FORM, et_alias.getText().toString());
                                RequestBody deskripsi = RequestBody.create(MultipartBody.FORM, et_deskripsi.getText().toString());

                                if (NetworkUtil.isConnected(TambahAkunSatpamActivity.this)) {
                                    ApiService apiService = ApiClient.getClient(TambahAkunSatpamActivity.this).create(ApiService.class);
                                    Call<JsonObject> call = apiService.jurusanAdd(jurusan, alias, deskripsi);

                                    call.enqueue(new Callback<JsonObject>() {
                                        @Override
                                        public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                                            if (response.isSuccessful() && response.body() != null) {
                                                setResult(RESULT_OK);
                                                Toast.makeText(getApplicationContext(), "Data berhasil ditambahkan!", Toast.LENGTH_SHORT).show();
                                                finish();
                                                loading.dismiss();
                                            } else {
                                                Toast.makeText(getApplicationContext(), "Gagal menambahkan data baru!", Toast.LENGTH_SHORT).show();
                                                loading.dismiss();
                                            }

                                        }

                                        @Override
                                        public void onFailure(Call<JsonObject> call, Throwable t) {
                                            Toast.makeText(getApplicationContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();

                                            loading.dismiss();
                                        }
                                    });
                                } else {
                                    Toast.makeText(TambahAkunSatpamActivity.this, "Koneksi internet tidak stabil.", Toast.LENGTH_SHORT).show();

                                    loading.dismiss();
                                }

                            } else {
                                Toast.makeText(TambahAkunSatpamActivity.this, "Koneksi Internet tidak stabil.", Toast.LENGTH_SHORT).show();

                                loading.dismiss();
                            }
                        } else {
                            loading.dismiss();
                        }

                        hideKeyboard(TambahAkunSatpamActivity.this);
                    }
                });
            }
        });

    }

    private void loadJurusan() {
        if (NetworkUtil.isConnected(this)) {
            jurusanList.clear();
            ApiService apiService = ApiClient.getClient(TambahAkunSatpamActivity.this).create(ApiService.class);
            apiService.info().enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        JsonObject json = response.body();

                        String status = json.has("status") ? json.get("status").getAsString() : "";

                        if (status.equals("success")) {
                            JsonObject main_data = json.has("data") ? json.get("data").getAsJsonObject() : null;
                            JsonObject data = main_data.has("data") ? main_data.get("data").getAsJsonObject() : null;
                            JsonArray jurusans = data.has("jurusan") ? data.get("jurusan").getAsJsonArray() : null;

                            if (jurusans != null || jurusans.size() != 0) {
                                for (int i = 0; i < jurusans.size(); i++) {
                                    JsonObject d = jurusans.get(i).getAsJsonObject();

                                    Map<String, Object> jurusan = new HashMap<>();
                                    jurusan.put("id", d.get("id").getAsString());
                                    jurusan.put("nama", d.get("nama").getAsString());
                                    jurusan.put("alias", d.get("alias").getAsString());
                                    jurusan.put("deskripsi", d.get("deskripsi").getAsString());

                                }
                            }
                        } else {
                            Toast.makeText(TambahAkunSatpamActivity.this, "Tidak dapat memuat data. Coba lagi nanti.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(TambahAkunSatpamActivity.this, "Tidak dapat memuat data. Coba lagi nanti.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Toast.makeText(TambahAkunSatpamActivity.this, "Terjadi kesalahan: " + t.getMessage(), Toast.LENGTH_LONG).show();
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