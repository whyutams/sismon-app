package com.example.orion.fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.orion.R;
import com.example.orion.activity.MainActivity;
import com.example.orion.activity.UbahPasswordActivity;
import com.example.orion.activity.WelcomeActivity;
import com.example.orion.utils.Helpers;
import com.example.orion.utils.PreferenceManager;
import com.example.orion.utils.api.ApiClient;
import com.example.orion.utils.api.ApiService;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.gson.JsonObject;

public class ProfileFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public ProfileFragment() {
    }

    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    private TextView top_tv_nama, top_tv_jurusan, top_tv_jurusan_alias, tv_role, tv_username, tv_nip, tv_jurusan, tv_nama, tv_email, tv_nomor_hp, tv_alamat;
    private LinearLayout ll_bio, ll_top;
    private ShapeableImageView top_siv_profile;
    private ShimmerFrameLayout shimmer1, shimmer2;
    private ApiService apiService;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        apiService = ApiClient.getClient(requireContext()).create(ApiService.class);

        shimmer1 = view.findViewById(R.id.shimmer);
        shimmer2 = view.findViewById(R.id.shimmer2);
        shimmer1.startShimmer();
        shimmer2.startShimmer();

        ll_bio = view.findViewById(R.id.ll_bio);
        ll_top = view.findViewById(R.id.ll_top);

        top_tv_nama = view.findViewById(R.id.top_tv_nama);
        top_tv_jurusan = view.findViewById(R.id.top_tv_jurusan);
        top_tv_jurusan_alias = view.findViewById(R.id.top_tv_jurusan_alias);
        top_siv_profile = view.findViewById(R.id.top_imgProfile);
        tv_role = view.findViewById(R.id.tv_role);
        tv_username = view.findViewById(R.id.tv_username);
        tv_nip = view.findViewById(R.id.tv_nip);
        tv_jurusan = view.findViewById(R.id.tv_jurusan);
        tv_nama = view.findViewById(R.id.tv_nama);
        tv_email = view.findViewById(R.id.tv_email);
        tv_nomor_hp = view.findViewById(R.id.tv_nomorHp);
        tv_alamat = view.findViewById(R.id.tv_alamat);

        PreferenceManager pref = new PreferenceManager(requireContext());
        JsonObject user = pref.getUserData();

        Button btn_keluar = view.findViewById(R.id.btn_logout);
        Button btn_edit_profil = view.findViewById(R.id.btn_editProfile);

        MaterialToolbar toolbar = view.findViewById(R.id.toolbar_profile);
        if (toolbar != null) {
            toolbar.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.nav_about) {
                    showDialogTentang();
                    return true;
                } else if (item.getItemId() == R.id.nav_logout) {
                    Helpers.showConfirm(requireActivity(), "Konfirmasi", "Apakah Anda yakin ingin keluar?", (dialogInterface, i) -> {
                        Intent intent = new Intent(requireContext(), WelcomeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        Helpers.deleteDeviceTokenIfAny(requireContext());
                        pref.clear();
                        startActivity(intent);
                        Helpers.overridePendingTransition_Enter_v1(requireActivity());
                        requireActivity().finish();
                    });
                }
                return false;
            });
        }

        top_siv_profile.setOnClickListener(v -> {
            Dialog dialog = new Dialog(requireContext());
            dialog.setContentView(R.layout.dialog_foto);
            dialog.setCancelable(true);
            ImageView popupFoto = dialog.findViewById(R.id.popupfoto);

            if (user.has("foto_profil") && !user.get("foto_profil").isJsonNull()) {
                String fotoUrl = user.get("foto_profil").getAsString();
                if (!fotoUrl.startsWith("http")) {
                    fotoUrl = ApiClient.base_url + fotoUrl;
                }

                popupFoto.setImageTintList(null);

                if (dialog.getWindow() != null) {
                    dialog.getWindow().setLayout(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    );
                    dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                }

                Glide.with(requireContext())
                        .load(fotoUrl)
                        .placeholder(R.drawable.ic_account_circle_24)
                        .error(R.drawable.ic_account_circle_24)
                        .into(popupFoto);

                dialog.show();
            } else {
                popupFoto.setImageResource(R.drawable.ic_account_circle_24);
                popupFoto.setImageTintList(
                        ContextCompat.getColorStateList(requireContext(), R.color.primary)
                );

                Toast.makeText(requireContext(), "Foto profil tidak ada", Toast.LENGTH_SHORT).show();
            }

        });

        btn_keluar.setOnClickListener(view1 -> {
            Helpers.showConfirm(requireActivity(), "Konfirmasi", "Apakah Anda yakin ingin keluar?", (dialogInterface, i) -> {
                Intent intent = new Intent(requireContext(), WelcomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                Helpers.deleteDeviceTokenIfAny(requireContext());
                        pref.clear();
                startActivity(intent);
                Helpers.overridePendingTransition_Enter_v1(requireActivity());
                requireActivity().finish();
            });
        });

        btn_edit_profil.setOnClickListener(view12 -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openEditProfile();
            }
        });

        Button btn_changePassword = view.findViewById(R.id.btn_changePassword);

        btn_changePassword.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), UbahPasswordActivity.class);
            startActivity(intent);
            Helpers.overridePendingTransition_Enter_v1(requireActivity());
        });

        return view;
    }

    public void refreshUserData() {
        ll_top.setVisibility(View.GONE);
        shimmer1.setVisibility(View.VISIBLE);
        shimmer1.startShimmer();
        ll_bio.setVisibility(View.GONE);
        shimmer2.setVisibility(View.VISIBLE);
        shimmer2.startShimmer();

        Helpers.delay(1000, this::removeShimmers);
    }

    public void removeShimmers() {
        if (!isAdded()) return;

        PreferenceManager pref = new PreferenceManager(requireContext());
        JsonObject user = pref.getUserData();

        if (user != null) {
            String role = user.get("role").getAsString();
            String jurusan_alias = "";
            JsonObject jurusan;

            if (user.has("foto_profil") && !user.get("foto_profil").isJsonNull()) {
                String fotoUrl = user.get("foto_profil").getAsString();
                if (!fotoUrl.startsWith("http")) {
                    fotoUrl = ApiClient.base_url + fotoUrl;
                }
                top_siv_profile.setImageTintList(null);
                Glide.with(requireContext())
                        .load(fotoUrl)
                        .placeholder(R.drawable.ic_account_circle_24)
                        .error(R.drawable.ic_account_circle_24)
                        .into(top_siv_profile);
            } else {
                top_siv_profile.setImageResource(R.drawable.ic_account_circle_24);
                top_siv_profile.setImageTintList(
                        ContextCompat.getColorStateList(requireContext(), R.color.primary)
                );
            }

            if (user.has("jurusan") && !user.get("jurusan").isJsonNull()) {
                jurusan = user.get("jurusan").getAsJsonObject();
                if (jurusan.has("alias") && !jurusan.get("alias").isJsonNull()) {
                    jurusan_alias = jurusan.get("alias").getAsString();
                }
                top_tv_jurusan_alias.setText((!jurusan_alias.isEmpty() ? "Guru " + jurusan_alias : "-"));
                if (jurusan.has("nama")) {
                    top_tv_jurusan.setText(jurusan.get("nama").getAsString());
                    tv_jurusan.setText(jurusan.get("nama").getAsString());
                    top_tv_jurusan.setVisibility(View.VISIBLE);
                } else {
                    top_tv_jurusan.setVisibility(View.GONE);
                    top_tv_jurusan.setText("");
                    tv_jurusan.setText("-");
                }
            } else {
                top_tv_jurusan_alias.setText(Helpers.formatRole(role));
                top_tv_jurusan.setVisibility(View.GONE);
                top_tv_jurusan.setText("");
                tv_jurusan.setText("-");
            }

            top_tv_nama.setText(user.get("nama").getAsString());
            tv_role.setText(Helpers.formatRole(role));
            tv_username.setText(user.get("username").getAsString());
            tv_nip.setText((user.has("nip") && !user.get("nip").isJsonNull() ? user.get("nip").getAsString() : "-"));
            tv_nama.setText(user.get("nama").getAsString());
            tv_email.setText((user.has("email") && !user.get("email").isJsonNull() ? user.get("email").getAsString() : "-"));
            tv_nomor_hp.setText((user.has("nomor_hp") && !user.get("nomor_hp").isJsonNull() ? user.get("nomor_hp").getAsString() : "-"));
            tv_alamat.setText((user.has("alamat") && !user.get("alamat").isJsonNull() ? user.get("alamat").getAsString() : "-"));

            ll_top.setVisibility(View.VISIBLE);
            shimmer1.setVisibility(View.GONE);
            shimmer1.stopShimmer();
            ll_bio.setVisibility(View.VISIBLE);
            shimmer2.setVisibility(View.GONE);
            shimmer2.stopShimmer();
        }
    }

    private void showDialogTentang() {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_tentang);
        dialog.setCancelable(true);

        Button btnOK = dialog.findViewById(R.id.btnOK);
        btnOK.setOnClickListener(v -> dialog.dismiss());

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        dialog.show();
    }
}
