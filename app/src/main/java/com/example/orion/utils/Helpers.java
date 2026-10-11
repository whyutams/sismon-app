package com.example.orion.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.app.Dialog;
import android.widget.ImageView;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;
import android.graphics.Bitmap;
import java.io.File;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Looper;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.orion.R;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Helpers {
    public static boolean validateRequired(final TextInputLayout til) {
        EditText et = til.getEditText();
        if (et == null) return false;

        String input = et.getText().toString().trim();
        if (input.isEmpty()) {
            til.setError(til.getHint() + " belum diisi!");
            til.setErrorEnabled(true);

            et.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    til.setErrorEnabled(false);
                }

                @Override
                public void afterTextChanged(Editable editable) {
                }
            });
            return false;
        } else {
            til.setErrorEnabled(false);
            return true;
        }
    }

    public static void delay(long delay, Runnable task) {
        new Handler(Looper.getMainLooper()).postDelayed(task, delay);
    }

    public static class LoadingHelper {
        private AlertDialog dialog;

        public LoadingHelper(Activity activity) {
            LinearLayout layout = new LinearLayout(activity);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setGravity(Gravity.CENTER);
            int padding = (int) (30 * activity.getResources().getDisplayMetrics().density);
            layout.setPadding(padding, padding, padding, padding);

            GradientDrawable bg = new GradientDrawable();
            bg.setCornerRadius(16 * activity.getResources().getDisplayMetrics().density);
            bg.setColor(activity.getColor(android.R.color.transparent)); // Warna hitam transparan
            layout.setBackground(bg);

            ProgressBar progressBar = new ProgressBar(activity);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );

            layout.addView(progressBar, params);

            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setView(layout);
            builder.setCancelable(false);
            dialog = builder.create();

            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
        }

        public void show() {
            if (!dialog.isShowing()) dialog.show();
        }

        public void dismiss() {
            if (dialog.isShowing()) dialog.dismiss();
        }
    }

    public static void showAlert(Activity activity, String title, String message, String positive_title, DialogInterface.OnClickListener onClickListener) {
        if (activity == null || activity.isFinishing()) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        if (title != null && !title.isEmpty()) {
            builder.setTitle(title);
        }

        builder.setMessage(message);
        builder.setCancelable(false);
        builder.setPositiveButton(positive_title, onClickListener);

        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.WHITE);
    }

    public static void showConfirm(Activity activity, String title, String message, DialogInterface.OnClickListener onClickListener) {
        if (activity == null || activity.isFinishing()) return;

        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_confirm, null);

        TextView tvTitle = view.findViewById(R.id.tvTitle);
        TextView tvMessage = view.findViewById(R.id.tvMessage);
        Button btnPositive = view.findViewById(R.id.btnPositive);
        Button btnNegative = view.findViewById(R.id.btnNegative);

        if (title != null && !title.isEmpty()) {
            tvTitle.setText(title);
        } else {
            tvTitle.setVisibility(View.GONE);
        }

        tvMessage.setText(message);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(view);
        builder.setCancelable(false);

        AlertDialog dialog = builder.create();

        btnPositive.setOnClickListener(v -> {
            onClickListener.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
        });
        btnNegative.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    public static String getRealPathFromURI(Context context, Uri uri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(uri, proj, null, null, null);
        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(column_index);
            cursor.close();
            return path;
        }
        return uri.getPath();
    }

    public static String formatRole(String role) {
        if (role == null || role.trim().isEmpty()) {
            return "-";
        }

        role = role.trim();

        if (role.equalsIgnoreCase("kepsek")) {
            return "Kepala Sekolah";
        }

        String[] parts = role.split("[ _]+");
        StringBuilder formattedRole = new StringBuilder();

        for (String part : parts) {
            if (!part.isEmpty()) {
                formattedRole.append(
                        part.substring(0, 1).toUpperCase()
                ).append(
                        part.substring(1).toLowerCase()
                ).append(" ");
            }
        }

        return formattedRole.toString().trim();
    }

    public static String formatCreatedAt(String isoString, int format) {
        if (isoString == null || isoString.trim().isEmpty()) {
            return "-";
        }

        try {
            SimpleDateFormat outputFormat = new SimpleDateFormat(
                    (format == 0 ? "EEEE, d MMMM yyyy" : "HH:mm"),
                    new Locale("id", "ID")
            );

            // Jika string merupakan ISO 8601 UTC (ada 'T' atau diakhiri 'Z')
            if (isoString.contains("T") || isoString.endsWith("Z")) {
                SimpleDateFormat inputFormat;
                if (isoString.contains(".")) {
                    inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault());
                } else {
                    inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
                }
                inputFormat.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                outputFormat.setTimeZone(java.util.TimeZone.getDefault());

                Date date = inputFormat.parse(isoString);
                return outputFormat.format(date);
            } else {
                // String date biasa dari MySQL (misal: "2026-10-11 08:00:00") yang sudah merupakan waktu lokal
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                inputFormat.setLenient(false);

                Date date = inputFormat.parse(isoString);
                return outputFormat.format(date);
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return isoString;
        }
    }

    public static File copyUriToCache(Context context, Uri uri) throws IOException {
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        File cacheFile = new File(context.getCacheDir(), "temp_" + System.currentTimeMillis() + ".jpg");
        OutputStream outputStream = new FileOutputStream(cacheFile);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }
        outputStream.close();
        inputStream.close();
        return cacheFile;
    }

    public static void animateZoomIn_v1(View fab) {
        fab.setScaleX(0f);
        fab.setScaleY(0f);
        fab.setVisibility(View.VISIBLE);

        fab.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(300)
                .setInterpolator(new OvershootInterpolator())
                .start();
    }

    public static void animateZoomOut_v1(final View fab) {
        fab.animate()
                .scaleX(0f)
                .scaleY(0f)
                .setDuration(300)
                .setInterpolator(new OvershootInterpolator())
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        fab.setVisibility(View.GONE);
                        fab.setScaleX(1f);
                        fab.setScaleY(1f);
                    }
                })
                .start();
    }

    public static void overridePendingTransition_Enter_v1(Activity activity) {
        activity.overridePendingTransition(R.anim.activity_enter, R.anim.activity_none);
    }

    public static void overridePendingTransition_Exit_v1(Activity activity) {
        activity.overridePendingTransition(R.anim.activity_none, R.anim.activity_exit);
    }

    public static String konversiKeRomawi(int x) {
        int[] base = {1, 4, 5, 9, 10, 40, 50, 90, 100, 400, 500, 900, 1000};
        String[] sym = {"I", "IV", "V", "IX", "X", "XL", "L", "XC", "C", "CD", "D", "CM", "M"};

        StringBuilder res = new StringBuilder();

        int i = base.length - 1;
        while (x > 0) {
            int div = x / base[i];
            while (div > 0) {
                res.append(sym[i]);
                div--;
            }

            x = x % base[i];
            i--;
        }

        return res.toString();
    }

    public static void hideKeyboard(Activity activity) {
        View view = activity.getCurrentFocus();
        if (view != null) {
            android.view.inputmethod.InputMethodManager imm =
                    (android.view.inputmethod.InputMethodManager) activity.getSystemService(activity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static void tintToolbarButtons(androidx.appcompat.widget.Toolbar toolbar) {
        if (toolbar == null) return;
        toolbar.post(() -> {
            for (int i = 0; i < toolbar.getChildCount(); i++) {
                View child = toolbar.getChildAt(i);
                if (child instanceof android.widget.ImageButton) {
                    android.widget.ImageButton btn = (android.widget.ImageButton) child;
                    btn.setColorFilter(Color.WHITE, android.graphics.PorterDuff.Mode.SRC_IN);
                    android.graphics.drawable.Drawable d = btn.getDrawable();
                    if (d != null) {
                        d.setColorFilter(new android.graphics.PorterDuffColorFilter(Color.WHITE, android.graphics.PorterDuff.Mode.SRC_IN));
                    }
                }
            }
        });
    }

    public static void deleteDeviceTokenIfAny(Context context) {
        if (context == null) return;
        try {
            PreferenceManager pref = new PreferenceManager(context);
            String fcmToken = pref.getFcmToken();
            if (fcmToken != null && !fcmToken.isEmpty() && pref.getToken() != null) {
                com.google.gson.JsonObject payload = new com.google.gson.JsonObject();
                payload.addProperty("fcm_token", fcmToken);

                com.example.orion.utils.api.ApiService apiService =
                        com.example.orion.utils.api.ApiClient.getClient(context).create(com.example.orion.utils.api.ApiService.class);
                apiService.deleteDeviceToken(payload).enqueue(new retrofit2.Callback<com.google.gson.JsonObject>() {
                    @Override
                    public void onResponse(retrofit2.Call<com.google.gson.JsonObject> call, retrofit2.Response<com.google.gson.JsonObject> response) {}

                    @Override
                    public void onFailure(retrofit2.Call<com.google.gson.JsonObject> call, Throwable t) {}
                });
            }
        } catch (Exception ignored) {}
    }

    public static void showImagePreview(Context context, Object imageSource) {
        if (context == null || imageSource == null) return;
        try {
            Dialog dialog = new Dialog(context);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_foto);
            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(true);

            if (dialog.getWindow() != null) {
                dialog.getWindow().setLayout(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                );
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.getWindow().getAttributes().windowAnimations = android.R.style.Animation_Dialog;
            }

            View root = dialog.findViewById(R.id.dialog_root);
            ImageView popupFoto = dialog.findViewById(R.id.popupfoto);

            // Close on clicking anywhere: background or photo itself
            if (root != null) root.setOnClickListener(v -> dialog.dismiss());
            if (popupFoto != null) popupFoto.setOnClickListener(v -> dialog.dismiss());

            if (popupFoto != null) {
                popupFoto.setImageTintList(null);
                if (imageSource instanceof String) {
                    String url = (String) imageSource;
                    if (!url.startsWith("http") && !url.startsWith("content:") && !url.startsWith("file:")) {
                        url = com.example.orion.utils.api.ApiClient.base_url + url;
                    }
                    com.bumptech.glide.Glide.with(context)
                            .load(url)
                            .placeholder(R.drawable.ic_image_24)
                            .error(R.drawable.ic_image_24)
                            .into(popupFoto);
                } else if (imageSource instanceof android.net.Uri) {
                    com.bumptech.glide.Glide.with(context)
                            .load((android.net.Uri) imageSource)
                            .placeholder(R.drawable.ic_image_24)
                            .error(R.drawable.ic_image_24)
                            .into(popupFoto);
                } else if (imageSource instanceof Bitmap) {
                    popupFoto.setImageBitmap((Bitmap) imageSource);
                } else if (imageSource instanceof File) {
                    com.bumptech.glide.Glide.with(context)
                            .load((File) imageSource)
                            .placeholder(R.drawable.ic_image_24)
                            .error(R.drawable.ic_image_24)
                            .into(popupFoto);
                }
            }

            dialog.show();
        } catch (Exception ignored) {}
    }
}