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
import androidx.core.content.ContextCompat;
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

public class TambahAkunGuruActivity extends AppCompatActivity {

    private TextInputEditText et_nip, et_nama, et_nomorHp, et_email, et_alamat, et_username, et_password, et_jurusan, et_role;
    private TextInputLayout til_nip, til_nama, til_nomorHp, til_email, til_alamat, til_username, til_password, til_jurusan, til_role;
    private Button btn_simpan;
    private List<Map<String, @Nullable Object>> userList = new ArrayList<>();
    private List<Map<String, @Nullable Object>> jurusanList = new ArrayList<>();
    private String selectedJurusanId = null;
    private String selectedRole = "guru";
    private JsonObject user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tambah_akun_guru);

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

        til_nip = findViewById(R.id.til_nip);
        til_nama = findViewById(R.id.til_nama);
        til_nomorHp = findViewById(R.id.til_nomorHp);
        til_jurusan = findViewById(R.id.til_jurusan);
        til_email = findViewById(R.id.til_email);
        til_alamat = findViewById(R.id.til_alamat);
        til_username = findViewById(R.id.til_username);
        til_password = findViewById(R.id.til_password);
        til_role = findViewById(R.id.til_role);
        et_role = findViewById(R.id.et_role);
        et_nip = findViewById(R.id.et_nip);
        et_nama = findViewById(R.id.et_nama);
        et_nomorHp = findViewById(R.id.et_nomorHp);
        et_jurusan = findViewById(R.id.et_jurusan);
        et_email = findViewById(R.id.et_email);
        et_alamat = findViewById(R.id.et_alamat);
        et_username = findViewById(R.id.et_username);
        et_password = findViewById(R.id.et_password);
        btn_simpan = findViewById(R.id.btn_simpan);

        loadUsers();
        loadJurusan();

        et_role.setOnClickListener(v -> showRolePicker());
        if (til_role != null) {
            til_role.setEndIconOnClickListener(v -> showRolePicker());
        }

        et_jurusan.setOnClickListener(v -> showJurusanPicker());

        til_jurusan.setEnabled(true);

        Helpers.LoadingHelper loading = new Helpers.LoadingHelper(this);

        btn_simpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loading.show();

                Helpers.delay(250, new Runnable() {
                    @Override
                    public void run() {
                        if (Helpers.validateRequired(til_nip) && Helpers.validateRequired(til_nama) && Helpers.validateRequired(til_jurusan) && Helpers.validateRequired(til_username) && Helpers.validateRequired(til_password)) {
                            boolean is_nomorHp = false, is_email = false, is_username = false;

                            for (Map<String, @Nullable Object> _d : userList) {
                                String nomorHp = _d.get("nomor_hp").toString();
                                String email = _d.get("email").toString();
                                String username = _d.get("username").toString();

                                if (!et_nomorHp.getText().toString().isEmpty() && nomorHp.equalsIgnoreCase(et_nomorHp.getText().toString()))
                                    is_nomorHp = true;
                                if (!et_email.getText().toString().isEmpty() && email.equalsIgnoreCase(et_email.getText().toString()))
                                    is_email = true;
                                if (username.equalsIgnoreCase(et_username.getText().toString()))
                                    is_username = true;
                            }

                            if (is_nomorHp || is_email || is_username) {
                                if (is_nomorHp) {
                                    til_nomorHp.setErrorEnabled(true);
                                    til_nomorHp.setError("Nomor HP sudah digunakan.");
                                }
                                if (is_email) {
                                    til_email.setErrorEnabled(true);
                                    til_email.setError("Email sudah digunakan.");
                                }
                                if (is_username) {
                                    til_username.setErrorEnabled(true);
                                    til_username.setError("Username sudah digunakan.");
                                }

                                loading.dismiss();

                                return;
                            } else {
                                til_nomorHp.setErrorEnabled(false);
                                til_nomorHp.setError(null);
                                til_email.setErrorEnabled(false);
                                til_email.setError(null);
                                til_username.setErrorEnabled(false);
                                til_username.setError(null);
                            }

                            if (NetworkUtil.isConnected(TambahAkunGuruActivity.this)) {
                                RequestBody nip = RequestBody.create(MultipartBody.FORM, et_nip.getText().toString());
                                RequestBody nama = RequestBody.create(MultipartBody.FORM, et_nama.getText().toString());
                                RequestBody nomor_hp = RequestBody.create(MultipartBody.FORM, et_nomorHp.getText().toString());
                                RequestBody email = RequestBody.create(MultipartBody.FORM, et_email.getText().toString());
                                RequestBody alamat = RequestBody.create(MultipartBody.FORM, et_alamat.getText().toString());
                                RequestBody jurusan = RequestBody.create(MultipartBody.FORM, selectedJurusanId);
                                RequestBody username = RequestBody.create(MultipartBody.FORM, et_username.getText().toString());
                                RequestBody password = RequestBody.create(MultipartBody.FORM, "12345678");
                                RequestBody role = RequestBody.create(MultipartBody.FORM, selectedRole);

                                if (NetworkUtil.isConnected(TambahAkunGuruActivity.this)) {
                                    ApiService apiService = ApiClient.getClient(TambahAkunGuruActivity.this).create(ApiService.class);
                                    Call<JsonObject> call = apiService.userGuruAdd(nip, nama, nomor_hp, email, alamat, username, password, jurusan, role);

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
                                    Toast.makeText(TambahAkunGuruActivity.this, "Koneksi internet tidak stabil.", Toast.LENGTH_SHORT).show();

                                    loading.dismiss();
                                }

                            } else {
                                Toast.makeText(TambahAkunGuruActivity.this, "Koneksi Internet tidak stabil.", Toast.LENGTH_SHORT).show();

                                loading.dismiss();
                            }
                        } else {
                            loading.dismiss();
                        }

                        hideKeyboard(TambahAkunGuruActivity.this);
                    }
                });
            }
        });

    }

    private void loadUsers() {
        if (NetworkUtil.isConnected(this)) {
            userList.clear();
            ApiService apiService = ApiClient.getClient(TambahAkunGuruActivity.this).create(ApiService.class);
            apiService.info().enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        JsonObject json = response.body();

                        String status = json.has("status") ? json.get("status").getAsString() : "";

                        if (status.equals("success")) {
                            JsonObject main_data = json.has("data") ? json.get("data").getAsJsonObject() : null;
                            JsonObject data = main_data.has("data") ? main_data.get("data").getAsJsonObject() : null;
                            JsonArray users = data.has("user") ? data.get("user").getAsJsonArray() : null;

                            if (users != null || users.size() != 0) {
                                for (int i = 0; i < users.size(); i++) {
                                    JsonObject d = users.get(i).getAsJsonObject();

                                    Map<String, Object> hashMap = new HashMap<>();
                                    hashMap.put("nama", d.get("nama").getAsString());
                                    hashMap.put("nomor_hp", !d.get("nomor_hp").isJsonNull() && d.has("nomor_hp") ? d.get("nomor_hp").getAsString() : "");
                                    hashMap.put("email", !d.get("email").isJsonNull() && d.has("email") ? d.get("email").getAsString() : "");
                                    hashMap.put("username", d.get("username").getAsString());

                                    userList.add(hashMap);
                                }
                            }
                        } else {
                            Toast.makeText(TambahAkunGuruActivity.this, "Tidak dapat memuat data. Coba lagi nanti.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(TambahAkunGuruActivity.this, "Tidak dapat memuat data. Coba lagi nanti.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Toast.makeText(TambahAkunGuruActivity.this, "Terjadi kesalahan: " + t.getMessage(), Toast.LENGTH_LONG).show();
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

    private void loadJurusan() {
        if (NetworkUtil.isConnected(this)) {
            jurusanList.clear();
            ApiService apiService = ApiClient.getClient(TambahAkunGuruActivity.this).create(ApiService.class);
            apiService.info().enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        JsonObject json = response.body();

                        String status = json.has("status") ? json.get("status").getAsString() : "";

                        if (status.equals("success")) {
                            JsonObject main_data = json.has("data") ? json.get("data").getAsJsonObject() : null;
                            JsonObject data = main_data.has("data") ? main_data.get("data").getAsJsonObject() : null;
                            JsonArray jurusan = data.has("jurusan") ? data.get("jurusan").getAsJsonArray() : null;

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
                        } else {
                            Toast.makeText(TambahAkunGuruActivity.this, "Tidak dapat memuat data. Coba lagi nanti.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(TambahAkunGuruActivity.this, "Tidak dapat memuat data. Coba lagi nanti.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Toast.makeText(TambahAkunGuruActivity.this, "Terjadi kesalahan: " + t.getMessage(), Toast.LENGTH_LONG).show();
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

    private void showRolePicker() {
        final String[] roleNames = {"Guru biasa (Default)", "Kesiswaan", "BK", "Piket"};
        final String[] roleValues = {"guru", "kesiswaan", "bimbingan_konseling", "guru_piket"};

        int checkedItem = 0;
        for (int i = 0; i < roleValues.length; i++) {
            if (roleValues[i].equals(selectedRole)) {
                checkedItem = i;
                break;
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pilih Role Guru");
        builder.setSingleChoiceItems(roleNames, checkedItem, (dialog, which) -> {
            selectedRole = roleValues[which];
            if (which == 0) {
                et_role.setText("Guru biasa");
            } else {
                et_role.setText(roleNames[which]);
            }
            dialog.dismiss();
        });
        builder.setNegativeButton("Batal", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
        if (dialog.getButton(DialogInterface.BUTTON_NEGATIVE) != null) {
            dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(this, R.color.primary));
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