import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import net.miginfocom.swing.MigLayout;

/**
 * This is the View for the MVC model that this application
 * is based around. This view is a simple GUI that has buttons
 * which will trigger the different actions involved in running
 * the application.
 * 
 * @author Ryan Robert Castner - ryancastner@msn.com
 * @version 1.0 - 8/20/2013
 *
 */

@SuppressWarnings("serial")
public class GUI extends JFrame implements ActionListener{

	private final String USER_DEFAULT = "Username";
	private final String PASS_DEFAULT = "Password";
	
	private final AccessController ac;
	
	private JMenuBar jmb;
	private JMenuItem close;
	private JMenuItem help;
	private JMenuItem about;
	private JMenuItem setPath;
	private JMenuItem setAccessPath;
	private JMenuItem resolveNacks;
	private JButton printNacks;
	private JButton printAcks;
	private JButton processNics;
	private JButton processAcks;
	private JPanel btnPanel;
	
	private JTextField usernameField;
	private JPasswordField passwordField;
	private JPanel loginPanel;
	
	/**
	 * Constructor for GUI window
	 * @param title - the title of the window
	 */
	public GUI(String title){
		super(title);
		this.ac = AccessController.getInstance();
		initComponents();
		fillComponents();
	}
	
	/**
	 * Initializes components
	 */
	private void initComponents(){
		jmb = new JMenuBar();
		close = new JMenuItem("Close");
		help = new JMenuItem("Help");
		about = new JMenuItem("About");
		setPath = new JMenuItem("Set File Path...");
		setAccessPath = new JMenuItem("Set Access File Path...");
		resolveNacks = new JMenuItem("Resolve Nacks");
		
		printNacks = new JButton("Non-Acknowledged Report");
		printAcks = new JButton("Acknowledged Report");
		processNics = new JButton("Process NICS");
		processAcks = new JButton("Process ACKS");
		
		printNacks.addActionListener(this);
		printAcks.addActionListener(this);
		processNics.addActionListener(this);
		processAcks.addActionListener(this);
		about.addActionListener(this);
		setPath.addActionListener(this);
		setAccessPath.addActionListener(this);
		help.addActionListener(this);
		close.addActionListener(this);
		resolveNacks.addActionListener(this);
		
		btnPanel = new JPanel();
		btnPanel.setLayout(new MigLayout());
		btnPanel.add(new JLabel("1."), "align label");
		btnPanel.add(processNics, "wrap");
		btnPanel.add(new JLabel("2."), "align label");
		btnPanel.add(processAcks, "wrap");
		btnPanel.add(new JLabel("3."), "align label");
		btnPanel.add(printAcks, "wrap");
		btnPanel.add(new JLabel("4."), "align label");
		btnPanel.add(printNacks, "wrap");		
		
		loginPanel = new JPanel();
		usernameField = new JTextField(20);
		passwordField = new JPasswordField(20);
		usernameField.setText(USER_DEFAULT);
		passwordField.setEchoChar((char)0);
		passwordField.setText(PASS_DEFAULT);
		
		usernameField.setText(USER_DEFAULT);
		usernameField.setForeground(Color.GRAY);
		passwordField.setText(PASS_DEFAULT);
		passwordField.setForeground(Color.GRAY);
		usernameField.addFocusListener(new FocusListener(){
			@Override
			public void focusGained(FocusEvent e){
				if(usernameField.getText().equals(USER_DEFAULT)){
					usernameField.setText("");
					usernameField.setForeground(Color.BLACK);
				}
			}
			@Override
			public void focusLost(FocusEvent e){
				if(usernameField.getText().equals("")){
					usernameField.setText(USER_DEFAULT);
					usernameField.setForeground(Color.GRAY);
				}
			}
		});
		passwordField.addFocusListener(new FocusListener(){
			@Override
			public void focusGained(FocusEvent e){
				if(new String(passwordField.getPassword()).equals(PASS_DEFAULT)){
					passwordField.setText("");
					passwordField.setForeground(Color.BLACK);
					passwordField.setEchoChar('*');
				}
			}
			@Override
			public void focusLost(FocusEvent e){
				if(passwordField.getPassword().length == 0){
					passwordField.setEchoChar((char)0);
					passwordField.setForeground(Color.GRAY);
					passwordField.setText(PASS_DEFAULT);
				}					
			}
		});
		loginPanel.setLayout(new MigLayout());
		loginPanel.add(new JLabel("Elevation Required"), "wrap, align");
		loginPanel.add(usernameField, "wrap, align");
		loginPanel.add(passwordField, "wrap, align");
		loginPanel.add(new JLabel("Domain: FLH"), "align");
		loginPanel.setBackground(new Color(135, 206, 250, 50));
	}
	
