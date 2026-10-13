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
import com.example.orion.utils.PaginationHelper;
import com.example.orion.utils.NetworkUtil;
import com.example.orion.utils.PreferenceManager;
import android.net.Uri;
import android.os.Environment;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.widget.Button;
import androidx.appcompat.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import java.io.OutputStream;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
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

public class AkunOrangtuaActivity extends AppCompatActivity {

    SwipeRefreshLayout swipeRefreshLayout;
    LinearLayout ll_progress, ll_countainer_list, ll_notfound;
    ArrayList<HashMap<String, String>> dataList = new ArrayList<>();
    private int currentPage = 1;
    private int lastPage = 1;
    private boolean isLoading = false;
    private boolean isLoadingMore = false;
    private String currentSearch = "";
    private PaginationHelper paginationHelper;
    private ActivityResultLauncher<Intent> addDataLauncher;
    private ActivityResultLauncher<Intent> excelPickerLauncher;
    private ActivityResultLauncher<Intent> saveTemplateLauncher;
    private FloatingActionButton fab_tambah;
    private boolean fab_tambah_allowed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_akun_orangtua);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setCollapseIcon(R.drawable.ic_arrow_back_white_24);
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

            if (role.equals("superadmin") || role.equals("kepsek")) {
                fab_tambah = findViewById(R.id.fab_add);
                fab_tambah_allowed = true;

                fab_tambah.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(AkunOrangtuaActivity.this, TambahAkunOrangtuaActivity.class);
                        addDataLauncher.launch(intent);
                        Helpers.overridePendingTransition_Enter_v1(AkunOrangtuaActivity.this);
                    }
                });
            }
        }

        ll_countainer_list = findViewById(R.id.ll_countainer_list);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout1);
        ll_progress = findViewById(R.id.ll_progress);
        ll_notfound = findViewById(R.id.ll_notfound);

        swipeRefreshLayout.setColorSchemeColors(getColor(R.color.primary));

        paginationHelper = new PaginationHelper(this, ll_countainer_list, () -> {
            if (!isLoading && !isLoadingMore && currentPage < lastPage) {
                loadData(currentPage + 1, false);
            }
        });

        loadData(1, true);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadData(1, true);
        });
                saveTemplateLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                        Uri targetUri = result.getData().getData();
                        downloadTemplateToUri(targetUri);
                    }
                }
        );
        excelPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                        Uri fileUri = result.getData().getData();
                        processImportExcel(fileUri);
                    }
                }
        );

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
        } else if (item.getItemId() == R.id.nav_download_template) {
            downloadTemplateExcel();
            return true;
        } else if (item.getItemId() == R.id.nav_import_excel) {
            openExcelFilePicker();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_top_nav_search_and_overflow, menu);

        MenuItem searchItem = menu.findItem(R.id.nav_search);
        if (searchItem != null && searchItem.getIcon() != null) {
            searchItem.getIcon().setTint(ContextCompat.getColor(this, R.color.white));
        }

        MenuItem downloadItem = menu.findItem(R.id.nav_download_template);
        if (downloadItem != null) {
            downloadItem.setVisible(fab_tambah_allowed);
            if (downloadItem.getIcon() != null) {
                downloadItem.getIcon().mutate().setColorFilter(new PorterDuffColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN));
            }
            SpannableString s = new SpannableString(downloadItem.getTitle());
            s.setSpan(new ForegroundColorSpan(Color.BLACK), 0, s.length(), 0);
            downloadItem.setTitle(s);
        }

        MenuItem importItem = menu.findItem(R.id.nav_import_excel);
        if (importItem != null) {
            importItem.setVisible(fab_tambah_allowed);
            if (importItem.getIcon() != null) {
                importItem.getIcon().mutate().setColorFilter(new PorterDuffColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN));
            }
            SpannableString s = new SpannableString(importItem.getTitle());
            s.setSpan(new ForegroundColorSpan(Color.BLACK), 0, s.length(), 0);
            importItem.setTitle(s);
        }

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
        if (searchButton != null) {
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
            if (toolbar.getOverflowIcon() != null) {
                toolbar.getOverflowIcon().setColorFilter(new PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN));
            }
        }

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fab_tambah_allowed) Helpers.animateZoomOut_v1(fab_tambah);

                if (articleItem != null) {
                    articleItem.setVisible(false);
                }
                if (downloadItem != null) downloadItem.setVisible(false);
                if (importItem != null) importItem.setVisible(false);
            }
        });

        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                currentSearch = query != null ? query.trim() : "";
                loadData(1, true);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty() && !currentSearch.isEmpty()) {
                    currentSearch = "";
                    loadData(1, true);
                }
                return true;
            }
        });

        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                Helpers.tintToolbarButtons(toolbar);
                if (fab_tambah_allowed) Helpers.animateZoomIn_v1(fab_tambah);
                if (downloadItem != null) downloadItem.setVisible(false);
                if (importItem != null) importItem.setVisible(false);
                return true; // allow expand
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                Helpers.tintToolbarButtons(toolbar);
                if (fab_tambah_allowed) Helpers.animateZoomIn_v1(fab_tambah);
                if (articleItem != null) {
                    articleItem.setVisible(true);
                }
                if (downloadItem != null) downloadItem.setVisible(fab_tambah_allowed);
                if (importItem != null) importItem.setVisible(fab_tambah_allowed);
                if (!currentSearch.isEmpty()) {
                    currentSearch = "";
                    loadData(1, true);
                }
                return true;
            }
        });

        return true;
    }


        private void loadData() {
        loadData(1, true);
    }

    private void loadData(int page, boolean isRefresh) {
        if (!NetworkUtil.isConnected(this)) {
            if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
            if (isRefresh) {
                Helpers.showAlert(this, null, "Tidak ada koneksi internet! Coba lagi nanti.", "OK", (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    finish();
                });
            } else {
                Toast.makeText(this, "Tidak ada koneksi internet", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        if (isRefresh) {
            isLoading = true;
            currentPage = 1;
            lastPage = 1;
            ll_progress.setVisibility(View.VISIBLE);
            swipeRefreshLayout.setVisibility(View.GONE);
            ll_notfound.setVisibility(View.GONE);
            ll_countainer_list.removeAllViews();
            dataList.clear();
        } else {
            isLoadingMore = true;
            if (paginationHelper != null) paginationHelper.showLoadingMore(true);
        }

        ApiService apiService = ApiClient.getClient(this).create(ApiService.class);
        apiService.getUsers("orangtua", page, 15, currentSearch.isEmpty() ? null : currentSearch).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (isFinishing() || isDestroyed()) return;

                isLoading = false;
                isLoadingMore = false;
                if (paginationHelper != null) paginationHelper.showLoadingMore(false);
                if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null) {
                    JsonObject json = response.body();
                    String status = json.has("status") ? json.get("status").getAsString() : "";

                    if (status.equals("success")) {
                        currentPage = json.has("current_page") ? json.get("current_page").getAsInt() : page;
                        lastPage = json.has("last_page") ? json.get("last_page").getAsInt() : 1;

                        JsonArray items = json.has("data") && json.get("data").isJsonArray() ? json.getAsJsonArray("data") : new JsonArray();

                        if (items.size() > 0) {
                            int total = json.has("total") ? json.get("total").getAsInt() : items.size();
                            int startIndex = dataList.size();
                            for (int i = 0; i < items.size(); i++) {
                                JsonObject d = items.get(i).getAsJsonObject();
                                addItem(d, startIndex + i, total);
                            }
                            ll_progress.setVisibility(View.GONE);
                            swipeRefreshLayout.setVisibility(View.VISIBLE);
                            ll_notfound.setVisibility(View.GONE);
                            if (fab_tambah_allowed && fab_tambah.getVisibility() != View.VISIBLE) {
                                Helpers.animateZoomIn_v1(fab_tambah);
                            }
                        } else {
                            if (page == 1) {
                                ll_progress.setVisibility(View.GONE);
                                ll_notfound.setVisibility(View.VISIBLE);
                                if (fab_tambah_allowed && fab_tambah.getVisibility() != View.VISIBLE) {
                                    Helpers.animateZoomIn_v1(fab_tambah);
                                }
                            }
                        }
                    } else {
                        Toast.makeText(AkunOrangtuaActivity.this, "Tidak dapat memuat data. Coba lagi nanti.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AkunOrangtuaActivity.this, "Tidak dapat memuat data. Coba lagi nanti.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                if (isFinishing() || isDestroyed()) return;

                isLoading = false;
                isLoadingMore = false;
                if (paginationHelper != null) paginationHelper.showLoadingMore(false);
                if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);

                if (page == 1) {
                    ll_progress.setVisibility(View.GONE);
                }
                Toast.makeText(AkunOrangtuaActivity.this, "Terjadi kesalahan: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void addItem(JsonObject data, int index, int total) {
        if (isFinishing() || isDestroyed()) return;

        View itemView = LayoutInflater.from(this).inflate(R.layout.item_akun_orangtua, ll_countainer_list, false);

        TextView tvNamaText = itemView.findViewById(R.id.namaText);
        TextView tvUsernameText = itemView.findViewById(R.id.usernameText);
        TextView tvDescText = itemView.findViewById(R.id.descText);
        TextView tvIndexText = itemView.findViewById(R.id.indexText);
        ImageView ivIcon = itemView.findViewById(R.id.iconImage);

        String nama = (data.has("nama") && !data.get("nama").isJsonNull()) ? data.get("nama").getAsString().trim() : "-";
        if (nama.isEmpty() || "null".equalsIgnoreCase(nama)) nama = "-";

        String username = (data.has("username") && !data.get("username").isJsonNull()) ? data.get("username").getAsString().trim() : "-";
        if (username.isEmpty() || "null".equalsIgnoreCase(username)) username = "-";

        String anakNama = "-";
        if (data.has("siswa") && !data.get("siswa").isJsonNull() && data.get("siswa").isJsonArray()) {
            com.google.gson.JsonArray arr = data.getAsJsonArray("siswa");
            if (arr.size() > 0) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < arr.size(); i++) {
                    com.google.gson.JsonObject s = arr.get(i).getAsJsonObject();
                    if (s.has("nama") && !s.get("nama").isJsonNull()) {
                        String sn = s.get("nama").getAsString().trim();
                        if (!sn.isEmpty() && !"null".equalsIgnoreCase(sn)) {
                            if (sb.length() > 0) sb.append(", ");
                            sb.append(sn);
                        }
                    }
                }
                if (sb.length() > 0) anakNama = sb.toString();
            }
        }

        tvNamaText.setText(nama);
        tvDescText.setText("Orang tua dari siswa: " + anakNama);
        tvUsernameText.setText(username);

        tvIndexText.setText((total - index) + "");

        HashMap<String, String> map = new HashMap<>();

        if (data.has("foto_profil") && !data.get("foto_profil").isJsonNull()) {
            String foto_profil = data.get("foto_profil").getAsString();

            if (!foto_profil.startsWith("http")) {
                foto_profil = ApiClient.base_url + foto_profil;
            }

            ivIcon.setImageTintList(null);

            Glide.with(this)
                    .load(foto_profil)
                    .placeholder(R.drawable.ic_image_24)
                    .error(R.drawable.ic_image_24)
                    .into(ivIcon);
        } else {
            ivIcon.setImageResource(R.drawable.ic_image_24);
            ivIcon.setImageTintList(
                    ContextCompat.getColorStateList(this, android.R.color.darker_gray)
            );
        }

        map.put("index", (total - index) + "");
        map.put("nama", nama);
        map.put("username", username);
        map.put("desc", tvDescText.getText().toString());
        map.put("id", data.get("id").getAsString());
        if (data.has("foto_profil") && !data.get("foto_profil").isJsonNull())
            map.put("foto_profil", data.get("foto_profil").getAsString());
        dataList.add(map);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AkunOrangtuaActivity.this, DetailAkunOrangtuaActivity.class);
                intent.putExtra("id", data.get("id").getAsString());
                intent.putExtra("index", tvIndexText.getText());
                addDataLauncher.launch(intent);
                Helpers.overridePendingTransition_Enter_v1(AkunOrangtuaActivity.this);
            }
        });

        ll_countainer_list.addView(itemView, ll_countainer_list.getChildCount() - (ll_countainer_list.getChildCount() + 1));

        if (fab_tambah_allowed && fab_tambah.getVisibility() != View.VISIBLE) {
            Helpers.animateZoomIn_v1(fab_tambah);
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
                    item.get("username")
            ).toLowerCase();

            if (allText.contains(query)) {
                totalMatches++;
            }
        }

        int currentIndex = 0;
        for (HashMap<String, String> item : dataList) {
            String allText = String.join(" ",
                    item.get("nama"),
                    item.get("username")
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
        View itemView = LayoutInflater.from(this).inflate(R.layout.item_akun_orangtua, ll_countainer_list, false);

        TextView tvNamaText = itemView.findViewById(R.id.namaText);
        TextView tvUsernameText = itemView.findViewById(R.id.usernameText);
        TextView tvDescText = itemView.findViewById(R.id.descText);
        TextView tvIndexText = itemView.findViewById(R.id.indexText);
        ImageView ivIcon = itemView.findViewById(R.id.iconImage);

        String nama = item.get("nama"), username = item.get("username"), desc = item.get("desc");

        tvNamaText.setText(nama);
        tvUsernameText.setText(username);
        tvDescText.setText(desc);
        tvIndexText.setText(String.valueOf(total - index));

        if (item.containsKey("foto_profil") && item.get("foto_profil") != null && !item.get("foto_profil").isEmpty()) {
            String foto_profil = item.get("foto_profil");
            if (!foto_profil.startsWith("http")) {
                foto_profil = ApiClient.base_url + foto_profil;
            }
            ivIcon.setImageTintList(null);
            Glide.with(this)
                    .load(foto_profil)
                    .placeholder(R.drawable.ic_image_24)
                    .error(R.drawable.ic_image_24)
                    .into(ivIcon);
        } else {
            ivIcon.setImageResource(R.drawable.ic_image_24);
            ivIcon.setImageTintList(
                    ContextCompat.getColorStateList(this, android.R.color.darker_gray)
            );
        }

        itemView.setOnClickListener(v -> {
//            Intent intent = new Intent(AkunOrangtuaActivity.this, DetailAkunOrangtuaActivity.class);
//            intent.putExtra("id", item.get("id"));
//            intent.putExtra("index", tvIndex.getText());
//            addDataLauncher.launch(intent);
//            Helpers.overridePendingTransition_Enter_v1(AkunOrangtuaActivity.this);
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


    private void downloadTemplateExcel() {
        showUnduhTemplateDialog();
    }

    private void showUnduhTemplateDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_unduh_template, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        View btnSimpan = dialogView.findViewById(R.id.btn_action_simpan);
        View btnBagikan = dialogView.findViewById(R.id.btn_action_bagikan);
        Button btnBatal = dialogView.findViewById(R.id.btn_batal_unduh);

        if (btnSimpan != null) {
            btnSimpan.setOnClickListener(v -> {
                dialog.dismiss();
                launchSaveTemplatePicker();
            });
        }

        if (btnBagikan != null) {
            btnBagikan.setOnClickListener(v -> {
                dialog.dismiss();
                shareTemplateViaWhatsApp();
            });
        }

        if (btnBatal != null) {
            btnBatal.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
    }

    private void launchSaveTemplatePicker() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        intent.putExtra(Intent.EXTRA_TITLE, "template_import_orangtua.xlsx");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, MediaStore.Downloads.EXTERNAL_CONTENT_URI);
        }
        try {
            saveTemplateLauncher.launch(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Tidak dapat membuka pengelola berkas: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void downloadTemplateToUri(Uri targetUri) {
        if (!NetworkUtil.isConnected(this)) {
            Toast.makeText(this, "Tidak ada koneksi internet.", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, "Menyimpan berkas template...", Toast.LENGTH_SHORT).show();
        ApiService apiService = ApiClient.getClient(this).create(ApiService.class);
        apiService.orangtuaExportTemplate().enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    new Thread(() -> {
                        try {
                            InputStream is = response.body().byteStream();
                            OutputStream os = getContentResolver().openOutputStream(targetUri);
                            if (os != null) {
                                byte[] buffer = new byte[4096];
                                int read;
                                while ((read = is.read(buffer)) != -1) {
                                    os.write(buffer, 0, read);
                                }
                                os.flush();
                                os.close();
                            }
                            is.close();
                            runOnUiThread(() -> Toast.makeText(AkunOrangtuaActivity.this, "Template berhasil disimpan di perangkat!", Toast.LENGTH_LONG).show());
                        } catch (Exception e) {
                            runOnUiThread(() -> Toast.makeText(AkunOrangtuaActivity.this, "Gagal menyimpan berkas: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        }
                    }).start();
                } else {
                    Toast.makeText(AkunOrangtuaActivity.this, "Gagal mengunduh template Excel.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(AkunOrangtuaActivity.this, "Terjadi kesalahan: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void shareTemplateViaWhatsApp() {
        if (!NetworkUtil.isConnected(this)) {
            Toast.makeText(this, "Tidak ada koneksi internet.", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, "Menyiapkan berkas template...", Toast.LENGTH_SHORT).show();
        ApiService apiService = ApiClient.getClient(this).create(ApiService.class);
        apiService.orangtuaExportTemplate().enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    new Thread(() -> {
                        try {
                            File cacheDir = getCacheDir();
                            File templateFile = new File(cacheDir, "template_import_orangtua.xlsx");

                            InputStream is = response.body().byteStream();
                            FileOutputStream fos = new FileOutputStream(templateFile);
                            byte[] buffer = new byte[4096];
                            int read;
                            while ((read = is.read(buffer)) != -1) {
                                fos.write(buffer, 0, read);
                            }
                            fos.flush();
                            fos.close();
                            is.close();

                            Uri contentUri = FileProvider.getUriForFile(
                                    AkunOrangtuaActivity.this,
                                    getPackageName() + ".provider",
                                    templateFile
                            );

                            runOnUiThread(() -> {
                                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                shareIntent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Template Import Orang Tua");
                                shareIntent.putExtra(Intent.EXTRA_TEXT, "Berikut berkas template import orang tua.");
                                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                shareIntent.setPackage("com.whatsapp");

                                try {
                                    startActivity(shareIntent);
                                } catch (ActivityNotFoundException e) {
                                    try {
                                        shareIntent.setPackage("com.whatsapp.w4b");
                                        startActivity(shareIntent);
                                    } catch (ActivityNotFoundException ex) {
                                        shareIntent.setPackage(null);
                                        startActivity(Intent.createChooser(shareIntent, "Bagikan Template Excel via"));
                                    }
                                }
                            });
                        } catch (Exception e) {
                            runOnUiThread(() -> Toast.makeText(AkunOrangtuaActivity.this, "Gagal memproses berkas: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        }
                    }).start();
                } else {
                    Toast.makeText(AkunOrangtuaActivity.this, "Gagal mengunduh template Excel.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(AkunOrangtuaActivity.this, "Terjadi kesalahan: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openExcelFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        String[] mimeTypes = {
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "application/vnd.ms-excel",
                "application/wps-office.xlsx"
        };
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        excelPickerLauncher.launch(Intent.createChooser(intent, "Pilih Berkas Excel Akun Orang Tua"));
    }

    private void processImportExcel(Uri uri) {
        if (!NetworkUtil.isConnected(this)) {
            Toast.makeText(this, "Tidak ada koneksi internet.", Toast.LENGTH_SHORT).show();
            return;
        }

        Helpers.LoadingHelper loading = new Helpers.LoadingHelper(this);
        loading.show();

        new Thread(() -> {
            try {
                File tempFile = new File(getCacheDir(), "import_orangtua_" + System.currentTimeMillis() + ".xlsx");
                InputStream is = getContentResolver().openInputStream(uri);
                if (is == null) {
                    runOnUiThread(() -> {
                        loading.dismiss();
                        Toast.makeText(this, "Gagal membaca berkas.", Toast.LENGTH_SHORT).show();
                    });
                    return;
                }
                FileOutputStream fos = new FileOutputStream(tempFile);
                byte[] buf = new byte[4096];
                int len;
                while ((len = is.read(buf)) != -1) {
                    fos.write(buf, 0, len);
                }
                fos.flush();
                fos.close();
                is.close();

                runOnUiThread(() -> {
                    RequestBody requestFile = RequestBody.create(MediaType.parse("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"), tempFile);
                    MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", tempFile.getName(), requestFile);
                    RequestBody previewPart = RequestBody.create(MultipartBody.FORM, "1");

                    ApiService apiService = ApiClient.getClient(this).create(ApiService.class);
                    apiService.orangtuaImport(filePart, previewPart).enqueue(new Callback<JsonObject>() {
                        @Override
                        public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                            loading.dismiss();
                            if (response.isSuccessful() && response.body() != null) {
                                JsonObject res = response.body();
                                String status = res.has("status") ? res.get("status").getAsString() : "";
                                if ("success".equalsIgnoreCase(status)) {
                                    int totalRows = 0;
                                    if (res.has("summary") && res.get("summary").isJsonObject()) {
                                        JsonObject summary = res.getAsJsonObject("summary");
                                        totalRows = summary.has("total_rows") ? summary.get("total_rows").getAsInt() : 0;
                                    }
                                    if (totalRows == 0) {
                                        if (tempFile.exists()) tempFile.delete();
                                        Toast.makeText(AkunOrangtuaActivity.this, "Berkas Excel tidak memiliki baris data.", Toast.LENGTH_LONG).show();
                                        return;
                                    }
                                    showImportPreviewDialog(tempFile, res);
                                } else {
                                    if (tempFile.exists()) tempFile.delete();
                                    String msg = res.has("message") ? res.get("message").getAsString() : "Gagal memproses berkas.";
                                    Toast.makeText(AkunOrangtuaActivity.this, msg, Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(AkunOrangtuaActivity.this, "Gagal memvalidasi berkas Excel.", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<JsonObject> call, Throwable t) {
                            loading.dismiss();
                            Toast.makeText(AkunOrangtuaActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    loading.dismiss();
                    Toast.makeText(this, "Gagal membaca berkas: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void showImportPreviewDialog(File tempFile, JsonObject res) {
        int checkTotal = 0;
        if (res.has("summary") && res.get("summary").isJsonObject()) {
            JsonObject summary = res.getAsJsonObject("summary");
            checkTotal = summary.has("total_rows") ? summary.get("total_rows").getAsInt() : 0;
        }
        if (checkTotal == 0) {
            if (tempFile.exists()) tempFile.delete();
            Toast.makeText(this, "Berkas Excel tidak memiliki baris data.", Toast.LENGTH_LONG).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_import_preview, null);
        builder.setView(dialogView);

        TextView tvTitle = dialogView.findViewById(R.id.tv_dialog_title);
        if (tvTitle != null) tvTitle.setText("Pratinjau Akun Orang Tua");

        TextView tvFileName = dialogView.findViewById(R.id.tv_file_name);
        TextView tvTotal = dialogView.findViewById(R.id.tv_total_rows);
        TextView tvValid = dialogView.findViewById(R.id.tv_valid_rows);
        TextView tvInvalid = dialogView.findViewById(R.id.tv_invalid_rows);
        LinearLayout llAllValid = dialogView.findViewById(R.id.ll_all_valid_section);
        LinearLayout llErrorSec = dialogView.findViewById(R.id.ll_error_section);
        LinearLayout llErrorContainer = dialogView.findViewById(R.id.ll_error_container);
        Button btnBatal = dialogView.findViewById(R.id.btn_batal_preview);
        Button btnSimpan = dialogView.findViewById(R.id.btn_simpan_database);

        tvFileName.setText("Berkas: " + tempFile.getName());

        int totalRows = 0, validRows = 0, invalidRows = 0;
        if (res.has("summary") && res.get("summary").isJsonObject()) {
            JsonObject summary = res.getAsJsonObject("summary");
            totalRows = summary.has("total_rows") ? summary.get("total_rows").getAsInt() : 0;
            validRows = summary.has("valid_rows") ? summary.get("valid_rows").getAsInt() : 0;
            invalidRows = summary.has("invalid_rows") ? summary.get("invalid_rows").getAsInt() : 0;
        }

        tvTotal.setText(String.valueOf(totalRows));
        tvValid.setText(String.valueOf(validRows));
        tvInvalid.setText(String.valueOf(invalidRows));

        JsonArray errors = res.has("errors") && res.get("errors").isJsonArray() ? res.getAsJsonArray("errors") : new JsonArray();

        if (invalidRows > 0 && errors.size() > 0) {
            llErrorSec.setVisibility(View.VISIBLE);
            llAllValid.setVisibility(View.GONE);
            llErrorContainer.removeAllViews();

            for (int i = 0; i < errors.size(); i++) {
                JsonObject err = errors.get(i).getAsJsonObject();
                int row = err.has("row") ? err.get("row").getAsInt() : 0;
                String identifier = err.has("identifier") ? err.get("identifier").getAsString() : "-";
                String msg = err.has("message") ? err.get("message").getAsString() : "";

                View itemErr = LayoutInflater.from(this).inflate(R.layout.item_import_error, llErrorContainer, false);
                TextView tvBadge = itemErr.findViewById(R.id.tv_error_badge);
                TextView tvId = itemErr.findViewById(R.id.tv_error_identifier);
                TextView tvMsg = itemErr.findViewById(R.id.tv_error_message);

                tvBadge.setText("Baris " + row);
                tvId.setText(identifier);
                tvMsg.setText(msg);

                llErrorContainer.addView(itemErr);
            }
        } else {
            llErrorSec.setVisibility(View.GONE);
            llAllValid.setVisibility(View.VISIBLE);
        }

        builder.setCancelable(false);
        AlertDialog dialog = builder.create();

        btnBatal.setOnClickListener(v -> {
            dialog.dismiss();
            if (tempFile.exists()) tempFile.delete();
        });

        if (validRows == 0) {
            btnSimpan.setEnabled(false);
            btnSimpan.setAlpha(0.5f);
        } else {
            final int finalValid = validRows;
            btnSimpan.setOnClickListener(v -> {
                dialog.dismiss();
                commitImportExcel(tempFile, finalValid);
            });
        }

        dialog.show();
    }

    private void commitImportExcel(File tempFile, int validCount) {
        Helpers.LoadingHelper loading = new Helpers.LoadingHelper(this);
        loading.show();

        RequestBody requestFile = RequestBody.create(MediaType.parse("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"), tempFile);
        MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", tempFile.getName(), requestFile);
        RequestBody previewPart = RequestBody.create(MultipartBody.FORM, "0");

        ApiService apiService = ApiClient.getClient(this).create(ApiService.class);
        apiService.orangtuaImport(filePart, previewPart).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                loading.dismiss();
                if (tempFile.exists()) tempFile.delete();

                if (response.isSuccessful() && response.body() != null) {
                    JsonObject res = response.body();
                    String status = res.has("status") ? res.get("status").getAsString() : "";
                    if ("success".equalsIgnoreCase(status)) {
                        Toast.makeText(AkunOrangtuaActivity.this, "Berhasil mengimpor " + validCount + " akun orang tua!", Toast.LENGTH_LONG).show();
                        loadData(1, true);
                    } else {
                        String msg = res.has("message") ? res.get("message").getAsString() : "Gagal mengimpor data.";
                        Toast.makeText(AkunOrangtuaActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AkunOrangtuaActivity.this, "Gagal menyimpan data ke database.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                loading.dismiss();
                if (tempFile.exists()) tempFile.delete();
                Toast.makeText(AkunOrangtuaActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}

