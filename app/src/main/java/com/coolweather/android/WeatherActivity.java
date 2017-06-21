package com.coolweather.android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.coolweather.android.gson.Forecast;
import com.coolweather.android.gson.HourlyForecast;
import com.coolweather.android.gson.Weather;
import com.coolweather.android.service.AutoUpdateService;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static org.litepal.LitePalApplication.getContext;

public class WeatherActivity extends AppCompatActivity {

    public DrawerLayout drawerLayout;
    private Button navButton;
    private Button settingButton;
    public SwipeRefreshLayout swipeRefresh;

    private ScrollView weatherLayout;
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;
    private LinearLayout forecastLayout;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;
    private TextView dressSuggestionText;
    private TextView travelText;
    private TextView uvText;
    private ImageView weatherIcon;
    private LinearLayout hoursForecastLayout;

    private ImageView bingPicImg;
    private String mWeatherId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }


        //初始化各控件

        bingPicImg = (ImageView) findViewById(R.id.bing_pic_img);

        weatherLayout = (ScrollView) findViewById(R.id.weather_layout);
        titleCity = (TextView) findViewById(R.id.title_city);
        titleUpdateTime = (TextView) findViewById(R.id.title_update_time);
        degreeText = (TextView) findViewById(R.id.degree_text);
        weatherInfoText = (TextView) findViewById(R.id.weather_info_text);
        forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        hoursForecastLayout = (LinearLayout) findViewById(R.id.hours_forecast_layout);
        aqiText = (TextView) findViewById(R.id.aqi_text);
        pm25Text = (TextView) findViewById(R.id.pm25_text);
        comfortText = (TextView) findViewById(R.id.comfort_text);
        carWashText = (TextView) findViewById(R.id.car_wash_text);
        sportText = (TextView) findViewById(R.id.sport_text);
        dressSuggestionText = (TextView) findViewById(R.id.dress_suggestion_text);
        travelText = (TextView) findViewById(R.id.travel_text);
        uvText = (TextView) findViewById(R.id.uv_text);
        weatherIcon = (ImageView) findViewById(R.id.weather_icon);


        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navButton = (Button) findViewById(R.id.nav_button);
        settingButton = (Button) findViewById(R.id.setting_button);
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather", null);
        if (weatherString != null) {
            //有缓存时直接解析天气数据
            Weather weather = Utility.handleWeatherResponse(weatherString);
            mWeatherId = weather.basic.weatherId;
            showWeatherInfo(weather);
        } else {
            //无缓存时去服务器查询天气
            mWeatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(mWeatherId);
        }

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(mWeatherId);
            }
        });

        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        String bingPic = prefs.getString("bing_pic", null);
        if (bingPic != null) {
            Glide.with(this).load(bingPic).into(bingPicImg);
        } else {
            loadBingPic();
        }
        settingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(settingButton);
            }
        });
    }

    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.main, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.add_in:

                        break;
                    case R.id.jump_to:
                        //Intent intent=new Intent(this,);
                        //startActivity(intent);
                        Toast.makeText(getContext(), "是否跳转", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.change_to:

                        break;
                    default:
                        break;
                }
                return false;
            }
        });
        popupMenu.show();
    }


    /**
     * 根据天气id请求城市天气信息
     */
    public void requestWeather(final String weatherId) {
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=2e85edb07a624c67909abc4baa64033a";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.status)) {
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather", responseText);
                            editor.apply();
                            mWeatherId = weather.basic.weatherId;
                            showWeatherInfo(weather);
                        } else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        }
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        });
        loadBingPic();
    }


    /**
     * 加载必应每日一图
     */
    private void loadBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
        });
    }


    /**
     * 处理并展示Weather实体类中的数据
     */
    private void showWeatherInfo(Weather weather) {
        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split(" ")[1] + " 发布";
        String degree = weather.now.temperature + "℃";
        String weatherInfo = weather.now.more.info;
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();
        hoursForecastLayout.removeAllViews();
        for (Forecast forecast : weather.forecastList) {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false);
            TextView dateText = (TextView) view.findViewById(R.id.date_text);
            TextView infoText = (TextView) view.findViewById(R.id.info_text);
            TextView maxText = (TextView) view.findViewById(R.id.max_text);
            TextView minText = (TextView) view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            minText.setText(forecast.temperature.min + "℃");
            maxText.setText(forecast.temperature.max + "℃");
            forecastLayout.addView(view);
        }
        for (HourlyForecast hourlyForecast : weather.hourlyForecastList) {
            View view = LayoutInflater.from(this).inflate(R.layout.hourly_forecast_item, hoursForecastLayout, false);
            TextView hoursDateText = (TextView) view.findViewById(R.id.hours_date_text);
            TextView hoursInfoText = (TextView) view.findViewById(R.id.hours_info_text);
            TextView temperatureText = (TextView) view.findViewById(R.id.temperature_text);
            hoursDateText.setText(hourlyForecast.date);
            hoursInfoText.setText(hourlyForecast.more.info);
            temperatureText.setText(hourlyForecast.temperature + "℃");
            hoursForecastLayout.addView(view);
        }
        if (weather.aqi != null) {
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }
        String comfort = "舒适度指数： " + weather.suggestion.comfort.info;
        String carWash = "洗车指数： " + weather.suggestion.carWash.info;
        String sport = "运动指数： " + weather.suggestion.sport.info;
        String dress = "穿衣指数： " + weather.suggestion.dressSuggestion.info;
        String travel = "旅游指数： " + weather.suggestion.travel.info;
        String uv = "紫外线指数： " + weather.suggestion.uv.info;
        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);
        dressSuggestionText.setText(dress);
        travelText.setText(travel);
        uvText.setText(uv);
        switch (weatherInfo) {
            case "晴":
                weatherIcon.setImageResource(R.drawable.ic_100);
                break;
            case "多云":
                weatherIcon.setImageResource(R.drawable.ic_101);
                break;
            case "少云":
                weatherIcon.setImageResource(R.drawable.ic_102);
                break;
            case "晴间多云":
                weatherIcon.setImageResource(R.drawable.ic_103);
                break;
            case "阴":
                weatherIcon.setImageResource(R.drawable.ic_104);
                break;
            case "有风":
                weatherIcon.setImageResource(R.drawable.ic_200);
                break;
            case "平静":
                weatherIcon.setImageResource(R.drawable.ic_201);
                break;
            case "微风":
                weatherIcon.setImageResource(R.drawable.ic_202);
                break;
            case "和风":
                weatherIcon.setImageResource(R.drawable.ic_203);
                break;
            case "清风":
                weatherIcon.setImageResource(R.drawable.ic_204);
                break;
            case "强风":
                weatherIcon.setImageResource(R.drawable.ic_205);
                break;
            case "劲风":
                weatherIcon.setImageResource(R.drawable.ic_205);
                break;
            case "疾风":
                weatherIcon.setImageResource(R.drawable.ic_206);
                break;
            case "大风":
                weatherIcon.setImageResource(R.drawable.ic_207);
                break;
            case "烈风":
                weatherIcon.setImageResource(R.drawable.ic_208);
                break;
            case "风暴":
                weatherIcon.setImageResource(R.drawable.ic_209);
                break;
            case "狂爆风":
                weatherIcon.setImageResource(R.drawable.ic_210);
                break;
            case "飓风":
                weatherIcon.setImageResource(R.drawable.ic_211);
                break;
            case "龙卷风":
                weatherIcon.setImageResource(R.drawable.ic_212);
                break;
            case "热带风暴":
                weatherIcon.setImageResource(R.drawable.ic_213);
                break;
            case "阵雨":
                weatherIcon.setImageResource(R.drawable.ic_300);
                break;
            case "强阵雨":
                weatherIcon.setImageResource(R.drawable.ic_301);
                break;
            case "雷阵雨":
                weatherIcon.setImageResource(R.drawable.ic_302);
                break;
            case "强雷阵雨":
                weatherIcon.setImageResource(R.drawable.ic_303);
                break;
            case "雷阵雨伴有冰雹":
                weatherIcon.setImageResource(R.drawable.ic_304);
                break;
            case "小雨":
                weatherIcon.setImageResource(R.drawable.ic_305);
                break;
            case "中雨":
                weatherIcon.setImageResource(R.drawable.ic_306);
                break;
            case "大雨":
                weatherIcon.setImageResource(R.drawable.ic_307);
                break;
            case "极端降雨":
                weatherIcon.setImageResource(R.drawable.ic_308);
                break;
            case "毛毛雨":
                weatherIcon.setImageResource(R.drawable.ic_309);
                break;
            case "细雨":
                weatherIcon.setImageResource(R.drawable.ic_309);
                break;
            case "暴雨":
                weatherIcon.setImageResource(R.drawable.ic_310);
                break;
            case "大暴雨":
                weatherIcon.setImageResource(R.drawable.ic_311);
                break;
            case "特大暴雨":
                weatherIcon.setImageResource(R.drawable.ic_312);
                break;
            case "冻雨":
                weatherIcon.setImageResource(R.drawable.ic_313);
                break;
            case "小雪":
                weatherIcon.setImageResource(R.drawable.ic_400);
                break;
            case "中雪":
                weatherIcon.setImageResource(R.drawable.ic_401);
                break;
            case "大雪":
                weatherIcon.setImageResource(R.drawable.ic_402);
                break;
            case "暴雪":
                weatherIcon.setImageResource(R.drawable.ic_403);
                break;
            case "雨夹雪":
                weatherIcon.setImageResource(R.drawable.ic_404);
                break;
            case "雨雪天气":
                weatherIcon.setImageResource(R.drawable.ic_405);
                break;
            case "阵雨夹雪":
                weatherIcon.setImageResource(R.drawable.ic_406);
                break;
            case "阵雪":
                weatherIcon.setImageResource(R.drawable.ic_407);
                break;
            case "薄雾":
                weatherIcon.setImageResource(R.drawable.ic_500);
                break;
            case "雾":
                weatherIcon.setImageResource(R.drawable.ic_501);
                break;
            case "霾":
                weatherIcon.setImageResource(R.drawable.ic_502);
                break;
            case "扬沙":
                weatherIcon.setImageResource(R.drawable.ic_503);
                break;
            case "浮尘":
                weatherIcon.setImageResource(R.drawable.ic_504);
                break;
            case "沙尘暴":
                weatherIcon.setImageResource(R.drawable.ic_507);
                break;
            case "强沙尘暴":
                weatherIcon.setImageResource(R.drawable.ic_508);
                break;
            case "热":
                weatherIcon.setImageResource(R.drawable.ic_900);
                break;
            case "冷":
                weatherIcon.setImageResource(R.drawable.ic_901);
                break;
            default:
                break;

        }
        weatherLayout.setVisibility(View.VISIBLE);
        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
    }
}
