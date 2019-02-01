package com.example.blockcanary.config;

/**
 * @author hesn
 * @function
 * @date 17-4-7
 * @company 步步高教育电子有限公司
 */

public class Config{

    public static boolean displayNotification = true;

    public static boolean toastFileSavePath = true;

    public static boolean deleteFilesLaunch = false;

    public static boolean monkeyTest = false;

    public static void clean(){
        displayNotification = true;
        toastFileSavePath = true;
        deleteFilesLaunch = false;
        monkeyTest = false;
    }
}
