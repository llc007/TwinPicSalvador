package com.durrutia.twinpic.services;

import com.durrutia.twinpic.domain.Pic;
import com.durrutia.twinpic.domain.Twin;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;


/**
 * Created by LuisLopez.
 * Se definen las rutas para Retrofit
 */

public interface WebService {
    String BASE_URL = "http://192.168.1.103:8080/";
    @POST("postPic")
    Call<Twin> enviarPic(@Body Pic pic);

    @GET("prueba/{user}")
    Call<Pic> obtenerPic(@Path("user") String user);

    class Factory{
        private  static WebService service;

        public static WebService getInstance(){
            if(service==null){
                Retrofit retrofit = new Retrofit.Builder().addConverterFactory(GsonConverterFactory.create()).baseUrl(BASE_URL).build();
                WebService service = retrofit.create(WebService.class);
                return service;
            }else{
                return service;
            }
        }

    }


}
