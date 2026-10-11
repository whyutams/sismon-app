package com.example.orion.activity;

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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.orion.R;
import com.example.orion.utils.Helpers;
import com.example.orion.utils.CropImageDialog;
import com.example.orion.utils.ImageCompressHelper;
import java.io.File;
import com.example.orion.utils.NetworkUtil;
import com.example.orion.utils.PreferenceManager;
import com.example.orion.utils.api.ApiClient;
import com.example.orion.utils.api.ApiService;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity {

    private Uri selectedImageUri = null;
    private String imagePath = null;
    private String currentProfileUrl = null;
    private ShapeableImageView siv_profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

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

        for (int i = 0; i < toolbar.getMenu().size(); i++) {
            Drawable icon = toolbar.getMenu().getItem(i).getIcon();
            if (icon != null) {
                icon.setTint(ContextCompat.getColor(this, R.color.white));
            }
        }

        // Views
        TextInputLayout til_nama = findViewById(R.id.til_nama),
                til_email = findViewById(R.id.til_email),
                til_nomor_hp = findViewById(R.id.til_nomorHp),
                til_alamat = findViewById(R.id.til_alamat);

        EditText et_nama = findViewById(R.id.et_nama),
                et_email = findViewById(R.id.et_email),
                et_nomor_hp = findViewById(R.id.et_nomorHp),
                et_alamat = findViewById(R.id.et_alamat);

        siv_profile = findViewById(R.id.imgProfile);
        Button btn_simpan = findViewById(R.id.btn_simpan);
        LinearLayout ll_progress = findViewById(R.id.ll_progress);
        ScrollView sv_main = findViewById(R.id.sv_main);

        PreferenceManager pref = new PreferenceManager(this);
        JsonObject user = pref.getUserData();

        if (user != null) {
            if (user.has("foto_profil") && !user.get("foto_profil").isJsonNull()) {
                currentProfileUrl = user.get("foto_profil").getAsString();
                if (!currentProfileUrl.startsWith("http")) currentProfileUrl = ApiClient.base_url + currentProfileUrl;
                siv_profile.setImageTintList(null);
                Glide.with(this)
                        .load(currentProfileUrl)
                        .placeholder(R.drawable.ic_account_circle_24)
                        .error(R.drawable.ic_account_circle_24)
                        .into(siv_profile);
            }
            et_nama.setText(user.has("nama") ? user.get("nama").getAsString() : "");
            et_email.setText(user.has("email") ? user.get("email").getAsString() : "");
            et_nomor_hp.setText(user.has("nomor_hp") ? user.get("nomor_hp").getAsString() : "");
            et_alamat.setText(user.has("alamat") ? user.get("alamat").getAsString() : "");
        }

        View ll_edit = findViewById(R.id.ll_edit);
        if (ll_edit != null) {
            ll_edit.setOnClickListener(v -> checkPermissionAndOpenGallery());
        }

        siv_profile.setOnClickListener(v -> {
            if (imagePath != null) {
                Helpers.showImagePreview(EditProfileActivity.this, new File(imagePath));
            } else if (currentProfileUrl != null && !currentProfileUrl.isEmpty()) {
                Helpers.showImagePreview(EditProfileActivity.this, currentProfileUrl);
            } else {
                checkPermissionAndOpenGallery();
            }
        });

        btn_simpan.setOnClickListener(v -> {
            if (Helpers.validateRequired(til_nama)) {
                RequestBody _method = RequestBody.create(MultipartBody.FORM, "put");
                RequestBody nama = RequestBody.create(MultipartBody.FORM, et_nama.getText().toString());
                RequestBody email = RequestBody.create(MultipartBody.FORM, et_email.getText().toString());
                RequestBody nomor_hp = RequestBody.create(MultipartBody.FORM, et_nomor_hp.getText().toString());
                RequestBody alamat = RequestBody.create(MultipartBody.FORM, et_alamat.getText().toString());

                MultipartBody.Part fotoProfilPart = null;
                if (imagePath != null) {
                    File file = new File(imagePath);
                    RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
                    fotoProfilPart = MultipartBody.Part.createFormData("foto_profil", file.getName(), requestFile);
                }

                Helpers.LoadingHelper loading = new Helpers.LoadingHelper(this);
                loading.show();

                if (NetworkUtil.isConnected(EditProfileActivity.this)) {
                    ApiService apiService = ApiClient.getClient(EditProfileActivity.this).create(ApiService.class);
                    Call<JsonObject> call = apiService.editProfile(_method, nama, email, nomor_hp, alamat, fotoProfilPart);

                    call.enqueue(new Callback<JsonObject>() {
                        @Override
                        public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                PreferenceManager pref = new PreferenceManager(EditProfileActivity.this);
                                JsonObject updatedUser = response.body().getAsJsonObject("user");
                                if (updatedUser != null) {
                                    pref.saveUserData(updatedUser);
                                }

                                setResult(RESULT_OK);
                                finish();
                                Toast.makeText(getApplicationContext(), "Profil berhasil diperbarui!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplicationContext(), "Gagal update profil!", Toast.LENGTH_SHORT).show();
                            }

                            loading.dismiss();
                        }

                        @Override
                        public void onFailure(Call<JsonObject> call, Throwable t) {
                            Toast.makeText(getApplicationContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();

                            loading.dismiss();
                        }
                    });

                } else {
                    Toast.makeText(this, "Koneksi internet tidak stabil.", Toast.LENGTH_SHORT).show();

                    loading.dismiss();
                }
            }
        });

        Helpers.delay(1000, () -> {
            ll_progress.setVisibility(View.GONE);
            sv_main.setVisibility(View.VISIBLE);
        });
    }

    private ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    openGallery();
                } else {
                    Toast.makeText(this, "Izin penyimpanan ditolak, tidak bisa memilih gambar.", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Izin diberikan. Silakan pilih foto.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Izin penyimpanan ditolak!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void checkPermissionAndOpenGallery() {
        String permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission = android.Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            permission = android.Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            openGallery();
        } else {
            requestPermissionLauncher.launch(permission);
        }
    }

    private ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        // Launch 1:1 square crop modal with draggable/pannable view
                        new CropImageDialog(EditProfileActivity.this, selectedImageUri, (croppedFile, croppedBitmap) -> {
                            siv_profile.setBackground(null);
                            siv_profile.setImageTintList(null);
                            siv_profile.setImageBitmap(croppedBitmap);
                            imagePath = croppedFile.getAbsolutePath();
                        }).show();
                    }
                }
            }
    );

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        pickImageLauncher.launch(intent);
    }

    @Override
    public void finish() {
        super.finish();
        Helpers.overridePendingTransition_Exit_v1(this);
    }
}