	/**
	 * Fills in JMenuBar and adds panels to frame
	 */
	private void fillComponents() {
		JMenu fileMenu = new JMenu("File");
		JMenu helpMenu = new JMenu("Help");
		fileMenu.add(setPath);
		fileMenu.add(setAccessPath);
		fileMenu.add(resolveNacks);
		fileMenu.add(close);
		helpMenu.add(help);
		helpMenu.add(about);
		jmb.add(fileMenu);
		jmb.add(helpMenu);
		JPanel menuPanel = new JPanel();
		menuPanel.add(jmb);
		setJMenuBar(jmb);
		this.add(btnPanel);
	}
	
	/**
	 * Creates the GUI frame and centers it on the user's screen
	 */
	protected static void createAndShowGUI(){
		JFrame f = null;
		f = new GUI("Patient Information Verifier");
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setPreferredSize(new Dimension(500,400));
		f.pack();
		// Get the current screen's size
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		// Compute and set the location so the frame is centered
		int x = screen.width/2-f.getSize().width/2;
		int y = screen.height/2-f.getSize().height/2;
		f.setLocation(x, y);
		f.setVisible(true);
	}
	
	/**
	 * Main method sets the look and feel for the application
	 * 
	 * @param args
	 * @throws InvocationTargetException
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws InvocationTargetException, InterruptedException{
		// Set the System look and feel for better user experience
		Runnable doCreateAndShowGUI = new Runnable(){
			@Override
			public void run(){
				createAndShowGUI();
			}
		};
		try{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}catch(Exception e){
		}
		SwingUtilities.invokeAndWait(doCreateAndShowGUI);
	}
	
	/**
	 * Method handles which methods of the controller are called
	 * when a button is pressed and the informational popups that
	 * are associated with the file and help menus.
	 */
	public void actionPerformed(ActionEvent e){
		if(e.getSource() == this.processNics){
			try {
				ac.processNics();
			} catch (IOException e1) {
			}
		}
		
		if(e.getSource() == this.processAcks){
			try {
				ac.processAcks();
			} catch (IOException e1) {
			}
		}
		
		if(e.getSource() == this.printAcks){
			ac.printAcknowledgements();
		}
		
		if(e.getSource() == this.printNacks){
			ac.printNacknowledgements();
		}
		
		if(e.getSource() == this.about){
			JOptionPane.showMessageDialog(null, "Author: Ryan Robert Castner\nYear: 2013\nDesigned for use by Finger Lakes Health.\nContact: ryancastner@msn.com");
		}
		
		if(e.getSource() == this.help){
			JOptionPane.showMessageDialog(null, "For help or more information about using this application\nplease contact Kelly Spano at kelly.spano@flhealth.org");
		}
		
		if(e.getSource() == this.setPath){
			int authenticationVal = authenticate();
			if(authenticationVal == 1)
				ac.setFilePath();
			else if(authenticationVal == -1)
				JOptionPane.showMessageDialog(null, "Authentication failed: either unknown user or bad password.");
		}
		
		if(e.getSource() == this.setAccessPath){
			int authenticationVal = authenticate();
			if(authenticationVal == 1)
				ac.setAccessPath();
			else if(authenticationVal == -1)
				JOptionPane.showMessageDialog(null, "Authentication failed: either unknown user or bad password.");
		}
		
		if(e.getSource() == this.resolveNacks){
			ac.resolveNacks();
		}
		
		if(e.getSource() == this.close){
			this.dispose();
		}
	}

	private int authenticate(){
		java.net.URL path = this.getClass().getResource("/uac_icon.png");
		BufferedImage imgs = null;
		try{
			imgs = ImageIO.read(path);
		}catch(IOException e){
		}
		Icon uacIcon = new ImageIcon(imgs);
		usernameField.setForeground(Color.GRAY);
		passwordField.setForeground(Color.GRAY);
		usernameField.setText(USER_DEFAULT);
		passwordField.setText(PASS_DEFAULT);
		passwordField.setEchoChar((char)0);
		int inputVal = JOptionPane.showConfirmDialog(null, loginPanel, "User Account Controls", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, uacIcon);
		if(inputVal == JOptionPane.CANCEL_OPTION || inputVal == JOptionPane.CLOSED_OPTION)
			return 0;	// cancelled
		else if(ac.verifyPassword(usernameField.getText(), new String(passwordField.getPassword())))
			return 1; // verified
		else
			return -1; // not verified
	}
}
