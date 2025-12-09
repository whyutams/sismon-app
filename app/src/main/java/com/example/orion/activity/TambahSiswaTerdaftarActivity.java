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

import org.json.JSONArray;
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

public class TambahSiswaTerdaftarActivity extends AppCompatActivity {

    private TextInputEditText et_nis, et_nama, et_nomorHp, et_jurusan, et_kelas, et_orangtua;
    private TextInputLayout til_nis, til_nama, til_nomorHp, til_jurusan, til_kelas, til_orangtua;
    private Button btn_simpan;
    private List<Map<String, @Nullable Object>> siswaList = new ArrayList<>(), jurusanList = new ArrayList<>(), orangtuaList = new ArrayList<>(), kelasList = new ArrayList<>(), filteredKelasList = new ArrayList<>();
    private String selectedJurusan = null, selectedJurusanId = null, selectedOrangtuaId = null, selectedKelas = null;
    private JsonObject user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tambah_siswa_terdaftar);

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

        til_nis = findViewById(R.id.til_nis);
        til_nama = findViewById(R.id.til_nama);
        til_nomorHp = findViewById(R.id.til_nomorHp);
        til_jurusan = findViewById(R.id.til_jurusan);
        til_kelas = findViewById(R.id.til_kelas);
        til_orangtua = findViewById(R.id.til_orangtua);
        et_nis = findViewById(R.id.et_nis);
        et_nama = findViewById(R.id.et_nama);
        et_nomorHp = findViewById(R.id.et_nomorHp);
        et_jurusan = findViewById(R.id.et_jurusan);
        et_kelas = findViewById(R.id.et_kelas);
        et_orangtua = findViewById(R.id.et_orangtua);
        btn_simpan = findViewById(R.id.btn_simpan);

        loadData();
        loadKelas();

        et_jurusan.setOnClickListener(v -> showJurusanPicker());
        et_kelas.setOnClickListener(v -> showKelasPicker());
        et_orangtua.setOnClickListener(v -> showOrangtuaPicker());

        til_jurusan.setEnabled(true);
        til_orangtua.setEnabled(true);

        Helpers.LoadingHelper loading = new Helpers.LoadingHelper(this);

        btn_simpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loading.show();

                Helpers.delay(250, new Runnable() {
                    @Override
                    public void run() {
                        if (Helpers.validateRequired(til_nama) && Helpers.validateRequired(til_kelas) && Helpers.validateRequired(til_jurusan)) {
                            boolean is_nomorHp = false, is_nis = false;

                            for (Map<String, @Nullable Object> _d : siswaList) {
                                String nomorHp = _d.get("nomor_hp").toString();
                                String nis = _d.get("nis").toString();

                                if (!et_nomorHp.getText().toString().isEmpty() && nomorHp.equalsIgnoreCase(et_nomorHp.getText().toString()))
                                    is_nomorHp = true;
                                if (!et_nis.getText().toString().isEmpty() && nis.equalsIgnoreCase(et_nis.getText().toString()))
                                    is_nis = true;
                            }

                            if (is_nomorHp || is_nis) {
                                if (is_nomorHp) {
                                    til_nomorHp.setErrorEnabled(true);
                                    til_nomorHp.setError("Nomor HP sudah digunakan.");
                                }
                                if (is_nis) {
                                    til_nis.setErrorEnabled(true);
                                    til_nis.setError("NIS sudah digunakan.");
                                }

                                loading.dismiss();

                                return;
                            } else {
                                til_nomorHp.setErrorEnabled(false);
                                til_nomorHp.setError(null);
                                til_nis.setErrorEnabled(false);
                                til_nis.setError(null);
                            }

                            if (NetworkUtil.isConnected(TambahSiswaTerdaftarActivity.this)) {
                                RequestBody nis = RequestBody.create(MultipartBody.FORM, et_nis.getText().toString());
                                RequestBody nama = RequestBody.create(MultipartBody.FORM, et_nama.getText().toString());
                                RequestBody nomor_hp = RequestBody.create(MultipartBody.FORM, et_nomorHp.getText().toString());
                                RequestBody jurusan = RequestBody.create(MultipartBody.FORM, selectedJurusanId);
                                RequestBody kelas = RequestBody.create(MultipartBody.FORM, selectedKelas);

                                if (NetworkUtil.isConnected(TambahSiswaTerdaftarActivity.this)) {
                                    ApiService apiService = ApiClient.getClient(TambahSiswaTerdaftarActivity.this).create(ApiService.class);
                                    Call<JsonObject> call = apiService.siswaAdd(nis, nama, nomor_hp, jurusan, kelas, (selectedOrangtuaId == null ? null : RequestBody.create(MultipartBody.FORM, selectedOrangtuaId)));

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
                                    Toast.makeText(TambahSiswaTerdaftarActivity.this, "Koneksi internet tidak stabil.", Toast.LENGTH_SHORT).show();

                                    loading.dismiss();
                                }

                            } else {
                                Toast.makeText(TambahSiswaTerdaftarActivity.this, "Koneksi Internet tidak stabil.", Toast.LENGTH_SHORT).show();

                                loading.dismiss();
                            }
                        } else {
                            loading.dismiss();
                        }

                        hideKeyboard(TambahSiswaTerdaftarActivity.this);
                    }
                });
            }
        });

    }

    private void loadData() {
        if (NetworkUtil.isConnected(this)) {
            siswaList.clear();
            ApiService apiService = ApiClient.getClient(TambahSiswaTerdaftarActivity.this).create(ApiService.class);
            apiService.info().enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        JsonObject json = response.body();

                        String status = json.has("status") ? json.get("status").getAsString() : "";

                        if (status.equals("success")) {
                            JsonObject main_data = json.has("data") ? json.get("data").getAsJsonObject() : null;
                            JsonObject data = main_data.has("data") ? main_data.get("data").getAsJsonObject() : null;
                            JsonArray siswa = data.has("siswa") ? data.get("siswa").getAsJsonArray() : null;
                            JsonArray jurusan = data.has("jurusan") ? data.get("jurusan").getAsJsonArray() : null;
                            JsonArray orangtua = data.has("user_orangtua") ? data.get("user_orangtua").getAsJsonArray() : null;

                            if (siswa != null || siswa.size() != 0) {
                                for (int i = 0; i < siswa.size(); i++) {
                                    JsonObject d = siswa.get(i).getAsJsonObject();

                                    Map<String, Object> hashMap = new HashMap<>();
                                    hashMap.put("nomor_hp", !d.get("nomor_hp").isJsonNull() && d.has("nomor_hp") ? d.get("nomor_hp").getAsString() : "");
                                    hashMap.put("nis", !d.get("nis").isJsonNull() && d.has("nis") ? d.get("nis").getAsString() : "");

                                    siswaList.add(hashMap);
                                }
                            }

                            if (jurusan != null || jurusan.size() != 0) {
                                for (int i = 0; i < jurusan.size(); i++) {
                                    JsonObject d = jurusan.get(i).getAsJsonObject();

                                    Map<String, Object> hashMap = new HashMap<>();
                                    hashMap.put("id", d.get("id").getAsString());
                                    hashMap.put("nama", d.get("nama").getAsString());
                                    hashMap.put("alias", d.get("alias").getAsString());
                                    hashMap.put("deskripsi", d.get("deskripsi").getAsString());

                                    jurusanList.add(hashMap);
                                }
                            }

                            if (orangtua != null || orangtua.size() != 0) {
                                for (int i = 0; i < orangtua.size(); i++) {
                                    JsonObject d = orangtua.get(i).getAsJsonObject();

                                    Map<String, Object> hashMap = new HashMap<>();
                                    hashMap.put("id", d.get("id").getAsString());
                                    hashMap.put("nama", d.get("nama").getAsString());
                                    hashMap.put("username", d.get("username").getAsString());

                                    orangtuaList.add(hashMap);
                                }
                            }
                        } else {
                            Toast.makeText(TambahSiswaTerdaftarActivity.this, "Tidak dapat memuat data. Coba lagi nanti.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(TambahSiswaTerdaftarActivity.this, "Tidak dapat memuat data. Coba lagi nanti.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Toast.makeText(TambahSiswaTerdaftarActivity.this, "Terjadi kesalahan: " + t.getMessage(), Toast.LENGTH_LONG).show();
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

    private void loadKelas() {
        if (NetworkUtil.isConnected(this)) {
            siswaList.clear();
            ApiService apiService = ApiClient.getClient(TambahSiswaTerdaftarActivity.this).create(ApiService.class);
            apiService.kelasList().enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        JsonObject json = response.body();

                        String status = json.has("status") ? json.get("status").getAsString() : "";

                        if (status.equals("success")) {
                            JsonArray data = json.has("data") ? json.get("data").getAsJsonArray() : null;

                            if (data != null || data.size() != 0) {
                                for (int i = 0; i < data.size(); i++) {
                                    JsonObject d = data.get(i).getAsJsonObject();

                                    Map<String, Object> hashMap = new HashMap<>();
                                    hashMap.put("kelas", d.get("kelas").getAsString());
                                    hashMap.put("nama_kelas", d.get("nama_kelas").getAsString());
                                    hashMap.put("jurusan", d.get("jurusan").getAsString());

                                    kelasList.add(hashMap);
                                }
                            }
                        } else {
                            Toast.makeText(TambahSiswaTerdaftarActivity.this, "Tidak dapat memuat data. Coba lagi nanti.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(TambahSiswaTerdaftarActivity.this, "Tidak dapat memuat data. Coba lagi nanti.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Toast.makeText(TambahSiswaTerdaftarActivity.this, "Terjadi kesalahan: " + t.getMessage(), Toast.LENGTH_LONG).show();
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

    private void showJurusanPicker() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pilih Jurusan");

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_picker_1, null);
        builder.setView(dialogView);

        SearchView searchView = dialogView.findViewById(R.id.searchView);
        ListView listView = dialogView.findViewById(R.id.listView);

        ArrayAdapter<Map<String, Object>> adapter = new ArrayAdapter<Map<String, Object>>(this,
                android.R.layout.simple_list_item_2, android.R.id.text1, new ArrayList<>(jurusanList)) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = view.findViewById(android.R.id.text1);
                TextView text2 = view.findViewById(android.R.id.text2);

                Map<String, Object> s = getItem(position);

                text1.setText(s.get("alias").toString());
                text2.setTextColor(getColor(android.R.color.darker_gray));
                text2.setText(s.get("nama").toString());

                return view;
            }
        };

        listView.setAdapter(adapter);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                List<Map<String, Object>> filtered = new ArrayList<>();
                for (Map<String, Object> s : jurusanList) {
                    if (s.get("nama").toString().toLowerCase().contains(newText.toLowerCase()) ||
                            s.get("alias").toString().toLowerCase().contains(newText.toLowerCase())) {
                        filtered.add(s);
                    }
                }
                adapter.clear();
                adapter.addAll(filtered);
                adapter.notifyDataSetChanged();
                return true;
            }
        });

        AlertDialog dialog = builder.create();

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Map<String, Object> selected = adapter.getItem(position);
            if (selected != null) {
                et_jurusan.setText(selected.get("nama").toString() + (selected.get("alias") != null ? " (" + selected.get("alias").toString() + ")" : ""));
                selectedJurusanId = selected.get("id").toString();
                selectedJurusan = selected.get("nama").toString();

                filteredKelasList = new ArrayList<>();

                if (selectedJurusan != null && !selectedJurusan.isEmpty()) {
                    for (Map<String, Object> s : kelasList) {
                        if (s.containsKey("jurusan") && s.get("jurusan") != null &&
                                s.get("jurusan").toString().equals(selectedJurusan)) {

                            filteredKelasList.add(s);
                        }
                    }

                    til_kelas.setEnabled(true);
                    et_kelas.setText(null);
                    selectedKelas=null;
                }
            }
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showOrangtuaPicker() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pilih Orangtua Siswa");

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_picker_1, null);
        builder.setView(dialogView);

        SearchView searchView = dialogView.findViewById(R.id.searchView);
        ListView listView = dialogView.findViewById(R.id.listView);

        ArrayAdapter<Map<String, Object>> adapter = new ArrayAdapter<Map<String, Object>>(this,
                android.R.layout.simple_list_item_2, android.R.id.text1, new ArrayList<>(orangtuaList)) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = view.findViewById(android.R.id.text1);
                TextView text2 = view.findViewById(android.R.id.text2);

                Map<String, Object> s = getItem(position);

                text1.setText(s.get("nama").toString());
                text2.setTextColor(getColor(android.R.color.darker_gray));
                text2.setText(s.get("username").toString());

                return view;
            }
        };

        listView.setAdapter(adapter);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                List<Map<String, Object>> filtered = new ArrayList<>();
                for (Map<String, Object> s : orangtuaList) {
                    if (s.get("nama").toString().toLowerCase().contains(newText.toLowerCase()) ||
                            s.get("username").toString().toLowerCase().contains(newText.toLowerCase())) {
                        filtered.add(s);
                    }
                }
                adapter.clear();
                adapter.addAll(filtered);
                adapter.notifyDataSetChanged();
                return true;
            }
        });

        AlertDialog dialog = builder.create();

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Map<String, Object> selected = adapter.getItem(position);
            if (selected != null) {
                et_orangtua.setText(selected.get("nama").toString() + " (" + selected.get("username").toString() + ")" );
                selectedOrangtuaId = selected.get("id").toString();
            }
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showKelasPicker() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pilih Kelas");

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_picker_1, null);
        builder.setView(dialogView);

        SearchView searchView = dialogView.findViewById(R.id.searchView);
        ListView listView = dialogView.findViewById(R.id.listView);

        ArrayAdapter<Map<String, Object>> adapter = new ArrayAdapter<Map<String, Object>>(this,
                android.R.layout.simple_list_item_2, android.R.id.text1, new ArrayList<>(filteredKelasList)) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = view.findViewById(android.R.id.text1);
                TextView text2 = view.findViewById(android.R.id.text2);

                Map<String, Object> s = getItem(position);

                text1.setText(s.get("nama_kelas").toString());
                text2.setTextColor(getColor(android.R.color.darker_gray));
                text2.setText(s.get("jurusan").toString());

                return view;
            }
        };

        listView.setAdapter(adapter);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                List<Map<String, Object>> filtered = new ArrayList<>();
                for (Map<String, Object> s : filteredKelasList) {
                    if (s.get("nama_kelas").toString().toLowerCase().contains(newText.toLowerCase()) ||
                            s.get("jurusan").toString().toLowerCase().contains(newText.toLowerCase())) {
                        filtered.add(s);
                    }
                }
                adapter.clear();
                adapter.addAll(filtered);
                adapter.notifyDataSetChanged();
                return true;
            }
        });

        AlertDialog dialog = builder.create();

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Map<String, Object> selected = adapter.getItem(position);
            if (selected != null) {
                et_kelas.setText(selected.get("nama_kelas").toString());
                selectedKelas = selected.get("nama_kelas").toString();
            }
            dialog.dismiss();
        });

        dialog.show();
    }


    @Override
    public void finish() {
        super.finish();
        Helpers.overridePendingTransition_Exit_v1(this);
    }
}