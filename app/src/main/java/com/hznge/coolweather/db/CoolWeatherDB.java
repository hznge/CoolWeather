package com.hznge.coolweather.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hznge.coolweather.model.City;
import com.hznge.coolweather.model.County;
import com.hznge.coolweather.model.Province;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hznge on 16-12-22.
 */

public class CoolWeatherDB {
    public static final String DB_NAME = "cool_weather";

    //Db version
    public static final int VERSION = 1;

    private static CoolWeatherDB sCoolWeatherDB;

    private SQLiteDatabase db;

    // Private Constructor
    private CoolWeatherDB(Context context) {
        CoolWeatherOpenHelper dbHelper = new CoolWeatherOpenHelper(context, DB_NAME, null, VERSION);
        db = dbHelper.getWritableDatabase();
    }

    // Get the CoolWeatherDB instance
    public synchronized static CoolWeatherDB getInstance(Context context) {
        if (sCoolWeatherDB == null) {
            sCoolWeatherDB = new CoolWeatherDB(context);
        }
        return sCoolWeatherDB;
    }

    // Store the Province Instance to db
    public void saveProvince(Province province) {
        if (province != null) {
            ContentValues values = new ContentValues();
            values.put("province_name", province.getProvinceName());
            values.put("province_code", province.getProvinceCode());
            db.insert("Province", null, values);
        }
    }

    // Get the province info from the db
    public List<Province> loadProvinces() {
        List<Province> list = new ArrayList<>();

        Cursor cursor = db.query
                ("Province", null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                Province province = new Province();
                province.setId(cursor.getInt(cursor.getColumnIndex("id")));
                province.setProvinceName(cursor.getString(cursor.getColumnIndex("province_name")));
                province.setProvinceCode(cursor.getString(cursor.getColumnIndex("province_code")));
                list.add(province);
            } while (cursor.moveToNext());
        }

        if (cursor != null) {
            cursor.close();
        }

        return list;
    }

    // Store the City Instance to db
    public void saveCity(City city) {
        if (city != null) {
            ContentValues values = new ContentValues();
            values.put("city_name", city.getCityName());
            values.put("city_code", city.getCityCode());
            values.put("province_id", city.getProvinceId());
            db.insert("City", null, values);
        }
    }

    // Get the sample province city info from db
    public List<City> loadCities(int provinceId) {
        List<City> list = new ArrayList<>();

        Cursor cursor = db.query
                ("City", null, "province_id = ? ",
                        new String[] { String.valueOf(provinceId) }, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                City city = new City();
                city.setId(cursor.getInt(cursor.getColumnIndex("id")));
                city.setCityName(cursor.getString(cursor.getColumnIndex("city_name")));
                city.setCityCode(cursor.getString(cursor.getColumnIndex("city_code")));
                city.setProvinceId(provinceId);
                list.add(city);
            } while (cursor.moveToNext());
        }

        if (cursor != null) {
            cursor.close();
        }

        return list;
    }

    // Store the County Instance to db
    public void saveCounty(County county) {
        if (county != null) {
            ContentValues values = new ContentValues();
            values.put("county_name", county.getCountyName());
            values.put("county_code", county.getCountyCode());
            values.put("city_id", county.getCityId());
            db.insert("County", null, values);
        }
    }

    // Iterator from db to get all counties from city
    public List<County> loadCounties(int cityId) {
        List<County> list = new ArrayList<>();
        Cursor cursor = db.query("County", null, "city_id = ? ",
                new String[] { String.valueOf(cityId) }, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                County county = new County();
                county.setId(cursor.getInt(cursor.getColumnIndex("id")));
                county.setCountyCode(cursor.getString(cursor.getColumnIndex("county_code")));
                county.setCountyName(cursor.getString(cursor.getColumnIndex("county_name")));
                county.setCityId(cityId);
                list.add(county);
            } while (cursor.moveToNext());
        }

        if (cursor != null) {
            cursor.close();
        }

        return list;
    }
}
