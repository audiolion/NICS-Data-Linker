import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.JOptionPane;

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
	
	private Database db;
	private Table patientsTable;
	private String accessPath;
	
	
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
	
	public String setFilePath(){
		FileChooser fc = new FileChooser();
		File file = fc.getDirectoryPath();
		String filePath = file.getAbsolutePath();
		if(filePath.equalsIgnoreCase("No File Selected")){
			return "";
		}
		return filePath;
	}
	
	public String setAccessPath(){
		FileChooser fc = new FileChooser();
		File file = fc.getDirectoryPath();
		String filePath = file.getAbsolutePath();
		if(filePath.equalsIgnoreCase("No File Selected")){
			return "";
		}
		return filePath;
	}
	
	public boolean verifyPassword(String username, String password){
		boolean authResult = false;
		MemberOfAuth auth = new MemberOfAuth("FLH.LOCAL");
		authResult = auth.isMemberOf("GGH Admins", username, password);
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
		}catch(IOException e){
			JOptionPane.showMessageDialog(null, "Could not find or open table.");
		}
		for(int i = 0; i < nicsData.size(); i++){
			if(i%3 == 0){
				try{
					DateFormat dateFormat = new SimpleDateFormat();
					Date date = new Date();
					patientsTable.addRow(Column.AUTO_NUMBER, nicsData.get(i+1), nicsData.get(i), "", nicsData.get(i+2), dateFormat.format(date));
				}catch(IOException e){
					JOptionPane.showMessageDialog(null, "Could not add a row to the Patients Table.");
				}
			}
		}
	}
	
	public void writeResolvedNack(int rowID, String filePath){
		AccessController ac = AccessController.getInstance();
		ac.openDatabase(filePath);
		try{
			patientsTable = db.getTable("Patients");
		}catch(IOException e){
			JOptionPane.showMessageDialog(null, "Could not find or open table.");
		}
		
		ArrayList<String> list = new ArrayList<String>();
		try{
			Cursor cur = CursorBuilder.createCursor(patientsTable);
			while(cur.moveToNextRow()){
				if(!String.valueOf(cur.getCurrentRowValue(patientsTable.getColumn("Acknowledged"))).contains("Yes")
						&& !String.valueOf(cur.getCurrentRowValue(patientsTable.getColumn("Resolved"))).contains("Yes")){
					list.add(cur.getCurrentRowValue(patientsTable.getColumn("Patient_Name")).toString() + "," +
					cur.getCurrentRowValue(patientsTable.getColumn("Meditech_ID")).toString() + "," +
					cur.getCurrentRowValue(patientsTable.getColumn("Admit_Update")).toString() + "," +
					cur.getCurrentRowValue(patientsTable.getColumn("Date/Time")).toString());
				}
			}
		}catch(IOException e){
		}
		String[] data = list.get(rowID).split(",");
		String meditechID = data[1];
		try{
			Cursor cursor = CursorBuilder.createCursor(patientsTable);
			while(cursor.moveToNextRow()){
				if(String.valueOf(cursor.getCurrentRowValue(patientsTable.getColumn("Meditech_ID"))).contains(meditechID)){
					cursor.setCurrentRowValue(patientsTable.getColumn("Resolved"), "Yes");
				}
			}
		}catch(IOException e){
		}
		ac.closeDatabase();
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
					if(String.valueOf(cur.getCurrentRowValue(patientsTable.getColumn("Meditech_ID"))).contains(ackData.get(i))){
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
	 * @param defaultFilePath 
	 * 
	 * @throws IOException
	 */
	public void processNics(String defaultFilePath, String defaultAccessPath) throws IOException{
		FileReader fr = new FileReader();
		FileChooser fc;
		if(!defaultFilePath.equals("")){
			fc = new FileChooser(defaultFilePath);
		}else{
			fc = new FileChooser();
		}
		String[] filePaths = fc.getFilePaths();
		if(filePaths[0].equalsIgnoreCase("No Selection")){
			return;
		}
		String filePath = fc.getDirPath();		
		ArrayList<String> data = fr.readFile(filePaths);
		ArrayList<String> nics = fr.getNicsData(data);
		if(nics.size() == 0){
			JOptionPane.showMessageDialog(null, "No NICS information was found. Please verify the correct files were chosen."); 
			return;
		}
		JOptionPane.showMessageDialog(null, "Please select the Microsoft Access Database to write information to.");
		if(!defaultAccessPath.equals("")){
			fc = new FileChooser(defaultAccessPath);
		}else{
			fc = new FileChooser();
		}
		filePath = fc.getFilePath();
		if(filePath.equalsIgnoreCase("No File Selected")){
			return;
		}
		AccessController ac = AccessController.getInstance();
		ac.openDatabase(filePath);
		ac.writeData(nics);
		ac.closeDatabase();
		JOptionPane.showMessageDialog(null, "Information successfully written.");
	}
	
	/**
	 * Method handles logic for gathering Ack data and cross-checking it against
	 * a MS Access database.
	 * @param defaultFilePath 
	 * 
	 * @throws IOException
	 */
	public void processAcks(String defaultFilePath, String defaultAccessPath) throws IOException{
		FileReader fr = new FileReader();
		FileChooser fc;
		if(!defaultFilePath.equals("")){
			fc = new FileChooser(defaultFilePath);
		}else{
			fc = new FileChooser();
		}
		String[] filePaths = fc.getFilePaths();
		if(filePaths[0].equalsIgnoreCase("No Selection")){
			return;
		}
		String filePath = fc.getDirPath();
		ArrayList<String> data = fr.readFile(filePaths);
		ArrayList<String> acks = fr.getAckData(data);
		if(acks.size() == 0){
			JOptionPane.showMessageDialog(null, "No ACK information was found. Please verify the correct files were chosen."); 
			return;
		}
		JOptionPane.showMessageDialog(null, "Please select the Microsoft Access Database to write information to.");
		if(!defaultAccessPath.equals("")){
			fc = new FileChooser(defaultAccessPath);
		}else{
			fc = new FileChooser();
		}
		filePath = fc.getFilePath();
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
	 * @param defaultAccessPath 
	 * @return 
	 */
	public ArrayList<String> printAcknowledgements(String defaultAccessPath){
		ArrayList<String> list = new ArrayList<String>();
		FileChooser fc;
		if(!defaultAccessPath.equals("")){
			fc = new FileChooser(defaultAccessPath);
		}else{
			fc = new FileChooser();
		}
		String filePath = fc.getFilePath();
		if(filePath.equalsIgnoreCase("No File Selected")){
			return list;
		}
		AccessController ac = AccessController.getInstance();
		ac.openDatabase(filePath);
		try{
			patientsTable = db.getTable("Patients");
			Cursor cur = CursorBuilder.createCursor(patientsTable);
			while(cur.moveToNextRow()){
				if(String.valueOf(cur.getCurrentRowValue(patientsTable.getColumn("Acknowledged"))).contains("Yes")){
					list.add(cur.getCurrentRowValue(patientsTable.getColumn("Patient_Name")).toString() + "," +
				cur.getCurrentRowValue(patientsTable.getColumn("Meditech_ID")).toString() + "," +
				cur.getCurrentRowValue(patientsTable.getColumn("Admit_Update")).toString() + "," +
				cur.getCurrentRowValue(patientsTable.getColumn("Date/Time")).toString());
				}
			}
		}catch(IOException e){
			JOptionPane.showMessageDialog(null, "Could not read Patients Table.");
		}
		ac.closeDatabase();
		return list;
	}
	
	/**
	 * Method handles logic for printing out a neat table of non-acknowledged patients
	 * with their name, meditech ID and Update/Submit code status.
	 * @param defaultAccessPath 
	 * @return 
	 */
	public ArrayList<String> printNacknowledgements(String defaultAccessPath){
		ArrayList<String> list = new ArrayList<String>();
		FileChooser fc;
		if(!defaultAccessPath.equals("")){
			fc = new FileChooser(defaultAccessPath);
		}else{
			fc = new FileChooser();
		}
		String filePath = fc.getFilePath();
		if(filePath.equalsIgnoreCase("No File Selected")){
			return list;
		}
		AccessController ac = AccessController.getInstance();
		ac.openDatabase(filePath);
		try{
			patientsTable = db.getTable("Patients");
			Cursor cur = CursorBuilder.createCursor(patientsTable);
			while(cur.moveToNextRow()){
				if(!String.valueOf(cur.getCurrentRowValue(patientsTable.getColumn("Acknowledged"))).contains("Yes")
						&& !String.valueOf(cur.getCurrentRowValue(patientsTable.getColumn("Resolved"))).contains("Yes")){
					list.add(cur.getCurrentRowValue(patientsTable.getColumn("Patient_Name")).toString() + "," +
					cur.getCurrentRowValue(patientsTable.getColumn("Meditech_ID")).toString() + "," +
					cur.getCurrentRowValue(patientsTable.getColumn("Admit_Update")).toString() + "," +
					cur.getCurrentRowValue(patientsTable.getColumn("Date/Time")).toString());
				}				
			}
		}catch(IOException e){
			JOptionPane.showMessageDialog(null, "Could not read Patients Table.");
		}
		ac.closeDatabase();
		return list;
	}
	
	public ArrayList<String> getUnresolvedNacks(String defaultAccessPath){
		ArrayList<String> list = new ArrayList<String>();
		FileChooser fc;
		if(!defaultAccessPath.equals("")){
			fc = new FileChooser(defaultAccessPath);
		}else{
			fc = new FileChooser();
		}
		accessPath = fc.getFilePath();
		if(accessPath.equalsIgnoreCase("No File Selected")){
			return list;
		}
		AccessController ac = AccessController.getInstance();
		ac.openDatabase(accessPath);
		try{
			patientsTable = db.getTable("Patients");
			Cursor cur = CursorBuilder.createCursor(patientsTable);
			while(cur.moveToNextRow()){
				if(!String.valueOf(cur.getCurrentRowValue(patientsTable.getColumn("Acknowledged"))).contains("Yes")
						&& !String.valueOf(cur.getCurrentRowValue(patientsTable.getColumn("Resolved"))).contains("Yes")){
					list.add(cur.getCurrentRowValue(patientsTable.getColumn("Patient_Name")).toString() + "," +
					cur.getCurrentRowValue(patientsTable.getColumn("Meditech_ID")).toString() + "," +
					cur.getCurrentRowValue(patientsTable.getColumn("Admit_Update")).toString() + "," +
					cur.getCurrentRowValue(patientsTable.getColumn("Date/Time")).toString());
				}
			}
		}catch(IOException e){
			JOptionPane.showMessageDialog(null, "Could not read Patients Table.");
		}
		ac.closeDatabase();
		return list;
	}
	
	public ArrayList<String> getRefreshedNacks(String accessPath){
		AccessController ac = AccessController.getInstance();
		ac.openDatabase(accessPath);
		ArrayList<String> list = new ArrayList<String>();
		try{
			patientsTable = db.getTable("Patients");
			Cursor cur = CursorBuilder.createCursor(patientsTable);
			while(cur.moveToNextRow()){
				if(!String.valueOf(cur.getCurrentRowValue(patientsTable.getColumn("Acknowledged"))).contains("Yes")
						&& !String.valueOf(cur.getCurrentRowValue(patientsTable.getColumn("Resolved"))).contains("Yes")){
					list.add(cur.getCurrentRowValue(patientsTable.getColumn("Patient_Name")).toString() + "," +
					cur.getCurrentRowValue(patientsTable.getColumn("Meditech_ID")).toString() + "," +
					cur.getCurrentRowValue(patientsTable.getColumn("Admit_Update")).toString() + "," +
					cur.getCurrentRowValue(patientsTable.getColumn("Date/Time")).toString());
				}
			}
		}catch(IOException e){
			JOptionPane.showMessageDialog(null, "Could not read Patients Table.");
		}
		ac.closeDatabase();
		return list;
	}
	
	public String getAccessPath(){
		return this.accessPath;
	}
}

