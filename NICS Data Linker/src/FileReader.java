import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * This is a wrapper class that creates a file reader that is optimized
 * for NICS, NACK, and ACK file processing through regular expressions.
 * 
 * @author Ryan Robert Castner - ryancastner@msn.com
 * @version 1.0 - 8/20/2013
 */
public class FileReader {
	/*
	 * Default Constructor
	 */
	public FileReader(){
		
	}
	
	/**
	 * Method returns an ArrayList of all the NICS data. The data is in the form
	 * ADT Unique ID (Meditech ID), Name, Update/Admit Code. Regular expressions
	 * are used to extract the data from the files and then string manipulation
	 * is used to put the information in a more presentable form. Because this is
	 * a flat array, a modulo counter needs to be used to parse through the data.
	 * 
	 * @param data - the text to filter through
	 * @return nicsData - ArrayList of data in a 3 mod form
	 */
	public ArrayList<String> getNicsData(ArrayList<String> data){
		ArrayList<String> nicsData = new ArrayList<String>();
		if(data.isEmpty()){
			return nicsData;
		}
		for(int i = 0; i < data.size(); i++){
			//Splitting a NACK, ACK or PRENIC
			String[] array = data.get(i).split("CS-ADT.1.");
			if(data.get(i).contains("ADT^")){
				String[] array2 = array[1].split("\\|T\\|");
				nicsData.add(array2[0].trim());
				String[] array3 = array[1].split("\\|");
				String temp = array3[14].replace('^', ' ');
				nicsData.add(temp.trim());
				String[] array4 = array[0].split("ADT\\^");
				String temp2 = array4[1].replace('|', ' ');
				nicsData.add(temp2.trim());
			}
		}		
		return nicsData;
	}
	
	/**
	 * Method returns an ArrayList of ACK data. This only contains the
	 * Meditech ID (ADT) numbers.
	 * 
	 * @param data - the data to filter through
	 * @return ArrayList of Acknowledged Meditech ID numbers
	 */
	public ArrayList<String> getAckData(ArrayList<String> data){
		ArrayList<String> ackData = new ArrayList<String>();
		if(data.isEmpty()){
			return ackData;
		}
		for(int i = 0; i < data.size(); i++){
			//Splitting a NACK, ACK or PRENIC
			String[] array = data.get(i).split("CS-ADT.1.");
			if(!data.get(i).contains("ERR") && data.get(i).contains("ACK^")){
				String[] array2 = array[1].split("\\|T\\|");
				ackData.add(array2[0].trim());
			}
		}
		
		return ackData;
	}
	
	/**
	 * Method returns an array list of unacknowledged data listed
	 * singly by their Meditech ID (ADT) number. 
	 * 
	 * @param data - the data to filter through
	 * @return ArrayList of Not-Acknowledged Meditech ID numbers
	 */
	public ArrayList<String> getNackData(ArrayList<String> data){
		ArrayList<String> nackData = new ArrayList<String>();
		if(data.isEmpty()){
			return nackData;
		}
		for(int i = 0; i < data.size(); i++){
			//Splitting a NACK, ACK or PRENIC
			String[] array = data.get(i).split("CS-ADT.1.");
			if(data.get(i).contains("ERR")){
				String[] array2 = array[1].split("ERR");
				nackData.add(array2[0].trim());
			}
		}
		
		return nackData;
	}
	
	/**
	 * Parses through files and grabs all the characters from the files and adds them
	 * as strings into an array list.
	 * 
	 * @param filePaths - the string array of file paths to parse through
	 * @return data - the list of character data in string form with each index representing a file
	 * @throws IOException
	 */
	public ArrayList<String> readFile(String[] filePaths) throws IOException{
		ArrayList<String> data = new ArrayList<String>();
		InputStream is = null; 
	    InputStreamReader isr = null;
	    BufferedReader br = null;
	    String filePath = "";
		for(int i = 0; i < filePaths.length; i++){
			String temp = filePaths[i];		
			if(temp.equalsIgnoreCase("No Selection")){
				return data;
			}
		      try{
		         // open input stream test.txt for reading purpose.
		         is = new FileInputStream(temp);
		         
		         // create new input stream reader
		         isr = new InputStreamReader(is);
		         
		         // create new buffered reader
		         br = new BufferedReader(isr);
		      
		         int value=0;
		         
		         // reads to the end of the stream 
		         while((value = br.read()) != -1)
		         {
		            // converts int to character
		            char c = (char)value;
		            
		            // add characters to string
		            filePath += c;
		         }
		         
		      }catch(Exception e){
		         e.printStackTrace();
		      }finally{
		         
		         // releases resources associated with the streams
		         if(is!=null)
		            is.close();
		         if(isr!=null)
		            isr.close();
		         if(br!=null)
		            br.close();
		      }
		      data.add(filePath);
		      filePath = "";
		}
		return data;
	}
}
