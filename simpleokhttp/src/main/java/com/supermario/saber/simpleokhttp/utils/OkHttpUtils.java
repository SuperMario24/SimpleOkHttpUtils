package com.supermario.saber.simpleokhttp.utils;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.gson.Gson;
import com.supermario.saber.simpleokhttp.ProgressResponseBody;
import com.supermario.saber.simpleokhttp.interfaze.IOkHttpDownloadcallback;
import com.supermario.saber.simpleokhttp.interfaze.IOkHttpOnLoadcallback;
import com.supermario.saber.simpleokhttp.interfaze.IOkHttpUpdate;
import com.supermario.saber.simpleokhttp.interfaze.IOkHttpUploadcallback;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by saber on 2017/7/12.
 */

public class OkHttpUtils {

    private static final String TAG = "OkHttpUtils";

    private static OkHttpUtils mInstance;
    public static final boolean DEPENDENCY_OKHTTP;

    static {
        boolean hasDependency;
        try {
            Class.forName("okhttp3.OkHttpClient");
            hasDependency = true;
        } catch (ClassNotFoundException e) {
            hasDependency = false;
        }
        DEPENDENCY_OKHTTP = hasDependency;
    }


    private OkHttpUtils(){
        handler = new Handler(Looper.getMainLooper());
    };

    private Handler handler ;

    public static OkHttpUtils getInstance() {
        if(mInstance == null){
            if (!DEPENDENCY_OKHTTP) { //must depend Okhttp
                throw new IllegalStateException("Must be dependency Okhttp");
            }
            synchronized (OkHttpUtils.class){
                if(mInstance == null){
                    mInstance = new OkHttpUtils();
                }
            }
        }
        return mInstance;
    }

    public final void downLoad(final String url,final String destDir,final IOkHttpDownloadcallback callback) {

        final Request request = new Request.Builder()
                .url(url)
                .build();
        final ProgressResponseBody.ProgressListener progressListener = createListener(callback);

        OkHttpClient okHttpClient = makeNewProgressResponseBody(progressListener);

        Call call = okHttpClient.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onDownloadError();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                InputStream in = null;

                byte[] buf = new byte[1024*8];
                int len = 0;
                FileOutputStream fos = null;

                try {
                    in = response.body().byteStream();
                    File dir = new File(destDir);
                    if(!dir.exists()){
                        dir.mkdirs();
                    }
                    File file = new File(destDir,getFileName(url));
                    if(file.exists()){
                        callback.onDownloadFileExists();
                        return;
                    }
                    fos = new FileOutputStream(file);
                    while((len = in.read(buf)) != -1){
                        fos.write(buf,0,len);
                    }
                    fos.flush();
                }catch (IOException e){
                    e.printStackTrace();
                }finally {
                    NormalUtils.close(in);
                    NormalUtils.close(fos);
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onDownloadSuccessed();
                    }
                });

            }
        });
    }


    public void onLoad(final Class<?> clazz, String url, final IOkHttpOnLoadcallback callback){
        Request request = new Request.Builder()
                .url(url)
                .build();

        ProgressResponseBody.ProgressListener progressListener = createListener(callback);

        OkHttpClient okHttpClient = makeNewProgressResponseBody(progressListener);

        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.loadError();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                boolean hasDependencyGson;
                try {
                    Class.forName("com.google.gson.Gson");
                    hasDependencyGson = true;
                } catch (ClassNotFoundException e) {
                    hasDependencyGson = false;
                }
                if(!hasDependencyGson){
                    throw new IllegalStateException("Must be dependency Gson");
                }
                Gson gson = new Gson();
                final Object object = gson.fromJson(response.body().string(),clazz);

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onLoadSuccess(object);
                    }
                });

            }
        });
    }

    public void upLoad(String url, String content, IOkHttpUploadcallback callback){

    }
    public void upLoad(String url, File file, IOkHttpUploadcallback callback){

    }
    public void upLoad(String url, Object object, IOkHttpUploadcallback callback){

    }



    private OkHttpClient makeNewProgressResponseBody(final ProgressResponseBody.ProgressListener progressListener) {
        return new OkHttpClient.Builder()
                .addNetworkInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Response response = chain.proceed(chain.request());
                        return response.newBuilder()
                                .body(new ProgressResponseBody(response.body(),progressListener))
                                .build();
                    }
                }).build();
    }

    private ProgressResponseBody.ProgressListener createListener(final IOkHttpUpdate callback) {
        final ProgressResponseBody.ProgressListener progressListener = new ProgressResponseBody.ProgressListener() {
            @Override
            public void update(final long bytesRead, final long contentLength, final boolean done) {

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG,"bytesRead:"+bytesRead);
                        Log.d(TAG,"contentLength:"+contentLength);
                        callback.onUpdate(bytesRead,contentLength,done);
                    }
                });
            }
        };
        return progressListener;
    }

    private String getFileName(String path)
    {
        int separatorIndex = path.lastIndexOf("/");
        return (separatorIndex < 0) ? path : path.substring(separatorIndex + 1, path.length());
    }


}
