/*
 * Copyright (C) 2016 MarkZhai (http://zhaiyifan.cn).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.moduth.blockcanary;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.eebbk.bfc.common.file.FileUtils;
import com.github.moduth.blockcanary.internal.BlockInfo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Log writer which runs in standalone thread.
 */
public class LogWriter {

    private static final String TAG = "LogWriter";

    private static final Object SAVE_DELETE_LOCK = new Object();
    private static final SimpleDateFormat FILE_NAME_FORMATTER
            = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss.SSS", Locale.US);
    private static final SimpleDateFormat TIME_FORMATTER
            = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
    public static final String FILE_SUFFIX = ".log";
//    private static final long OBSOLETE_DURATION = 2*24*3600*1000L;

    private LogWriter() {
        throw new InstantiationError("Must not instantiate this class");
    }

    /**
     * Save log to file
     *
     * @param str block info string
     * @return log file path
     */
    public static String save(String str) {
        String path;
        synchronized (SAVE_DELETE_LOCK) {
            path = save2Log(str);
        }

        return path;
    }

    /**
     * Delete obsolete log files, which is by default 2 days.
     */
    public static void cleanObsolete(final long duration) {
        if (duration < 0) {
            return;
        }
        HandlerThreadFactory.getWriteLogThreadHandler().post(new Runnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                File[] f = BlockCanaryInternals.getLogFiles();
                if (f != null && f.length > 0) {
                    synchronized (SAVE_DELETE_LOCK) {
                        for (File aF : f) {
                            if (now - aF.lastModified() > duration) {
                                FileUtils.deleteFile(aF);
                                scanFile(BlockCanaryInternals.getContext().provideContext(), aF.getAbsolutePath());
                            }
                        }
                    }
                }
            }
        });
    }

    public static void deleteAll() {
        HandlerThreadFactory.getWriteLogThreadHandler().post(new Runnable() {
            @Override
            public void run() {
                synchronized (SAVE_DELETE_LOCK) {
                    try {
                        FileUtils.deleteDir(new File(BlockCanaryInternals.getPath()));
                        scanFile(BlockCanaryInternals.getContext().provideContext(), BlockCanaryInternals.getPath());
                    } catch (Throwable e) {
                        Log.e(TAG, "deleteAll: ", e);
                    }
                }
            }
        });
    }

    private static String save2Log(String str) {
        String path = "";
        BufferedWriter writer = null;
        try {
            File file = BlockCanaryInternals.detectedBlockDirectory();
            long time = System.currentTimeMillis();
            path = file.getAbsolutePath() + "/"
                    + FILE_NAME_FORMATTER.format(time) + FILE_SUFFIX;
            FileUtils.createFileOrExists(path);

            OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(path, true), "UTF-8");

            writer = new BufferedWriter(out);

            writer.write(BlockInfo.SEPARATOR);
            writer.write("**********************");
            writer.write(BlockInfo.SEPARATOR);
            writer.write(TIME_FORMATTER.format(time) + "(write log time)");
            writer.write(BlockInfo.SEPARATOR);
            writer.write(BlockInfo.SEPARATOR);
            writer.write(str);
            writer.write(BlockInfo.SEPARATOR);

            writer.flush();
            writer.close();
            writer = null;

            toastSavePath(path);
            //通知媒体库更新
            scanFile(BlockCanaryInternals.getContext().provideContext(), path);
        } catch (Throwable t) {
            Log.e(TAG, "save: ", t);
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (Exception e) {
                Log.e(TAG, "save: ", e);
            }
        }
        return path;
    }

    /**
     * 通知媒体库更新
     */
    public static void scanFile(Context context, String filePath) {
        if (context == null) {
            Log.w(TAG, "media scanner scan file fail, context == null");
            return;
        }
        Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        scanIntent.setData(Uri.fromFile(new File(filePath)));
        context.sendBroadcast(scanIntent);
    }

    public static File generateTempZip(String filename) {
        return new File(BlockCanaryInternals.getPath() + "/" + filename + ".zip");
    }

    /**
     * 提示卡顿报告文件保存路径
     *
     * @param path
     */
    private static void toastSavePath(String path) {
        if (BlockCanaryInternals.getContext() == null
                || BlockCanaryInternals.getContext().provideContext() == null
                || TextUtils.isEmpty(path)) {
            return;
        }

        if (!BlockCanaryInternals.getContext().toastFileSavePath()) {
            return;
        }

        Toast.makeText(BlockCanaryInternals.getContext().provideContext()
                , "BlockCanary file save path:" + path
                , Toast.LENGTH_LONG).show();
    }

}
