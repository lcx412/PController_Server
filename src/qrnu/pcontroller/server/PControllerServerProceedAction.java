package qrnu.pcontroller.server;

import java.awt.Desktop;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Rectangle;
import java.awt.SystemTray;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ProtocolException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import qrnu.pcontroller.action.AuthentificationAction;
import qrnu.pcontroller.action.AuthentificationResponseAction;
import qrnu.pcontroller.action.DirectKeyAction;
import qrnu.pcontroller.action.FileExploreRequestAction;
import qrnu.pcontroller.action.FileExploreResponseAction;
import qrnu.pcontroller.action.KeyboardAction;
import qrnu.pcontroller.action.MouseClickAction;
import qrnu.pcontroller.action.MouseMoveAction;
import qrnu.pcontroller.action.MouseWheelAction;
import qrnu.pcontroller.action.PControllerAction;
import qrnu.pcontroller.action.ScreenCaptureRequestAction;
import qrnu.pcontroller.action.ScreenCaptureResponseAction;

public class PControllerServerProceedAction implements Runnable {
	private PControllerServer app;
	private PControllerConnection conn;
	private boolean authentificated;

	public PControllerServerProceedAction(PControllerServer app,
			PControllerConnection connection) {
		this.app = app;
		this.conn = connection;
		this.authentificated = false;
		(new Thread(this)).start();
	}

