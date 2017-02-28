package com.view.john.tcpudpdemo.tcp;


import com.orhanobut.logger.Logger;
import com.view.john.tcpudpdemo.Constant;
import com.view.john.tcpudpdemo.bean.MessageMsg;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class TcpRunnable implements Runnable{

	private Socket mServerSocket = null;
	private BufferedReader mBufferedReader =null;
	private BufferedWriter mBufferedWriter = null;
	private String Message = null;

	public String getMessage() {
		return Message;
	}

	public void setMessage(String message) {
		Message = message;
	}

	public boolean isConnected() {
		return isConnected;
	}

	public void setConnected(boolean connected) {
		isConnected = connected;
	}

	private boolean isConnected = true;

	public void run(){

		try {
//			createSocket();
			while(isConnected){
				if(mServerSocket == null){
					createSocket();
					if (getMessage()!=null){
						Logger.d(getMessage());
						sendMessage(getMessage());
						setMessage(null);
					}


				}
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

	private void createSocket() throws IOException{
		mServerSocket = new Socket(Constant.TARGET_IP,Constant.PORT);
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
		mBufferedReader = new BufferedReader(new InputStreamReader(mServerSocket.getInputStream(),"utf-8"));
		StringBuilder sb = new StringBuilder();
		String data= null;
		while((data = mBufferedReader.readLine())!=null){
			sb.append(data);
		}
		EventBus.getDefault().post(new MessageMsg(sb.toString()));
		return sb.toString();

	}

	public void sendMessage(String msg ) throws IOException{
		if (mServerSocket == null)
			createSocket();
		Logger.d(msg);
		mBufferedWriter = new BufferedWriter(new OutputStreamWriter(mServerSocket.getOutputStream(),"utf-8"));
		mBufferedWriter.write(msg+"\n");
		mBufferedWriter.flush();
	}


}
