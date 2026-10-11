package com.example.orion.activity;

import android.Manifest;
import android.app.DownloadManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.example.orion.R;
import com.example.orion.utils.Helpers;
import com.example.orion.utils.PaginationHelper;
import com.example.orion.utils.NetworkUtil;
import com.example.orion.utils.PreferenceManager;
import com.example.orion.utils.api.ApiClient;
import com.example.orion.utils.api.ApiService;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import android.os.Handler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class KeterlambatanSiswaActivity extends AppCompatActivity {

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
    private FloatingActionButton fab_tambah;
    private boolean fab_tambah_allowed = false;
    private static final String CHANNEL_ID = "download_channel";
    private static final int NOTIFICATION_PERMISSION_CODE = 100;
    private static final int STORAGE_PERMISSION_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_keterlambatan_siswa);

        createNotificationChannel();

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

            if (role.equals("guru_piket") || role.equals("kesiswaan") || role.equals("bimbingan_konseling")) {
                fab_tambah = findViewById(R.id.fab_add);
                fab_tambah_allowed = true;

                fab_tambah.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(KeterlambatanSiswaActivity.this, TambahKeterlambatanSiswaActivity.class);
                        addDataLauncher.launch(intent);
                        Helpers.overridePendingTransition_Enter_v1(KeterlambatanSiswaActivity.this);
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

        addDataLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        ll_progress.setVisibility(View.VISIBLE);
                        swipeRefreshLayout.setVisibility(View.GONE);

                        Helpers.delay(1000, new Runnable() {
                            @Override
                            public void run() {
                                loadData(1, true);
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
        } else if (item.getItemId() == R.id.nav_export) {
            checkPermissionsAndExport();
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
                if (articleItem != null) articleItem.setVisible(false);
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
                if (articleItem != null) articleItem.setVisible(false);
                return true;
            }
            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                Helpers.tintToolbarButtons(toolbar);
                if (fab_tambah_allowed) Helpers.animateZoomIn_v1(fab_tambah);
                if (articleItem != null) articleItem.setVisible(true);
                if (!currentSearch.isEmpty()) {
                    currentSearch = "";
                    loadData(1, true);
                }
                return true;
            }
        });

        return true;
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
        apiService.getKeterlambatan(page, 15, currentSearch.isEmpty() ? null : currentSearch).enqueue(new Callback<JsonObject>() {
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
                        } else {
                            if (page == 1) {
                                ll_progress.setVisibility(View.GONE);
                                ll_notfound.setVisibility(View.VISIBLE);
                            }
                        }
                    } else {
                        Toast.makeText(KeterlambatanSiswaActivity.this, "Tidak dapat memuat data. Coba lagi nanti.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(KeterlambatanSiswaActivity.this, "Tidak dapat memuat data. Coba lagi nanti.", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(KeterlambatanSiswaActivity.this, "Terjadi kesalahan: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void addItem(JsonObject data, int index, int total) {
        if (isFinishing() || isDestroyed()) return;

        View itemView = LayoutInflater.from(this).inflate(R.layout.item_keterlambatan, ll_countainer_list, false);

        TextView tvTitle = itemView.findViewById(R.id.titleText);
        TextView tvDesc = itemView.findViewById(R.id.descText);
        TextView tvDate = itemView.findViewById(R.id.dateText);
        TextView tvTime = itemView.findViewById(R.id.timeText);
        TextView tvIndex = itemView.findViewById(R.id.indexText);
        ImageView ivIcon = itemView.findViewById(R.id.iconImage);

        JsonObject siswa = data.get("siswa").getAsJsonObject();
        String nama = siswa.get("nama").getAsString(), keterangan = data.get("keterangan").getAsString();

        tvTitle.setText(nama);
        tvDesc.setText((keterangan.length() > 65 ? keterangan.substring(0, 65) + "..." : keterangan));
        tvDate.setText((Helpers.formatCreatedAt(data.get("date").getAsString(), 0)));
        tvTime.setText((Helpers.formatCreatedAt(data.get("date").getAsString(), 1)));
        tvIndex.setText((total - index) + "");

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
        map.put("index", (total - index) + "");
        map.put("nama", nama);
        map.put("keterangan", keterangan);
        map.put("date", data.get("date").getAsString());
        map.put("id", data.get("id").getAsString());
        map.put("nis", siswa.has("nis") && !siswa.get("nis").isJsonNull() ? siswa.get("nis").getAsString() : "-");
        map.put("kelas", siswa.has("kelas") && !siswa.get("kelas").isJsonNull() ? siswa.get("kelas").getAsString() : "-");
        if (data.has("gambar") && !data.get("gambar").isJsonNull()) map.put("gambar", data.get("gambar").getAsString());
        dataList.add(map);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(KeterlambatanSiswaActivity.this, DetailKeterlambatanSiswaActivity.class);
                intent.putExtra("id", data.get("id").getAsString());
                intent.putExtra("index", tvIndex.getText());
                addDataLauncher.launch(intent);
                Helpers.overridePendingTransition_Enter_v1(KeterlambatanSiswaActivity.this);
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
            String allText = String.join(" ", item.get("nama"), item.get("keterangan"), item.get("date")).toLowerCase();
            if (allText.contains(query)) totalMatches++;
        }

        int currentIndex = 0;
        for (HashMap<String, String> item : dataList) {
            String allText = String.join(" ", item.get("nama"), item.get("keterangan"), item.get("date")).toLowerCase();
            if (allText.contains(query)) {
                addFilteredItem(item, currentIndex, totalMatches);
                currentIndex++;
            }
        }

        if (totalMatches == 0) ll_notfound.setVisibility(View.VISIBLE);
        else ll_notfound.setVisibility(View.GONE);
    }

    private void addFilteredItem(HashMap<String, String> item, int index, int total) {
        View itemView = LayoutInflater.from(this).inflate(R.layout.item_keterlambatan, ll_countainer_list, false);

        TextView tvTitle = itemView.findViewById(R.id.titleText);
        TextView tvDesc = itemView.findViewById(R.id.descText);
        TextView tvDate = itemView.findViewById(R.id.dateText);
        TextView tvTime = itemView.findViewById(R.id.timeText);
        TextView tvIndex = itemView.findViewById(R.id.indexText);
        ImageView ivIcon = itemView.findViewById(R.id.iconImage);

        tvTitle.setText(item.get("nama"));
        String keterangan = item.get("keterangan");
        tvDesc.setText(keterangan.length() > 65 ? keterangan.substring(0, 65) + "..." : keterangan);
        tvDate.setText(Helpers.formatCreatedAt(item.get("date"), 0));
        tvTime.setText(Helpers.formatCreatedAt(item.get("date"), 1));
        tvIndex.setText(String.valueOf(total - index));

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
            Intent intent = new Intent(KeterlambatanSiswaActivity.this, DetailKeterlambatanSiswaActivity.class);
            intent.putExtra("id", item.get("id"));
            intent.putExtra("index", tvIndex.getText());
            addDataLauncher.launch(intent);
            Helpers.overridePendingTransition_Enter_v1(KeterlambatanSiswaActivity.this);
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

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Download File";
            String description = "Notifikasi untuk file yang didownload";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.enableLights(true);
            channel.enableVibration(true);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void checkPermissionsAndExport() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_CODE);
            } else {
                exportToCsv();
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
            } else {
                exportToCsv();
            }
        } else {
            exportToCsv();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_CODE || requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                exportToCsv();
            } else {
                Toast.makeText(this, "Permission diperlukan untuk export dan notifikasi", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void exportToCsv() {
        if (dataList == null || dataList.isEmpty()) {
            Toast.makeText(this, "Tidak ada data untuk diekspor", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                String fileName = "Keterlambatan_" + timeStamp + ".csv";

                String[] headers = {"No", "NIS", "Nama Siswa", "Kelas", "Keterangan", "Tanggal", "Waktu"};
                StringBuilder csvContent = new StringBuilder();
                csvContent.append(String.join(",", headers)).append("\n");

                for (int i = 0; i < dataList.size(); i++) {
                    HashMap<String, String> item = dataList.get(i);
                    String no = String.valueOf(i + 1);
                    String nis = item.getOrDefault("nis", "-");
                    String nama = escapeCsv(item.getOrDefault("nama", "-"));
                    String kelas = item.getOrDefault("kelas", "-");
                    String keterangan = escapeCsv(item.getOrDefault("keterangan", "-"));
                    String date = item.getOrDefault("date", "-");

                    String tanggal = date;
                    String waktu = "";
                    if (date != null && date.contains(" ")) {
                        int sp = date.indexOf(" ");
                        tanggal = date.substring(0, sp);
                        waktu = date.substring(sp + 1);
                    }

                    String[] row = {no, nis, nama, kelas, keterangan, tanggal, waktu};
                    csvContent.append(String.join(",", row)).append("\n");
                }

                Uri fileUri = null;
                String filePath = "";

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
                    values.put(MediaStore.Downloads.MIME_TYPE, "text/csv");
                    values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

                    fileUri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                    if (fileUri != null) {
                        OutputStream outputStream = getContentResolver().openOutputStream(fileUri);
                        outputStream.write(csvContent.toString().getBytes());
                        outputStream.close();

                        Cursor cursor = getContentResolver().query(fileUri, new String[]{MediaStore.Downloads.DATA}, null, null, null);
                        if (cursor != null && cursor.moveToFirst()) {
                            filePath = cursor.getString(0);
                            cursor.close();
                        }
                        if (filePath.isEmpty()) {
                            filePath = "Download/" + fileName;
                        }
                    }
                } else {
                    File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                    if (!downloadDir.exists()) downloadDir.mkdirs();
                    File file = new File(downloadDir, fileName);
                    FileOutputStream fos = new FileOutputStream(file);
                    fos.write(csvContent.toString().getBytes());
                    fos.close();
                    fileUri = Uri.fromFile(file);
                    filePath = file.getAbsolutePath();
                }

                final Uri finalUri = fileUri;
                final String finalPath = filePath;

                runOnUiThread(() -> {
                    showSuccessDialog(fileName, finalPath, finalUri);
                    showDownloadNotification(fileName, finalUri);
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Gagal mengekspor: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private void showSuccessDialog(String fileName, String filePath, Uri fileUri) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("✅ Export Berhasil");
        builder.setMessage("File Keterlambatan berhasil disimpan:\n\n" +
                "📁 " + filePath + "\n\n" +
                "Total: " + dataList.size() + " data");

        // Tidak ada tombol — dialog hanya bisa ditutup dengan mengetuk di luar
        builder.setCancelable(true);

        builder.show();
    }


    private void openFile(Uri fileUri) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(fileUri, "text/csv");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(Intent.createChooser(intent, "Buka dengan"));
        } catch (Exception e) {
            Toast.makeText(this, "Tidak ada aplikasi untuk membuka file CSV", Toast.LENGTH_LONG).show();
        }
    }

    private void showDownloadNotification(String fileName, Uri fileUri) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(fileUri, "text/csv");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            PendingIntent pendingIntent = PendingIntent.getActivity(
                    this,
                    0,
                    intent,
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ?
                            PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT :
                            PendingIntent.FLAG_UPDATE_CURRENT
            );

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.stat_sys_download_done)
                    .setContentTitle("Download Selesai")
                    .setContentText(fileName)
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText("File " + fileName + " berhasil didownload.\nKlik untuk membuka."))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setDefaults(NotificationCompat.DEFAULT_ALL);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                notificationManager.notify(1001, builder.build());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        boolean hasSpecial = value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r");
        String v = value.replace("\"", "\"\"");
        if (hasSpecial) v = "\"" + v + "\"";
        return v;
    }

    @Override
    public void finish() {
        super.finish();
        Helpers.overridePendingTransition_Exit_v1(this);
    }
}