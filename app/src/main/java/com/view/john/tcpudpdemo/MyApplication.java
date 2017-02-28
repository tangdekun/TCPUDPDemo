package com.view.john.tcpudpdemo;

import android.app.Application;

import com.orhanobut.logger.Logger;

/**
 * 项目名称：TCPUDPDemo
 * 类描述：
 * 创建人：John
 * 创建时间：2017/2/25 23:20
 * 修改人：John
 * 修改时间：2017/2/25 23:20
 * 修改备注：
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Logger.init("ServerApplication");
    }
}
