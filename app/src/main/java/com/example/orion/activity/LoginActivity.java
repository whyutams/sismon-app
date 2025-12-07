package com.example.orion.activity;

import static com.example.orion.utils.Helpers.hideKeyboard;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.orion.R;
import com.example.orion.utils.Helpers;
import com.example.orion.utils.PreferenceManager;
import com.example.orion.utils.api.ApiClient;
import com.example.orion.utils.api.ApiService;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.JsonObject;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();

        // Hapus translucent flag
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        // Disable scrim atau overlay
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

        // Set status bar warna sama dengan background
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.background));

        // Ikon status bar gelap (untuk background terang)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );
        }

        setContentView(R.layout.activity_login);

        Button btn_masuk = findViewById(R.id.btn_masuk);
        TextInputLayout til_username = findViewById(R.id.til_username), til_password = findViewById(R.id.til_password);
        TextInputEditText tiet_username = findViewById(R.id.et_username), tiet_password = findViewById(R.id.et_password);

        Helpers.LoadingHelper loading = new Helpers.LoadingHelper(this);

        btn_masuk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                hideKeyboard(LoginActivity.this);

                if (Helpers.validateRequired(til_username) && Helpers.validateRequired(til_password)) {
                    loading.show();

                    JsonObject request = new JsonObject();
                    request.addProperty("username", tiet_username.getText().toString().trim());
                    request.addProperty("password", tiet_password.getText().toString().trim());


                    ApiService apiService = ApiClient.getClient(LoginActivity.this).create(ApiService.class);
                    apiService.login(request).enqueue(new Callback<JsonObject>() {
                        @Override
                        public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                JsonObject json = response.body();

                                String status = json.has("status") ? json.get("status").getAsString() : "";
                                String message = json.has("message") ? json.get("message").getAsString() : "";

                                if (status.equals("success")) {
                                    String token = json.has("token") ? json.get("token").getAsString() : "";

                                    JsonObject data = json.has("user") ? json.getAsJsonObject("user") : null;

                                    PreferenceManager pref = new PreferenceManager(LoginActivity.this);
                                    pref.saveToken(token);

                                    if (data != null) {
                                        pref.saveUserData(data);
                                    }

                                    JsonObject user = pref.getUserData();
                                    if (user != null) {
                                        Log.d("PREF", "Nama: " + user.get("nama").getAsString());

                                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        Helpers.overridePendingTransition_Enter_v1(LoginActivity.this);
                                        finish();
                                    }

                                } else {
                                    Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                                }

                            } else {
                                Toast.makeText(LoginActivity.this, "Username atau Password salah", Toast.LENGTH_SHORT).show();
                            }

                            loading.dismiss();
                        }

                        @Override
                        public void onFailure(Call<JsonObject> call, Throwable t) {
                            Toast.makeText(LoginActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();

                            loading.dismiss();
                        }
                    });
                }
            }
        });
    }

}