package qrnu.pcontroller.server;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.SystemTray;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.prefs.Preferences;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;


public class HelpWindow extends JFrame implements Runnable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public  String localAddr;
	private Preferences preferences;
	private PControllerServer app;
	private String password;
	private ImageIcon icon;
	private ImageIcon bg;
	private ImageIcon cp;
	public HelpWindow(PControllerServer app) 
	{
		
		this.app = app;
		this.preferences = this.app.getPreferences();
		this.password = this.preferences.get("password","12345");
		getTcpListenAddresses();
	    icon=new ImageIcon(this.getClass().getResource("icon.png"));
	    bg=new ImageIcon(this.getClass().getResource("bg.png"));
	    cp=new ImageIcon(this.getClass().getResource("cp.png"));
	    
	    JLabel backGroundLabel=new JLabel(bg);
        this.getLayeredPane().add(backGroundLabel, new Integer(Integer.MIN_VALUE));
        backGroundLabel.setBounds(0, 0, bg.getIconWidth(), bg.getIconHeight());
        ((JPanel)this.getContentPane()).setOpaque(false);
        
        JButton button = new JButton(cp);
        button.setBounds(375, 410, 85, 28);
        add(button);
        button.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				password = JOptionPane.showInputDialog(null, "Please input a new password:", password);
				if (password != null)
				{
					preferences.put("password", password);
					repaint();
				}
			}
		});
        
        setLayout(null);
		setIconImage(icon.getImage());
		setTitle("PController Server");
		setSize(485,500);
		setLocationRelativeTo(null);
		setResizable(false);
		setVisible(true);
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				// TODO Auto-generated method stub
				if(SystemTray.isSupported())
				setVisible(false);
				else HelpWindow.this.app.exit();
			}
			
			
		});
		
		new Thread(this).start();
	}
	
	private void getTcpListenAddresses()
	{
			try {
					localAddr = getLocalHost();
				}
			catch (UnknownHostException ex) {
				localAddr = "Fail to get IP.";
			}
		
	}
	
	private  String getLocalHost() throws UnknownHostException {
		
		StringBuilder message = new StringBuilder();
		
		try
		{
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements())
			{
				NetworkInterface currentInterface = interfaces.nextElement();
				
				Enumeration<InetAddress> addresses = currentInterface.getInetAddresses();
				
				while (addresses.hasMoreElements())
				{
					InetAddress currentAddress = addresses.nextElement();
					
					if (!currentAddress.isLoopbackAddress() && !(currentAddress instanceof Inet6Address))
					{
						message.append(currentAddress.getHostAddress()  + "\n");
					}
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		return message.toString();
	}
	
	public void paint(Graphics g) {
		super.paint(g);
		int startY = 272;
		g.setFont(new Font("Arial",Font.BOLD,15));
		g.setColor(Color.blue);
		for(int i=0;i<localAddr.split("\\n").length;i++)
		g.drawString(localAddr.split("\\n")[i],315, startY+=18);
		g.drawString(password,245, 455);
	}	
	
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(true)
		{
			try{
			Thread.sleep(5000);
			}catch(Exception e){
			}
			getTcpListenAddresses();
			this.password = this.preferences.get("password","12345");
			this.repaint();
		}
	}
}