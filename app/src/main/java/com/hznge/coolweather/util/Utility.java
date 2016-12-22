package com.hznge.coolweather.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.icu.text.SimpleDateFormat;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.hznge.coolweather.db.CoolWeatherDB;
import com.hznge.coolweather.model.City;
import com.hznge.coolweather.model.County;
import com.hznge.coolweather.model.Province;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.Locale;

/**
 * Created by hznge on 16-12-22.
 */

public class Utility {
    // parse and response the provinces data from service
    public synchronized static boolean handleProvincesResponse(CoolWeatherDB coolWeatherDB,
                                                               String response) {
        if (!TextUtils.isEmpty(response)) {
            String[] allProvinces = response.split(",");
            if (allProvinces != null && allProvinces.length > 0) {
                for (String p : allProvinces) {
                    String[] array = p.split("\\|");
                    Province province = new Province();
                    province.setProvinceCode(array[0]);
                    province.setProvinceName(array[1]);

                    coolWeatherDB.saveProvince(province);
                }

                return true;
            }
        }

        return false;
    }

    // parse the city data from service
    public static boolean handleCitiesResponse(CoolWeatherDB coolWeatherDB, String response,
                                               int provinceId) {
        if (!TextUtils.isEmpty(response)) {
            String[] allCities = response.split(",");
            if (allCities != null && allCities.length > 0) {
                for (String c : allCities) {
                    String[] array = c.split("\\|");
                    City city = new City();
                    city.setCityCode(array[0]);
                    city.setCityName(array[1]);
                    city.setProvinceId(provinceId);

                    coolWeatherDB.saveCity(city);
                }

                return true;
            }
        }

        return false;
    }

    // parse the county data from service
    public static boolean handleCountiesResponse(CoolWeatherDB coolWeatherDB, String response,
                                                 int cityId) {
        if (!TextUtils.isEmpty(response)) {
            String[] allCounties = response.split(",");
            if (allCounties != null && allCounties.length > 0) {
                for (String c : allCounties) {
                    String[] array = c.split("\\|");
                    County county = new County();
                    county.setCountyCode(array[0]);
                    county.setCountyName(array[1]);
                    county.setCityId(cityId);

                    coolWeatherDB.saveCounty(county);
                }

                return true;
            }
        }

        return false;
    }

    // Parse the JSON to Local
    public static void handleWeatherResponse(Context context, String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONObject weatherInfo = jsonObject.getJSONObject("weatherinfo");

            String cityName = weatherInfo.getString("city");
            String weatherCode = weatherInfo.getString("cityid");
            String temp1 = weatherInfo.getString("temp1");
            String temp2 = weatherInfo.getString("temp2");
            String weatherDesp = weatherInfo.getString("weather");
            String publishTime = weatherInfo.getString("ptime");
            saveWeatherInfo(context, cityName, weatherCode, temp1, temp2,
                    weatherDesp, publishTime);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Store the info from the server into SharePreferences
    public static void saveWeatherInfo(Context context, String cityName, String weatherCode,
                                       String temp1, String temp2, String weatherDesp, String publishTime) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年M月d日", Locale.CHINA);
        SharedPreferences.Editor editor = PreferenceManager
                .getDefaultSharedPreferences(context).edit();
        editor.putBoolean("city_selected", true);
        editor.putString("city_name", cityName);
        editor.putString("weather_code", weatherCode);
        editor.putString("temp1", temp1);
        editor.putString("temp2", temp2);
        editor.putString("weather_desp", weatherDesp);
        editor.putString("publish_time", publishTime);
        editor.putString("current_date", simpleDateFormat.format(new Date()));
        editor.commit();
    }
}
