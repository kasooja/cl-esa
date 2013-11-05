package eu.monnetproject.clesa.core.utils;



import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;


/**
 *  
 * @author kasooja 
 */

public class BasicFileTools {

	public static int numberOfLineRead = 0;

	public static BufferedReader getBufferedReader(File file) {
		FileInputStream inputFile = null;
		InputStreamReader streamRead = null;
		BufferedReader read = null;				
		try {		
			inputFile = new FileInputStream(file.getAbsolutePath());
			streamRead = new InputStreamReader(inputFile, "UTF8");
			read = new BufferedReader(streamRead);
		} catch(Exception e) {
			System.out.println("File Not there probably or yet not created: " + file.getAbsolutePath());
			numberOfLineRead = 0;
		}		
		return read;
	}

	public static BufferedReader getBufferedReaderFile(String filePath) {
		return getBufferedReader(getFile(filePath));
	}

	public static String extractText(File file) throws IOException {
		BufferedReader bufferedReader = getBufferedReader(file);
		StringBuffer fileText = new StringBuffer();
		try {
			String line;
			int j = 0;
			while((line = bufferedReader.readLine())!= null) {
				j++;
				fileText.append(line + "\n");
			}
			numberOfLineRead = j;			
		} catch (IOException e) {
			e.printStackTrace();
		}	
		bufferedReader.close();
		return fileText.toString().trim();
	}	

	private static File getFile(String filePath){
		File file = null;
		try {
			file = new File(filePath);
		} catch(Exception E) {
			System.out.println("Not a File Path or File Not Found");
		}
		return file;
	}

	public static String extractText(String filePath) throws IOException {
		return extractText(getFile(filePath));
	}	

	public static boolean writeFile(String filePath, String text) {
		File file = new File(filePath);
		Writer output = null;
		boolean written = false;
		try {
			output = new BufferedWriter(new OutputStreamWriter(
				    new FileOutputStream(file.getAbsolutePath()), "UTF-8"));
			output.write(text);
			output.close();
			written = true;			
		} catch (IOException e) {
			e.printStackTrace();
		}				
		return written;
	}

	public static void deleteDirOrFile(String dirOrFilePath) {	
		File directory = new File(dirOrFilePath);
		if(!directory.exists()){
			System.out.println("Directory does not exist.");
			System.exit(0);
		}else{
			try{
				deleteDirOrFile(directory);
			}catch(IOException e){
				e.printStackTrace();
				System.exit(0);
			}
		}
	}

	private static void deleteDirOrFile(File fileOrFolder)
			throws IOException{
		if(fileOrFolder.isDirectory()){
			//directory is empty, then delete it
			if(fileOrFolder.list().length==0){
				fileOrFolder.delete();
				//				System.out.println("Directory is deleted : " 
				//						+ fileOrFolder.getAbsolutePath());
			}else{
				//list all the directory contents
				String files[] = fileOrFolder.list();
				for (String temp : files) {
					//construct the file structure
					File fileDelete = new File(fileOrFolder, temp);
					//recursive delete
					deleteDirOrFile(fileDelete);
				}
				//check the directory again, if empty then delete it
				if(fileOrFolder.list().length==0){
					fileOrFolder.delete();
					//					System.out.println("Directory is deleted : " 
					//							+ fileOrFolder.getAbsolutePath());
				}
			}
		}else{
			//if file, then delete it
			fileOrFolder.delete();
			//	System.out.println("File is deleted : " + fileOrFolder.getAbsolutePath());
		}
	}

	public static boolean createNewDirectory(String dirPath) {
		boolean success = (
				new File(dirPath)).mkdir();
		return success;
	}

	public static List<String> listSubDir(String dirPath){
		File dir = new File(dirPath);
		if(!dir.isDirectory()){
			System.err.println(dirPath + " is not a directory");
			return null;
		}
		String[] files = dir.list();
		ArrayList<String> filePathList = new ArrayList<String>();

		for(String file: files){
			if(!file.startsWith("."))
				filePathList.add(dirPath+ File.separatorChar +file);
		}

		return filePathList;

	}

}
