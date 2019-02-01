package com.example.blockcanary.config;

import android.widget.Switch;

import com.example.blockcanary.ABaseActivity;
import com.example.blockcanary.R;

/**
 * @author hesn
 * @function 配置界面
 * @date 17-4-7
 * @company 步步高教育电子有限公司
 */

public class ConfigActivity extends ABaseActivity {

    private Switch mNotificationSwitch;
    private Switch mToastFileSavePathSwitch;
    private Switch mDeleteFilesLaunchSwitch;
    private Switch mMonkeyTestSwitch;
    private ConfigPresenter mPresenter;

    @Override
    protected int setLayoutId() {
        return R.layout.activity_config_layout;
    }

    @Override
    protected void initView() {
        mNotificationSwitch = (Switch) findViewById(R.id.notificationSwitch);
        mToastFileSavePathSwitch = (Switch) findViewById(R.id.toastFileSavePathSwitch);
        mDeleteFilesLaunchSwitch = (Switch) findViewById(R.id.deleteFilesLaunchSwitch);
        mMonkeyTestSwitch = (Switch) findViewById(R.id.monkeyTestSwitch);
    }

    @Override
    protected void initData() {
        mPresenter = new ConfigPresenter();
        mNotificationSwitch.setChecked(mPresenter.enableNotification());
        mToastFileSavePathSwitch.setChecked(mPresenter.enableToastFileSavePath());
        mDeleteFilesLaunchSwitch.setChecked(mPresenter.enableDeleteFilesLaunch());
        mMonkeyTestSwitch.setChecked(mPresenter.enableMonkeyTest());
    }

    @Override
    protected void onPause() {
        mPresenter.save(mNotificationSwitch.isChecked()
                , mToastFileSavePathSwitch.isChecked()
                , mDeleteFilesLaunchSwitch.isChecked()
                , mMonkeyTestSwitch.isChecked()
        );
        super.onPause();
    }
}
