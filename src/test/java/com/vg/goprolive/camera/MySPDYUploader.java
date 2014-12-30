package com.vg.goprolive.camera;

import com.vg.jetty.spdy.api.BytesDataInfo;
import com.vg.jetty.spdy.api.GoAwayResultInfo;
import com.vg.jetty.spdy.api.PingResultInfo;
import com.vg.jetty.spdy.api.ReplyInfo;
import com.vg.jetty.spdy.api.RstInfo;
import com.vg.jetty.spdy.api.SPDY;
import com.vg.jetty.spdy.api.Session;
import com.vg.jetty.spdy.api.SessionFrameListener;
import com.vg.jetty.spdy.api.Settings;
import com.vg.jetty.spdy.api.SettingsInfo;
import com.vg.jetty.spdy.api.Stream;
import com.vg.jetty.spdy.api.StreamFrameListener;
import com.vg.jetty.spdy.api.StreamStatus;
import com.vg.jetty.spdy.api.SynInfo;
import com.vg.jetty.spdy.client.SPDYClient;
import com.vg.jetty.util.Callback;
import com.vg.jetty.util.Fields;
import com.vg.util.Log;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MySPDYUploader {
    private static final String TAG = "LIVE4SPDYUploader";

    private final InetSocketAddress address;
    private final SPDYClient spdyClient;

    public MySPDYUploader(URL url) throws Exception {
        SPDYClient.Factory clientFactory = new SPDYClient.Factory();
        clientFactory.setConnectTimeout(1000);
        clientFactory.start();
        this.spdyClient = clientFactory.newSPDYClient(SPDY.V3);
        this.address = new InetSocketAddress(url.getHost(), url.getPort());
    }

    public static void sendFile(final Session session, final File ts, final Callback callback)
            throws ExecutionException, InterruptedException, TimeoutException, IOException {

        StreamFrameListener listener = new StreamFrameListener.Adapter() {
            @Override
            public void onReply(Stream stream, ReplyInfo replyInfo) {
                Log.d(TAG, Thread.currentThread().getName() + " got reply " + replyInfo);
                callback.succeeded();
            }

            @Override
            public void onFailure(Stream stream, Throwable x) {
                Log.d(TAG, Thread.currentThread().getName() + " " + ts.getName() + " onFailure " + x);
                try {
                    if (!stream.isClosed()) {
                        session.rst(new RstInfo(stream.getId(), StreamStatus.CANCEL_STREAM));
                    }
                } catch (Exception e) {
                    Log.e(TAG, "stream failure " + e);
                }

                callback.failed(x);
            }
        };

        String userId = ts.getParentFile().getParentFile().getName();
        String streamId = ts.getParentFile().getName();
        String filename = ts.getName();
        Fields headers = new Fields();
        headers.add(":method", "POST");
        headers.add(":path", "/gopro/" + userId + "/" + streamId + "/" + filename);
        headers.add("content-length", ts.length() + "");
        headers.add("last-modified", httpDateFormat(ts.lastModified()));

        SynInfo synInfo = new SynInfo(30, TimeUnit.SECONDS, headers, false, (byte) 0);

        final Stream stream = session.syn(synInfo, listener);
        byte[] bytes = FileUtils.readFileToByteArray(ts);
        BytesDataInfo dataInfo = new BytesDataInfo(30, TimeUnit.SECONDS, bytes, true);

        if (!stream.isClosed() || !stream.isReset()) {
            Log.d(TAG, stream.getId() + " :: " + "stream.data " + ts.getName() + " " + dataInfo.length());
            stream.data(dataInfo, new Callback() {
                @Override
                public void succeeded() {
                    Log.d(TAG, stream.getId() + " :: " + ts.getName() + " sent but no reply yet");
                    // XXX: fires when data sent but not confirmed as received. use Stream.onReply event instead
                }

                @Override
                public void failed(Throwable x) {
                    Log.e(TAG, stream.getId() + " :: " + ts.getName() + " failed upload " + x + " " + stream.isClosed()
                            + " " + stream.isReset() + " " + stream);
                    callback.failed(x);
                }
            });
        } else {
            throw new RuntimeException(stream + " is closed");
        }
    }

    public static String httpDateFormat(long mtime) {
        return httpDateFormat().format(new Date(mtime));
    }

    public static SimpleDateFormat httpDateFormat() {
        return new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
    }

    public Session connect() throws ExecutionException, InterruptedException {
        SessionFrameListener sessionFrameListener = new SessionFrameListener() {
            @Override
            public void onGoAway(Session session, GoAwayResultInfo goAwayResultInfo) {
                Log.d(TAG, session + "onGoAway " + goAwayResultInfo.getSessionStatus());
                throw new RuntimeException("server told us to go away");
            }

            @Override
            public StreamFrameListener onSyn(Stream stream, SynInfo synInfo) {
                Log.d(TAG, "onSyn " + synInfo);
                return null;
            }

            @Override
            public void onRst(Session session, RstInfo rstInfo) {
                Log.d(TAG, "onRst " + rstInfo);
            }

            @Override
            public void onSettings(Session session, SettingsInfo settingsInfo) {
                Log.d(TAG, "onSettings " + settingsInfo.getSettings());
                Settings sessionSettings = settingsInfo.getSettings();
                Settings.Setting setting = sessionSettings.get(Settings.ID.MAX_CONCURRENT_STREAMS);
                if (setting != null) {
                    int MAX_CONCURRENT_STREAMS = setting.value();
                    Log.d(TAG, "session MAX_CONCURRENT_STREAMS value updated to " + MAX_CONCURRENT_STREAMS);
                }
            }

            @Override
            public void onPing(Session session, PingResultInfo pingResultInfo) {
                Log.d(TAG, "onPing " + pingResultInfo);
            }

            @Override
            public void onFailure(Session session, Throwable x) {
                Log.d(TAG, x + " onFailure " + session);
            }
        };

        return this.spdyClient.connect(this.address, sessionFrameListener);
    }
}
