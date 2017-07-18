package com.example.l.myapplication;

/**
 * Created by Abner on 2017/6/13.
 */

public  class BlueToothContion {

    //蓝牙设备UUid  socket进行通讯最主要的协议
    public static String UUID="00001101-0000-1000-8000-00805F9B34FB";
    //开始测量
    public static final byte[] START_MEASURE = { (byte) 0xEB, 0x20, 0x00, (byte) 0xf5, (byte) 0xEB };
    //停止测量
    public static final byte[] STOP_MEASURE = { (byte) 0xEB, 0x20, 0x01, (byte) 0xf4, (byte) 0xEB };
    //获取结果
    public static final byte[] GET_REUSLT = { (byte) 0xEB, 0x21, (byte) 0xf4, (byte) 0xEB };
    //获取电量
    public static final byte[] GET_BATTERY = { (byte) 0xEB, 0x02, 0x13, (byte) 0xEB };
}
