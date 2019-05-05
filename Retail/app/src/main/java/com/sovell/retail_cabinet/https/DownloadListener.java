package com.sovell.retail_cabinet.https;

/**
 * 下载进度 Listener
 */
public interface DownloadListener {
    void update(long bytesRead, long contentLength, boolean done);
}
