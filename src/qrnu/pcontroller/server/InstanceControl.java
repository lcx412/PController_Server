package qrnu.pcontroller.server;

import java.net.*;
public class InstanceControl extends Thread {
	
  public void run() {
	 
	 try{
		 new Socket("127.0.0.1", 22222);//����socket,����22222�˿�
		 System.exit(0); //���ӳɹ���˵����ʵ�����ڣ����˳�
	 }catch (Exception e)
	 {}
 
	 try{
		 ServerSocket server = new ServerSocket(22222);//����socket������22222�˿�
		 while (true)
		 {
			 server.accept(); //������������
		 }
	 }catch (Exception e)
	 {
		 e.printStackTrace();
	 }
  }
}
