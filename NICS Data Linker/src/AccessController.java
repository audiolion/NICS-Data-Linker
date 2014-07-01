import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.security.auth.login.LoginException;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.healthmarketscience.jackcess.*;


/**
 * This class is a controller as part of the Model-View-Controller
 * design pattern. This controller deals with reading and writing
 * to an access database. The class is set up as a singleton so that
 * only one instance of the database can be active and there are not
 * issues with multiple readers and writers or issues with lock handling
 * and scheduling. This controller uses a free library called Jackcess
 * to interface with a Microsoft Access Database.
 * 
 * @author Ryan Robert Castner - ryancastner@msn.com
 * @version 1.0 - 8/20/2013
 *
 */

public class AccessController {
	
	private static final String ADMINACCT = "ryantest";
	private Database db;
	private Table patientsTable;
	private String filePath = null;
	private String accessPath = null;
	
	/**
	 * Singleton Wrapper class 
	 */
	private static class SingletonWrapper {
		private static AccessController INSTANCE = new AccessController();
	}

	/**
	 * Use as an alternative to a constructor. This will ensure singleton
	 * behavior
	 * @return	the singleton instance of AccessController
	 */
	public static AccessController getInstance() {
		return SingletonWrapper.INSTANCE;
	}

	/**
	 * Private constructor ensures no extraneous AccessControllers will be created
	 */
	private AccessController() {
	}
	
	/**
	 * Establishes a connection with the database
	 * @param filePath - the file path to the MS Access database
	 */
	public void openDatabase(String filePath){
		try {
			db = DatabaseBuilder.open(new File(filePath));
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Could not open database.");
		}
	}
	
	/**
	 * Closes the connection with the database
	 */
	public void closeDatabase(){
		try {
			db.close();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Could not properly close database.");
		}
	}
	
	public void setFilePath(){
		JOptionPane.showMessageDialog(null, "Please select the default file path to open to.");
		FileChooser fc = new FileChooser();
		File file = fc.getDirectoryPath();
		String filePath = file.getAbsolutePath();
		if(filePath.equalsIgnoreCase("No File Selected")){
			return;
		}
		else{
			this.filePath = filePath;
		}
	}
	
	public void setAccessPath(){
		JOptionPane.showMessageDialog(null, "Please select the default file path to open to.");
		FileChooser fc = new FileChooser();
		File file = fc.getDirectoryPath();
		String filePath = file.getAbsolutePath();
		if(filePath.equalsIgnoreCase("No File Selected")){
			return;
		}
		else{
			this.accessPath = filePath;
		}
	}
	
	public boolean verifyPassword(String username, String password){
		boolean authResult = false;
		try{
			MemberOfAuth auth = new MemberOfAuth("FLH.LOCAL");
			authResult = auth.isMemberOf("GGH Admins", username, password);
		}catch(LoginException e){
		}
		return authResult;
	}
	
	/**
	 * Writes data to the access database based on the processed NICS files. Data includes
	 * Patient Name, Meditech ID, Acknowledged Boolean State, and Initial/Update status.
	 * Uses Jackcess library to interact with the MS Access database.
	 * 
	 * @param nicsData - the process nics data in 3 modulo form
	 */
	public void writeData(ArrayList<String> nicsData){
		try {
			patientsTable = db.getTable("Patients");
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Could not find or open table.");
		}
		for(int i = 0; i < nicsData.size(); i++){
			if(i%3 == 0){
				try {
					DateFormat dateFormat = new SimpleDateFormat();
					Date date = new Date();
					patientsTable.addRow(Column.AUTO_NUMBER, nicsData.get(i+1), nicsData.get(i), "", nicsData.get(i+2), dateFormat.format(date));
				} catch (IOException e) {
					JOptionPane.showMessageDialog(null, "Could not add a row to the Patients Table.");
				}
			}
		}
	}
	
