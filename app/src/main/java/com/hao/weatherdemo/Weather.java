package com.hao.weatherdemo;

import com.google.gson.annotations.SerializedName;

public class Weather {

    @SerializedName("weatherinfo")
    public Weatherinfo weatherinfo;

    public static class Weatherinfo{

        @SerializedName("city")
        public String city;

        @SerializedName("cityid")
        public String cityid;

        @SerializedName("temp1")
        public String temp1;

        @SerializedName("temp2")
        public String temp2;

        @SerializedName("weather")
        public String weather;

        @SerializedName("img1")
        public String img1;

        @SerializedName("img2")
        public String img2;

        @SerializedName("ptime")
        public String ptime;
    }

}
