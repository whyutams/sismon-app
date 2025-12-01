package com.example.orion.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.orion.R;
import com.example.orion.activity.AkunGuruActivity;
import com.example.orion.activity.AkunOrangtuaActivity;
import com.example.orion.activity.AkunSatpamActivity;
import com.example.orion.activity.JurusanTerdaftarActivity;
import com.example.orion.activity.KeterlambatanSiswaActivity;
import com.example.orion.activity.LoginActivity;
import com.example.orion.activity.PengaturanKelasActivity;
import com.example.orion.activity.PerizinanSiswaActivity;
import com.example.orion.activity.SiswaTerdaftarActivity;
import com.example.orion.activity.WelcomeActivity;
import com.example.orion.utils.Helpers;
import com.example.orion.utils.NetworkUtil;
import com.example.orion.utils.PreferenceManager;
import com.example.orion.utils.api.ApiClient;
import com.example.orion.utils.api.ApiService;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;


public class BerandaFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public BerandaFragment() {
    }

    public static BerandaFragment newInstance(String param1, String param2) {
        BerandaFragment fragment = new BerandaFragment();
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

    private TextView tv_user_name, tv_user_jurusan, tv_user_jurusan_alias;
    private ShapeableImageView siv_profile;
    private ShimmerFrameLayout shimmer1, shimmer2;

    private LinearLayout ll_menu, ll_menu1, ll_menu2, ll_menu3, ll_menu4, ll_menu5, ll_menu6, ll_menu7, ll_menu8, ll_count, ll_count1, ll_count2, ll_count3, ll_count4, ll_count_div1, ll_count_div2, ll_count_div3;
    private LinearLayout cardContainer;
    private LinearLayout llProfileInfo;
    private LinearLayout llMenuContainer;

    private ApiService apiService;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_beranda, container, false);

        apiService = ApiClient.getClient(requireContext()).create(ApiService.class);

        shimmer1 = view.findViewById(R.id.shimmer);
        shimmer2 = view.findViewById(R.id.shimmer2);
        shimmer1.startShimmer();
        shimmer2.startShimmer();

        tv_user_name = view.findViewById(R.id.user_name);
        tv_user_jurusan = view.findViewById(R.id.user_jurusan);
        tv_user_jurusan_alias = view.findViewById(R.id.user_jurusan_alias);
        siv_profile = view.findViewById(R.id.imgProfile);

        ll_menu = view.findViewById(R.id.ll_menu);
        ll_menu1 = view.findViewById(R.id.ll_menu1);
        ll_menu2 = view.findViewById(R.id.ll_menu2);
        ll_menu3 = view.findViewById(R.id.ll_menu3);
        ll_menu4 = view.findViewById(R.id.ll_menu4);
        ll_menu5 = view.findViewById(R.id.ll_menu5);
        ll_menu6 = view.findViewById(R.id.ll_menu6);
        ll_menu7 = view.findViewById(R.id.ll_menu7);
        ll_menu8 = view.findViewById(R.id.ll_menu8);

        ll_count = view.findViewById(R.id.ll_count);
        ll_count1 = view.findViewById(R.id.ll_count1);
        ll_count2 = view.findViewById(R.id.ll_count2);
        ll_count3 = view.findViewById(R.id.ll_count3);
        ll_count4 = view.findViewById(R.id.ll_count4);

        ll_count_div1 = view.findViewById(R.id.count_div1);
        ll_count_div2 = view.findViewById(R.id.count_div2);
        ll_count_div3 = view.findViewById(R.id.count_div3);

        cardContainer = view.findViewById(R.id.card_statistik);
        llProfileInfo = view.findViewById(R.id.ll_profile_info);
        llMenuContainer = view.findViewById(R.id.ll_menu_container);

        PreferenceManager pref = new PreferenceManager(requireContext());
        if (pref != null) {
            JsonObject user = pref.getUserData();

            if (user != null) {
                tv_user_name.setText(user.get("nama").getAsString());

                if (user.has("jurusan")) {
                    JsonObject jurusan = user.get("jurusan").getAsJsonObject();
                    tv_user_jurusan_alias.setText(jurusan.has("alias") && !jurusan.get("alias").isJsonNull()
                            ? jurusan.get("alias").getAsString() : "-");
                    tv_user_jurusan.setText(jurusan.has("nama") && !jurusan.get("nama").isJsonNull()
                            ? jurusan.get("nama").getAsString() : "");
                } else {
                    tv_user_jurusan_alias.setText("-");
                    tv_user_jurusan.setText("");
                }

                if (user.has("foto_profil") && !user.get("foto_profil").isJsonNull()) {
                    String fotoUrl = user.get("foto_profil").getAsString();

                    if (!fotoUrl.startsWith("http")) {
                        fotoUrl = ApiClient.base_url + fotoUrl;
                    }

                    siv_profile.setImageTintList(null);

                    Glide.with(requireContext())
                            .load(fotoUrl)
                            .placeholder(R.drawable.ic_account_circle_24)
                            .error(R.drawable.ic_account_circle_24)
                            .into(siv_profile);
                } else {
                    siv_profile.setImageResource(R.drawable.ic_account_circle_24);
                    siv_profile.setImageTintList(
                            ContextCompat.getColorStateList(requireContext(), R.color.white)
                    );
                }
            } else {
                startActivity(new Intent(requireActivity(), LoginActivity.class));
                requireActivity().finish();
            }
        } else {
            startActivity(new Intent(requireActivity(), LoginActivity.class));
            requireActivity().finish();
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        PreferenceManager pref = new PreferenceManager(requireContext());

        if (getActivity() != null) {
            Window window = getActivity().getWindow();
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            );
            window.setStatusBarColor(Color.TRANSPARENT);
        }

        MaterialToolbar toolbar = view.findViewById(R.id.toolbar_beranda);
        AppBarLayout appBarLayout = view.findViewById(R.id.appBarLayout);
        NestedScrollView scrollView = view.findViewById(R.id.nestedScrollView);

        if (toolbar != null) {
            ViewCompat.setOnApplyWindowInsetsListener(toolbar, (v, insets) -> {
                int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top;
                int navBarHeight = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;

                ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) toolbar.getLayoutParams();
                p.topMargin = statusBarHeight + 360;
                toolbar.setLayoutParams(p);
                toolbar.requestLayout();

                if (scrollView != null) {
                    scrollView.setPadding(0, 0, 0, navBarHeight + 300);
                }

                return insets;
            });


            toolbar.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.nav_about) {
                    showDialogTentang();
                    return true;
                } else if (item.getItemId() == R.id.nav_logout) {
                    Helpers.showConfirm(requireActivity(), "Konfirmasi", "Apakah Anda yakin ingin keluar?", (dialogInterface, i) -> {
                        Intent intent = new Intent(requireContext(), WelcomeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        pref.clear();
                        startActivity(intent);
                        Helpers.overridePendingTransition_Enter_v1(requireActivity());
                        requireActivity().finish();
                    });
                }
                return false;
            });

        }

        if (appBarLayout != null) {
            appBarLayout.addOnOffsetChangedListener((appBar, verticalOffset) -> {
                int totalScroll = appBar.getTotalScrollRange();
                float percentage = (float) Math.abs(verticalOffset) / (float) totalScroll;

                if (llProfileInfo != null) {
                    float profileAlpha = 1.0f - (percentage * 2.0f);
                    if (profileAlpha < 0) profileAlpha = 0;
                    llProfileInfo.setAlpha(profileAlpha);
                }

                if (cardContainer != null && llMenuContainer != null) {
                    float startFade = 0.4f;
                    float endFade = 0.8f;
                    float alpha = 1.0f;

                    if (percentage > startFade) {
                        alpha = 1f - ((percentage - startFade) / (endFade - startFade));
                        if (alpha < 0) alpha = 0;
                    }
                    cardContainer.setAlpha(alpha);

                    if (alpha == 0) {
                        cardContainer.setVisibility(View.INVISIBLE);
                    } else {
                        cardContainer.setVisibility(View.VISIBLE);
                    }

                    float cardHeight = cardContainer.getHeight();
                    if (cardHeight > 0) {
                        float translationY = -1 * percentage * (cardHeight * 0.2f);
                        llMenuContainer.setTranslationY(translationY);
                    }
                }
            });
        }
    }

    public void refreshUserData() {
        PreferenceManager pref = new PreferenceManager(requireContext());
        JsonObject user = pref.getUserData();
        if (user != null) {
            tv_user_name.setText(user.get("nama").getAsString());

            if (user.get("jurusan") != null && !user.get("jurusan").isJsonNull()) {
                JsonObject jurusan = user.get("jurusan").getAsJsonObject();
                tv_user_jurusan_alias.setText(jurusan.has("alias") && !jurusan.get("alias").isJsonNull()
                        ? jurusan.get("alias").getAsString() : "-");
                tv_user_jurusan.setText(jurusan.has("nama") && !jurusan.get("nama").isJsonNull()
                        ? jurusan.get("nama").getAsString() : "");
            } else {
                tv_user_jurusan_alias.setText("-");
                tv_user_jurusan.setText("");
            }

            if (user.has("foto_profil") && !user.get("foto_profil").isJsonNull()) {
                String fotoUrl = user.get("foto_profil").getAsString();

                if (!fotoUrl.startsWith("http")) {
                    fotoUrl = ApiClient.base_url + fotoUrl;
                }

                siv_profile.setImageTintList(null);

                Glide.with(requireContext())
                        .load(fotoUrl)
                        .placeholder(R.drawable.ic_account_circle_24)
                        .error(R.drawable.ic_account_circle_24)
                        .into(siv_profile);
            } else {
                siv_profile.setImageResource(R.drawable.ic_account_circle_24);
                siv_profile.setImageTintList(
                        ContextCompat.getColorStateList(requireContext(), R.color.white)
                );
            }
        }
    }

    public void removeShimmers() {
        PreferenceManager pref = new PreferenceManager(requireContext());
        JsonObject user = pref.getUserData();

        if (user != null) {
            String role = user.get("role").getAsString();
            String jurusan_alias = "";

            if (role.equals("guru")) {
                if (user.get("jurusan") != null && !user.get("jurusan").isJsonNull()) {
                    JsonObject jurusan = user.get("jurusan").getAsJsonObject();

                    if (jurusan.has("alias") && !jurusan.get("alias").isJsonNull()) {
                        jurusan_alias = jurusan.get("alias").getAsString();
                    }
                }
            }

            String finalJurusan_alias = jurusan_alias;

            View.OnClickListener clickListener_ll_menu1 = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(requireContext(), KeterlambatanSiswaActivity.class);
                    startActivity(intent);
                    Helpers.overridePendingTransition_Enter_v1(requireActivity());
                }
            };
            View.OnClickListener clickListener_ll_menu2 = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(requireContext(), PerizinanSiswaActivity.class);
                    startActivity(intent);
                    Helpers.overridePendingTransition_Enter_v1(requireActivity());
                }
            };
            View.OnClickListener clickListener_ll_menu3 = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(requireContext(), SiswaTerdaftarActivity.class);
                    startActivity(intent);
                    Helpers.overridePendingTransition_Enter_v1(requireActivity());
                }
            };
            View.OnClickListener clickListener_ll_menu4 = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(requireContext(), PengaturanKelasActivity.class);
                    startActivity(intent);
                    Helpers.overridePendingTransition_Enter_v1(requireActivity());
                }
            };
            View.OnClickListener clickListener_ll_menu5 = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(requireContext(), AkunGuruActivity.class);
                    startActivity(intent);
                    Helpers.overridePendingTransition_Enter_v1(requireActivity());
                }
            };
            View.OnClickListener clickListener_ll_menu6 = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(requireContext(), AkunOrangtuaActivity.class);
                    startActivity(intent);
                    Helpers.overridePendingTransition_Enter_v1(requireActivity());
                }
            };
            View.OnClickListener clickListener_ll_menu7 = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(requireContext(), AkunSatpamActivity.class);
                    startActivity(intent);
                    Helpers.overridePendingTransition_Enter_v1(requireActivity());
                }
            };
            View.OnClickListener clickListener_ll_menu8 = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(requireContext(), JurusanTerdaftarActivity.class);
                    startActivity(intent);
                    Helpers.overridePendingTransition_Enter_v1(requireActivity());
                }
            };

            if (role.equals("superadmin") || role.equals("kepsek")) {
                setMenu(ll_menu1, "Keterlambatan Siswa", R.drawable.ic_access_time_24, clickListener_ll_menu1);
                setMenu(ll_menu2, "Perizinan Siswa", R.drawable.ic_exit_to_app_24, clickListener_ll_menu2);
                setMenu(ll_menu3, "Siswa Terdaftar", R.drawable.ic_groups_24, clickListener_ll_menu3);
                setMenu(ll_menu4, "Pengaturan Kelas", R.drawable.ic_apartment_24, clickListener_ll_menu4);
                setMenu(ll_menu5, "Akun Guru", R.drawable.ic_group_24, clickListener_ll_menu5);
                setMenu(ll_menu6, "Akun Orangtua", R.drawable.ic_group_24, clickListener_ll_menu6);
                setMenu(ll_menu7, "Akun Satpam", R.drawable.ic_group_24, clickListener_ll_menu7);
                setMenu(ll_menu8, "Jurusan Terdaftar", R.drawable.ic_school_24, clickListener_ll_menu8);

                ll_menu.setVisibility(View.VISIBLE);
                shimmer2.setVisibility(View.GONE);
                shimmer2.stopShimmer();
            } else if (role.equals("guru_piket") || role.equals("kesiswaan")) {
                setMenu(ll_menu1, "Keterlambatan Siswa", R.drawable.ic_access_time_24, clickListener_ll_menu1);
                setMenu(ll_menu2, "Perizinan Siswa", R.drawable.ic_exit_to_app_24, clickListener_ll_menu2);
                setMenu(ll_menu3, "Siswa Terdaftar", R.drawable.ic_school_24, clickListener_ll_menu3);

                ll_menu4.setVisibility(View.INVISIBLE);
                ll_menu5.setVisibility(View.GONE);
                ll_menu6.setVisibility(View.GONE);
                ll_menu7.setVisibility(View.GONE);
                ll_menu8.setVisibility(View.GONE);

                ll_menu.setVisibility(View.VISIBLE);
                shimmer2.setVisibility(View.GONE);
                shimmer2.stopShimmer();
            } else if (role.equals("guru")) {
                setMenu(ll_menu1, "Keterlambatan Siswa" + (jurusan_alias.isEmpty() ? "" : " " + jurusan_alias), R.drawable.ic_access_time_24, clickListener_ll_menu1);
                setMenu(ll_menu2, "Perizinan Siswa" + (jurusan_alias.isEmpty() ? "" : " " + jurusan_alias), R.drawable.ic_exit_to_app_24, clickListener_ll_menu2);
                setMenu(ll_menu3, "Siswa" + (jurusan_alias.isEmpty() ? "" : " " + jurusan_alias) + " Terdaftar", R.drawable.ic_school_24, clickListener_ll_menu3);

                ll_menu4.setVisibility(View.GONE);
                ll_menu5.setVisibility(View.GONE);
                ll_menu6.setVisibility(View.GONE);
                ll_menu7.setVisibility(View.GONE);
                ll_menu8.setVisibility(View.GONE);

                ll_menu.setVisibility(View.VISIBLE);
                shimmer2.setVisibility(View.GONE);
                shimmer2.stopShimmer();
            } else if (role.equals("orangtua")) {
                setMenu(ll_menu1, "Keterlambatan Siswa", R.drawable.ic_access_time_24, clickListener_ll_menu1);
                setMenu(ll_menu2, "Perizinan Siswa", R.drawable.ic_exit_to_app_24, clickListener_ll_menu2);

                ll_menu3.setVisibility(View.GONE);
                ll_menu4.setVisibility(View.GONE);
                ll_menu5.setVisibility(View.GONE);
                ll_menu6.setVisibility(View.GONE);
                ll_menu7.setVisibility(View.GONE);
                ll_menu8.setVisibility(View.GONE);

                ll_menu.setVisibility(View.VISIBLE);
                shimmer1.setVisibility(View.GONE);
                shimmer1.stopShimmer();
            }

            if (NetworkUtil.isConnected(requireContext())) {
                apiService.info().enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            JsonObject json = response.body();

                            String status = json.has("status") ? json.get("status").getAsString() : "";

                            if (status.equals("success")) {
                                JsonObject data = json.has("data") ? json.getAsJsonObject("data") : null;

                                if (data != null) {
                                    JsonObject counts = data.has("counts") ? data.getAsJsonObject("counts") : null;

                                    if (counts != null) {
                                        if (role.equals("superadmin") || role.equals("kepsek")) {
                                            setCount(ll_count1, counts.get("total_user_guru").getAsString(), "Total Akun Guru");
                                            setCount(ll_count2, counts.get("total_user_orangtua").getAsString(), "Total Akun Orangtua");
                                            setCount(ll_count3, counts.get("total_jurusan").getAsString(), "Total Jurusan");
                                            setCount(ll_count4, counts.get("total_kelas").getAsString(), "Total Kelas");

                                            ll_count.setVisibility(View.VISIBLE);
                                            shimmer1.setVisibility(View.GONE);
                                            shimmer1.stopShimmer();
                                        } else if (role.equals("guru_piket") || role.equals("kesiswaan")) {
                                            setCount(ll_count1, counts.get("total_keterlambatan").getAsString(), "Total Keterlambatan Siswa");
                                            setCount(ll_count2, counts.get("total_perizinan").getAsString(), "Total Perizinan Siswa");
                                            setCount(ll_count3, counts.get("total_siswa").getAsString(), "Total Siswa Terdaftar");

                                            ll_count4.setVisibility(View.GONE);
                                            ll_count_div3.setVisibility(View.GONE);

                                            ll_count.setVisibility(View.VISIBLE);
                                            shimmer1.setVisibility(View.GONE);
                                            shimmer1.stopShimmer();
                                        } else if (role.equals("guru")) {
                                            setCount(ll_count1, counts.get("total_keterlambatan").getAsString(), "Total Keterlambatan Siswa" + (finalJurusan_alias.isEmpty() ? "" : " " + finalJurusan_alias));
                                            setCount(ll_count2, counts.get("total_perizinan").getAsString(), "Total Perizinan Siswa" + (finalJurusan_alias.isEmpty() ? "" : " " + finalJurusan_alias));
                                            setCount(ll_count3, counts.get("total_siswa").getAsString(), "Total Siswa" + (finalJurusan_alias.isEmpty() ? "" : " " + finalJurusan_alias) + " Terdaftar");

                                            ll_count4.setVisibility(View.GONE);
                                            ll_count_div3.setVisibility(View.GONE);

                                            ll_count.setVisibility(View.VISIBLE);
                                            shimmer1.setVisibility(View.GONE);
                                            shimmer1.stopShimmer();
                                        } else if (role.equals("orangtua")) {
                                            setCount(ll_count1, counts.get("total_keterlambatan").getAsString(), "Total Keterlambatan Siswa");
                                            setCount(ll_count2, counts.get("total_perizinan").getAsString(), "Total Perizinan Siswa");

                                            ll_count3.setVisibility(View.GONE);
                                            ll_count_div2.setVisibility(View.GONE);
                                            ll_count4.setVisibility(View.GONE);
                                            ll_count_div3.setVisibility(View.GONE);

                                            ll_count.setVisibility(View.VISIBLE);
                                            shimmer1.setVisibility(View.GONE);
                                            shimmer1.stopShimmer();
                                        }
                                    }
                                }
                            }
                        } else {

                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {

                    }
                });
            } else {
                Toast.makeText(requireContext(), "Tidak ada koneksi internet! Tidak dapat memuat informasi.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setMenu(LinearLayout ll, String title, int drawable, @Nullable View.OnClickListener listener) {
        View child0 = ll.getChildAt(0);
        View child1 = ll.getChildAt(1);

        if (child0 instanceof ImageView) {
            ImageView iv = (ImageView) child0;
            iv.setImageResource(drawable);
            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            iv.setColorFilter(ContextCompat.getColor(requireContext(), R.color.primary));
        }

        if (child1 instanceof TextView) {
            ((TextView) child1).setText(title);
        }

        if (listener != null) ll.setOnClickListener(listener);
    }

    private void setCount(LinearLayout ll, String count, String title) {
        View child0 = ll.getChildAt(0);
        View child1 = ll.getChildAt(1);

        if (child0 instanceof TextView) {
            ((TextView) child0).setText(count);
        }
        if (child1 instanceof TextView) {
            ((TextView) child1).setText(title);
        }
    }

    private void showDialogTentang() {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_tentang);
        dialog.setCancelable(true);

        Button btnOK=dialog.findViewById(R.id.btnOK);
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