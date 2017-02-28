package com.view.john.tcpserver;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.net.ServerSocket;
import java.net.Socket;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "TcpRunnable";
    private TextView showTv;
    private EditText msgEt;
    private static ServerSocket mServerSocket = null;
    private static Socket mSocket = null;
    private static BufferedReader mBufferedReader = null;
    private static BufferedWriter mBufferedWriter = null;
    private static String msg;
    private static Handler myHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        showTv = (TextView) findViewById(R.id.showMessage);
        msgEt = (EditText) findViewById(R.id.message_et);
        EventBus.getDefault().register(this);
        myHandler = new MyHandler(MainActivity.this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.startServer:
                StartServerRunnable mStartServerRunnable = new StartServerRunnable();
                new Thread(mStartServerRunnable).start();
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
                    Log.d(TAG, "receiver thread open");
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
                    Log.d(TAG, "send thread open");
                    if (msg != null) {
                        Log.d(TAG, msg);
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

    private static  class StartServerRunnable implements  Runnable{

        @Override
        public void run() {
            try {
                if (mServerSocket != null){
                    mServerSocket = new ServerSocket(Constant.PORT);
                    myHandler.sendEmptyMessage(1);
                    while (true) {
                        mSocket = mServerSocket.accept();
                        if( mSocket !=null){
                            myHandler.sendEmptyMessage(2);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    private void closeSocket() throws IOException {
        if (mBufferedReader != null) {
            mBufferedReader.close();
        }
        if (mBufferedWriter != null) {
            mBufferedWriter.close();
        }
        if (mServerSocket != null) {
            mServerSocket.close();
        }
    }

    private static class  MyHandler extends Handler{
        private final WeakReference<MainActivity> mActivity;

        public  MyHandler(MainActivity mainActivity){
            this.mActivity = new WeakReference<MainActivity>(mainActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            MainActivity mainActivity = mActivity.get();
            if (mainActivity != null ){
                switch(msg.what){
                    case  1:
                        Log.d(TAG, "startServer");
                        Toast.makeText(mainActivity, "startServer", Toast.LENGTH_SHORT).show();
                        break;
                    case 2:
                        Toast.makeText(mainActivity, "connect Server", Toast.LENGTH_SHORT).show();
                        ReceiverRunnable mReceiverRunnable = new ReceiverRunnable();
                        new Thread(mReceiverRunnable).start();
                        SendRunnable mSendRunnable = new SendRunnable();
                        new Thread(mSendRunnable).start();
                        break;
                    default:
                        break;

                }
            }

        }
    }

}
