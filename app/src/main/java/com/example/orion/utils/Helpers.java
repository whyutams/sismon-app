package com.example.orion.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
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
        try {
            // Parsing string ISO 8601 (Laravel timestamps)
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            inputFormat.setLenient(false);
            inputFormat.setTimeZone(java.util.TimeZone.getTimeZone("UTC")); // karena ada Z (UTC)

            Date date = inputFormat.parse(isoString);

            SimpleDateFormat outputFormat = new SimpleDateFormat((format == 0 ? "EEEE, d MMMM yyyy" : "HH:mm"), new Locale("id", "ID"));
            outputFormat.setTimeZone(java.util.TimeZone.getDefault());

            return outputFormat.format(date);

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


}
