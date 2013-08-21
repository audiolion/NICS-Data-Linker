import java.io.File;
import javax.swing.JFileChooser;

/**
 * This is a wrapper class for a JFileChooser that provides two methods
 * for choosing files, one for multiple file selection and one for single
 * file selection. The methods also provide a return value if no file was
 * selected or the cancel or exit command was issued by the user.
 * 
 * @author Ryan Robert Castner - ryancastner@msn.com
 * @version 1.0	- 8/20/2013
 * 
 */

public class FileChooser {
	
	/*
	 * Default Constructor
	 */
	public FileChooser(){
		
	}
	
	/**
	 * Method for single file selection that is optimized for NICS processing.
	 * If no selection was made "No File Selected" is returned.
	 * @return returnVal - the file name path
	 */
	public String getFile(){
		JFileChooser fc = new JFileChooser();
		fc.setMultiSelectionEnabled(false);
		if(fc.showOpenDialog(fc) == JFileChooser.APPROVE_OPTION){
			File file = fc.getSelectedFile();
			String returnVal = file.getAbsolutePath();
			return returnVal;
		}else{
			return "No File Selected";
		}
	}
	
	/**
	 * Method for multiple file selection that is optimized for NICS processing.
	 * If no selection was made "No Selection" is returned at index 0.
	 * @return returnVal - a string array containing the file paths
	 */
	public String[] getFiles(){
		JFileChooser fc = new JFileChooser();
		fc.setMultiSelectionEnabled(true);
		if(fc.showOpenDialog(fc) == JFileChooser.APPROVE_OPTION){
			File[] files = fc.getSelectedFiles();
			String[] returnVal = new String[files.length];
			for(int i = 0; i < files.length; i++){
				returnVal[i] = files[i].getAbsolutePath();
			}
			return returnVal;
		}else{
			String[] returnVal = new String[]{"No Selection"};
			return returnVal;
		}		
	}
	
}
