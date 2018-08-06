package com.crskdev.photosurfer.data.remote;

/**
 * Created by Cristian Pela on 06.08.2018.
 */
public interface ProgressListener {
    void update(long bytesRead, long contentLength, boolean done);
}
