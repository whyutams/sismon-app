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

    @POST("device-token")
    Call<JsonObject> sendDeviceToken(@Body JsonObject body);

    @retrofit2.http.HTTP(method = "DELETE", path = "device-token", hasBody = true)
    Call<JsonObject> deleteDeviceToken(@Body JsonObject body);

    @GET("notifications")
    Call<JsonObject> getNotifications();

    @POST("notifications/{id}/read")
    Call<JsonObject> markNotificationAsRead(@Path("id") String id);

    @POST("notifications/read-all")
    Call<JsonObject> markAllNotificationsAsRead();

    @GET("app-update/check")
    Call<JsonObject> checkAppUpdate(@retrofit2.http.Query("current_version_name") String currentVersionName);

    @retrofit2.http.Streaming
    @GET
    Call<okhttp3.ResponseBody> downloadApkFile(@retrofit2.http.Url String fileUrl);

@GET("keterlambatan")
    Call<JsonObject> getKeterlambatan(
            @retrofit2.http.Query("page") int page,
            @retrofit2.http.Query("per_page") int perPage,
            @retrofit2.http.Query("search") String search
    );

    @GET("perizinan")
    Call<JsonObject> getPerizinan(
            @retrofit2.http.Query("page") int page,
            @retrofit2.http.Query("per_page") int perPage,
            @retrofit2.http.Query("search") String search
    );

    @GET("siswa")
    Call<JsonObject> getSiswa(
            @retrofit2.http.Query("page") int page,
            @retrofit2.http.Query("per_page") int perPage,
            @retrofit2.http.Query("search") String search
    );

    @GET("users")
    Call<JsonObject> getUsers(
            @retrofit2.http.Query("role") String role,
            @retrofit2.http.Query("page") int page,
            @retrofit2.http.Query("per_page") int perPage,
            @retrofit2.http.Query("search") String search
    );

    @GET("jurusan")
    Call<JsonObject> getJurusan(
            @retrofit2.http.Query("page") int page,
            @retrofit2.http.Query("per_page") int perPage,
            @retrofit2.http.Query("search") String search
    );

    @GET("kelas")
    Call<JsonObject> getKelas(
            @retrofit2.http.Query("page") int page,
            @retrofit2.http.Query("per_page") int perPage,
            @retrofit2.http.Query("search") String search
    );

    @GET("siswa/{id}")
    Call<JsonObject> siswaShow(@Path("id") String id);

    @GET("jurusan/{id}")
    Call<JsonObject> jurusanShow(@Path("id") String id);

    @GET("kelas/{id}")
    Call<JsonObject> kelasShow(@Path("id") String id);

    @Multipart
    @POST("keterlambatan/{id}")
    Call<JsonObject> keterlambatanUpdate(
            @Path("id") String id,
            @Part("_method") RequestBody method,
            @Part("siswa_id") RequestBody siswa_id,
            @Part("date") RequestBody date,
            @Part("keterangan") RequestBody keterangan,
            @Part MultipartBody.Part gambar
    );

    @Multipart
    @POST("perizinan/{id}")
    Call<JsonObject> perizinanUpdate(
            @Path("id") String id,
            @Part("_method") RequestBody method,
            @Part("siswa_id") RequestBody siswa_id,
            @Part("date") RequestBody date,
            @Part("keterangan") RequestBody keterangan,
            @Part MultipartBody.Part gambar
    );

    @Multipart
    @POST("siswa/{id}")
    Call<JsonObject> siswaUpdate(
            @Path("id") String id,
            @Part("_method") RequestBody method,
            @Part("nis") @Nullable RequestBody nis,
            @Part("nama") RequestBody nama,
            @Part("nomor_hp") @Nullable RequestBody nomor_hp,
            @Part("jurusan_id") RequestBody jurusan_id,
            @Part("kelas") RequestBody kelas,
            @Part("orangtua_id") @Nullable RequestBody orangtua_id
    );

    @Multipart
    @POST("jurusan/{id}")
    Call<JsonObject> jurusanUpdate(
            @Path("id") String id,
            @Part("_method") RequestBody method,
            @Part("nama") RequestBody jurusan,
            @Part("alias") RequestBody alias,
            @Part("deskripsi") RequestBody deskripsi
    );

    @Multipart
    @POST("kelas/{id}")
    Call<JsonObject> kelasUpdate(
            @Path("id") String id,
            @Part("_method") RequestBody method,
            @Part("kelas") RequestBody kelas,
            @Part("jumlah_kelas") RequestBody jumlah_kelas,
            @Part("jurusan_id") RequestBody jurusan_id
    );

}