	/**
	 * Parses through the MS Access database and for each row it checks the Meditech ID
	 * against the given list of new acknowledgements. If there is a match, the boolean
	 * state is set to true (Yes), if there is no match it is left alone. This way non-
	 * -acknowledged records will be left and previously acknowledged files will not be
	 * touched.
	 * 
	 * @param ackData - List of acknowledgements to cross check against the database
	 */
	public void setAcknowledgements(ArrayList<String> ackData){
		try{
			patientsTable = db.getTable("Patients");
			Cursor cur = CursorBuilder.createCursor(patientsTable);
			while(cur.moveToNextRow()){
				for(int i = 0; i < ackData.size(); i++){
					if(cur.getCurrentRowValue(patientsTable.getColumn("Meditech_ID")).toString().contains(ackData.get(i))){
						cur.setCurrentRowValue(patientsTable.getColumn("Acknowledged"), "Yes");
					}
				}
			}
		}catch(IOException e){
			JOptionPane.showMessageDialog(null, "Could not read from the Patients Table.");
		}
	}
	
	/**
	 * Method handles the logic for gathering NICS data and writing it to a MS access
	 * database.
	 * 
	 * @throws IOException
	 */
	public void processNics() throws IOException{
		JOptionPane.showMessageDialog(null, "Please select the NICS files you would like to process.\nYou can select multiple files by holding down the 'CTRL' key while you select.");
		FileReader fr = new FileReader();
		FileChooser fc;
		if(filePath != null){
			fc = new FileChooser(filePath);
		}else{
			fc = new FileChooser();
		}
		String[] filePaths = fc.getFilePaths();
		if(filePaths[0].equalsIgnoreCase("No Selection")){
			return;
		}
		filePath = fc.getDirPath();		
		ArrayList<String> data = fr.readFile(filePaths);
		ArrayList<String> nics = fr.getNicsData(data);
		if(nics.size() == 0){
			JOptionPane.showMessageDialog(null, "No NICS information was found. Please verify the correct files were chosen."); 
			return;
		}
		JOptionPane.showMessageDialog(null, "Please select the Microsoft Access Database to write information to.");
		FileChooser fc2;
		if(accessPath != null){
			fc2 = new FileChooser(accessPath);
		}else{
			fc2 = new FileChooser();
		}
		String filePath = fc2.getFilePath();
		if(filePath.equalsIgnoreCase("No File Selected")){
			return;
		}
		accessPath = filePath;
		AccessController ac = AccessController.getInstance();
		ac.openDatabase(filePath);
		ac.writeData(nics);
		ac.closeDatabase();
		JOptionPane.showMessageDialog(null, "Information successfully written.");
	}
	
	/**
	 * Method handles logic for gathering Ack data and cross-checking it against
	 * a MS Access database.
	 * 
	 * @throws IOException
	 */
	public void processAcks() throws IOException{
		JOptionPane.showMessageDialog(null, "Please select the ACK files you would like to process.\nYou can select multiple files by holding down the 'CTRL' key while you select.");
		FileReader fr = new FileReader();
		FileChooser fc;
		if(filePath != null){
			fc = new FileChooser(filePath);
		}else{
			fc = new FileChooser();
		}
		String[] filePaths = fc.getFilePaths();
		if(filePaths[0].equalsIgnoreCase("No Selection")){
			return;
		}
		filePath = fc.getDirPath();
		ArrayList<String> data = fr.readFile(filePaths);
		ArrayList<String> acks = fr.getAckData(data);
		if(acks.size() == 0){
			JOptionPane.showMessageDialog(null, "No ACK information was found. Please verify the correct files were chosen."); 
			return;
		}
		JOptionPane.showMessageDialog(null, "Please select the Microsoft Access Database to write information to.");
		FileChooser fc2;
		if(accessPath != null){
			fc2 = new FileChooser(accessPath);
		}else{
			fc2 = new FileChooser();
		}
		String filePath = fc2.getFilePath();
		if(filePath.equalsIgnoreCase("No File Selected")){
			return;
		}
		AccessController ac = AccessController.getInstance();
		ac.openDatabase(filePath);
		ac.setAcknowledgements(acks);
		ac.closeDatabase();
		JOptionPane.showMessageDialog(null, "Information successfully written.");
	}
	
