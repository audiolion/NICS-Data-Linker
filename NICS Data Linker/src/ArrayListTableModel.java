import javax.imageio.ImageIO;
import javax.naming.NamingException;
import javax.swing.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;

import net.miginfocom.swing.MigLayout;

/**
 * Class serves as a table model for a JTable that
 * converts comma separated arraylist strings into 
 * a JTable.
 *
 * @contributor Ryan Castner
 */
@SuppressWarnings("serial")
public class ArrayListTableModel extends JFrame {
	
	public JTable table;
	private JTextField filterText;
	private TableRowSorter<MyModel> sorter;
	private final static String[] header = {"Patient Name", "Meditech ID", "Code", "Date/Time"};
	private static final String DEFAULT = "Search Table...";
	
	private JTextField usernameField;
	private JPasswordField passwordField;
	private JPanel loginPanel;
	
	private final String USER_DEFAULT = "Username";
	private final String PASS_DEFAULT = "Password";
	
	private final AccessController ac;
	
	public ArrayListTableModel(ArrayList<String> al) {
		ac = AccessController.getInstance();
		MyModel mm = new MyModel(al, header);
		table = new JTable(mm);
		sorter = new TableRowSorter<MyModel>(mm);
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(JLabel.CENTER);
		((DefaultTableCellRenderer)table.getTableHeader().getDefaultRenderer())
	    .setHorizontalAlignment(JLabel.CENTER);
		table.setRowSorter(sorter);
		TableColumn col = null;
		for(int i = 0; i < table.getColumnCount(); i++){
			col = table.getColumnModel().getColumn(i);
			if(i != 0){
				col.setCellRenderer(centerRenderer);
			}
			if(i == 2){
				col.setPreferredWidth(30);
			}else if(i == 0){
				col.setPreferredWidth(160);
			}else if(i == 1){
				col.setPreferredWidth(80);
			}else{
				col.setPreferredWidth(100);
			}
		}
		this.setSize(600,300);
		
		this.add(new JScrollPane(table));
		JPanel form = new JPanel();
		filterText = new JTextField(DEFAULT);
		filterText.setPreferredSize(new Dimension(120,30));
		filterText.getDocument().addDocumentListener(
				new DocumentListener(){
					public void changedUpdate(DocumentEvent e){
						newFilter();
					}
					public void insertUpdate(DocumentEvent e){
						newFilter();
					}
					public void removeUpdate(DocumentEvent e){
						newFilter();
					}
				});
		filterText.addFocusListener(new FocusListener(){
			@Override
			public void focusGained(FocusEvent e) {
				if(filterText.getText().equals(DEFAULT)) {
					filterText.setText("");
				}
			}
			@Override
			public void focusLost(FocusEvent e) {
				if(filterText.getText().equals("")) {
					filterText.setText(DEFAULT);
				}
			}
		});
		filterText.setMinimumSize(filterText.getPreferredSize());
		form.add(filterText);
		this.add(form, BorderLayout.SOUTH);
		setVisible(true);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
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
	
	private void newFilter(){
		if(!filterText.getText().equals(DEFAULT)){
			RowFilter<MyModel, Object> rf = null;
			try{
				rf = RowFilter.regexFilter("(?i)" + filterText.getText());
			}catch(java.util.regex.PatternSyntaxException e){
				return;
			}
			sorter.setRowFilter(rf);
		}
	}
	
	private int authenticate() throws NamingException{
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
		else if(this.verifyPassword(usernameField.getText(), new String(passwordField.getPassword())))
			return 1; // verified
		else
			return -1; // not verified
	}
	
	public boolean verifyPassword(String username, String password){
		boolean authResult = false;
		MemberOfAuth auth = new MemberOfAuth("FLH.LOCAL");
		authResult = auth.isMemberOf("GGH Admins", username, password);
		return authResult;
	}
	
	class MyModel extends AbstractTableModel {
		
		private ArrayList<String> al;
		private String[] header;
		
		MyModel(ArrayList<String> al, String[] header) {
			this.al = al;
			this.header = header;
		}

		public int getColumnCount() {
			return header.length;
		}

		public int getRowCount() {
			return al.size();
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			String[] token = al.get(rowIndex).split(",");
			return token[columnIndex];
		}

		public String getColumnName(int col) {
			return header[col];
		}	
		
		public boolean isCellEditable(int row, int col){
			return false;
		}
	}
}

