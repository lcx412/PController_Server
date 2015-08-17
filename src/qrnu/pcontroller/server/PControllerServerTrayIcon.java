package qrnu.pcontroller.server;

import java.awt.AWTException;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

import javax.imageio.ImageIO;

public class PControllerServerTrayIcon
{
	private PControllerServer app;
	private TrayIcon trayIcon;
	private HelpWindow hw;
	
	public PControllerServerTrayIcon(PControllerServer app) throws AWTException, IOException
	{
		this.app = app;
		this.hw = new HelpWindow(app);
		this.initTrayIcon();
	}
	
	public void notifyConnection(PControllerConnection connection)
	{
		String message = "";
		message = connection.getInetAddress().getHostAddress();
		
		this.trayIcon.displayMessage("PController", "New Connection, IP : " + message, MessageType.INFO);
	}
	
	
	public void close()
	{
		SystemTray.getSystemTray().remove(this.trayIcon);
	}
	
	private void initTrayIcon() throws AWTException, IOException
	{
		PopupMenu menu = new PopupMenu();
		
		
		MenuItem menuItemExit = new MenuItem("Exit PController Server");
		menuItemExit.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				PControllerServerTrayIcon.this.app.exit();
			}
		});
		menu.add(menuItemExit);
		
		this.trayIcon = new TrayIcon(ImageIO.read(this.getClass().getResourceAsStream("icon.png")));
		this.trayIcon.setImageAutoSize(true);
		this.trayIcon.setToolTip("PController server");
		this.trayIcon.setPopupMenu(menu);
		
		SystemTray.getSystemTray().add(this.trayIcon);
		StringBuilder message = new StringBuilder("PController Server has started.");
		this.trayIcon.displayMessage("PController", message.toString()+"\n ", TrayIcon.MessageType.INFO);
		trayIcon.addMouseListener(new TrayIconMouseListener());
	}
	 private class TrayIconMouseListener extends MouseAdapter {
         public void mousePressed(MouseEvent me) {
                	 if(me.getButton()==MouseEvent.BUTTON1&&me.getClickCount()==1)  {
                		 hw.setVisible(true);
                    	 hw.repaint();
                 }
         }
 }
}
