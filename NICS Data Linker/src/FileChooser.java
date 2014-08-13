import java.awt.Component;
import java.awt.HeadlessException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
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
	JFileChooser fc;
	File directoryPath;
	String dirPath;
	public FileChooser(){
		fc = new JFileChooser();
	}
	@SuppressWarnings("serial")
	public FileChooser(String filePath){
		fc = new JFileChooser(filePath){
			@Override
			protected JDialog createDialog(Component parent) throws HeadlessException{
				JDialog dialog = super.createDialog(parent);
				java.net.URL path = this.getClass().getResource("/folder-links-icon.png");
				BufferedImage imgs = null;
				try{
					imgs = ImageIO.read(path);
				}catch(IOException e){
				}
				ImageIcon nicsIcon = new ImageIcon(imgs);
				dialog.setIconImage(nicsIcon.getImage());
				return dialog;
			}
		};
		dirPath = filePath;
	}
	
	/**
	 * Method for single file selection that is optimized for NICS processing.
	 * If no selection was made "No File Selected" is returned.
	 * @return returnVal - the file name path
	 */
	public String getFilePath(){
		if(directoryPath != null){
			fc.setCurrentDirectory(this.directoryPath);
		}
		fc.setMultiSelectionEnabled(false);
		if(fc.showOpenDialog(fc) == JFileChooser.APPROVE_OPTION){
			File file = fc.getSelectedFile();
			String filePath = file.getAbsolutePath();
			this.setDirPath(filePath);
			return filePath;
		}else{
			return "No File Selected";
		}
	}
	
	/**
	 * Method for single file selection that is optimized for NICS processing.
	 * If no selection was made "No File Selected" is returned.
	 * @return returnVal - the file name path
	 */
	public String getFilePath(File directoryPath){
		if(directoryPath != null){
			this.directoryPath = directoryPath;
		}
		fc.setCurrentDirectory(this.directoryPath);
		fc.setMultiSelectionEnabled(false);
		if(fc.showOpenDialog(fc) == JFileChooser.APPROVE_OPTION){
			this.dirPath = fc.getSelectedFile().getAbsolutePath();
			return dirPath;
		}else{
			return "No File Selected";
		}
	}
	
	/**
	 * Method for multiple file selection that is optimized for NICS processing.
	 * If no selection was made "No Selection" is returned at index 0.
	 * @return returnVal - a string array containing the file paths
	 */
	public String[] getFilePaths(){
		if(directoryPath != null){
			fc.setCurrentDirectory(this.directoryPath);
		}
		fc.setMultiSelectionEnabled(true);		
		if(fc.showOpenDialog(fc) == JFileChooser.APPROVE_OPTION){
			File[] files = fc.getSelectedFiles();
			String[] returnVal = new String[files.length];
			for(int i = 0; i < files.length; i++){
				returnVal[i] = files[i].getAbsolutePath();
			}
			this.dirPath = returnVal[0];
			return returnVal;
		}else{
			String[] returnVal = new String[]{"No Selection"};
			return returnVal;
		}		
	}
	
	/**
	 * Method for multiple file selection that is optimized for NICS processing.
	 * If no selection was made "No Selection" is returned at index 0.
	 * @return returnVal - a string array containing the file paths
	 */
	public String[] getFilePaths(File directoryPath){
		if(directoryPath != null){
			this.directoryPath = directoryPath;
		}
		fc.setCurrentDirectory(this.directoryPath);
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
	
	public File getDirectoryPath(){
		fc.setMultiSelectionEnabled(false);
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		File file = null;
		if(fc.showSaveDialog(fc) == JFileChooser.APPROVE_OPTION){
			file = fc.getSelectedFile();
		}else{
			file = new File("No File Selected");
		}
		return file;
	}
	
	public void setDirectoryPath(File directoryPath){
		this.directoryPath = directoryPath;
	}
	
	public void setDirPath(String dirPath){
		this.dirPath = dirPath;
	}
	
	public void setFileFilter(AccessFileFilter aff){
		fc.setFileFilter(aff);
	}
	
	public String getDirPath(){
		return this.dirPath;
	}
	
}
