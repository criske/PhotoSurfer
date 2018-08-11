package com.crskdev.photosurfer.data.remote.download;

/**
 * Created by Cristian Pela on 06.08.2018.
 */
public interface ProgressListener {
    void update(boolean isStartingValue,long bytesRead, long contentLength, boolean done);
}
