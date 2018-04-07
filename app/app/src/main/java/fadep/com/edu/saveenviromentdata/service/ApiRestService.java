package fadep.com.edu.saveenviromentdata.service;

import java.util.List;

import fadep.com.edu.saveenviromentdata.model.Info;
import fadep.com.edu.saveenviromentdata.model.Place;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiRestService {

    @GET("/info")
    Call<List<Info>> getAllinfo();

    @GET("/info/{id}")
    Call<List<Info>> getInfoById(@Path("id") int id);

    @POST("/info")
    Call<Info> createInfo(@Body Info info);

    @GET("/place")
    Call<List<Place>> getAllplace();

    @GET("/place/{id}")
    Call<List<Place>> getPlaceById(@Path("id") int id);

    @POST("/place")
    Call<Place> createPlace(@Body Place info);
}