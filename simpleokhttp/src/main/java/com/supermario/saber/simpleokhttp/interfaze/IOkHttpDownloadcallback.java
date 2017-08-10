package com.supermario.saber.simpleokhttp.interfaze;

/**
 * Created by saber on 2017/7/12.
 */

public interface IOkHttpDownloadcallback extends IOkHttpUpdate {

    /**
     * show progress
     * @param bytesRead current progress
     * @param contentLength total progress
     * @param done isCompleted
     */
    void onUpdate(long bytesRead, long contentLength, boolean done);

    void onDownloadSuccessed();
    void onDownloadError();
    void onDownloadFileExists();




}
