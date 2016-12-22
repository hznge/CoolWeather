package com.hznge.coolweather.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hznge.coolweather.R;
import com.hznge.coolweather.db.CoolWeatherDB;
import com.hznge.coolweather.model.City;
import com.hznge.coolweather.model.County;
import com.hznge.coolweather.model.Province;
import com.hznge.coolweather.util.HttpCallbackListener;
import com.hznge.coolweather.util.HttpUtil;
import com.hznge.coolweather.util.Utility;

import java.util.ArrayList;
import java.util.List;

import static android.widget.AdapterView.*;

/**
 * Created by hznge on 16-12-22.
 */

public class ChooseAreaActivity extends AppCompatActivity {
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;

    private ProgressDialog mProgressDialog;
    private TextView titleText;
    private ListView mListView;
    private ArrayAdapter<String> mAdapter;
    private CoolWeatherDB mCoolWeatherDB;
    private List<String> dataList = new ArrayList<>();

    private List<Province> mProvinceList;
    private List<City> mCityList;
    private List<County> mCountyList;

    private Province selectedProvince;
    private City selectedCity;

    private int currentLevel;
    private boolean isFromWeatherActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_area);

        isFromWeatherActivity = getIntent().getBooleanExtra("from_weather_activity", false);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.getBoolean("city_selected", false) && !isFromWeatherActivity) {
            Intent intent = new Intent(this, WeatherActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        mListView = (ListView) findViewById(R.id.list_view);
        titleText = (TextView) findViewById(R.id.title_text);

        mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, dataList);
        mListView.setAdapter(mAdapter);
        mCoolWeatherDB = CoolWeatherDB.getInstance(this);

        mListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel == LEVEL_PROVINCE) {
                    selectedProvince = mProvinceList.get(position);
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    selectedCity = mCityList.get(position);
                    queryCounties();
                } else if (currentLevel == LEVEL_COUNTY) {
                    String countyCode = mCountyList.get(position).getCountyCode();
                    Intent intent = new Intent(ChooseAreaActivity.this, WeatherActivity.class);
                    intent.putExtra("county_code", countyCode);
                    startActivity(intent);
                    finish();
                }
            }
        });

        queryProvinces();
    }

    // Query all Provinces, primary from db else from server
    private void queryProvinces() {
        mProvinceList = mCoolWeatherDB.loadProvinces();

        if (mProvinceList.size() > 0) {
            dataList.clear();
            for (Province province: mProvinceList) {
                dataList.add(province.getProvinceName());
            }
            mAdapter.notifyDataSetChanged();
            mListView.setSelection(0);
            titleText.setText("China");
            currentLevel = LEVEL_PROVINCE;
        } else {
            queryFromServer(null, "province");
        }
    }

    // Query all Cities, primary from db else from server
    private void queryCities() {
        mCityList = mCoolWeatherDB.loadCities(selectedProvince.getId());

        if (mCityList.size() > 0) {
            dataList.clear();
            for (City city: mCityList) {
                dataList.add(city.getCityName());
            }
            mAdapter.notifyDataSetChanged();
            mListView.setSelection(0);
            titleText.setText(selectedProvince.getProvinceName());
            currentLevel = LEVEL_CITY;
        } else {
            queryFromServer(selectedProvince.getProvinceCode(), "city");
        }
    }

    // Query all counties, primary from db else server
    private void queryCounties() {
        mCountyList = mCoolWeatherDB.loadCounties(selectedCity.getId());

        if (mCountyList.size() > 0) {
            dataList.clear();
            for (County county: mCountyList) {
                dataList.add(county.getCountyName());
            }
            mAdapter.notifyDataSetChanged();
            mListView.setSelection(0);
            titleText.setText(selectedCity.getCityName());
            currentLevel = LEVEL_COUNTY;
        } else {
            queryFromServer(selectedCity.getCityCode(), "county");
        }
    }

    private void queryFromServer(final String code, final String type) {
        String address;
        if (!TextUtils.isEmpty(code)) {
            address = "http://www.weather.com.cn/data/list3/city" + code + ".xml";
        } else {
            address = "http://www.weather.com.cn/data/list3/city.xml";
        }
        showProgressDialog();

        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                boolean result = false;
                if ("province".equals(type)) {
                    result = Utility.handleProvincesResponse(mCoolWeatherDB, response);
                } else if ("city".equals(type)) {
                    result = Utility.handleCitiesResponse(mCoolWeatherDB, response,
                            selectedProvince.getId());
                } else if ("county".equals(type)) {
                    result = Utility.handleCountiesResponse(mCoolWeatherDB, response,
                            selectedCity.getId());
                }

                if (result) {
                    // Through runOnUiThread() methods jump to main Thread
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(type)) {
                                queryProvinces();
                            } else if ("city".equals(type)) {
                                queryCities();
                            } else if ("county".equals(type)) {
                                queryCounties();
                            }
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(ChooseAreaActivity.this,
                                "Load Error", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("Loading...");
            mProgressDialog.setCanceledOnTouchOutside(false);
        }
        mProgressDialog.show();
    }

    private void closeProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void onBackPressed() {
        if (currentLevel == LEVEL_COUNTY) {
            queryCities();
        } else if (currentLevel == LEVEL_CITY) {
            queryProvinces();
        } else {
            if (isFromWeatherActivity) {
                Intent intent = new Intent(this, WeatherActivity.class);
                startActivity(intent);
            }
            finish();
        }
    }
}
