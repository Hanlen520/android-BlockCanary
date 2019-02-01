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
package com.example.blockcanary.test;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.blockcanary.R;

import java.io.FileInputStream;
import java.io.IOException;

public class DemoFragment extends Fragment implements View.OnClickListener {

    private static final String DEMO_FRAGMENT = "DemoFragment";

    public static DemoFragment newInstance() {
        return new DemoFragment();
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_main, null);
    }

    @Override
    public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setClickListener(view, R.id.button1, R.id.button2, R.id.button3, R.id.button4);
    }

    private void setClickListener(View view, int... ids) {
        for (int id : ids) {
            view.findViewById(id).setOnClickListener(this);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button1:
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Log.e(DEMO_FRAGMENT, "onClick of R.id.button1: ", e);
                }
                break;
            case R.id.button2:
                for (int i = 0; i < 100; ++i) {
                    readFile();
                }
                break;
            case R.id.button3:
                double result = compute();
                System.out.println(result);
                break;
            case R.id.button4:
                //更多
                startActivity(new Intent(getActivity(), MoreActivity.class));
                break;
            default:
                break;
        }
    }

    private static double compute() {
        double result = 0;
        for (int i = 0; i < 1000000; ++i) {
            result += Math.acos(Math.cos(i));
            result -= Math.asin(Math.sin(i));
        }
        return result;
    }

    private static void readFile() {
        FileInputStream reader = null;
        try {
            reader = new FileInputStream("/proc/stat");
            while (reader.read() != -1) ;
        } catch (IOException e) {
            Log.e(DEMO_FRAGMENT, "readFile: /proc/stat", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(DEMO_FRAGMENT, " on close reader ", e);
                }
            }
        }
    }
}