	/**
	 * Method handles logic for printing out a neat table of acknowledged patients
	 * with the name, meditech ID and update/submit code status.
	 */
	public void printAcknowledgements(){
		JOptionPane.showMessageDialog(null, "Please select the Microsoft Access Database to retrieve information from.");
		FileChooser fc2;
		if(accessPath != null){
			fc2 = new FileChooser(accessPath);
		}else{
			fc2 = new FileChooser();
		}
		String filePath = fc2.getFilePath();
		if(filePath.equalsIgnoreCase("No File Selected")){
			return;
		}
		AccessController ac = AccessController.getInstance();
		ac.openDatabase(filePath);
		ArrayList<String> list = new ArrayList<String>();
		try{
			patientsTable = db.getTable("Patients");
			Cursor cur = CursorBuilder.createCursor(patientsTable);
			while(cur.moveToNextRow()){
				if(cur.getCurrentRowValue(patientsTable.getColumn("Acknowledged")).toString().contains("Yes")){
					list.add(cur.getCurrentRowValue(patientsTable.getColumn("Patient_Name")).toString() + "," +
				cur.getCurrentRowValue(patientsTable.getColumn("Meditech_ID")).toString() + "," +
				cur.getCurrentRowValue(patientsTable.getColumn("Admit_Update")).toString() + "," +
				cur.getCurrentRowValue(patientsTable.getColumn("Date/Time")).toString());
				
				}
			}
			ArrayListTableModel altm = new ArrayListTableModel(list);
			altm.pack();
			Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
			// Compute and set the location so the frame is centered
			int x = screen.width/2-altm.getSize().width/2;
			int y = screen.height/2-altm.getSize().height/2;
			altm.setLocation(x, y);
			altm.setTitle("Acknowledged Patients");
			altm.setVisible(true);
		}catch(IOException e){
			JOptionPane.showMessageDialog(null, "Could not read Patients Table.");
		}
		ac.closeDatabase();
	}
	
	/**
	 * Method handles logic for printing out a neat table of non-acknowledged patients
	 * with their name, meditech ID and Update/Submit code status.
	 */
	public void printNacknowledgements(){
		JOptionPane.showMessageDialog(null, "Please select the Microsoft Access Database to retrieve information from.");
		FileChooser fc2;
		if(accessPath != null){
			fc2 = new FileChooser(accessPath);
		}else{
			fc2 = new FileChooser();
		}
		String filePath = fc2.getFilePath();
		if(filePath.equalsIgnoreCase("No File Selected")){
			return;
		}
		AccessController ac = AccessController.getInstance();
		ac.openDatabase(filePath);
		ArrayList<String> list = new ArrayList<String>();
		try{
			patientsTable = db.getTable("Patients");
			Cursor cur = CursorBuilder.createCursor(patientsTable);
			while(cur.moveToNextRow()){
				if(!cur.getCurrentRowValue(patientsTable.getColumn("Acknowledged")).toString().contains("Yes")){
					list.add(cur.getCurrentRowValue(patientsTable.getColumn("Patient_Name")).toString() + "," +
					cur.getCurrentRowValue(patientsTable.getColumn("Meditech_ID")).toString() + "," +
					cur.getCurrentRowValue(patientsTable.getColumn("Admit_Update")).toString() + "," +
					cur.getCurrentRowValue(patientsTable.getColumn("Date/Time")).toString());
				}
			}
			ArrayListTableModel altm = new ArrayListTableModel(list);
			altm.pack();
			Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
			// Compute and set the location so the frame is centered
			int x = screen.width/2-altm.getSize().width/2;
			int y = screen.height/2-altm.getSize().height/2;
			altm.setLocation(x, y);
			altm.setTitle("Non-Acknowledged Patients");
			altm.setVisible(true);
		}catch(IOException e){
			JOptionPane.showMessageDialog(null, "Could not read Patients Table.");
		}
		ac.closeDatabase();
	}
}
