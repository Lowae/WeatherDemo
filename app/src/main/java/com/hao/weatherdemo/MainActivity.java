package com.hao.weatherdemo;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.util.LruCache;
import android.support.v7.app.AppCompatActivity;
import android.util.ArrayMap;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.weatherCity)
    TextView weatherCity;
    @BindView(R.id.weather)
    TextView weatherinfo;
    @BindView(R.id.img1)
    NetworkImageView img1;
    @BindView(R.id.img2)
    NetworkImageView img2;
    @BindView(R.id.spinner_province)
    Spinner spinnerProvince;
    @BindView(R.id.spinner_city)
    Spinner spinnerCity;
    @BindView(R.id.temperature)
    TextView temperature;

    private CityCode cityCode;
    private ArrayMap<String, List<CityCode.City>> provinces = new ArrayMap<>();

    private RequestQueue requestQueue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        getCode();
        initViews();
        initEvents();

    }

    public void initViews() {
        List<String> provinceName = new ArrayList<>();
        for (CityCode.Province province : cityCode.provinces) {
            provinceName.add(province.province);
            provinces.put(province.province, province.cities);
        }

        ArrayAdapter<String> provinceAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, provinceName);
        provinceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProvince.setAdapter(provinceAdapter);
    }

    public void initEvents() {

        spinnerProvince.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d("select ", parent.getItemAtPosition(position).toString());
                List<String> cityName = new ArrayList<>();
                for (CityCode.City city : Objects.requireNonNull(provinces.get(parent.getItemAtPosition(position).toString()))) {
                    cityName.add(city.city);
                }
                ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, cityName);
                cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                cityAdapter.notifyDataSetChanged();
                spinnerCity.setAdapter(cityAdapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinnerCity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d("code ", provinces.get(spinnerProvince.getSelectedItem().toString()).get(position).code);
                String code = provinces.get(spinnerProvince.getSelectedItem().toString()).get(position).code;
                sendRequest("http://www.weather.com.cn/data/cityinfo/" + code + ".html");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    public void getCode() {
        try {
            InputStream in = MainActivity.this.getResources().getAssets().open("citycode.json");
            int length = in.available();//获取文件字节数
            byte[] buffer = new byte[length];
            in.read(buffer);//读取到byte数组
            String line = new String(buffer);

            Gson gson = new Gson();
            cityCode = gson.fromJson(line, new TypeToken<CityCode>() {}.getType());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendRequest(String url) {

        requestQueue = Volley.newRequestQueue(this);

        UpdateWeatherTask updateWeatherTask = new UpdateWeatherTask();
        updateWeatherTask.execute(url);

    }

    public class UpdateWeatherTask extends AsyncTask<String, Integer, Weather> {

        @Override
        protected Weather doInBackground(String... url) {
            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(url[0])
                        .build();
                Response response = client.newCall(request).execute();
                Gson gson = new Gson();
                return gson.fromJson(response.body() != null ? response.body().string() : null, Weather.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Weather weather) {
            super.onPostExecute(weather);
            weatherCity.setText(weather.weatherinfo.city);
            weatherinfo.setText(weather.weatherinfo.weather);
            temperature.setText(weather.weatherinfo.temp1 + "-" + weather.weatherinfo.temp2);
            String img1Url = "http://www.weather.com.cn/m/i/weatherpic/29x20/"+ weather.weatherinfo.img1;
            String img2Url = "http://www.weather.com.cn/m/i/weatherpic/29x20/"+ weather.weatherinfo.img2;

            showImageByNetworkImageView(img1Url, img1);
            showImageByNetworkImageView(img2Url, img2);
        }

        private void showImageByNetworkImageView(String url, NetworkImageView img) {
            int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
            int cacheSize = maxMemory / 8; // 使用最大可用内存值的1/8作为缓存的大小。
            final LruCache<String, Bitmap> lruCache = new LruCache<String, Bitmap>(cacheSize) {

                @Override
                protected int sizeOf(@NonNull String key, Bitmap bitmap) {
                    // 重写此方法来衡量每张图片的大小，默认返回图片数量。
                    return bitmap.getByteCount() / 1024;
                }
            };

            ImageLoader.ImageCache imageCache = new ImageLoader.ImageCache() {
                @Override
                public Bitmap getBitmap(String url) {
                    return lruCache.get(url);
                }

                @Override
                public void putBitmap(String url, Bitmap bitmap) {
                    lruCache.put(url, bitmap);
                }
            };

            img.setImageUrl(url, new ImageLoader(requestQueue, imageCache));
        }
    }

}

