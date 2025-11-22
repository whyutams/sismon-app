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

public class PerizinanSiswaActivity extends AppCompatActivity {

    SwipeRefreshLayout swipeRefreshLayout;
    LinearLayout ll_progress, ll_countainer_list, ll_notfound;
    ArrayList<HashMap<String, String>> dataList = new ArrayList<>();
    private ActivityResultLauncher<Intent> addDataLauncher;
    private FloatingActionButton fab_tambah;
    private boolean fab_tambah_showed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perizinan_siswa);

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

            if (user.has("jurusan") && role.equals("guru")) {
                JsonObject jurusan = user.get("jurusan").getAsJsonObject();
                if (jurusan.has("alias")) {
                    getSupportActionBar().setTitle(getSupportActionBar().getTitle() + " " + jurusan.get("alias").getAsString());
                }
            }

            if (role.equals("guru_piket") || role.equals("kesiswaan") || role.equals("guru") && user.has("jurusan")) {
                fab_tambah = findViewById(R.id.fab_add);
                fab_tambah_showed = true;

                fab_tambah.setOnClickListener(view -> {
                    Intent intent = new Intent(PerizinanSiswaActivity.this, TambahPerizinanSiswaActivity.class);
                    addDataLauncher.launch(intent);
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
                        Helpers.delay(1000, this::loadData);
                    }
                }
        );
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

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            Drawable navIcon = toolbar.getNavigationIcon();
            if (navIcon != null) {
                navIcon.setColorFilter(new PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP));
            }
        }


        MenuItem searchItem = menu.findItem(R.id.nav_search);
        androidx.appcompat.widget.SearchView searchView =
                (androidx.appcompat.widget.SearchView) searchItem.getActionView();

        searchView.setQueryHint("Cari ...");

        int searchTextId = androidx.appcompat.R.id.search_src_text;
        TextView searchText = searchView.findViewById(searchTextId);
        if (searchText != null) {
            searchText.setTextColor(Color.WHITE);
            searchText.setHintTextColor(Color.parseColor("#80FFFFFF"));
        }
        ImageView searchButton = searchView.findViewById(androidx.appcompat.R.id.search_button);
        if (searchButton != null)
            searchButton.setColorFilter(new PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP));
        ImageView searchIcon = searchView.findViewById(androidx.appcompat.R.id.search_mag_icon);
        if (searchIcon != null)
            searchIcon.setColorFilter(new PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN));
        ImageView closeButton = searchView.findViewById(androidx.appcompat.R.id.search_close_btn);
        if (closeButton != null)
            closeButton.setColorFilter(new PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN));

        searchView.setOnSearchClickListener(view -> {
            if (fab_tambah_showed) Helpers.animateZoomOut_v1(fab_tambah);
            for (int i = 0; i < searchView.getChildCount(); i++) {
                View v = searchView.getChildAt(i);
                if (v instanceof ImageView) {
                    ImageView iv = (ImageView) v;
                    iv.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
                }
            }

            LinearLayout ll = (LinearLayout) searchView.getChildAt(0);
            if (ll != null) {
                for (int i = 0; i < ll.getChildCount(); i++) {
                    View v = ll.getChildAt(i);
                    if (v instanceof ImageView) {
                        ((ImageView) v).setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
                    }
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
                if (newText.isEmpty()) reloadAllItems();
                else filterData(newText);
                return true;
            }
        });

        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                if (fab_tambah_showed) Helpers.animateZoomIn_v1(fab_tambah);

                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                if (fab_tambah_showed) Helpers.animateZoomIn_v1(fab_tambah);
                reloadAllItems();
                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.nav_search) return true;
        return super.onOptionsItemSelected(item);
    }

    private void loadData() {
        if (!NetworkUtil.isConnected(this)) {
            Helpers.showAlert(this, null, "Tidak ada koneksi internet! Coba lagi nanti.", "OK", (dialogInterface, i) -> {
                dialogInterface.dismiss();
                finish();
            });
            return;
        }

        ApiService apiService = ApiClient.getClient(this).create(ApiService.class);
        apiService.info().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(PerizinanSiswaActivity.this, "Tidak dapat memuat data. Coba lagi nanti.", Toast.LENGTH_SHORT).show();
                    return;
                }

                JsonObject json = response.body();
                String status = json.has("status") ? json.get("status").getAsString() : "";

                if (!status.equals("success")) {
                    Toast.makeText(PerizinanSiswaActivity.this, "Tidak dapat memuat data. Coba lagi nanti.", Toast.LENGTH_SHORT).show();
                    return;
                }

                ll_countainer_list.removeAllViews();
                dataList.clear();

                JsonObject main_data = json.getAsJsonObject("data");
                JsonObject data = main_data.getAsJsonObject("data");
                JsonArray perizinan = data.getAsJsonArray("perizinan");

                if (perizinan.size() != 0) {
                    for (int i = 0; i < perizinan.size(); i++) {
                        JsonObject d = perizinan.get(i).getAsJsonObject();
                        addItem(d, i, perizinan.size());
                    }
                    ll_progress.setVisibility(View.GONE);
                    swipeRefreshLayout.setVisibility(View.VISIBLE);
                } else {
                    Helpers.delay(1000, () -> {
                        if (isFinishing() || isDestroyed()) return;
                        ll_progress.setVisibility(View.GONE);
                        ll_notfound.setVisibility(View.VISIBLE);
                    });
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(PerizinanSiswaActivity.this, "Terjadi kesalahan: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void addItem(JsonObject data, int index, int total) {
        if (isFinishing() || isDestroyed()) return;

        View itemView = LayoutInflater.from(this).inflate(R.layout.item_perizinan, ll_countainer_list, false);

        TextView tvTitle = itemView.findViewById(R.id.titleText);
        TextView tvDesc = itemView.findViewById(R.id.descText);
        TextView tvDate = itemView.findViewById(R.id.dateText);
        TextView tvTime = itemView.findViewById(R.id.timeText);
        TextView tvIndex = itemView.findViewById(R.id.indexText);
        TextView tvStatus = itemView.findViewById(R.id.statusText);
        ImageView ivIcon = itemView.findViewById(R.id.iconImage);

        JsonObject siswa = data.getAsJsonObject("siswa");
        String nama = siswa.get("nama").getAsString();
        String keterangan = data.get("keterangan").getAsString();

        tvTitle.setText(nama);
        tvDesc.setText(keterangan.length() > 65 ? keterangan.substring(0, 65) + "..." : keterangan);
        tvDate.setText(Helpers.formatCreatedAt(data.get("date").getAsString(), 0));
        tvTime.setText(Helpers.formatCreatedAt(data.get("date").getAsString(), 1));
        tvIndex.setText(String.valueOf(total - index));

        String status = data.get("status").getAsString();
        if (status.equals("approved")) {
            tvStatus.setText("Disetujui");
            tvStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_16, 0, 0, 0);
            tvStatus.setCompoundDrawableTintList(ContextCompat.getColorStateList(this, R.color.hijau));
            tvStatus.setTextColor(getColor(R.color.hijau));
        } else if (status.equals("rejected")) {
            tvStatus.setText("Ditolak");
            tvStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_close_16, 0, 0, 0);
            tvStatus.setCompoundDrawableTintList(ContextCompat.getColorStateList(this, R.color.merah));
            tvStatus.setTextColor(getColor(R.color.merah));
        } else {
            tvStatus.setText("Menunggu Persetujuan");
            tvStatus.setTextColor(getColor(R.color.orange));
        }

        if (data.has("gambar") && !data.get("gambar").isJsonNull()) {
            String gambar = data.get("gambar").getAsString();
            if (!gambar.startsWith("http")) gambar = ApiClient.base_url + gambar;
            ivIcon.setImageTintList(null);
            Glide.with(this).load(gambar).placeholder(R.drawable.ic_image_24).error(R.drawable.ic_image_24).into(ivIcon);
        } else {
            ivIcon.setImageResource(R.drawable.ic_image_24);
            ivIcon.setImageTintList(ContextCompat.getColorStateList(this, android.R.color.darker_gray));
        }

        HashMap<String, String> map = new HashMap<>();
        map.put("index", String.valueOf(total - index));
        map.put("nama", nama);
        map.put("keterangan", keterangan);
        map.put("date", data.get("date").getAsString());
        map.put("id", data.get("id").getAsString());
        map.put("status", status);
        if (data.has("gambar") && !data.get("gambar").isJsonNull())
            map.put("gambar", data.get("gambar").getAsString());
        dataList.add(map);

        itemView.setOnClickListener(v -> {
            Intent intent = new Intent(PerizinanSiswaActivity.this, DetailPerizinanSiswaActivity.class);
            intent.putExtra("id", data.get("id").getAsString());
            intent.putExtra("index", tvIndex.getText());
            intent.putExtra("status", status);
            startActivity(intent);
        });

        ll_countainer_list.addView(itemView);

        if (total == index + 1 && fab_tambah_showed) Helpers.animateZoomIn_v1(fab_tambah);
    }

    private void filterData(String query) {
        ll_countainer_list.removeAllViews();
        query = query.toLowerCase();
        int totalMatches = 0;

        for (HashMap<String, String> item : dataList) {
            String allText = String.join(" ",
                    item.get("nama"),
                    item.get("keterangan"),
                    item.get("date"),
                    item.get("status").equals("approved") ? "Disetujui" : (item.get("status").equals("rejected") ? "Ditolak" : "Menunggu Persetujuan")).toLowerCase();

            if (allText.contains(query)) totalMatches++;
        }

        int currentIndex = 0;
        for (HashMap<String, String> item : dataList) {
            String allText = String.join(" ",
                    item.get("nama"),
                    item.get("keterangan"),
                    item.get("date"),
                    item.get("status").equals("approved") ? "Disetujui" : (item.get("status").equals("rejected") ? "Ditolak" : "Menunggu Persetujuan")).toLowerCase();

            if (allText.contains(query)) {
                addFilteredItem(item, currentIndex, totalMatches);
                currentIndex++;
            }
        }

        ll_notfound.setVisibility(totalMatches == 0 ? View.VISIBLE : View.GONE);
    }

    private void addFilteredItem(HashMap<String, String> item, int index, int total) {
        View itemView = LayoutInflater.from(this).inflate(R.layout.item_perizinan, ll_countainer_list, false);

        TextView tvTitle = itemView.findViewById(R.id.titleText);
        TextView tvDesc = itemView.findViewById(R.id.descText);
        TextView tvDate = itemView.findViewById(R.id.dateText);
        TextView tvTime = itemView.findViewById(R.id.timeText);
        TextView tvIndex = itemView.findViewById(R.id.indexText);
        TextView tvStatus = itemView.findViewById(R.id.statusText);
        ImageView ivIcon = itemView.findViewById(R.id.iconImage);

        tvTitle.setText(item.get("nama"));
        String keterangan = item.get("keterangan");
        tvDesc.setText(keterangan.length() > 65 ? keterangan.substring(0, 65) + "..." : keterangan);
        tvDate.setText(Helpers.formatCreatedAt(item.get("date"), 0));
        tvTime.setText(Helpers.formatCreatedAt(item.get("date"), 1));
        tvIndex.setText(String.valueOf(total - index));

        String status = item.get("status");
        if (status.equals("approved")) {
            tvStatus.setText("Disetujui");
            tvStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_16, 0, 0, 0);
            tvStatus.setCompoundDrawableTintList(ContextCompat.getColorStateList(this, R.color.hijau));
            tvStatus.setTextColor(getColor(R.color.hijau));
        } else if (status.equals("rejected")) {
            tvStatus.setText("Ditolak");
            tvStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_close_16, 0, 0, 0);
            tvStatus.setCompoundDrawableTintList(ContextCompat.getColorStateList(this, R.color.merah));
            tvStatus.setTextColor(getColor(R.color.merah));
        } else {
            tvStatus.setText("Menunggu Persetujuan");
            tvStatus.setTextColor(getColor(R.color.orange));
        }

        if (item.containsKey("gambar") && item.get("gambar") != null && !item.get("gambar").isEmpty()) {
            String gambar = item.get("gambar");
            if (!gambar.startsWith("http")) gambar = ApiClient.base_url + gambar;
            ivIcon.setImageTintList(null);
            Glide.with(this).load(gambar).placeholder(R.drawable.ic_image_24).error(R.drawable.ic_image_24).into(ivIcon);
        } else {
            ivIcon.setImageResource(R.drawable.ic_image_24);
            ivIcon.setImageTintList(ContextCompat.getColorStateList(this, android.R.color.darker_gray));
        }

        itemView.setOnClickListener(v -> {
            Intent intent = new Intent(PerizinanSiswaActivity.this, DetailPerizinanSiswaActivity.class);
            intent.putExtra("id", item.get("id"));
            intent.putExtra("index", tvIndex.getText());
            intent.putExtra("status", status);
            startActivity(intent);
        });

        ll_countainer_list.addView(itemView);
    }

    private void reloadAllItems() {
        ll_countainer_list.removeAllViews();
        int total = dataList.size();
        for (int i = 0; i < total; i++) addFilteredItem(dataList.get(i), i, total);
        ll_notfound.setVisibility(View.GONE);
    }
}
