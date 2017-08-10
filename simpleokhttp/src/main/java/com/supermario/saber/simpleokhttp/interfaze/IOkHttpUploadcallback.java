package com.supermario.saber.simpleokhttp.interfaze;

/**
 * Created by saber on 2017/8/9.
 */

public interface IOkHttpUploadcallback extends IOkHttpUpdate{
    @Override
    public void onUpdate(long bytesRead, long contentLength, boolean done);
    void loadError();
    void onLoadSuccess(Object object);
}
