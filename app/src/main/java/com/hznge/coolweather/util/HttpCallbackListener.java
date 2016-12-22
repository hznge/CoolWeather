package com.hznge.coolweather.util;

/**
 * Created by hznge on 16-12-22.
 */

public interface HttpCallbackListener {
    void onFinish(String response);

    void onError(Exception e);
}
