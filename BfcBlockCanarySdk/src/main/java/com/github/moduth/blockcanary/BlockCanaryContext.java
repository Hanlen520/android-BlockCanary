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

import com.github.moduth.blockcanary.internal.BlockInfo;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * User should provide a real implementation of this class to use BlockCanary.
 */
public class BlockCanaryContext implements BlockInterceptor {

    private static Context sApplicationContext;
    private static BlockCanaryContext sInstance = null;

    public BlockCanaryContext() {
    }

    static void init(Context context, BlockCanaryContext blockCanaryContext) {
        sApplicationContext = context.getApplicationContext();
        sInstance = blockCanaryContext;
    }

    public static BlockCanaryContext get() {
        if (sInstance == null) {
            throw new RuntimeException("BlockCanaryContext null");
        } else {
            return sInstance;
        }
    }

    /**
     * 提供app的上下文
     * <p>
     * Provide application context.
     * </p>
     */
    public Context provideContext() {
        return sApplicationContext;
    }

    /**
     * 限定符,显示app信息
     * <p>
     * Implement in your project.
     * </p>
     *
     * @return Qualifier which can specify this installation, like version + flavor.
     */
    public String provideQualifier() {
        return "unknown";
    }

    /**
     * 用户id
     * <p>
     * Implement in your project.
     * </p>
     *
     * @return user id
     */
    public String provideUid() {
        return "uid";
    }

    /**
     * Network type
     *
     * @return {@link String} like 2G, 3G, 4G, wifi, etc.
     */
    public String provideNetworkType() {
        return "unknown";
    }

    /**
     * 设置监控的时间,单位为小时,配合{@code BlockCanary}的 isMonitorDurationEnd 和 recordStartTime 可以自行实现超过监控时间关闭监控
     * <p>
     * Config monitor duration, after this time BlockCanary will stop, use
     * with {@code BlockCanary}'s isMonitorDurationEnd
     * </p>
     *
     * @return monitor last duration (in hour)
     */
    public int provideMonitorDuration() {
        return -1;
    }

    /**
     * 多长时间的阻塞算是卡顿，单位是毫秒
     * <p>
     * Config block threshold (in millis), dispatch over this duration is regarded as a BLOCK. You may set it
     * from performance of device.
     * </p>
     *
     * @return threshold in mills
     */
    public int provideBlockThreshold() {
        return 1000;
    }

    /**
     * Thread stack dump interval, use when block happens, BlockCanary will dump on main thread
     * stack according to current sample cycle.
     * <p>
     * Because the implementation mechanism of Looper, real dump interval would be longer than
     * the period specified here (especially when cpu is busier).
     * </p>
     *
     * @return dump interval (in millis)
     */
    public int provideDumpInterval() {
        return provideBlockThreshold();
    }

    /**
     * 卡顿报告日志保存路径,如:"/blockcanary/com.eebbk.xxx/",其中包名的路径会自动添加
     * <p>
     * Path to save log, like "/blockcanary/", will save to sdcard if can.
     *
     * @return path of log files
     */
    public String providePath() {
        return "/blockcanary/";
    }

    /**
     * 是否需要通知栏提示
     * <p>
     * If need notification to notice block.
     * <p/>
     *
     * @return true if need, else if not need.
     */
    public boolean displayNotification() {
        return true;
    }

    /**
     * Implement in your project, bundle files into a zip file.
     *
     * @param src  files before compress
     * @param dest files compressed
     * @return true if compression is successful
     */
    public boolean zip(File[] src, File dest) {
        return false;
    }

    /**
     * Implement in your project, bundled log files.
     *
     * @param zippedFile zipped file
     */
    public void upload(File zippedFile) {
        throw new UnsupportedOperationException();
    }

    /**
     * 关注的包名
     * <p>
     * Packages that developer concern, by default it uses process name,
     * put high priority one in pre-order.
     * </p>
     *
     * @return null if simply concern only package with process name.
     */
    public List<String> concernPackages() {
        return null;
    }

    /**
     * 过滤不关注的堆栈,需要配合 {@code concernPackages()} 一起使用
     * <p>
     * Filter stack without any in concern package, used with {@code concernPackages}.
     * </p>
     *
     * @return true if filter, false it not.
     */
    public boolean filterNonConcernStack() {
        return false;
    }

    /**
     * 白名单，哪些包名的卡顿不算在内
     * <p>
     * Provide white list, entry in white list will not be shown in ui list.
     * </p>
     *
     * @return return null if you don't need white-list filter.
     */
    public List<String> provideWhiteList() {
        LinkedList<String> whiteList = new LinkedList<>();
        whiteList.add("org.chromium");
        return whiteList;
    }

    /**
     * 是否需要删除白名单对应的卡顿报告文件
     * <p>
     * Whether to delete files whose stack is in white list, used with white-list.
     * </p>
     *
     * @return true if delete, false it not.
     */
    public boolean deleteFilesInWhiteList() {
        return true;
    }

    /**
     * 出现卡顿时,会返回卡顿信息
     * <p>
     * Block interceptor, developer may provide their own actions.
     * </p>
     *
     * @param context
     * @param blockInfo 卡顿信息
     */
    @Override
    public void onBlock(Context context, BlockInfo blockInfo) {

    }

    /**
     * Whether to stop monitoring when in debug mode.
     *
     * @return true if stop, false otherwise
     */
    public boolean stopWhenDebugging() {
        return true;
    }

    /**
     * 删除卡顿报告文件时间间隔
     *
     * @return 单位:毫秒 (-1:不执行此功能)
     */
    public long deleteFilesDuration() {
        // 默认删除2天前的文件
        return 2 * 24 * 3600 * 1000L;
    }

    /**
     * 是否每次初始化都删除所有卡顿报告文件
     * <p>
     * 注意:跑monkey的时候记得设置为false
     * </p>
     *
     * @return 默认false
     */
    public boolean deleteFilesLaunch() {
        return false;
    }

    /**
     * 是否toast提示卡顿报告文件保存路径
     *
     * @return 默认true提示
     */
    public boolean toastFileSavePath() {
        return true;
    }

    /**
     * 是否monkey测试
     * <p>
     * 跑monkey时候请设置为true,避免monkey经常跑到显示卡顿信息机界面,效果:<br>
     * 1.如果设置通知栏提示,在monkeyTest下点击通知栏不会跳转到卡顿信息界面{@link com.github.moduth.blockcanary.ui.DisplayActivity};<br>
     * 2.桌面屏蔽卡顿信息界面{@link com.github.moduth.blockcanary.ui.DisplayActivity}入口.<br>
     * <p/>
     * <p>
     * 注意:桌面刷新是不及时的.如果发现app自己的桌面图标消失了,是因为桌面没有即时更新,请到 设置 --> 应用程序 --> 清除桌面数据,图标就可以正常显示了.
     * <p/>
     *
     * @return 默认false提示
     */
    public boolean monkeyTest() {
        return false;
    }
}