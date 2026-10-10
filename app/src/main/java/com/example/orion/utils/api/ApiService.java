package com.example.orion.utils.api;

import androidx.annotation.Nullable;

import com.google.gson.JsonObject;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface ApiService {
    @POST("login")
    Call<JsonObject> login(@Body JsonObject request);

    @POST("whoami")
    Call<JsonObject> whoami();

    @GET("info")
    Call<JsonObject> info();

    @Multipart
    @POST("profile/edit")
    Call<JsonObject> editProfile(
            @Part("_method") RequestBody method,
            @Part("nama") RequestBody nama,
            @Part("email") RequestBody email,
            @Part("nomor_hp") RequestBody nomor_hp,
            @Part("alamat") RequestBody alamat,
            @Part MultipartBody.Part foto_profil
    );

    @GET("siswa")
    Call<JsonObject> siswa();

    @Multipart
    @POST("keterlambatan")
    Call<JsonObject> keterlambatan(
            @Part("keterangan") RequestBody keterangan,
            @Part("siswa_id") RequestBody siswa_id,
            @Part MultipartBody.Part gambar,
            @Part("date") RequestBody date
    );

    @GET("keterlambatan/{id}")
    Call<JsonObject> keterlambatanShow(@Path("id") String id);

    @Multipart
    @POST("perizinan")
    Call<JsonObject> perizinan(
            @Part("keterangan") RequestBody keterangan,
            @Part("siswa_id") RequestBody siswa_id,
            @Part MultipartBody.Part gambar,
            @Part("date") RequestBody date
    );

    @GET("perizinan/{id}")
    Call<JsonObject> perizinanShow(@Path("id") String id);

    @Multipart
    @POST("perizinan/status/{id}")
    Call<JsonObject> setStatus(
            @Path("id") String id,
            @Part("_method") RequestBody method,
            @Part("status") RequestBody statusBaru
    );

    @Multipart
    @POST("jurusan")
    Call<JsonObject> jurusanAdd(
            @Part("nama") RequestBody jurusan,
            @Part("alias") RequestBody alias,
            @Part("deskripsi") RequestBody deskripsi
    );

    @Multipart
    @POST("users")
    Call<JsonObject> userSatpamAdd(
            @Part("nama") RequestBody nama,
            @Part("nomor_hp") @Nullable RequestBody nomor_hp,
            @Part("email") @Nullable RequestBody email,
            @Part("alamat") @Nullable RequestBody alamat,
            @Part("username") RequestBody username,
            @Part("password") RequestBody password,
            @Part("role") RequestBody role
    );

    @Multipart
    @POST("users")
    Call<JsonObject> userGuruAdd(
            @Part("nip") RequestBody nip,
            @Part("nama") RequestBody nama,
            @Part("nomor_hp") @Nullable RequestBody nomor_hp,
            @Part("email") @Nullable RequestBody email,
            @Part("alamat") @Nullable RequestBody alamat,
            @Part("username") RequestBody username,
            @Part("password") RequestBody password,
            @Part("jurusan_id") RequestBody jurusan_id,
            @Part("role") RequestBody role
    );

    @Multipart
    @POST("users")
    Call<JsonObject> userOrangtuaAdd(
            @Part("nama") RequestBody nama,
            @Part("nomor_hp") @Nullable RequestBody nomor_hp,
            @Part("email") @Nullable RequestBody email,
            @Part("alamat") @Nullable RequestBody alamat,
            @Part("username") RequestBody username,
            @Part("password") RequestBody password,
            @Part("role") RequestBody role
    );

    @GET("users/{id}")
    Call<JsonObject> userShow(@Path("id") String id);

    @Multipart
    @POST("siswa")
    Call<JsonObject> siswaAdd(
            @Part("nis") @Nullable RequestBody nis,
            @Part("nama") RequestBody nama,
            @Part("nomor_hp") @Nullable RequestBody nomor_hp,
            @Part("jurusan_id") RequestBody jurusan_id,
            @Part("kelas") RequestBody kelas,
            @Part("orangtua_id") @Nullable RequestBody orangtua_id
    );

    @GET("list/kelas")
    Call<JsonObject> kelasList();
    @Multipart
    @POST("kelas")
    Call<JsonObject> kelasAdd(
            @Part("kelas") RequestBody kelas,
            @Part("jumlah_kelas") RequestBody jumlah_kelas,
            @Part("jurusan_id") RequestBody jurusan_id
    );

    @Multipart
    @POST("reset-password")
    Call<JsonObject> ubahPassword(
            @Part("password_lama") RequestBody password_lama,
            @Part("password_baru") RequestBody password_baru,
            @Part("password_baru_confirmation") RequestBody konfirmasi_password
    );

    @DELETE("keterlambatan/{id}")
    Call<JsonObject> keterlambatanDelete(@Path("id") String id);

    @DELETE("perizinan/{id}")
    Call<JsonObject> perizinanDelete(@Path("id") String id);

    @DELETE("siswa/{id}")
    Call<JsonObject> siswaDelete(@Path("id") String id);

    @DELETE("users/{id}")
    Call<JsonObject> userDelete(@Path("id") String id);

    @DELETE("jurusan/{id}")
    Call<JsonObject> jurusanDelete(@Path("id") String id);

    @DELETE("kelas/{id}")
    Call<JsonObject> kelasDelete(@Path("id") String id);
}