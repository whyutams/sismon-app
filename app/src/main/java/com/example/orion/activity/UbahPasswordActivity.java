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

public class UbahPasswordActivity extends AppCompatActivity {

    private TextInputEditText et_passwordLama, et_passwordBaru, et_konfirmasiPasswordBaru;
    private TextInputLayout til_passwordLama, til_passwordBaru, til_konfirmasiPasswordBaru;
    private Button btn_simpan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ubah_password);

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

        et_passwordLama = findViewById(R.id.et_password_skrg);
        et_passwordBaru = findViewById(R.id.et_password_baru);
        et_konfirmasiPasswordBaru = findViewById(R.id.et_password_baru1);
        til_passwordLama = findViewById(R.id.til_password_skrg);
        til_passwordBaru = findViewById(R.id.til_password_baru);
        til_konfirmasiPasswordBaru = findViewById(R.id.til_password_baru1);
        btn_simpan = findViewById(R.id.btn_simpan);

        Helpers.LoadingHelper loading = new Helpers.LoadingHelper(this);

        btn_simpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loading.show();

                Helpers.delay(250, new Runnable() {
                    @Override
                    public void run() {
                        if (Helpers.validateRequired(til_passwordLama) && Helpers.validateRequired(til_passwordBaru) && Helpers.validateRequired(til_konfirmasiPasswordBaru)) {
                            if(!et_passwordBaru.getText().toString().equals(et_konfirmasiPasswordBaru.getText().toString())) {
                                til_passwordBaru.setErrorEnabled(true);
                                til_passwordBaru.setError("Password baru dan konfirmasi password baru harus sama.");
                                til_konfirmasiPasswordBaru.setErrorEnabled(true);
                                til_konfirmasiPasswordBaru.setError("Password baru dan konfirmasi password baru harus sama.");

                                loading.dismiss();
                                return;
                            } else {
                                til_passwordBaru.setErrorEnabled(false);
                                til_konfirmasiPasswordBaru.setErrorEnabled(false);
                            }

                            if (NetworkUtil.isConnected(UbahPasswordActivity.this)) {
                                RequestBody password_lama = RequestBody.create(MultipartBody.FORM, et_passwordLama.getText().toString());
                                RequestBody password_baru = RequestBody.create(MultipartBody.FORM, et_passwordBaru.getText().toString());
                                RequestBody konfirmasi_password_baru = RequestBody.create(MultipartBody.FORM, et_konfirmasiPasswordBaru.getText().toString());

                                if (NetworkUtil.isConnected(UbahPasswordActivity.this)) {
                                    ApiService apiService = ApiClient.getClient(UbahPasswordActivity.this).create(ApiService.class);
                                    Call<JsonObject> call = apiService.ubahPassword(password_lama, password_baru, konfirmasi_password_baru);

                                    call.enqueue(new Callback<JsonObject>() {
                                        @Override
                                        public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                                            if (response.isSuccessful() && response.body() != null) {
                                                setResult(RESULT_OK);
                                                Toast.makeText(getApplicationContext(), "Password berhasil diubah!", Toast.LENGTH_SHORT).show();
                                                finish();
                                                loading.dismiss();
                                            } else {
                                                Toast.makeText(getApplicationContext(), "Gagal mengubah password!", Toast.LENGTH_SHORT).show();
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
                                    Toast.makeText(UbahPasswordActivity.this, "Koneksi internet tidak stabil.", Toast.LENGTH_SHORT).show();

                                    loading.dismiss();
                                }

                            } else {
                                Toast.makeText(UbahPasswordActivity.this, "Koneksi Internet tidak stabil.", Toast.LENGTH_SHORT).show();

                                loading.dismiss();
                            }
                        } else {
                            loading.dismiss();
                        }

                        hideKeyboard(UbahPasswordActivity.this);
                    }
                });
            }
        });

    }

    @Override
    public void finish() {
        super.finish();
        Helpers.overridePendingTransition_Exit_v1(this);
    }
}