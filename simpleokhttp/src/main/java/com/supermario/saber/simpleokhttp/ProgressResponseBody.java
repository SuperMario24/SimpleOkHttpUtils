package com.supermario.saber.simpleokhttp;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

/**
 * Created by saber on 2017/8/7.
 */

public class ProgressResponseBody extends ResponseBody {

    private ResponseBody responseBody;
    private BufferedSource bufferedSource;
    private ProgressListener progressListener;

    public ProgressResponseBody(ResponseBody responseBody, ProgressListener progressListener) {
        this.responseBody = responseBody;
        this.progressListener = progressListener;
    }

    @Override
    public MediaType contentType() {
        return responseBody.contentType();
    }

    @Override
    public long contentLength() {
        return responseBody.contentLength();
    }

    @Override
    public BufferedSource source() {
        if (bufferedSource == null) {
            bufferedSource = Okio.buffer(source(responseBody.source()));
        }
        return bufferedSource;
    }


    private Source source(Source source) {
        return new ForwardingSource(source) {
            long totalBytesRead = 0L;
            @Override
            public long read(Buffer sink, long byteCount) throws IOException {
                long bytesRead = super.read(sink, byteCount);
                totalBytesRead += bytesRead != -1? bytesRead:0;
                progressListener.update(totalBytesRead,responseBody.contentLength(),bytesRead == -1);
                return bytesRead;
            }
        };
    }


    public interface ProgressListener{
        void update(long bytesRead, long contentLength, boolean done);
    }
}
