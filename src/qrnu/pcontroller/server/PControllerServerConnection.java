package qrnu.pcontroller.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class PControllerServerConnection  implements Runnable
{
	private ServerSocket serverSocket;
	private PControllerServer app;
	
	public PControllerServerConnection(PControllerServer app) throws IOException
	{
		this.app = app;
		this.serverSocket = new ServerSocket(58585);
		(new Thread(this)).start();
	}
	
	public void run()
	{
		try
		{
			while (true)
			{
				Socket socket = this.serverSocket.accept();
				PControllerConnection conn = new PControllerConnection(socket);
				new PControllerServerProceedAction(this.app,conn);
			}
		}
		catch (IOException e)
		{
//			e.printStackTrace();
		}
	}
	
	public void close()
	{
		try
		{
			this.serverSocket.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
}
