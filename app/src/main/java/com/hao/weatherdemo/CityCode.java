package com.hao.weatherdemo;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CityCode {

    @SerializedName("城市代码")
    public List<Province> provinces;

    public static class Province{

        @SerializedName("省")
        public String province;

        @SerializedName("市")
        public List<City> cities;
    }

    public static class City{

        @SerializedName("市名")
        public String city;

        @SerializedName("编码")
        public String code;
    }

}
