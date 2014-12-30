package com.vg.goprolive.camera;

import static org.apache.commons.io.FilenameUtils.getBaseName;
import static org.apache.commons.lang3.math.NumberUtils.toInt;

import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.TestCase;

import org.apache.commons.io.filefilter.FileFileFilter;

import com.vg.jetty.spdy.api.Session;
import com.vg.jetty.spdy.api.Stream;
import com.vg.jetty.util.Callback;
import com.vg.util.Log;

public class MySPDYUploaderTest extends TestCase {
    private static final FileFilter FILES = (FileFilter) FileFileFilter.FILE;
    private static final String TAG = "LIVE4SPDYUploaderTest";

    public void testName() throws Exception {
        URL url = new URL("http://localhost:8181");

        MySPDYUploader uploader = new MySPDYUploader(url);
        Session session = uploader.connect();

        session.addListener(new Session.StreamListener() {
            @Override
            public void onStreamCreated(Stream stream) {
                Log.d(TAG, "onStreamCreated " + stream);
            }

            @Override
            public void onStreamClosed(Stream stream) {
                Log.d(TAG, "onStreamClosed " + stream);
            }
        });

        //        File externalStorageDirectory = Environment.getExternalStorageDirectory();
        File externalStorageDirectory = new File("/Users/zhukov/live.tmp/774128639/");
        File[] listFiles = externalStorageDirectory.listFiles();
        System.out.println(listFiles);
        File tsDir = new File(externalStorageDirectory, "upload.MP4");
        File[] files = tsDir.listFiles(FILES);
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File f1, File f2) {
                int int1 = toInt(getBaseName(f1.getName()));
                int int2 = toInt(getBaseName(f2.getName()));
                return int1 - int2;
            }
        });

        int permits = 1;
        final Semaphore semaphore = new Semaphore(permits);
        final long startTime = System.currentTimeMillis();
        final AtomicInteger bytes = new AtomicInteger(0);
        for (final File ts : files) {
            Callback.Adapter callback = new Callback.Adapter() {
                boolean semaphoreReleased = false;

                @Override
                public void succeeded() {
                    int b = bytes.addAndGet((int) ts.length());
                    long now = System.currentTimeMillis();
                    long msec = now - startTime;
                    long kbps = b / msec;

                    Log.i(TAG, Thread.currentThread().getName() + " ===> " + ts.getName() + " UPLOAD OK " + kbps
                            + " KB/s " + (b / 1000) + "KB in " + (msec / 1000) + "sec");
                    if (!semaphoreReleased) {
                        semaphoreReleased = true;
                        semaphore.release();
                    }
                }

                @Override
                public void failed(Throwable x) {
                    Log.e(TAG, Thread.currentThread().getName() + " ===> " + ts.getName() + " UPLOAD FAILED " + x);
                    if (!semaphoreReleased) {
                        semaphoreReleased = true;
                        semaphore.release();
                    }
                }
            };
            Thread.sleep(15);

            Log.d(TAG, "sendFile " + ts.getName()+" "+Runtime.getRuntime().freeMemory());
            Log.d(TAG, "streams count " + session.getStreams().size());
            semaphore.acquire();
            MySPDYUploader.sendFile(session, ts, callback);
        }

        Log.d(TAG, "waiting for completion");
        semaphore.acquire(permits);
    }
}