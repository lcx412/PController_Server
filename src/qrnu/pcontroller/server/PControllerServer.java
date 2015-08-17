package qrnu.pcontroller.server;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.SystemTray;
import java.io.IOException;
import java.util.prefs.Preferences;

public class PControllerServer
{
	private Preferences preferences;
	private PControllerServerTrayIcon trayIcon;
	private Robot robot;
	
	private PControllerServerConnection serverconnection;
	
	public PControllerServer() throws AWTException, IOException
	{
		InstanceControl ic = new InstanceControl();
		ic.start();

		
		this.preferences = Preferences.userNodeForPackage(this.getClass());
		
		this.robot = new Robot();
		
		if (SystemTray.isSupported())
		this.trayIcon = new PControllerServerTrayIcon(this);
		else 
			new HelpWindow(this);

		try
		{
			this.serverconnection = new PControllerServerConnection(this);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		
	}
	
	public Preferences getPreferences()
	{
		return preferences;
	}
	
	public PControllerServerTrayIcon getTrayIcon()
	{
		return trayIcon;
	}
	
	public Robot getRobot()
	{
		return robot;
	}
	
	
	public void exit()
	{
		this.trayIcon.close();
		
		if (this.serverconnection != null)
		{
			this.serverconnection.close();
		}
		
		
		System.exit(0);
	}
	
	public static void main(String[] args)
	{
		try
		{
			new PControllerServer();
		}
		catch (AWTException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
	}
}
