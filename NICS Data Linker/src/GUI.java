import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Properties;

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
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
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
	private final String PROPS = "props";
	
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
	
	private String defaultFilePath;
	private String defaultAccessPath;
	
	
	/**
	 * Constructor for GUI window
	 * @param title - the title of the window
	 */
	public GUI(String title){
		super(title);
		this.ac = AccessController.getInstance();
		initComponents();
		fillComponents();
		getProps();
	}
	
	
	private void getProps(){
		Properties props = new Properties();
		InputStream is = null;
		
		try{
			File f = new File(PROPS);
			is = new FileInputStream(f);
		}catch(Exception e){
			is = null;
		}
		
		try{
			if(is == null){
				is = this.getClass().getResourceAsStream("props");
			}
			
			props.load(is);
		}catch(Exception e){
		}
		
		defaultFilePath = props.getProperty("defaultFilePath", "");
		defaultAccessPath = props.getProperty("defaultAccessPath", "");
	}
	
	private void setProps(String defaultFilePath, String defaultAccessPath){
		try{
			Properties props = new Properties();
			if(!defaultFilePath.equals(""))
				props.setProperty("defaultFilePath", defaultFilePath);
			if(!defaultAccessPath.equals(""))
				props.setProperty("defaultAccessPath", defaultAccessPath);
			File f = new File(PROPS);
			OutputStream out = new FileOutputStream(f);
			props.store(out, "");
		}catch(Exception e){
			e.printStackTrace();
		}
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
			JOptionPane.showMessageDialog(null, "Please select the NICS files you would like to process.\nYou can select multiple files by holding down the 'CTRL' key while you select.");
			try {
				ac.processNics(defaultFilePath, defaultAccessPath);
			} catch (IOException e1) {
			}
		}
		
		if(e.getSource() == this.processAcks){
			JOptionPane.showMessageDialog(null, "Please select the ACK files you would like to process.\nYou can select multiple files by holding down the 'CTRL' key while you select.");
			try {
				ac.processAcks(defaultFilePath, defaultAccessPath);
			} catch (IOException e1) {
			}
		}
		
		if(e.getSource() == this.printAcks){
			JOptionPane.showMessageDialog(null, "Please select the Microsoft Access Database to retrieve information from.");
			ArrayList<String> list = ac.printAcknowledgements(defaultAccessPath);
			ArrayListTableModel altm = new ArrayListTableModel(list);
			altm.pack();
			Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
			// Compute and set the location so the frame is centered
			int x = screen.width/2-altm.getSize().width/2;
			int y = screen.height/2-altm.getSize().height/2;
			altm.setLocation(x, y);
			altm.setTitle("Acknowledged Patients");
			altm.setVisible(true);
		}
		
		if(e.getSource() == this.printNacks){
			JOptionPane.showMessageDialog(null, "Please select the Microsoft Access Database to retrieve information from.");
			ArrayList<String> list = ac.printNacknowledgements(defaultAccessPath);
			ArrayListTableModel altm = new ArrayListTableModel(list);
			altm.pack();
			Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
			// Compute and set the location so the frame is centered
			int x = screen.width/2-altm.getSize().width/2;
			int y = screen.height/2-altm.getSize().height/2;
			altm.setLocation(x, y);
			altm.setTitle("Non-Acknowledged Patients");
			altm.setVisible(true);
		}
		
		if(e.getSource() == this.about){
			JOptionPane.showMessageDialog(null, "Author: Ryan Robert Castner\nYear: 2014\nDesigned for use by Finger Lakes Health.\nContact: castner.rr@gmail.com");
		}
		
		if(e.getSource() == this.help){
			JOptionPane.showMessageDialog(null, "For help or more information about using this application\nplease contact Kelly Spano at kelly.spano@flhealth.org");
		}
		
		if(e.getSource() == this.setPath){
			int authenticationVal = authenticate();
			if(authenticationVal == 1){
				JOptionPane.showMessageDialog(null, "Please select the default file path to open to.");
				String path = ac.setFilePath();
				if(!path.equals("")){
					this.setProps(path, "");
					defaultFilePath = path;
				}
			}
			else if(authenticationVal == -1)
				JOptionPane.showMessageDialog(null, "Authentication failed: either unknown user or bad password.");
		}
		
		if(e.getSource() == this.setAccessPath){
			int authenticationVal = authenticate();
			if(authenticationVal == 1){
				JOptionPane.showMessageDialog(null, "Please select the default file path to open to.");
				String path = ac.setAccessPath();
				if(!path.equals("")){
					this.setProps("", path);
					defaultAccessPath = path;
				}
			}
			else if(authenticationVal == -1)
				JOptionPane.showMessageDialog(null, "Authentication failed: either unknown user or bad password.");
		}
		
		if(e.getSource() == this.resolveNacks){
			JOptionPane.showMessageDialog(null, "Please select the Microsoft Access Database to retrieve information from.");
			showResolveWindow();
		}
		
		if(e.getSource() == this.close){
			this.dispose();
		}
	}
	
	private void showResolveWindow(String accessPath) {
		
		ArrayList<String> list = ac.getRefreshedNacks(accessPath);
		final ArrayListTableModel altm = new ArrayListTableModel(list);
		altm.pack();
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		// Compute and set the location so the frame is centered
		int x = screen.width/2-altm.getSize().width/2;
		int y = screen.height/2-altm.getSize().height/2;
		altm.setLocation(x, y);
		altm.setTitle("Non-Acknowledged Patients");
		altm.setVisible(true);
		altm.table.addMouseListener(new MouseAdapter(){
			public void mousePressed(MouseEvent e){
				JTable table = (JTable) e.getSource();
				if(e.isPopupTrigger()){
					//http://www.stupidjavatricks.com/2005/11/jtable-right-click-row-selection/
					Point p = e.getPoint();
					int rowNumber = table.rowAtPoint(p);
					ListSelectionModel model = table.getSelectionModel();
					model.setSelectionInterval(rowNumber, rowNumber);
					int row = table.getSelectedRow();
					doContextMenu(e, row);
				}
			}
			
			public void mouseReleased(MouseEvent e){
				JTable table = (JTable) e.getSource();
				if(e.isPopupTrigger()){
					//http://www.stupidjavatricks.com/2005/11/jtable-right-click-row-selection/
					Point p = e.getPoint();
					int rowNumber = table.rowAtPoint(p);
					ListSelectionModel model = table.getSelectionModel();
					model.setSelectionInterval(rowNumber, rowNumber);
					int row = table.getSelectedRow();
					doContextMenu(e, row);
				}
			}
			
			private void doContextMenu(MouseEvent e, int row){
				ResolveContextMenu menu = new ResolveContextMenu(row, altm);
				menu.show(e.getComponent(), e.getX(), e.getY());
			}
		});
		
	}
	
	private void showResolveWindow() {
		ArrayList<String> list = ac.getUnresolvedNacks(defaultAccessPath);
		final ArrayListTableModel altm = new ArrayListTableModel(list);
		altm.pack();
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		// Compute and set the location so the frame is centered
		int x = screen.width/2-altm.getSize().width/2;
		int y = screen.height/2-altm.getSize().height/2;
		altm.setLocation(x, y);
		altm.setTitle("Non-Acknowledged Patients");
		altm.setVisible(true);
		altm.table.addMouseListener(new MouseAdapter(){
			public void mousePressed(MouseEvent e){
				JTable table = (JTable) e.getSource();
				if(e.isPopupTrigger()){
					//http://www.stupidjavatricks.com/2005/11/jtable-right-click-row-selection/
					Point p = e.getPoint();
					int rowNumber = table.rowAtPoint(p);
					ListSelectionModel model = table.getSelectionModel();
					model.setSelectionInterval(rowNumber, rowNumber);
					int row = table.getSelectedRow();
					doContextMenu(e, row);
				}
			}
			
			public void mouseReleased(MouseEvent e){
				JTable table = (JTable) e.getSource();
				if(e.isPopupTrigger()){
					//http://www.stupidjavatricks.com/2005/11/jtable-right-click-row-selection/
					Point p = e.getPoint();
					int rowNumber = table.rowAtPoint(p);
					ListSelectionModel model = table.getSelectionModel();
					model.setSelectionInterval(rowNumber, rowNumber);
					int row = table.getSelectedRow();
					doContextMenu(e, row);
				}
			}
			
			private void doContextMenu(MouseEvent e, int row){
				ResolveContextMenu menu = new ResolveContextMenu(row, altm);
				menu.show(e.getComponent(), e.getX(), e.getY());
			}
		});
		
	}

	class ResolveContextMenu extends JPopupMenu implements ActionListener{
		private JMenuItem resolveNack;
		private AccessController ac;
		private int rowID;
		private ArrayListTableModel altm;
		
		public ResolveContextMenu(int rowID, ArrayListTableModel altm){
			this.altm = altm;
			this.rowID = rowID;
			ac = AccessController.getInstance();
			java.net.URL path = this.getClass().getResource("/uac_icon.png");
			BufferedImage imgs = null;
			BufferedImage scaledImage = null;
			try{
				imgs = ImageIO.read(path);
				scaledImage = new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB);
				scaledImage.getGraphics().drawImage(imgs, 0, 0, 20, 20, null);
			}catch(IOException e){
			}
			Icon uacIcon = new ImageIcon(scaledImage);
			resolveNack = new JMenuItem("Resolve Nack", uacIcon);
			resolveNack.setIcon(uacIcon);
			resolveNack.addActionListener(this);
			add(resolveNack);
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			if(e.getSource() == this.resolveNack);{
				int authVal = 0;
				authVal = authenticate();
				if(authVal == 1){
					String filePath = ac.getAccessPath();
					ac.writeResolvedNack(rowID, filePath);
					altm.dispose();
					showResolveWindow(filePath);
				}else if(authVal == -1){
					JOptionPane.showMessageDialog(null, "Authentication failed: either unknown user or bad password.");
				}
			}
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
