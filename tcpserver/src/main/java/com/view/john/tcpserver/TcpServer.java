package com.view.john.tcpserver;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

public class TcpServer extends Service {

    private boolean mIsServiceDestroyed = false;
    private String[] mDefaultMessages = new String[]
            {
                    "你好啊，哈哈",
                    "请问你叫什么名字啊？",
                    "今天天气不错",
                    "和你聊天很开心",
                    "晚安"

            };
    private BufferedReader mBufferedReader;
    private PrintWriter mBufferedWriter;
    private ServerSocket mServerSocket;

    @Override
    public void onCreate() {
        super.onCreate();
        new Thread(new TcpServerRunnable()).start();
        Log.d(MainActivity.TAG, "onCreate");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private class TcpServerRunnable implements Runnable {

        @Override
        public void run() {
            Log.d(MainActivity.TAG, "run");
            ServerSocket serverSocket = null;
            try {
                serverSocket = new ServerSocket(Constant.PORT);
                mServerSocket = serverSocket;
            } catch (IOException e) {
                System.err.println("establish tcp server failed ,port:" + Constant.PORT);
                e.printStackTrace();
                return;
            }
            while (!mIsServiceDestroyed) {
                try {
                    final Socket client = serverSocket.accept();
                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                responseClient(client);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        private void responseClient(Socket client) throws IOException {
            mBufferedReader = new BufferedReader(new InputStreamReader(client.getInputStream()));
            mBufferedWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream())), true);
            mBufferedWriter.println("welcome to here");
            while (!mIsServiceDestroyed) {
                String str = mBufferedReader.readLine();
                if (str != null) {
                    String msg = mDefaultMessages[new Random().nextInt(mDefaultMessages.length)];
                    mBufferedWriter.println(msg);
                }
            }
            mBufferedWriter.close();
            mBufferedReader.close();
            client.close();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(MainActivity.TAG," onDestroy");
        mIsServiceDestroyed = true;
//        try {
//            closeSocket();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

    }
    /**
     * 关闭输出流、输入流、ServerSocket
     * @author John
     * @time 2017/3/3 0:13
     * @param
     * @return
     *
     */
    private void closeSocket() throws IOException {
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
}
