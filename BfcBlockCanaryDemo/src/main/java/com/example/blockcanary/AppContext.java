/*
 * Copyright (C) 2016 MarkZhai (http://zhaiyifan.cn).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.blockcanary;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.example.blockcanary.config.Config;
import com.github.moduth.blockcanary.BlockCanaryContext;

import java.util.List;

public class AppContext extends BlockCanaryContext {
    private static final String TAG = "AppContext";

    @Override
    public String provideQualifier() {
        String qualifier = "";
        try {
            PackageInfo info = DemoApplication.getAppContext().getPackageManager()
                    .getPackageInfo(DemoApplication.getAppContext().getPackageName(), 0);
            qualifier += info.versionCode + "_" + info.versionName + "_YYB";
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "provideQualifier exception", e);
        }

        return qualifier;
    }

    @Override
    public String provideUid() {
        // 用户ID，对于我们来讲，可以随便写
        return "C2";
    }

    @Override
    public String provideNetworkType() {
        // 这个不知道是干什么的，先不管它
        return "4G";
    }

    @Override
    public int provideMonitorDuration() {
        // 单位是毫秒
        return 10*1000;
    }

    @Override
    public int provideBlockThreshold() {
        // 主要配置这里，多长时间的阻塞算是卡顿，单位是毫秒
        return 500;
    }

    @Override
    public boolean displayNotification() {
//        return BuildConfig.DEBUG;
        return Config.displayNotification;
    }

    @Override
    public List<String> concernPackages() {
        List<String> list = super.provideWhiteList();

        return list;
    }

    @Override
    public List<String> provideWhiteList() {
        // 白名单，哪些包名的卡顿不算在内
        List<String> list = super.provideWhiteList();
        return list;
    }

    @Override
    public boolean stopWhenDebugging() {
        return true;
    }

    @Override
    public boolean deleteFilesLaunch() {
        return Config.deleteFilesLaunch;
    }

    @Override
    public boolean toastFileSavePath() {
        return Config.toastFileSavePath;
    }

    @Override
    public boolean monkeyTest() {
        return Config.monkeyTest;
    }
}