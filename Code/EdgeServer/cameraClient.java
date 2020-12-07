

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;
import java.nio.file.Path;
import java.nio.ByteBuffer;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.nio.file.Files;

public class TcpServer {
	
	public void main() throws Exception{
		String FACE_DETECT_HOST="0.0.0.0";
		int FACE_DETECT_PORT= 8180;
		String S_CONN_HOST = "0.0.0.0";
		int S_CONN_PORT = 8181;
		String S_CONN_TYPE = "tcp";
		String CONN_HOST = "0.0.0.0";
		int CONN_PORT = 8178;
		String CONN_TYPE = "tcp";
		int BUFFERSIZE = 1024;
		
		ServerSocket ss = new ServerSocket(CONN_PORT); // this acts as a server
		System.out.println("server starts, listening");
		int i=0;
		
		while(true) {
			System.out.println("ready for commands");
			
			// This is the socket for Client
			Socket conn = ss.accept();
			System.out.println("Accepted Connection: %%d "+i);
			i++;
			//Process this command
			handleRequest(conn);
		}			
	}
	
	public static void handleRequest(Socket clientCon) {
		String msg;
		String processedFileName = "";
		String fileName;
		Boolean Flag;
		msg = RecvText(clientCon);
		
		fileName = getImageFromRaspberrypi();
		
		
		Flag = fileExists(fileName);
		if (Flag == false){
			System.out.println("fileName does not exist");
			
		} else {
			System.out.println("Processing the image option");
			processedFileName = faceDetection(fileName);
		}
		SendFile(clientCon, processedFileName);
//		
//		
//		SendFile(clientCon, processedFileName);
		
	}
	
	//Get commands from Client
	//Get data from face_detection part
	public static String RecvText(Socket conn)  {
		
		
		String data = " ";
//		try {
//			InputStream input = conn.getInputStream();
//			
//			System.out.println("input: ");
//			BufferedReader br = new BufferedReader(new InputStreamReader(input));
//			System.out.println("BufferedReader: ");
//			data = br.readLine();	    
//			System.out.println("data: "+data);
//			input.close();
//		
//		} catch(IOException ie) {
//			System.out.println("IOException");	
//		}
		

		
		
		try {
			byte[] buffname = new byte[64];
			InputStream input = conn.getInputStream();
			input.read(buffname);
			String nameline = new String(buffname, StandardCharsets.UTF_8);
			String fd_filename = nameline.replace(":","");
			data = fd_filename;
			
			//input.close();
		
		} catch(IOException ie) {
			System.out.println("IOException");	
		}
		
		return data;
	}
	
	public static String RecvText_from_face(Socket conn)  {
		
		
		String data = " ";
		try {
			byte[] buffname = new byte[32];
			InputStream input = conn.getInputStream();
			input.read(buffname);
			String nameline = new String(buffname, StandardCharsets.UTF_8);
			String fd_filename = nameline.replace(":","");
			data = fd_filename;
			
			
		//	input.close();
		
		} catch(IOException ie) {
			System.out.println("IOException");	
		}
		
		return data;
	}

	
	
	
	// request of getting the picture 
	public static String getImageFromRaspberrypi() {

		String data = " ";
		String S_CONN_HOST = "192.168.2.2";
		int S_CONN_PORT = 8181;
		
		
		try {
		Socket conn = new Socket(S_CONN_HOST,S_CONN_PORT);
		Writer writer = new OutputStreamWriter(conn.getOutputStream());
		
	    writer.write("sending picture taking request");
		writer.flush();
		//writer.close();
	//	data = RecvFile(conn, "/Users/feihu/workspace/Project/face_det/Docker/");
		data = RecvFile(conn, "/tmp/");
		
		
		} catch (IOException ie){
			System.out.println("IOException");
		}
		return data;
	}
	
	// operation of getting the picture
	public static String RecvFile(Socket conn, String path) {
		String data = " ";
		
		
		try {
//			
			
			
			byte[] buffsize = new byte[10];
			byte[] buffname = new byte[32];
			InputStream input = conn.getInputStream();
			
			input.read(buffsize);
			input.read(buffname);
			
			String sizeline = new String(buffsize, StandardCharsets.UTF_8);
			String u_sizeline = sizeline.replace(":","");
			int filesize = Integer.parseInt(u_sizeline);
			
			String nameline = new String(buffname, StandardCharsets.UTF_8);
			String u_nameline = nameline.replace(":","");
			String filename = path+u_nameline;
			data = filename;
		
			File newFile = new File(filename);
			
			

			FileOutputStream  fos0 = new FileOutputStream(newFile);
			byte[] buf = new byte[1024];
			int len = 0;
							
			while ((len = input.read(buf)) != -1)
			{
				fos0.write(buf,0,len);
			}
			
			fos0.close();
			//input.close();
//			conn.close();
//			System.out.println("close this connection");
		} catch(IOException ie) {
			//ie.printStackTrace();
			System.out.println("IOException");	
		}
		
		return data;
	}
	
	//Check if the picture(filename) exists or not
	public static Boolean fileExists(String filename) {
		if (!Files.exists(Paths.get(filename))) {
			return false;
			
		}
		return true;
	}
	
	// Send the filename to face_detection container
	public static String faceDetection(String fileName) {
		String data = "data";
		int FACE_DETECT_HOST = 8180;
		try {
		Socket conn = new Socket("0.0.0.0",FACE_DETECT_HOST);
		SendText(conn, fileName);
		data = RecvText_from_face(conn);
		
		}catch(IOException ie) {
			//ie.printStackTrace();
			System.out.println("IOException");	
		}
		return data;
	}
	
	// send filename to 
	public static void SendText(Socket conn, String data) {
		try {
		Writer writer = new OutputStreamWriter(conn.getOutputStream());
		
	    writer.write(data);
		writer.flush();
		}catch(IOException ie) {
			//ie.printStackTrace();
			System.out.println("IOException");	
		}	
	}
	
	public static String FillString(String returnString, int toLength) {
		while(true) {
			int lenString = returnString.length();
			
			if (lenString<toLength){
				returnString = returnString+":";
			
				continue;
			}
			break;	
		}
		return returnString;
		
	}
	
	
	//Send file back to mobileClient
	public static void SendFile(Socket conn, String filename) {
		
		try {
			
			
			filename = filename.trim();
			Path path = Paths.get(filename);
			
			long filesize = Files.size(path);
			Path path_filename = path.getFileName();
			
			String filename_s = path_filename.toString();
			
			String filesize_str = Long.toString(filesize);
			
			
			String fileSize = FillString(Long.toString(filesize),10);
			
			
			String fileName = FillString(filename_s,32);
			
			
			OutputStream output = conn.getOutputStream();
			
			output.write(fileSize.getBytes());
			
			output.write(fileName.getBytes());
			
			
			
			FileInputStream fis1 = new FileInputStream(filename);
			
			OutputStream out = conn.getOutputStream();
			byte[] buf = new byte[1024];
			int len = 0;
			
			while ((len = fis1.read(buf)) != -1)
			{
				out.write(buf,0,len);
			}
			
			fis1.close();		
			
		}catch(IOException ie) {
			//ie.printStackTrace();
			System.out.println("IOException");	
		}
		
		
	}
	


}

	


			
	
	
	


