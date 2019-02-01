package com.example.blockcanary.config;

import com.example.blockcanary.AppContext;

/**
 * @author hesn
 * @function
 * @date 17-4-7
 * @company 步步高教育电子有限公司
 */

public class ConfigPresenter {

    public static final AppContext mAppContext = new AppContext();

    /**
     * 通知栏
     * @return
     */
    public boolean enableNotification(){
        return Config.displayNotification;
    }

    /**
     * toast提示卡顿报告文件保存路径
     * @return
     */
    public boolean enableToastFileSavePath(){
        return Config.toastFileSavePath;
    }

    /**
     * 每次初始化都删除所有卡顿报告文件
     * @return
     */
    public boolean enableDeleteFilesLaunch(){
        return Config.deleteFilesLaunch;
    }

    /**
     * monkey测试
     * @return
     */
    public boolean enableMonkeyTest(){
        return Config.monkeyTest;
    }

    public void save(boolean...switchs){
        Config.displayNotification = switchs[0];
        Config.toastFileSavePath = switchs[1];
        Config.deleteFilesLaunch = switchs[2];
        Config.monkeyTest = switchs[3];
    }
}
