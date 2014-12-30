package com.vg.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.io.IOUtils;

public class Log {

    private final static AtomicReference<Writer> writer = new AtomicReference<Writer>(new PrintWriter(System.out));

    public static void replaceWriter(Writer writer) {
        Writer oldWriter = getWriter();
        setWriter(writer);
        IOUtils.closeQuietly(oldWriter);
    }

    public static void setWriter(Writer writer) {
        Log.writer.set(writer);
    }

    public static Writer getWriter() {
        return Log.writer.get();
    }
    public final static int ALL = 0;
    public final static int VERBOSE = 1;
    public final static int DEBUG = 2;
    public final static int INFO = 3;
    public final static int WARN = 4;
    public final static int ERROR = 5;
    public final static int ASSERT = 6;

    //TODO
    static int level = VERBOSE;

    public static void v(String tag, String msg) {
        log(VERBOSE, tag, msg);
    }

    public static void d(String tag, String msg) {
        log(DEBUG, tag, msg);
    }

    public static void w(String tag, String msg) {
        log(WARN, tag, msg);
    }

    public static void i(String tag, String msg) {
        log(INFO, tag, msg);
    }

    public static void e(String tag, String msg) {
        log(ERROR, tag, msg);
    }

    public static void e(String tag, String msg, Throwable e) {
        log(ERROR, tag, msg, e);
    }

    private final static String[] levelStrings = new String[8];
    static {
        levelStrings[0] = " ? ";
        levelStrings[VERBOSE] = " V ";
        levelStrings[DEBUG] = " D ";
        levelStrings[INFO] = " I ";
        levelStrings[WARN] = " W ";
        levelStrings[ERROR] = " E ";
        levelStrings[ASSERT] = " ! ";
    }

    static ConcurrentMap<String, Integer> levels = new ConcurrentHashMap<String, Integer>();

    public static void setLevel(String TAG, int level) {
        levels.put(TAG, level);
    }

    public static int getLevel(String TAG) {
        Integer integer = levels.get(TAG);
        if (integer == null) {
            return VERBOSE;
        } else {
            return integer.intValue();
        }
    }

    public static void log(int level, String tag, String msg) {
        log(level, tag, msg, null);
    }

    public static void log(int level, String tag, String msg, Throwable t) {
        if (level < getLevel(tag)) {
            return;
        }
//        android.util.Log.println(level, tag, msg);
        Writer w = writer.get();
        if (w != null) {
            String date = date();
            StringBuilder sb = new StringBuilder(128);
            sb.append(date);
            sb.append(levelStrings[level]);
            sb.append(tag);
            sb.append(' ');
            sb.append(msg);
            if (t != null) {
                StringWriter sw = new StringWriter();
                t.printStackTrace(new PrintWriter(sw));
                sb.append(' ');
                sb.append(sw.toString());
            }
            sb.append('\n');
            try {
                w.write(sb.toString());
                w.flush();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private static String date() {
        return dftl.get().format(new Date());
    }

    private final static ThreadLocal<SimpleDateFormat> dftl = new ThreadLocal<SimpleDateFormat>() {
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ", Locale.US);
        };
    };

}
