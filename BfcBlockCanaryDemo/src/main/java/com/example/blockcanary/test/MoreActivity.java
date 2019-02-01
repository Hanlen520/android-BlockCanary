package com.example.blockcanary.test;

import android.view.View;

import com.example.blockcanary.ABaseActivity;
import com.example.blockcanary.R;
import com.github.moduth.blockcanary.BlockCanary;

/**
 * @author hesn
 * @function
 * @date 17-4-13
 * @company 步步高教育电子有限公司
 */

public class MoreActivity extends ABaseActivity {
    @Override
    protected int setLayoutId() {
        return R.layout.activity_more_layout;
    }

    @Override
    protected void initView() {

    }

    @Override
    protected void initData() {

    }

    /**
     * 删除
     *
     * @param view
     */
    public void onDelete(View view) {
        BlockCanary.get().deleteAllFiles();
    }
}
