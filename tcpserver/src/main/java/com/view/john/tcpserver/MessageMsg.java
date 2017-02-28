package com.view.john.tcpserver;

/**
 * 项目名称：TCPUDPDemo
 * 类描述：
 * 创建人：John
 * 创建时间：2017/2/25 22:11
 * 修改人：John
 * 修改时间：2017/2/25 22:11
 * 修改备注：
 */

public class MessageMsg {
    private String message;

    public MessageMsg(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return  message;
    }
}
