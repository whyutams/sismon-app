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
import com.example.orion.utils.ImageCompressHelper;
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

public class TambahKeterlambatanSiswaActivity extends AppCompatActivity {

    private TextInputEditText et_tanggal, et_keterangan, et_siswa;
    private TextInputLayout til_tanggal, til_keterangan, til_siswa;
    private Button btn_simpan, btn_pilihGambar;
    private ImageView iv_gambar;
    private String selectedSiswaId = null, createdAt = null, imagePath;
    private List<Map<String, @Nullable Object>> siswaList = new ArrayList<>();
    private final Calendar calendar = Calendar.getInstance();
    private static final int REQUEST_CAMERA = 100;
    private static final int REQUEST_GALLERY = 101;
    private Uri imageUri = null;
    private ActivityResultLauncher<Intent> activityResultLauncher;
    private ActivityResultLauncher<String> requestCameraPermissionLauncher;
    private ActivityResultLauncher<String> requestStoragePermissionLauncher;
    private ActivityResultLauncher<Intent> pickImageLauncher;
    private ActivityResultLauncher<Intent> takePhotoLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tambah_keterlambatan_siswa);

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

        et_siswa = findViewById(R.id.et_siswa);
        et_keterangan = findViewById(R.id.et_keterangan);
        et_tanggal = findViewById(R.id.et_tanggal);
        iv_gambar = findViewById(R.id.iv_gambar);
        til_tanggal = findViewById(R.id.til_tanggal);
        til_keterangan = findViewById(R.id.til_keterangan);
        til_siswa = findViewById(R.id.til_siswa);
        btn_simpan = findViewById(R.id.btn_simpan);
        btn_pilihGambar = findViewById(R.id.btn_pilih);

        et_tanggal.setOnClickListener(v -> showMaterialDatePicker());
        loadSiswa();
        et_siswa.setOnClickListener(v -> showSiswaPicker());

        til_tanggal.setEnabled(true);
        til_siswa.setEnabled(true);

        Helpers.LoadingHelper loading = new Helpers.LoadingHelper(this);

        btn_simpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loading.show();

                Helpers.delay(250, new Runnable() {
                    @Override
                    public void run() {
                        if (imageUri == null) {
                            Toast.makeText(TambahKeterlambatanSiswaActivity.this, "Gambar belum dipilih.", Toast.LENGTH_SHORT).show();
                            loading.dismiss();
                            return;
                        }

                        if (Helpers.validateRequired(til_siswa) && Helpers.validateRequired(til_tanggal) && Helpers.validateRequired(til_keterangan)) {
                            if (NetworkUtil.isConnected(TambahKeterlambatanSiswaActivity.this)) {
                                RequestBody keterangan = RequestBody.create(MultipartBody.FORM, et_keterangan.getText().toString());
                                RequestBody siswa_id = RequestBody.create(MultipartBody.FORM, selectedSiswaId);
                                RequestBody date = RequestBody.create(MultipartBody.FORM, createdAt);

                                MultipartBody.Part gambar = null;
                                if (imagePath != null) {
                                    File fileToUpload = null;
                                    if (imageUri != null) {
                                        fileToUpload = ImageCompressHelper.compressImage(TambahKeterlambatanSiswaActivity.this, imageUri);
                                    }
                                    if (fileToUpload == null) {
                                        fileToUpload = new File(imagePath);
                                    }
                                    RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), fileToUpload);
                                    gambar = MultipartBody.Part.createFormData("gambar", fileToUpload.getName(), requestFile);
                                }

                                if (NetworkUtil.isConnected(TambahKeterlambatanSiswaActivity.this)) {
                                    ApiService apiService = ApiClient.getClient(TambahKeterlambatanSiswaActivity.this).create(ApiService.class);
                                    Call<JsonObject> call = apiService.keterlambatan(keterangan, siswa_id, gambar, date);

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
                                    Toast.makeText(TambahKeterlambatanSiswaActivity.this, "Koneksi internet tidak stabil.", Toast.LENGTH_SHORT).show();

                                    loading.dismiss();
                                }

                            } else {
                                Toast.makeText(TambahKeterlambatanSiswaActivity.this, "Koneksi Internet tidak stabil.", Toast.LENGTH_SHORT).show();

                                loading.dismiss();
                            }
                        } else {
                            loading.dismiss();
                        }

                        hideKeyboard(TambahKeterlambatanSiswaActivity.this);
                    }
                });
            }
        });

        initActivityResultLaunchers();
        btn_pilihGambar.setOnClickListener(v -> showImagePickerDialog());
    }

    private void showMaterialDatePicker() {
        MaterialDatePicker<Long> datePicker =
                MaterialDatePicker.Builder.datePicker()
                        .setTitleText("Pilih Tanggal")
                        .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                        .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            Calendar utcCalendar = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"));
            utcCalendar.setTimeInMillis(selection);

            calendar.set(Calendar.YEAR, utcCalendar.get(Calendar.YEAR));
            calendar.set(Calendar.MONTH, utcCalendar.get(Calendar.MONTH));
            calendar.set(Calendar.DAY_OF_MONTH, utcCalendar.get(Calendar.DAY_OF_MONTH));

            showMaterialTimePicker();
        });

        datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
    }

    private void showMaterialTimePicker() {
        MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(calendar.get(Calendar.HOUR_OF_DAY))
                .setMinute(calendar.get(Calendar.MINUTE))
                .setTitleText("Pilih Waktu")
                .build();

        timePicker.addOnPositiveButtonClickListener(v -> {
            calendar.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
            calendar.set(Calendar.MINUTE, timePicker.getMinute());
            calendar.set(Calendar.SECOND, 0);

            SimpleDateFormat displayFormat =
                    new SimpleDateFormat("EEEE, dd MMM yyyy (HH:mm)", new Locale("id", "ID"));
            et_tanggal.setText(displayFormat.format(calendar.getTime()));

            SimpleDateFormat mysqlFormat =
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            createdAt = mysqlFormat.format(calendar.getTime());
        });

        timePicker.show(getSupportFragmentManager(), "TIME_PICKER");
    }

    private void loadSiswa() {
        if (NetworkUtil.isConnected(this)) {
            siswaList.clear();
            ApiService apiService = ApiClient.getClient(TambahKeterlambatanSiswaActivity.this).create(ApiService.class);
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

                                    Map<String, Object> siswa = new HashMap<>();
                                    siswa.put("id", d.get("id").getAsString());
                                    siswa.put("nis", d.get("nis").isJsonNull() ? "-" : d.get("nis").getAsString());
                                    siswa.put("nama", d.get("nama").getAsString());
                                    siswa.put("nomor_hp", (d.get("nomor_hp").isJsonNull() ? "-" : d.get("nomor_hp").getAsString()));
                                    siswa.put("jurusan", d.get("jurusan").getAsJsonObject());
                                    siswa.put("orangtua", (!d.get("orangtua").isJsonNull() ? d.get("orangtua").getAsJsonObject() : null));
                                    siswa.put("kelas", d.get("kelas").getAsString());

                                    siswaList.add(siswa);
                                }
                            }
                        } else {
                            Toast.makeText(TambahKeterlambatanSiswaActivity.this, "Tidak dapat memuat data. Coba lagi nanti.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(TambahKeterlambatanSiswaActivity.this, "Tidak dapat memuat data. Coba lagi nanti.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Toast.makeText(TambahKeterlambatanSiswaActivity.this, "Terjadi kesalahan: " + t.getMessage(), Toast.LENGTH_LONG).show();
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

    private void showSiswaPicker() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pilih Siswa");

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_picker_1, null);
        builder.setView(dialogView);

        SearchView searchView = dialogView.findViewById(R.id.searchView);
        ListView listView = dialogView.findViewById(R.id.listView);

        ArrayAdapter<Map<String, Object>> adapter = new ArrayAdapter<Map<String, Object>>(this,
                android.R.layout.simple_list_item_2, android.R.id.text1, new ArrayList<>(siswaList)) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = view.findViewById(android.R.id.text1);
                TextView text2 = view.findViewById(android.R.id.text2);

                Map<String, Object> s = getItem(position);
                JsonObject jurusan = null;
                if (s.get("jurusan") != null) {
                    jurusan = ((JsonObject) s.get("jurusan")).getAsJsonObject();
                }

                text1.setText(s.get("nama").toString());
                text2.setTextColor(getColor(android.R.color.darker_gray));
                String nama_kelas = s.get("kelas").toString();
                text2.setText("NIS: " + (s.get("nis").toString().isEmpty() ? "-" : s.get("nis").toString()) + " | " + (s.get("jurusan") != null ? (jurusan.has("alias") ? jurusan.get("alias").getAsString() : jurusan.get("nama").getAsString()) : "-") + " | " + (nama_kelas.isEmpty() ? "-" : nama_kelas));

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
                for (Map<String, Object> s : siswaList) {
                    if (s.get("nama").toString().toLowerCase().contains(newText.toLowerCase()) ||
                            s.get("nis").toString().toLowerCase().contains(newText.toLowerCase())) {
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
                et_siswa.setText(selected.get("nama").toString() + (selected.get("nis") != null ? " (" + selected.get("nis").toString() + ")" : ""));
                selectedSiswaId = selected.get("id").toString();
            }
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showImagePickerDialog() {
        String[] options = {"Kamera", "Galeri"};

        new AlertDialog.Builder(this)
                .setTitle("Pilih Gambar Dari")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        // Kamera
                        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
                        } else {
                            openCamera();
                        }
                    } else {
                        // Galeri
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            if (checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                                requestStoragePermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
                            } else {
                                openGallery();
                            }
                        } else {
                            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                requestStoragePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                            } else {
                                openGallery();
                            }
                        }
                    }
                })
                .show();
    }


    private void openCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Keterlambatan_" + System.currentTimeMillis());
        values.put(MediaStore.Images.Media.DESCRIPTION, "Foto keterlambatan siswa");

        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        takePhotoLauncher.launch(cameraIntent);
    }


    private void openGallery() {
        Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");
        pickImageLauncher.launch(pickIntent);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CAMERA && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else if (requestCode == REQUEST_CAMERA) {
            Toast.makeText(this, "Izin kamera diperlukan", Toast.LENGTH_SHORT).show();
        }
    }

    private void setImageView(Uri uri) {
        if (uri == null) return;
        iv_gambar.setBackground(null);
        iv_gambar.setImageTintList(null);
        iv_gambar.setColorFilter(null);
        iv_gambar.setImageURI(uri);

        imagePath = Helpers.getRealPathFromURI(this, uri);

        if (imagePath == null) {
            try {
                File cacheFile = Helpers.copyUriToCache(this, uri);
                imagePath = cacheFile.getAbsolutePath();
            } catch (Exception e) {
                Toast.makeText(this, "Gagal membaca file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                imagePath = null;
            }
        }

        if (imagePath == null) {
            Toast.makeText(this, "Tidak dapat memuat gambar.", Toast.LENGTH_SHORT).show();
        }
    }


    private void initActivityResultLaunchers() {
        requestCameraPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) openCamera();
                    else Toast.makeText(this, "Izin kamera ditolak", Toast.LENGTH_SHORT).show();
                }
        );

        requestStoragePermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) openGallery();
                    else
                        Toast.makeText(this, "Izin akses galeri ditolak", Toast.LENGTH_SHORT).show();
                }
        );

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        imageUri = result.getData().getData();
                        setImageView(imageUri);
                    }
                }
        );

        takePhotoLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && imageUri != null) {
                        setImageView(imageUri);
                    }
                }
        );
    }

    @Override
    public void finish() {
        super.finish();
        Helpers.overridePendingTransition_Exit_v1(this);
    }
}