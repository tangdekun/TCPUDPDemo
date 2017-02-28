
package com.view.john.tcpudpdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.orhanobut.logger.Logger;
import com.view.john.tcpudpdemo.bean.MessageMsg;
import com.view.john.tcpudpdemo.tcp.TcpRunnable;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TcpRunnable mTcpRunnable;
    private TextView showTv;
    private EditText msgEt;
    private static Socket mSocket = null;
    private static BufferedReader mBufferedReader = null;
    private static BufferedWriter mBufferedWriter = null;
    private static String msg;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        showTv = (TextView) findViewById(R.id.showMessage);
        msgEt = (EditText) findViewById(R.id.message_et);
        EventBus.getDefault().register(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.startServer:
                try {
                    if (mSocket!=null){
                        mSocket = new Socket(Constant.TARGET_IP, Constant.PORT);
                        ReceiverRunnable mReceiverRunnable = new ReceiverRunnable();
                        new Thread(mReceiverRunnable).start();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.closeServer:
                try {
                    closeSocket();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;

            case R.id.sendMsg:
                msg = msgEt.getText().toString();
                break;

            default:
                break;

        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void showMessage(MessageMsg messageMsg) {
        StringBuilder sb = new StringBuilder(showTv.getText().toString() + "\n");
        sb.append(messageMsg + "\n");
        showTv.setText(sb.toString());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        try {
            closeSocket();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static class ReceiverRunnable implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    mBufferedReader = new BufferedReader(new InputStreamReader(mSocket.getInputStream(), "utf-8"));
                    StringBuilder sb = new StringBuilder();
                    String data = null;
                    while ((data = mBufferedReader.readLine()) != null) {
                        sb.append(data);
                    }
                    EventBus.getDefault().post(new MessageMsg(sb.toString()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static class SendRunnable implements Runnable {

        @Override
        public void run() {
            while (true) {
                try {
                    if (msg != null) {
                        Logger.d(msg);
                        mBufferedWriter = new BufferedWriter(new OutputStreamWriter(mSocket.getOutputStream(), "utf-8"));
                        mBufferedWriter.write(msg + "\n");
                        mBufferedWriter.flush();
                        msg = null;
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void closeSocket() throws IOException {
        if (mBufferedReader != null) {
            mBufferedReader.close();
            mBufferedReader = null;
        }
        if (mBufferedWriter != null) {
            mBufferedWriter.close();
            mBufferedWriter = null;
        }
        if (mSocket != null) {
            mSocket.close();
            mSocket = null;
        }
    }
}
