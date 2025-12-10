package com.example.orion.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;

import java.util.ArrayList;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class JurusanTerdaftarActivity extends AppCompatActivity {

    SwipeRefreshLayout swipeRefreshLayout;
    LinearLayout ll_progress, ll_countainer_list, ll_notfound;
    ArrayList<HashMap<String, String>> dataList = new ArrayList<>();
    private ActivityResultLauncher<Intent> addDataLauncher;
    private FloatingActionButton fab_tambah;
    private boolean fab_tambah_allowed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jurusan_terdaftar);

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

        if (user != null && user.has("role")) {
            String role = user.get("role").getAsString();

            if (role.equals("kepsek") || role.equals("superadmin")) {
                fab_tambah = findViewById(R.id.fab_add);
                fab_tambah_allowed = true;

                fab_tambah.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(JurusanTerdaftarActivity.this, TambahJurusanTerdaftarActivity.class);
                        addDataLauncher.launch(intent);
                        Helpers.overridePendingTransition_Enter_v1(JurusanTerdaftarActivity.this);
                    }
                });
            }
        }

        ll_countainer_list = findViewById(R.id.ll_countainer_list);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout1);
        ll_progress = findViewById(R.id.ll_progress);
        ll_notfound = findViewById(R.id.ll_notfound);

        swipeRefreshLayout.setColorSchemeColors(getColor(R.color.primary));
        loadData();

        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefreshLayout.setRefreshing(true);

            new Handler().postDelayed(() -> {
                loadData();
                swipeRefreshLayout.setRefreshing(false);
            }, 1000);
        });

        addDataLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        ll_progress.setVisibility(View.VISIBLE);
                        swipeRefreshLayout.setVisibility(View.GONE);

                        Helpers.delay(1000, new Runnable() {
                            @Override
                            public void run() {
                                loadData();
                            }
                        });
                    }
                }
        );
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.nav_search) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_top_nav_list_activity, menu);

        for (int i = 0; i < menu.size(); i++) {
            Drawable icon = menu.getItem(i).getIcon();
            if (icon != null) {
                icon.setTint(ContextCompat.getColor(this, R.color.white));
            }
        }

        MenuItem searchItem = menu.findItem(R.id.nav_search);
        MenuItem articleItem = menu.findItem(R.id.nav_export);
        androidx.appcompat.widget.SearchView searchView =
                (androidx.appcompat.widget.SearchView) searchItem.getActionView();

        searchView.setQueryHint("Cari ...");

        int searchTextId = androidx.appcompat.R.id.search_src_text;
        TextView searchText = searchView.findViewById(searchTextId);
        if (searchText != null) {
            searchText.setTextColor(Color.WHITE);
            searchText.setHintTextColor(Color.parseColor("#80FFFFFF"));
        }

        // Set icon color
        ImageView searchButton = searchView.findViewById(androidx.appcompat.R.id.search_button);
        if(searchButton != null) {
            searchButton.setColorFilter(new PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP));
        }

        ImageView searchIcon = searchView.findViewById(androidx.appcompat.R.id.search_mag_icon);
        if (searchIcon != null) {
            searchIcon.setColorFilter(new PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN));
        }

        ImageView closeButton = searchView.findViewById(androidx.appcompat.R.id.search_close_btn);
        if (closeButton != null) {
            closeButton.setColorFilter(new PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN));
        }

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            Drawable navIcon = toolbar.getNavigationIcon();
            if (navIcon != null) {
                navIcon.setColorFilter(new PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN));
            }
        }

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fab_tambah_allowed) Helpers.animateZoomOut_v1(fab_tambah);

                if (articleItem != null) {
                    articleItem.setVisible(false);
                }
            }
        });

        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterData(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    reloadAllItems();
                } else {
                    filterData(newText);
                }
                return true;
            }
        });

        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                if (fab_tambah_allowed) Helpers.animateZoomIn_v1(fab_tambah);
                return true; // allow expand
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                if (fab_tambah_allowed) Helpers.animateZoomIn_v1(fab_tambah);
                reloadAllItems(); // tampilkan semua
                if (articleItem != null) {
                    articleItem.setVisible(true);
                }

                reloadAllItems();
                return true;
            }
        });

        return true;
    }


    private void loadData() {
        if (NetworkUtil.isConnected(this)) {
            ApiService apiService = ApiClient.getClient(JurusanTerdaftarActivity.this).create(ApiService.class);
            apiService.info().enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        JsonObject json = response.body();

                        String status = json.has("status") ? json.get("status").getAsString() : "";

                        if (status.equals("success")) {
                            dataList.clear();
                            ll_countainer_list.removeAllViews();

                            JsonObject main_data = json.has("data") ? json.get("data").getAsJsonObject() : null;
                            JsonObject data = main_data.has("data") ? main_data.get("data").getAsJsonObject() : null;
                            JsonArray jurusan = data.has("jurusan") ? data.get("jurusan").getAsJsonArray() : null;

                            if (jurusan.size() != 0) {
                                for (int i = 0; i < jurusan.size(); i++) {
                                    JsonObject d = jurusan.get(i).getAsJsonObject();
                                    addItem(d, i, jurusan.size());
                                }

                                ll_progress.setVisibility(View.GONE);
                                swipeRefreshLayout.setVisibility(View.VISIBLE);
                            } else {
                                Helpers.delay(1000, new Runnable() {
                                    @Override
                                    public void run() {
                                        if (isFinishing() || isDestroyed()) return;

                                        ll_progress.setVisibility(View.GONE);
                                        ll_notfound.setVisibility(View.VISIBLE);
                                    }
                                });
                            }

                        } else {
                            Toast.makeText(JurusanTerdaftarActivity.this, "Tidak dapat memuat data. Coba lagi nanti.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(JurusanTerdaftarActivity.this, "Tidak dapat memuat data. Coba lagi nanti.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Toast.makeText(JurusanTerdaftarActivity.this, "Terjadi kesalahan: " + t.getMessage(), Toast.LENGTH_LONG).show();
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

    private void addItem(JsonObject data, int index, int total) {
        if (isFinishing() || isDestroyed()) return;

        View itemView = LayoutInflater.from(this).inflate(R.layout.item_jurusan_terdafar, ll_countainer_list, false);

        TextView tvNamaJurusan = itemView.findViewById(R.id.jurusanText);
        TextView tvAlias = itemView.findViewById(R.id.aliasText);
        TextView tvDescText = itemView.findViewById(R.id.descText);
        TextView tvIndex = itemView.findViewById(R.id.indexText);

        String nama = data.get("nama").getAsString(), alias = data.get("alias").getAsString(), desc = data.get("deskripsi").getAsString();

        tvNamaJurusan.setText(nama);
        tvAlias.setText(alias);
        tvDescText.setText(desc);
        tvIndex.setText((total - index) + "");

        HashMap<String, String> map = new HashMap<>();
        map.put("index", (total - index) + "");
        map.put("nama", nama);
        map.put("alias", alias);
        map.put("desc", desc);
        map.put("id", data.get("id").getAsString());
        dataList.add(map);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent intent = new Intent(JurusanTerdaftarActivity.this, DetailJurusanTerdaftarActivity.class);
//                intent.putExtra("id", data.get("id").getAsString());
//                intent.putExtra("index", tvIndex.getText());
//                startActivity(intent);
//                Helpers.overridePendingTransition_Enter_v1(JurusanTerdaftarActivity.this);
            }
        });

        ll_countainer_list.addView(itemView, ll_countainer_list.getChildCount() - (ll_countainer_list.getChildCount() + 1));

        if (total == index + 1) {
            if (fab_tambah_allowed) Helpers.animateZoomIn_v1(fab_tambah);
            Helpers.delay(1000, new Runnable() {
                @Override
                public void run() {
                    ll_progress.setVisibility(View.GONE);
                    swipeRefreshLayout.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    private void filterData(String query) {
        ll_countainer_list.removeAllViews();

        query = query.toLowerCase();
        int totalMatches = 0;

        for (HashMap<String, String> item : dataList) {
            String allText = String.join(" ",
                    item.get("nama"),
                    item.get("desc"),
                    item.get("alias")
            ).toLowerCase();

            if (allText.contains(query)) {
                totalMatches++;
            }
        }

        int currentIndex = 0;
        for (HashMap<String, String> item : dataList) {
            String allText = String.join(" ",
                    item.get("nama"),
                    item.get("desc"),
                    item.get("alias")
            ).toLowerCase();

            if (allText.contains(query)) {
                addFilteredItem(item, currentIndex, totalMatches);
                currentIndex++;
            }
        }

        if (totalMatches == 0) {
            ll_notfound.setVisibility(View.VISIBLE);
        } else {
            ll_notfound.setVisibility(View.GONE);
        }
    }


    private void addFilteredItem(HashMap<String, String> item, int index, int total) {
        View itemView = LayoutInflater.from(this).inflate(R.layout.item_jurusan_terdafar, ll_countainer_list, false);

        TextView tvNamaJurusan = itemView.findViewById(R.id.jurusanText);
        TextView tvAlias = itemView.findViewById(R.id.aliasText);
        TextView tvDescText = itemView.findViewById(R.id.descText);
        TextView tvIndex = itemView.findViewById(R.id.indexText);

        String nama = item.get("nama"), alias = item.get("alias"), desc = item.get("desc");

        tvNamaJurusan.setText(nama);
        tvAlias.setText(alias);
        tvDescText.setText(desc);
        tvIndex.setText(String.valueOf(total - index));

        itemView.setOnClickListener(v -> {
//            Intent intent = new Intent(JurusanTerdaftarActivity.this, DetailJurusanTerdaftarActivity.class);
//            intent.putExtra("id", item.get("id"));
//            intent.putExtra("index", tvIndex.getText());
//            startActivity(intent);
//            Helpers.overridePendingTransition_Enter_v1(JurusanTerdaftarActivity.this);
        });

        ll_countainer_list.addView(itemView);
    }


    private void reloadAllItems() {
        ll_countainer_list.removeAllViews();
        int total = dataList.size();

        for (int i = 0; i < dataList.size(); i++) {
            addFilteredItem(dataList.get(i), i, total);
        }

        ll_notfound.setVisibility(View.GONE);
    }

    @Override
    public void finish() {
        super.finish();
        Helpers.overridePendingTransition_Exit_v1(this);
    }

}
