package com.supermario.saber.simpleokhttp.interfaze;

/**
 * Created by saber on 2017/8/7.
 */

public interface IOkHttpOnLoadcallback extends IOkHttpUpdate{
    void onUpdate(long bytesRead, long contentLength, boolean done);
    void loadError();
    void onLoadSuccess(Object object);
}
