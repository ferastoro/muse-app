package com.example.muse.network;

import com.example.muse.model.HarvardArtwork;
import com.example.muse.model.HarvardListResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @GET("object")
    Call<HarvardListResponse> getFeaturedArtworks(
        @Query("apikey") String apiKey,
        @Query("hasimage") int hasImage,
        @Query("imagepermissionlevel") Integer permission,
        @Query("classification") String classification,
        @Query("sortby") String sortBy,
        @Query("size") int size,
        @Query("fields") String fields
    );

    @GET("object")
    Call<HarvardListResponse> getRecentArtworks(
        @Query("apikey") String apiKey,
        @Query("hasimage") int hasImage,
        @Query("imagepermissionlevel") Integer permission,
        @Query("size") int size,
        @Query("page") int page,
        @Query("fields") String fields
    );

    @GET("object")
    Call<HarvardListResponse> searchArtworks(
        @Query("apikey") String apiKey,
        @Query("q") String query,
        @Query("hasimage") int hasImage,
        @Query("imagepermissionlevel") Integer permission,
        @Query("size") int size,
        @Query("fields") String fields
    );

    @GET("object")
    Call<HarvardListResponse> searchWithFilters(
        @Query("apikey") String apiKey,
        @Query("q") String query,
        @Query("hasimage") int hasImage,
        @Query("imagepermissionlevel") Integer permission,
        @Query("classification") String classification,
        @Query("culture") String culture,
        @Query("datebegin") Integer dateBegin,
        @Query("dateend") Integer dateEnd,
        @Query("century") String century,
        @Query("size") int size,
        @Query("fields") String fields
    );

    @GET("object/{id}")
    Call<HarvardArtwork> getArtworkDetail(
        @Path("id") int id,
        @Query("apikey") String apiKey
    );

    @GET("object")
    Call<HarvardListResponse> getRelatedArtworks(
        @Query("apikey") String apiKey,
        @Query("classification") String classification,
        @Query("hasimage") int hasImage,
        @Query("imagepermissionlevel") Integer permission,
        @Query("size") int size,
        @Query("fields") String fields
    );
}
