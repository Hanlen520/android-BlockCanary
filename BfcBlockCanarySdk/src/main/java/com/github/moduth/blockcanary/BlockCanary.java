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

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.eebbk.bfc.common.app.ToastUtils;
import com.github.moduth.blockcanary.internal.CommonUtils;
import com.github.moduth.blockcanary.ui.DisplayActivity;
import com.github.moduth.blockcanary.version.SDKVersion;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.security.auth.DestroyFailedException;
import javax.security.auth.Destroyable;

import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
import static android.content.pm.PackageManager.DONT_KILL_APP;

public final class BlockCanary implements Destroyable {

    private static final String TAG = BlockCanary.class.getName();

    private static BlockCanary sInstance;
    private BlockCanaryInternals mBlockCanaryCore;
    private boolean mMonitorStarted = false;

    private BlockCanary() {
        BlockCanaryInternals.setContext(BlockCanaryContext.get());
        mBlockCanaryCore = BlockCanaryInternals.getInstance();
        mBlockCanaryCore.addBlockInterceptor(BlockCanaryContext.get());
        if (!BlockCanaryContext.get().displayNotification()) {
            return;
        }
        mBlockCanaryCore.addBlockInterceptor(new DisplayService());
        // 检查通知是否授权
        if (!CommonUtils.isNotificationEnabled(BlockCanaryInternals.getContext().provideContext())) {
            ToastUtils.getInstance(BlockCanaryInternals.getContext().provideContext())
                    .l(SDKVersion.getLibraryName() + ": 通知未授权,请到\"设置\"->\"通知管理\"中授权通知.").show();
        }
    }

    /**
     * Install {@link BlockCanary}
     *
     * @param context            Application context
     * @param blockCanaryContext BlockCanary context
     * @return {@link BlockCanary}
     */
    public static BlockCanary install(@NonNull Context context, @NonNull BlockCanaryContext blockCanaryContext) {
        Log.i(TAG, getVersion());
        if (blockCanaryContext == null) {
            throw new RuntimeException("BlockCanaryContext null");
        }
        if (blockCanaryContext == null) {
            throw new RuntimeException("Context null");
        }
        BlockCanaryContext.init(context.getApplicationContext(), blockCanaryContext);
//        setEnabled(context, DisplayActivity.class, BlockCanaryContext.get().displayNotification());
        setEnabled(context.getApplicationContext(), DisplayActivity.class, !BlockCanaryContext.get().monkeyTest());
        return get();
    }

    /**
     * Get {@link BlockCanary} singleton.
     *
     * @return {@link BlockCanary} instance
     */
    public static BlockCanary get() {
        if (sInstance == null) {
            synchronized (BlockCanary.class) {
                if (sInstance == null) {
                    sInstance = new BlockCanary();
                }
            }
        }
        return sInstance;
    }

    /**
     * Start monitoring.
     */
    public void start() {
        if (!mMonitorStarted) {
            mMonitorStarted = true;
            Looper.getMainLooper().setMessageLogging(mBlockCanaryCore.monitor);
        }
    }

    /**
     * Stop monitoring.
     */
    public void stop() {
        if (mMonitorStarted) {
            mMonitorStarted = false;
            Looper.getMainLooper().setMessageLogging(null);
            mBlockCanaryCore.stackSampler.stop();
            mBlockCanaryCore.cpuSampler.stop();
        }
    }

    /**
     * Zip and upload log files, will user context's zip and log implementation.
     */
    public void upload() {
        Uploader.zipAndUpload();
    }

    /**
     * Record monitor start time to preference, you may use it when after push which tells start
     * BlockCanary.
     */
    public void recordStartTime() {
        PreferenceManager.getDefaultSharedPreferences(BlockCanaryContext.get().provideContext())
                .edit()
                .putLong("BlockCanary_StartTime", System.currentTimeMillis())
                .commit();
    }

    /**
     * Is monitor duration end, compute from recordStartTime end provideMonitorDuration.
     *
     * @return true if ended
     */
    public boolean isMonitorDurationEnd() {
        long startTime =
                PreferenceManager.getDefaultSharedPreferences(BlockCanaryContext.get().provideContext())
                        .getLong("BlockCanary_StartTime", 0);
        return startTime != 0 && System.currentTimeMillis() - startTime >
                BlockCanaryContext.get().provideMonitorDuration() * 3600 * 1000;
    }

    /**
     * 删除所有卡顿报告文件
     */
    public void deleteAllFiles() {
        LogWriter.deleteAll();
    }

    // these lines are originally copied from LeakCanary: Copyright (C) 2015 Square, Inc.
    private static final Executor fileIoExecutor = newSingleThreadExecutor("File-IO");

    private static void setEnabledBlocking(Context appContext,
                                           Class<?> componentClass,
                                           boolean enabled) {
        ComponentName component = new ComponentName(appContext, componentClass);
        PackageManager packageManager = appContext.getPackageManager();
        int newState = enabled ? COMPONENT_ENABLED_STATE_ENABLED : COMPONENT_ENABLED_STATE_DISABLED;
        // Blocks on IPC.
        packageManager.setComponentEnabledSetting(component, newState, DONT_KILL_APP);
    }
    // end of lines copied from LeakCanary

    private static void executeOnFileIoThread(Runnable runnable) {
        fileIoExecutor.execute(runnable);
    }

    private static Executor newSingleThreadExecutor(String threadName) {
        return Executors.newSingleThreadExecutor(new SingleThreadFactory(threadName));
    }

    private static void setEnabled(Context context,
                                   final Class<?> componentClass,
                                   final boolean enabled) {
        final Context appContext = context.getApplicationContext();
        executeOnFileIoThread(new Runnable() {
            @Override
            public void run() {
                setEnabledBlocking(appContext, componentClass, enabled);
            }
        });
    }

    /**
     * 销毁
     *
     * @throws DestroyFailedException
     */
    @Override
    public void destroy() throws DestroyFailedException {
        synchronized (BlockCanary.class) {
            stop();
            BlockCanaryInternals.getInstance().destroy();
            mBlockCanaryCore = null;
            sInstance = null;
        }
    }

    /**
     * 是否已经销毁
     *
     * @return
     */
    @Override
    public boolean isDestroyed() {
        return sInstance == null;
    }

    /**
     * 获取 Bfc-BlockCanary 版本信息
     *
     * @return
     */
    public static String getVersion() {
        return TextUtils.concat(
                SDKVersion.getLibraryName(), ", version: ", SDKVersion.getVersionName(),
                " code: ", String.valueOf(SDKVersion.getSDKInt()),
                " build: ", SDKVersion.getBuildName()
        ).toString();
    }
}
