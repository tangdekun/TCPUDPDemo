package com.view.john.tcpserver;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.net.ServerSocket;
import java.net.Socket;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String TAG = "TcpRunnable";
    private TextView showTv;
    private EditText msgEt;
    private static ServerSocket mServerSocket = null;
    private static Socket mSocket = null;
    private static BufferedReader mBufferedReader = null;
    private static BufferedWriter mBufferedWriter = null;
    private static String msg;
    private static Handler myHandler;
    private static final int MESSAGE_RECEIVER_NEW_MSG = 1;
    private static final int MESSAGE_SOCKET_CONNECTED = 2;
    private static boolean IS_CONNENTED = false;
    Intent serverintent = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        showTv = (TextView) findViewById(R.id.showMessage);
        msgEt = (EditText) findViewById(R.id.message_et);
        myHandler = new MyHandler(MainActivity.this);
        serverintent = new Intent(MainActivity.this, TcpServer.class);


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.startServer:
               startService(serverintent);
                IS_CONNENTED = true;
                StartSocketRunnable mStartServerRunnable = new StartSocketRunnable();
                new Thread(mStartServerRunnable).start();
                break;
            case R.id.closeServer:
               stopService(serverintent);
                try {
                    closeSocket();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.sendMsg:
                msg = msgEt.getText().toString();
                if (msg != null) {
                    StringBuilder sb = new StringBuilder(showTv.getText() + "\n");
                    sb.append("client:\n" + msg + "\n");
                    showTv.setText(sb.toString());
                    try {
                        mBufferedWriter = new BufferedWriter(new OutputStreamWriter(mSocket.getOutputStream(), "utf-8"));
                        mBufferedWriter.write(msg + "\n");
                        mBufferedWriter.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;

            default:
                break;

        }

    }

//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void showMessage(MessageMsg messageMsg) {
//        StringBuilder sb = new StringBuilder(showTv.getText().toString() + "\n");
//        sb.append(messageMsg + "\n");
//        showTv.setText(sb.toString());
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        EventBus.getDefault().unregister(this);
        try {
            closeSocket();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


//    private static class ReceiverRunnable implements Runnable {
//        @Override
//        public void run() {
//            while (true) {
//                try {
//                    Log.d(TAG, "receiver thread open");
//                    mBufferedReader = new BufferedReader(new InputStreamReader(mSocket.getInputStream(), "utf-8"));
//                    StringBuilder sb = new StringBuilder();
//                    String data = null;
//                    while ((data = mBufferedReader.readLine()) != null) {
//                        sb.append(data);
//                    }
//                    EventBus.getDefault().post(new MessageMsg(sb.toString()));
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }

//    private static class SendRunnable implements Runnable {
//
//        @Override
//        public void run() {
//            while (true) {
//                try {
//                    Log.d(TAG, "send thread open");
//                    if (msg != null) {
//                        Log.d(TAG, msg);
//                        mBufferedWriter = new BufferedWriter(new OutputStreamWriter(mSocket.getOutputStream(), "utf-8"));
//                        mBufferedWriter.write(msg + "\n");
//                        mBufferedWriter.flush();
//                        msg = null;
//                    }
//
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }

//    private static class StartServerRunnable implements Runnable {
//
//        @Override
//        public void run() {
//            try {
//                if (mServerSocket != null) {
//                    mServerSocket = new ServerSocket(Constant.PORT);
//                    myHandler.sendEmptyMessage(1);
//                    while (true) {
//                        mSocket = mServerSocket.accept();
//                        if (mSocket != null) {
//                            myHandler.sendEmptyMessage(2);
//                        }
//                    }
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }


    private class StartSocketRunnable implements Runnable {
        @Override
        public void run() {
            Socket socket = null;
            while (socket == null) {
                try {
                    socket = new Socket(getLocal(), Constant.PORT);
                    mSocket = socket;
                    mBufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                    myHandler.sendEmptyMessage(MESSAGE_RECEIVER_NEW_MSG);
                } catch (IOException e) {
                    SystemClock.sleep(1000);
                    e.printStackTrace();
                }
            }
            try {
                mBufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                while (IS_CONNENTED) {

                    String msg = mBufferedReader.readLine();
                    if (msg != null) {
                        String showedMsg = "server:\n" + msg + "\n";
                        Message contentmsg = myHandler.obtainMessage();
                        contentmsg.what = MESSAGE_SOCKET_CONNECTED;
                        contentmsg.obj = showedMsg;
                        myHandler.sendMessage(contentmsg);
                    }
                }
                mBufferedWriter.close();
                mBufferedReader.close();
                mSocket.close();

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

        /**
         * 关闭Socket
         *
         * @param
         * @return
         * @author John
         * @time 2017/3/2 23:47
         */
        private void closeSocket() throws IOException {
            IS_CONNENTED = false;
            if (mBufferedWriter != null) {
                mBufferedWriter.close();
                mBufferedWriter = null;
            }
            if (mBufferedReader != null) {
                mBufferedReader.close();
                mBufferedReader = null;
            }

            if (mServerSocket != null) {
                mServerSocket.close();
                mServerSocket = null;
            }


        }

        private static class MyHandler extends Handler {
            private final WeakReference<MainActivity> mActivity;

            public MyHandler(MainActivity mainActivity) {
                this.mActivity = new WeakReference<MainActivity>(mainActivity);
            }

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                MainActivity mainActivity = mActivity.get();
                if (mainActivity != null) {
                    switch (msg.what) {
                        case MESSAGE_RECEIVER_NEW_MSG:
                            Log.d(TAG, "startServer");
                            Toast.makeText(mainActivity, "startServer", Toast.LENGTH_SHORT).show();
                            break;
                        case MESSAGE_SOCKET_CONNECTED:

                            String content = (String) msg.obj;
                            StringBuilder sb = new StringBuilder(mainActivity.showTv.getText() + "\n");
                            sb.append(content);
                            mainActivity.showTv.setText(sb.toString());

                            break;
                        default:
                            break;

                    }
                }

            }
        }

    private String intToIp(int i) {

        return (i & 0xFF ) + "." +
                ((i >> 8 ) & 0xFF) + "." +
                ((i >> 16 ) & 0xFF) + "." +
                ( i >> 24 & 0xFF) ;
    }

    private  String getLocal(){
        //获取wifi服务
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        //判断wifi是否开启
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        String ip = intToIp(ipAddress);
        return  ip;
    }
}

