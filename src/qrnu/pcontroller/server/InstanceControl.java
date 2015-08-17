package qrnu.pcontroller.server;

import java.net.*;
public class InstanceControl extends Thread {
	
  public void run() {
	 
	 try{
		 new Socket("127.0.0.1", 22222);//创建socket,连接22222端口
		 System.exit(0); //连接成功，说明有实例存在，则退出
	 }catch (Exception e)
	 {}
 
	 try{
		 ServerSocket server = new ServerSocket(22222);//创建socket，连接22222端口
		 while (true)
		 {
			 server.accept(); //接受连接请求
		 }
	 }catch (Exception e)
	 {
		 e.printStackTrace();
	 }
  }
}
