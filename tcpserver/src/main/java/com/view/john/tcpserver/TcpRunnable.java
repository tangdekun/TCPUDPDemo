package com.view.john.tcpserver;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class TcpRunnable implements Runnable{

	private static final String TAG = "TcpRunnable" ;
	private ServerSocket mServerSocket = null;
	private BufferedReader mBufferedReader =null;
	private BufferedWriter mBufferedWriter = null;
	private Socket mSocket = null;

	public boolean isConnected() {
		return isConnected;
	}

	public void setConnected(boolean connected) {
		isConnected = connected;
	}

	private boolean isConnected = true;

	public void run(){
		ServerSocket mServerSocket = null;
		try {
			createServerSocket();
			while(isConnected){
				createSocket();
				getInputMessage();
			}
			if (!isConnected){
				closeSocket();
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void createServerSocket() throws IOException{
		mServerSocket = new ServerSocket(Constant.PORT);
		Log.d(TAG," createServerSocket");
	}

	private void createSocket() throws IOException{
		mSocket  = mServerSocket.accept();
		Log.d(TAG,"createSocket()");

	}



	private void closeSocket() throws IOException{
		if (mBufferedReader != null) {
			mBufferedReader.close();
		}
		if (mBufferedWriter!= null) {
			mBufferedWriter.close();
		}
		if (mServerSocket != null) {
			mServerSocket.close();
		}
	}

	private String getInputMessage() throws IOException{
		mBufferedReader = new BufferedReader(new InputStreamReader(mSocket.getInputStream(),"utf-8"));
		StringBuilder sb = new StringBuilder();
		String data= null;
		while((data = mBufferedReader.readLine())!=null){
			sb.append(data);
		}
		EventBus.getDefault().post(new MessageMsg(sb.toString()));
		return sb.toString();

	}

	public void sendMessage(String msg ) throws IOException{
		mBufferedWriter = new BufferedWriter(new OutputStreamWriter(mSocket.getOutputStream(),"utf-8"));
		mBufferedWriter.write(msg+"\n");
		mBufferedWriter.flush();
	}


}
