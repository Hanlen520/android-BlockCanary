## 一、说明
该库改造于[AndroidPerformanceMonitor](https://github.com/markzhai/AndroidPerformanceMonitor)，在AndroidPerformanceMonitor的基础上修改了以下功能：
- 1、解决API版本必须要在21以上才能引用该库的问题；
- 2、解决跑monkey过程中因为误点击AndroidPerformanceMonitor界面的Delete按钮删掉卡顿详情的问题；
- 3、将卡顿信息保存在磁盘的“blockcanary/应用包名/卡顿时间.txt"文件下，方便查看详细的卡顿信息。

### 升级清单文档
- 文档名称：[UPDATE.md](http://172.28.2.93/bfc/BfcBlockCanary/blob/master/UPDATE.md)

## 二、使用说明：

#### 前置条件
##### 1. 需要动态申请的敏感权限
        <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
        <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
        <uses-permission android:name="android.permission.READ_PHONE_STATE"/>
> 5.0以上系统还需要在代码中动态申请权限,具体请查看Android API

##### 2. 已申请的权限
        <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
        <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
        <uses-permission android:name="android.permission.READ_PHONE_STATE"/>
> 在Bfc-BlockCanary库中的AndroidManifest.xml中已申请以上所有权限

#### 配置
- 0、在project的build.gradle中作如下配置：

        // 引入BFC的网络配置
        apply from: "http://172.28.2.93/bfc/Bfc/raw/master/public-config/newbfc-config.gradle"
        allprojects {
            repositories {
                // 配置BFC各版本的仓库地址
                maven { url bfcBuildConfig.MAVEN_URL }
                // Bfc项目的灰度仓库
                maven { url bfcBuildConfig.MAVEN_RC_URL }
                jcenter()
            }
        }
            
- 1、在主module的build.gradle文件中依赖该库：

        dependencies {
           // 只在debug模式下工作
           debugCompile bfcBuildConfig.deps["bfc-blockcanary"]
           // 下单版本和自动化测试模式下，屏蔽掉blockcanary，因为该库在检查卡顿问题时会导致应用的性能问题，应谨慎使用
           releaseCompile bfcBuildConfig.deps["bfc-blockcanary-no-op"]
           testCompile bfcBuildConfig.deps["bfc-blockcanary-no-op"]
         }

- 2、定义AppContext类，继承于BlockCanaryContext，用于对BlockCanary的配置：

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
                return BuildConfig.DEBUG;
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
             * 是否每次app启动都删除所有卡顿报告文件
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
             *
             * 跑monkey时候请设置为true,避免monkey经常跑到显示卡顿信息机界面,效果:<br>
             * 1.如果设置通知栏提示,在monkeyTest下点击通知栏不会跳转到卡顿信息界面{@link com.github.moduth.blockcanary.ui.DisplayActivity};<br>
             * 2.桌面屏蔽卡顿信息界面{@link com.github.moduth.blockcanary.ui.DisplayActivity}入口.<br>
             *
             * 注意:桌面刷新是不及时的.如果发现app自己的桌面图标消失了,是因为桌面没有即时更新,请到 设置 --> 应用程序 --> 清除桌面数据,图标就可以正常显示了.
             *
             * @return 默认false提示
             */
            public boolean monkeyTest() {
                return false;
            }
        }
         
> **跑monkey** 时候monkeyTest()设置为true,可以避免monkey跑到显示卡顿信息机界面, **卡顿的图标会消失** ,这属于正常现象.
>注意:如果发现app自己的 **桌面图标消失** 了,是桌面刷新不及时,请到 设置 --> 应用程序 --> 清除桌面数据,图标就可以正常显示了.

- 3、在主工程的Application类中做初始化：
        
        public class DemoApplication extends Application {
            private static Context sContext;
        
            @Override
            public void onCreate() {
                super.onCreate();
                sContext = this;
                BlockCanary.install(this, new AppContext()).start();
            }
        
            public static Context getAppContext() {
                return sContext;
            }
        }

- 4、退出app或不需要时请调用销毁:

        BlockCanary.get().destroy();

#### 可选功能
- BlockCanary 桌面图标名称修改：

    strings.xml

        <string name="block_canary_display_activity_label">隔壁老王的Block</string>
        
- 删除卡顿报告记录文件

        BlockCanary.get().deleteAllFiles();

## 三、更为详细地使用说明请查看blockcanary的[wiki文档](http://blog.zhaiyifan.cn/2016/01/16/BlockCanaryTransparentPerformanceMonitor/)

## 四、TODO

- 将卡顿详情上传到后台；

## 五、依赖
本项目已集成:
- bfc-common

## 六、联系人
- 何思宁
- 工号：20251494
        