	public void run() {
		try {
			try {
				while (true) {
					PControllerAction action = conn.receiveAction();
					this.proceedAction(action);
				}
			} finally {
				conn.close();
			}
		} catch (ProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void proceedAction(PControllerAction action) {
		if (this.authentificated) {
			if (action instanceof AuthentificationAction) {
				this.authentificate((AuthentificationAction) action);
			} else if (action instanceof MouseMoveAction) {
				this.moveMouse((MouseMoveAction) action);
			} else if (action instanceof MouseClickAction) {
				this.mouseClick((MouseClickAction) action);
			} else if (action instanceof MouseWheelAction) {
				this.mouseWheel((MouseWheelAction) action);
			} else if (action instanceof DirectKeyAction) {
				this.directkey((DirectKeyAction) action);
			} else if (action instanceof FileExploreRequestAction) {
				this.fileExplore((FileExploreRequestAction) action);
			} else if (action instanceof ScreenCaptureRequestAction) {
				this.screenCapture((ScreenCaptureRequestAction) action);
			} else if (action instanceof KeyboardAction) {
				this.keyboard((KeyboardAction) action);
			}
		} else {
			if (action instanceof AuthentificationAction) {
				this.authentificate((AuthentificationAction) action);
			}

			if (!this.authentificated) {
				try {
					conn.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void authentificate(AuthentificationAction action) {
		if (action.password.equals(this.app.getPreferences().get("password",
				"12345"))) {
			this.authentificated = true;

			if (SystemTray.isSupported())
				this.app.getTrayIcon().notifyConnection(this.conn);
		}

		this.sendAction(new AuthentificationResponseAction(this.authentificated));
	}

	private void moveMouse(MouseMoveAction action) {
		PointerInfo pointerInfo = MouseInfo.getPointerInfo();

		if (pointerInfo != null) {
			Point mouseLocation = pointerInfo.getLocation();

			if (mouseLocation != null) {
				int x = mouseLocation.x + action.moveX;
				int y = mouseLocation.y + action.moveY;
				this.app.getRobot().mouseMove(x, y);
			}
		}
	}

	private void mouseClick(MouseClickAction action) {
		int button;

		switch (action.button) {
		case MouseClickAction.BUTTON_LEFT:
			button = InputEvent.BUTTON1_MASK;
			break;
		case MouseClickAction.BUTTON_RIGHT:
			button = InputEvent.BUTTON3_MASK;
			break;
		default:
			return;
		}

		if (action.state == MouseClickAction.STATE_DOWN) {
			this.app.getRobot().mousePress(button);
		} else if (action.state == MouseClickAction.STATE_UP) {
			this.app.getRobot().mouseRelease(button);
		}

	}

	private void mouseWheel(MouseWheelAction action) {
		this.app.getRobot().mouseWheel(action.amount);
	}

	private void directkey(DirectKeyAction action) {
		if (action.state == 0) {
			if (action.directcode2 == -5) {
				this.app.getRobot().keyPress(action.directcode1);
				this.app.getRobot().keyRelease(action.directcode1);
			} else {
				this.app.getRobot().keyPress(action.directcode1);
				this.app.getRobot().keyPress(action.directcode2);
				this.app.getRobot().keyRelease(action.directcode2);
				this.app.getRobot().keyRelease(action.directcode1);
			}
		} else if (action.state == 1) {
			this.app.getRobot().keyPress(action.directcode1);
		} else {
			this.app.getRobot().keyRelease(action.directcode1);
		}

	}

	private void keyboard(KeyboardAction action) {
		this.keyboardAction(action);
	}

	private void keyboardAction(KeyboardAction action) {
		int keycode = UnicodeToSwingKeyCodeConverter.convert(action.unicode);

		if (keycode != UnicodeToSwingKeyCodeConverter.NO_SWING_KEYCODE) {
			boolean useShift = UnicodeToSwingKeyCodeConverter
					.useShift(action.unicode);

			if (useShift) {
				this.app.getRobot().keyPress(KeyEvent.VK_SHIFT);
			}

			this.app.getRobot().keyPress(keycode);
			this.app.getRobot().keyRelease(keycode);

			if (useShift) {
				this.app.getRobot().keyRelease(KeyEvent.VK_SHIFT);
			}
		}
	}

	private void screenCapture(ScreenCaptureRequestAction action) {
		try {
			BufferedImage capture;
			Point mouseLocation = MouseInfo.getPointerInfo().getLocation();
			Rectangle r = new Rectangle(mouseLocation.x - (action.width / 2),
					mouseLocation.y - (action.height / 2), action.width,
					action.height);
			Rectangle r_ppt = new Rectangle(java.awt.Toolkit
					.getDefaultToolkit().getScreenSize().width,
					java.awt.Toolkit.getDefaultToolkit().getScreenSize().height);
			if (action.type == ScreenCaptureRequestAction.TOUCHPAD) {
				capture = this.app.getRobot().createScreenCapture(r);
			} else {
				capture = this.app.getRobot().createScreenCapture(r_ppt);
				BufferedImage scapture = new BufferedImage(action.width,
						action.height, BufferedImage.TYPE_INT_RGB);
				scapture.getGraphics().drawImage(capture, 0, 0, action.width,
						action.height, null);
				capture = scapture;
			}
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(capture, "jpg", baos);
			byte[] data = baos.toByteArray();

			this.sendAction(new ScreenCaptureResponseAction(data));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void fileExplore(FileExploreRequestAction action) {
		if (action.directory.isEmpty() && action.file.isEmpty()) {
			this.fileExploreRoots();
		} else {
			if (action.directory.isEmpty()) {
				this.fileExplore(new File(action.file));
			} else {
				File directory = new File(action.directory);

				if (directory.getParent() == null && action.file.equals("..")) {
					this.fileExploreRoots();
				} else {
					try {
						this.fileExplore(new File(directory, action.file)
								.getCanonicalFile());
					} catch (IOException e) {
						e.printStackTrace();

						this.fileExploreRoots();
					}
				}
			}
		}
	}

	private void fileExplore(File file) {
		if (file.exists() && file.canRead()) {
			if (file.isDirectory()) {
				this.sendFileExploreResponse(file.getAbsolutePath(),
						file.listFiles(), true, false);
			} else {
				if (Desktop.isDesktopSupported()) {
					Desktop desktop = Desktop.getDesktop();

					if (desktop.isSupported(Desktop.Action.OPEN)) {
						try {
							desktop.open(file);
						} catch (IOException e) {
							e.printStackTrace();

							if (System.getProperty("os.name").toLowerCase()
									.contains("windows")) {

								try {
									Process process = Runtime.getRuntime()
											.exec("cmd /C "
													+ file.getAbsolutePath());
									BufferedReader br = new BufferedReader(
											new InputStreamReader(
													process.getInputStream()));

									String line;
									while ((line = br.readLine()) != null) {
										System.out.println(line);
									}
								} catch (IOException e1) {
									e1.printStackTrace();
								}
							}
						}
					}
				}
			}
		} else {
			this.fileExploreRoots();
		}
	}

	private void fileExploreRoots() {
		String directory = "";

		File[] files = File.listRoots();

		this.sendFileExploreResponse(directory, files, false, true);
	}

	private void sendFileExploreResponse(String directory, File[] f,
			boolean parent, boolean showHidden) {
		if (f != null) {
			ArrayList<String> list = new ArrayList<String>();

			if (parent) {
				list.add("..");
			}

			for (int i = 0; i < f.length; i++) {
				if (showHidden || !f[i].isHidden()) {
					String name = f[i].getName();

					if (!name.isEmpty()) {
						if (f[i].isDirectory()) {
							name += File.separator;
						}
					} else {
						name = f[i].getAbsolutePath();
					}

					list.add(name);
				}
			}

			String[] files = new String[list.size()];

			files = list.toArray(files);

			this.sendAction(new FileExploreResponseAction(directory, files));
		}
	}

	private void sendAction(PControllerAction action) {
		try {
			conn.sendAction(action);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
