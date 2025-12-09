package com.example.orion.activity;

import static com.example.orion.utils.Helpers.hideKeyboard;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TambahPengaturanKelasActivity extends AppCompatActivity {

    private TextInputEditText et_jurusan;
    private AutoCompleteTextView act_kelas, act_jumlahKelas;
    private TextInputLayout til_jurusan, til_kelas, til_jumlahKelas;
    private Button btn_simpan;
    private String selectedJurusanId = null;
    private int selectedKelasValue = 0;
    private int selectedJumlahKelasValue = 0;
    private List<Map<String, @Nullable Object>> jurusanList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tambah_pengaturan_kelas);

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
        JsonObject user = pref.getUserData();

        et_jurusan = findViewById(R.id.et_jurusan);
        act_kelas = findViewById(R.id.et_kelas);
        act_jumlahKelas = findViewById(R.id.et_jumlahkelas);
        til_jurusan = findViewById(R.id.til_jurusan);
        til_kelas = findViewById(R.id.til_kelas);
        til_jumlahKelas = findViewById(R.id.til_jumlahkelas);
        btn_simpan = findViewById(R.id.btn_simpan);

        loadJurusan();
        setupStaticDropdowns();

        et_jurusan.setOnClickListener(v -> showJurusanPicker());

        til_jurusan.setEnabled(true);
        til_kelas.setEnabled(true);
        til_jumlahKelas.setEnabled(true);

        Helpers.LoadingHelper loading = new Helpers.LoadingHelper(this);

        btn_simpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loading.show();

                Helpers.delay(250, new Runnable() {
                    @Override
                    public void run() {
                        if (Helpers.validateRequired(til_jurusan) && Helpers.validateRequired(til_kelas) && Helpers.validateRequired(til_jumlahKelas)) {
                            if (NetworkUtil.isConnected(TambahPengaturanKelasActivity.this)) {

                                if (selectedJurusanId == null) {
                                    Toast.makeText(TambahPengaturanKelasActivity.this, "Jurusan belum dipilih.", Toast.LENGTH_SHORT).show();
                                    loading.dismiss();
                                    return;
                                }

                                String kelasString = String.valueOf(selectedKelasValue);
                                String jumlahKelasString = String.valueOf(selectedJumlahKelasValue);

                                RequestBody kelas = RequestBody.create(MultipartBody.FORM, kelasString);
                                RequestBody jumlah_kelas = RequestBody.create(MultipartBody.FORM, jumlahKelasString);
                                RequestBody jurusan_id = RequestBody.create(MultipartBody.FORM, selectedJurusanId);

                                if (NetworkUtil.isConnected(TambahPengaturanKelasActivity.this)) {
                                    ApiService apiService = ApiClient.getClient(TambahPengaturanKelasActivity.this).create(ApiService.class);
                                    Call<JsonObject> call = apiService.kelasAdd(kelas, jumlah_kelas, jurusan_id);

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
                                            Toast.makeText(getApplicationContext(), "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();

                                            loading.dismiss();
                                        }
                                    });

                                } else {
                                    Toast.makeText(TambahPengaturanKelasActivity.this, "Koneksi internet tidak stabil.", Toast.LENGTH_SHORT).show();

                                    loading.dismiss();
                                }

                            } else {
                                Toast.makeText(TambahPengaturanKelasActivity.this, "Koneksi Internet tidak stabil.", Toast.LENGTH_SHORT).show();

                                loading.dismiss();
                            }
                        } else {
                            loading.dismiss();
                        }

                        hideKeyboard(TambahPengaturanKelasActivity.this);
                    }
                });
            }
        });

    }

    private void setupStaticDropdowns() {
        String[] pilihanKelas = {"X (Sepuluh)", "XI (Sebelas)", "XII (Dua belas)"};
        ArrayAdapter<String> adapterKelas = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                pilihanKelas
        );
        act_kelas.setAdapter(adapterKelas);

        act_kelas.setOnItemClickListener((parent, view, position, id) -> {
            String selectedText = (String) parent.getItemAtPosition(position);

            if (selectedText.startsWith("X")) {
                selectedKelasValue = 10;
            } else if (selectedText.startsWith("XI")) {
                selectedKelasValue = 11;
            } else if (selectedText.startsWith("XII")) {
                selectedKelasValue = 12;
            } else {
                selectedKelasValue = 0;
            }
        });

        String[] pilihanJumlah = {"1", "2", "3", "4"};
        ArrayAdapter<String> adapterJumlah = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                pilihanJumlah
        );
        act_jumlahKelas.setAdapter(adapterJumlah);

        act_jumlahKelas.setOnItemClickListener((parent, view, position, id) -> {
            try {
                selectedJumlahKelasValue = Integer.parseInt((String) parent.getItemAtPosition(position));
            } catch (NumberFormatException e) {
                selectedJumlahKelasValue = 0;
            }
        });
    }

    private void loadJurusan() {
        if (NetworkUtil.isConnected(this)) {
            jurusanList.clear();
            ApiService apiService = ApiClient.getClient(TambahPengaturanKelasActivity.this).create(ApiService.class);
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
                            Toast.makeText(TambahPengaturanKelasActivity.this, "Tidak dapat memuat data. Coba lagi nanti.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(TambahPengaturanKelasActivity.this, "Tidak dapat memuat data. Coba lagi nanti.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Toast.makeText(TambahPengaturanKelasActivity.this, "Terjadi kesalahan: " + t.getMessage(), Toast.LENGTH_LONG).show();
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