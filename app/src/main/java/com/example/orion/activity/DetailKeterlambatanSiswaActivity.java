package com.example.orion.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import androidx.appcompat.app.AlertDialog;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.example.orion.R;
import com.example.orion.utils.Helpers;
import com.example.orion.utils.NetworkUtil;
import com.example.orion.utils.PreferenceManager;
import com.example.orion.utils.api.ApiClient;
import com.example.orion.utils.api.ApiService;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.jspecify.annotations.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailKeterlambatanSiswaActivity extends AppCompatActivity {
    private boolean isDataLoaded = false;

    ImageView iv_gambar;
    TextView tv_no, tv_nis, tv_nama, tv_nomorHp, tv_jurusan, tv_kelas, tv_ortu, tv_terlambat, tv_keterangan, tv_dibuat_oleh, tv_dibuat_pada, tv_diperbarui_oleh, tv_diperbarui_pada;
    private ActivityResultLauncher<Intent> editDataLauncher;
    Button btn_bagikan;
    LinearLayout ll_progress;
    ScrollView sv_main;
    private static final int PERMISSION_REQUEST_CODE = 100;
    private String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_keterlambatan_siswa);

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

        ll_progress = findViewById(R.id.ll_progress);
        sv_main = findViewById(R.id.sv_main);
        iv_gambar = findViewById(R.id.iv_gambar);
        tv_no = findViewById(R.id.tv_no);
        tv_nis = findViewById(R.id.tv_nis);
        tv_nama = findViewById(R.id.tv_nama);
        tv_nomorHp = findViewById(R.id.tv_nomorHp);
        tv_jurusan = findViewById(R.id.tv_jurusan);
        tv_kelas = findViewById(R.id.tv_kelas);
        tv_ortu = findViewById(R.id.tv_ortu);
        tv_terlambat = findViewById(R.id.tv_terlambat);
        tv_keterangan = findViewById(R.id.tv_keterangan);
        btn_bagikan = findViewById(R.id.btn_bagikan);
        tv_dibuat_oleh = findViewById(R.id.tv_dibuat_oleh);
        tv_dibuat_pada = findViewById(R.id.tv_dibuat_pada);
        tv_diperbarui_oleh = findViewById(R.id.tv_diperbarui_oleh);
        tv_diperbarui_pada = findViewById(R.id.tv_diperbarui_pada);

        editDataLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        loadData(id, getIntent().getStringExtra("index"));
                        setResult(RESULT_OK);
                    }
                }
        );

        PreferenceManager pref = new PreferenceManager(this);
        JsonObject user = pref.getUserData();

        id = getIntent().getStringExtra("id");
        String index = getIntent().getStringExtra("index");
        loadData(id, index);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        finish();
    }

    private void loadData(String id, String index) {
        if (NetworkUtil.isConnected(this)) {
            ApiService apiService = ApiClient.getClient(DetailKeterlambatanSiswaActivity.this).create(ApiService.class);
            apiService.keterlambatanShow(id).enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        JsonObject json = response.body();

                        String status = json.has("status") ? json.get("status").getAsString() : "";

                        if (status.equals("success")) {
                            JsonObject data = json.has("data") ? json.get("data").getAsJsonObject() : null;

                            if (isFinishing() || isDestroyed()) return;

                            if (data.has("siswa") && !data.get("siswa").isJsonNull() && data.get("siswa").isJsonObject()) {
                                JsonObject siswa = data.get("siswa").getAsJsonObject();

                                if (siswa.has("jurusan") && !siswa.get("jurusan").isJsonNull() && siswa.get("jurusan").isJsonObject()) {
                                    JsonObject jurusan = siswa.get("jurusan").getAsJsonObject();
                                    String namaJ = (jurusan.has("nama") && !jurusan.get("nama").isJsonNull()) ? jurusan.get("nama").getAsString().trim() : "";
                                    String alias = (jurusan.has("alias") && !jurusan.get("alias").isJsonNull()) ? jurusan.get("alias").getAsString().trim() : "";
                                    if (!namaJ.isEmpty() && !alias.isEmpty()) tv_jurusan.setText(namaJ + " (" + alias + ")");
                                    else if (!namaJ.isEmpty()) tv_jurusan.setText(namaJ);
                                    else if (!alias.isEmpty()) tv_jurusan.setText(alias);
                                    else tv_jurusan.setText("-");
                                } else {
                                    tv_jurusan.setText("-");
                                }

                                if (siswa.has("orangtua") && !siswa.get("orangtua").isJsonNull() && siswa.get("orangtua").isJsonObject()) {
                                    JsonObject orangtua = siswa.get("orangtua").getAsJsonObject();
                                    String namaOrtu = (orangtua.has("nama") && !orangtua.get("nama").isJsonNull()) ? orangtua.get("nama").getAsString().trim() : "";
                                    String userOrtu = (orangtua.has("username") && !orangtua.get("username").isJsonNull()) ? orangtua.get("username").getAsString().trim() : "";
                                    if (!namaOrtu.isEmpty() && !userOrtu.isEmpty()) tv_ortu.setText(namaOrtu + " (" + userOrtu + ")");
                                    else if (!namaOrtu.isEmpty()) tv_ortu.setText(namaOrtu);
                                    else if (!userOrtu.isEmpty()) tv_ortu.setText(userOrtu);
                                    else tv_ortu.setText("-");
                                } else {
                                    tv_ortu.setText("-");
                                }

                                String nis = (siswa.has("nis") && !siswa.get("nis").isJsonNull()) ? siswa.get("nis").getAsString().trim() : "";
                                tv_nis.setText(!nis.isEmpty() && !"null".equalsIgnoreCase(nis) ? nis : "-");

                                String nama = (siswa.has("nama") && !siswa.get("nama").isJsonNull()) ? siswa.get("nama").getAsString().trim() : "";
                                tv_nama.setText(!nama.isEmpty() && !"null".equalsIgnoreCase(nama) ? nama : "-");

                                String nomorHp = (siswa.has("nomor_hp") && !siswa.get("nomor_hp").isJsonNull()) ? siswa.get("nomor_hp").getAsString().trim() : "";
                                tv_nomorHp.setText(!nomorHp.isEmpty() && !"null".equalsIgnoreCase(nomorHp) ? nomorHp : "-");

                                String kelas = (siswa.has("kelas") && !siswa.get("kelas").isJsonNull()) ? siswa.get("kelas").getAsString().trim() : "";
                                tv_kelas.setText(!kelas.isEmpty() && !"null".equalsIgnoreCase(kelas) ? kelas : "-");
                            } else {
                                tv_ortu.setText("-");
                                tv_jurusan.setText("-");
                                tv_nis.setText("-");
                                tv_nama.setText("-");
                                tv_nomorHp.setText("-");
                                tv_kelas.setText("-");
                            }

                            tv_no.setText(index != null && !index.trim().isEmpty() && !"null".equalsIgnoreCase(index.trim()) ? index.trim() : "-");
                            String ket = (data.has("keterangan") && !data.get("keterangan").isJsonNull()) ? data.get("keterangan").getAsString().trim() : "";
                            tv_keterangan.setText(!ket.isEmpty() && !"null".equalsIgnoreCase(ket) ? ket : "-");

                            String dateIso = (data.has("date") && !data.get("date").isJsonNull()) ? data.get("date").getAsString() : null;
                            tv_terlambat.setText(Helpers.formatDateTimeCombined(dateIso));

                            // Audit trail
                            tv_dibuat_oleh.setText(Helpers.formatUserAudit(data.has("created_by") && !data.get("created_by").isJsonNull() ? data.getAsJsonObject("created_by") : null));
                            tv_dibuat_pada.setText(Helpers.formatDateTimeCombined(data.has("created_at") && !data.get("created_at").isJsonNull() ? data.get("created_at").getAsString() : null));

                            boolean hasUpdatedBy = data.has("updated_by") && !data.get("updated_by").isJsonNull();
                            tv_diperbarui_oleh.setText(hasUpdatedBy ? Helpers.formatUserAudit(data.getAsJsonObject("updated_by")) : "-");
                            tv_diperbarui_pada.setText(hasUpdatedBy && data.has("updated_at") && !data.get("updated_at").isJsonNull() ? Helpers.formatDateTimeCombined(data.get("updated_at").getAsString()) : "-");

                            if (data.has("gambar") && !data.get("gambar").isJsonNull()) {
                                String gambar = data.get("gambar").getAsString();

                                if (!gambar.startsWith("http")) {
                                    gambar = ApiClient.base_url + gambar;
                                }

                                iv_gambar.setImageTintList(null);

                                Glide.with(DetailKeterlambatanSiswaActivity.this)
                                        .load(gambar)
                                        .placeholder(R.drawable.ic_image_24)
                                        .error(R.drawable.ic_image_24)
                                        .into(iv_gambar);

                    final String previewGambar = gambar;
                    iv_gambar.setOnClickListener(v -> Helpers.showImagePreview(DetailKeterlambatanSiswaActivity.this, previewGambar));
                            } else {
                                iv_gambar.setImageResource(R.drawable.ic_image_24);
                                iv_gambar.setImageTintList(
                                        ContextCompat.getColorStateList(DetailKeterlambatanSiswaActivity.this, android.R.color.darker_gray)
                                );
                            }

                            btn_bagikan.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if (checkPermission()) {
                                        Drawable drawable = iv_gambar.getDrawable();
                                        Bitmap studentImage = null;
                                        if (drawable instanceof BitmapDrawable) {
                                            studentImage = ((BitmapDrawable) drawable).getBitmap();
                                        }

                                        String dataNo = tv_no.getText().toString();
                                        String dataNis = tv_nis.getText().toString();
                                        String dataNama = tv_nama.getText().toString();
                                        String dataHP = tv_nomorHp.getText().toString();
                                        String dataJurusan = tv_jurusan.getText().toString();
                                        String dataKelas = tv_kelas.getText().toString();
                                        String dataOrtu = tv_ortu.getText().toString();
                                        String dataTerlambat = tv_terlambat.getText().toString();
                                        String dataKeterangan = tv_keterangan.getText().toString();

                                        File pdf = createPdf(studentImage, dataNo, dataNis, dataNama, dataHP, dataJurusan, dataKelas, dataOrtu, dataTerlambat, dataKeterangan);
                                        if (pdf != null) {
                                            sharePdfViaWhatsapp(pdf);
                                        }
                                    } else {
                                        requestPermission();
                                    }
                                }
                            });

                            ll_progress.setVisibility(View.GONE);
                            sv_main.setVisibility(View.VISIBLE);
                            isDataLoaded = true;
                            invalidateOptionsMenu();
                        } else {
                            Toast.makeText(DetailKeterlambatanSiswaActivity.this, "Tidak dapat memuat data. Coba lagi nanti.", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } else {
                        Toast.makeText(DetailKeterlambatanSiswaActivity.this, "Tidak dapat memuat data. Coba lagi nanti.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Toast.makeText(DetailKeterlambatanSiswaActivity.this, "Terjadi kesalahan: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    finish();
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

    private boolean checkPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            return true;
        } else {
            int result = ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
            return result == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestPermission() {
        if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.P) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Izin diberikan, coba lagi Bagikan.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Izin penyimpanan ditolak. Tidak bisa membuat PDF.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private File createPdf(Bitmap image, String dataNo, String dataNis, String dataNama, String dataHP, String dataJurusan, String dataKelas, String dataOrtu, String dataTerlambat, String dataKeterangan) {

        String[] labels = new String[]{"No", "NIS", "Nama", "Nomor HP", "Jurusan", "Kelas", "Orangtua", "Terlambat Pada"};
        String[] data = new String[]{dataNo, dataNis, dataNama, dataHP, dataJurusan, dataKelas, dataOrtu, dataTerlambat};

        int pageWidth = 595;
        int marginX = 50;
        int startY = 50;
        int lineSpacing = 40;

        int calculatedHeight = startY;

        int maxImageWidth = pageWidth - (2 * marginX);
        int finalImageWidth = 200;
        int finalImageHeight = 200;

        if (image != null) {
            float aspectRatio = (float) image.getHeight() / image.getWidth();
            finalImageWidth = Math.min(image.getWidth(), 400);
            if (finalImageWidth > maxImageWidth) finalImageWidth = maxImageWidth;

            finalImageHeight = (int) (finalImageWidth * aspectRatio);
            int maxHeightLimit = 600;
            if (finalImageHeight > maxHeightLimit) {
                finalImageHeight = maxHeightLimit;
                finalImageWidth = (int) (finalImageHeight / aspectRatio);
            }
        }
        calculatedHeight += finalImageHeight + 60;

        calculatedHeight += (labels.length * lineSpacing);

        calculatedHeight += 40;
        int maxCharsPerLine = 70;
        List<String> wrappedKeterangan = new ArrayList<>();
        if (dataKeterangan.length() > maxCharsPerLine) {
            String[] words = dataKeterangan.split(" ");
            StringBuilder line = new StringBuilder();
            for (String word : words) {
                if (line.length() + word.length() + 1 > maxCharsPerLine) {
                    wrappedKeterangan.add(line.toString());
                    line = new StringBuilder(word + " ");
                } else {
                    line.append(word).append(" ");
                }
            }
            wrappedKeterangan.add(line.toString());
        } else {
            wrappedKeterangan.add(dataKeterangan);
        }
        calculatedHeight += (wrappedKeterangan.size() * 20);

        calculatedHeight += 50;
        calculatedHeight += 100;

        int pageHeight = Math.max(calculatedHeight, 842);

        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        Paint paint = new Paint();
        paint.setColor(Color.BLACK);

        int currentY = startY;

        int imageLeft = (pageWidth - finalImageWidth) / 2;

        if (image != null) {
            Bitmap scaledImage = Bitmap.createScaledBitmap(image, finalImageWidth, finalImageHeight, true);
            canvas.drawBitmap(scaledImage, imageLeft, currentY, paint);
        } else {
            Paint placeholderPaint = new Paint();
            placeholderPaint.setColor(Color.LTGRAY);
            canvas.drawRect(imageLeft, currentY, imageLeft + finalImageWidth, currentY + finalImageHeight, placeholderPaint);

            paint.setTextSize(12f);
            paint.setColor(Color.WHITE);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("Gambar Tidak Tersedia", imageLeft + finalImageWidth / 2, currentY + finalImageHeight / 2 + 5, paint);
            paint.setTextAlign(Paint.Align.LEFT);
            paint.setColor(Color.BLACK);
        }

        currentY += finalImageHeight + 60;

        Paint dividerPaint = new Paint();
        dividerPaint.setColor(Color.DKGRAY);
        dividerPaint.setStrokeWidth(2);
        canvas.drawLine(marginX, currentY - 30, pageWidth - marginX, currentY - 30, dividerPaint);

        paint.setTextSize(14f);
        int labelX = marginX;

        for (int i = 0; i < labels.length; i++) {
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
            canvas.drawText(labels[i], labelX, currentY, paint);

            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            paint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText(data[i], pageWidth - marginX, currentY, paint);
            paint.setTextAlign(Paint.Align.LEFT);

            currentY += lineSpacing;
        }

        currentY += 20;
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("Keterangan:", labelX, currentY, paint);
        currentY += 20;

        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        paint.setTextSize(12f);

        for (String line : wrappedKeterangan) {
            canvas.drawText(line, labelX, currentY, paint);
            currentY += 20;
        }

        currentY += 30;
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.ITALIC));
        paint.setTextSize(10f);
        paint.setColor(Color.DKGRAY);
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("Sistem Monitoring Siswa - SMK Negeri 1 Kota Gorontalo", pageWidth - marginX, currentY, paint);

        document.finishPage(page);

        File pdfFile = null;
        try {
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (!downloadsDir.exists()) downloadsDir.mkdirs();

            pdfFile = new File(downloadsDir, "Laporan_Keterlambatan_" + System.currentTimeMillis() + ".pdf");
            FileOutputStream fos = new FileOutputStream(pdfFile);
            document.writeTo(fos);
            fos.close();
            Toast.makeText(this, "PDF berhasil dibuat di folder Downloads.", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Gagal membuat PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            document.close();
        }
        return pdfFile;
    }
    private void sharePdfViaWhatsapp(File pdfFile) {
        if (pdfFile == null || !pdfFile.exists()) {
            Toast.makeText(this, "File PDF tidak ditemukan.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Uri pdfUri = FileProvider.getUriForFile(
                    this,
                    getApplicationContext().getPackageName() + ".provider",
                    pdfFile);

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/pdf");
            shareIntent.putExtra(Intent.EXTRA_STREAM, pdfUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            shareIntent.setPackage("com.whatsapp");

            try {
                startActivity(shareIntent);
            } catch (android.content.ActivityNotFoundException ex) {
                shareIntent.setPackage(null);
                startActivity(Intent.createChooser(shareIntent, "Bagikan Laporan Siswa via"));
            }

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error FileProvider: Pastikan konfigurasi FileProvider sudah benar.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void finish() {
        super.finish();
        Helpers.overridePendingTransition_Exit_v1(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!isDataLoaded) {
            return true;
        }
        PreferenceManager pref = new PreferenceManager(this);
        JsonObject user = pref.getUserData();
        String role = user != null && user.has("role") ? user.get("role").getAsString() : "";
        if (role.equals("superadmin") || role.equals("kesiswaan") || role.equals("guru_piket") || role.equals("piket") || role.equals("bimbingan_konseling")) {
            getMenuInflater().inflate(R.menu.menu_top_nav_detail_edit_delete, menu);
            for (int i = 0; i < menu.size(); i++) {
                Drawable icon = menu.getItem(i).getIcon();
                if (icon != null) {
                    icon.setTint(ContextCompat.getColor(this, R.color.white));
                }
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.nav_edit) {
            Intent editIntent = new Intent(this, EditKeterlambatanSiswaActivity.class);
            editIntent.putExtra("id", id);
            editDataLauncher.launch(editIntent);
            Helpers.overridePendingTransition_Enter_v1(this);
            return true;
        }

        if (item.getItemId() == R.id.nav_delete) {
            Helpers.showConfirm(this, "Konfirmasi", "Apakah Anda yakin ingin menghapus data keterlambatan ini?", (dialogInterface, i) -> {
            dialogInterface.dismiss();
            deleteKeterlambatan();
        });
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteKeterlambatan() {
        if (!NetworkUtil.isConnected(this)) {
            Toast.makeText(this, "Tidak ada koneksi internet", Toast.LENGTH_SHORT).show();
            return;
        }
        Helpers.LoadingHelper loading = new Helpers.LoadingHelper(this);
        loading.show();
        ApiService apiService = ApiClient.getClient(this).create(ApiService.class);
        apiService.keterlambatanDelete(id).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                loading.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(DetailKeterlambatanSiswaActivity.this, "Data keterlambatan berhasil dihapus", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    String msg = "Gagal menghapus data keterlambatan";
                    try {
                        if (response.errorBody() != null) {
                            JsonObject err = new com.google.gson.JsonParser().parse(response.errorBody().string()).getAsJsonObject();
                            if (err.has("message")) msg = err.get("message").getAsString();
                        }
                    } catch (Exception ignored) {}
                    Toast.makeText(DetailKeterlambatanSiswaActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                loading.dismiss();
                Toast.makeText(DetailKeterlambatanSiswaActivity.this, "Terjadi kesalahan: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
