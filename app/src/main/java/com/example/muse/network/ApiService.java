package com.example.muse.network;

import com.example.muse.model.MetArtwork;
import com.example.muse.model.MetObjectsResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // Highlight / Featured
    @GET("search")
    Call<MetObjectsResponse> getHighlightObjects(
        @Query("isHighlight") boolean isHighlight,
        @Query("hasImages") boolean hasImages,
        @Query("isPublicDomain") boolean isPublicDomain,
        @Query("q") String query
    );

    // Search umum
    @GET("search")
    Call<MetObjectsResponse> searchObjects(
        @Query("q") String query,
        @Query("hasImages") boolean hasImages,
        @Query("isPublicDomain") boolean isPublicDomain,
        @Query("departmentId") Integer departmentId
    );

    // FIX 2: Search dengan filters lengkap
    @GET("search")
    Call<MetObjectsResponse> searchWithFilters(
        @Query("q") String query,
        @Query("hasImages") boolean hasImages,
        @Query("isPublicDomain") boolean isPublicDomain,
        @Query("departmentId") Integer departmentId,
        @Query("dateBegin") Integer dateBegin,
        @Query("dateEnd") Integer dateEnd,
        @Query("geoLocation") String geoLocation
    );

    // Detail satu karya
    @GET("objects/{objectId}")
    Call<MetArtwork> getObjectDetail(
        @Path("objectId") int objectId
    );
}
