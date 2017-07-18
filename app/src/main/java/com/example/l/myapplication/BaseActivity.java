package com.example.l.myapplication;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Abner on 2017/6/9.
 */


public abstract class BaseActivity extends AppCompatActivity {
    //
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.activity = this;
        setContentView(layoutId());
        App.activity=this;
        initView();
        initData();
        initListener();
    }




    //指定布局
    protected abstract int layoutId();

    //初始化布局
    protected abstract void initView();

    //初始化数据
    protected abstract void initData();

    //加载数据
    protected abstract void loadData();

    //加载监听
    protected abstract void initListener();


    //activity 暂停的时候
    @Override
    protected void onPause() {
        super.onPause();
    }

    //activity 重新展示的时候
    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }
    //activity activity停止的时候
    @Override
    protected void onStop() {
        super.onStop();
    }
    // activity摧毁的的时候
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
