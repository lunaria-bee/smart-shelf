package netNode;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import Test.TestServer;


public class NetServer extends Thread {

    IOException ioException;
    UnknownHostException unknownHostException;
    Socket socket = null;

    public NetServer(Socket s) {
        socket = s;
    }
	
    public void run() {
    	TestServer testServer = new TestServer();
    	testServer.test(this);
    }

    public void dump(){
    	//create request string
        String request = "0 " + "DumpDatabase~";
        
        // send request through socket
        System.out.println("sending request through socket...");
        System.out.println("string sent: " + request);
        
		try {
			OutputStream out = socket.getOutputStream();
		
			out.write(request.getBytes());
	        out.flush();
	    	
	    	System.out.println("sending C:/smart-shelf/networking/javaServer/database.txt to server...");
	
	        //get file from external storage
	        File file = new File("C:\\smart-shelf\\networking\\javaServer", "database.txt");
	
	        //byte array with size of the file 
	        byte[] bytes = new byte[(int) file.length()];
	
	        //read in from the file
	        try{
	        	BufferedInputStream bIn = new BufferedInputStream(new FileInputStream(file));
	        	
	        	bIn.read(bytes, 0, bytes.length);
	        	
		        //output on socket
		        out.write(bytes, 0, bytes.length);
		        out.flush();
		        out.write("~".getBytes());
		        out.flush();
		
		        //close stream objects
		        bIn.close();
	        	
	        } catch (FileNotFoundException e)
	        {
	        	e.printStackTrace();
	        	System.out.println("FileNotFoundException in dump()");
	        }
	        
	        out.close();
	        
	        System.out.println("Dumped Database");
        
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("IOException in dump()");
		}
    }
    
    public void request(){
    	//create request string
        String request = "0 " + "ReqDatabase~";
        
        // send request through socket
        System.out.println("sending request through socket...");
        System.out.println("string sent: " + request);
        
        try {
	        OutputStream out = socket.getOutputStream();
	        out.write(request.getBytes());
	        out.flush();
	        out.close();
	        
	        //open file
	        File file = new File("C:\\smart-shelf\\networking\\javaClient", "database.txt");
	        //will need to increase size of byte array if information exceeds 1024 bytes
	        byte[] bytes = new byte[1024];
	        InputStream in = socket.getInputStream();
	        BufferedOutputStream bOut = new BufferedOutputStream(new FileOutputStream(file));
	
	        //read in from the socket input stream and write to file output stream
	        int bytesRead = in.read(bytes, 0, bytes.length);
	        bOut.write(bytes, 0, bytesRead);
	        
	        //closing stream objects
	        bOut.close();
	        in.close();
	        
	        System.out.println("Database received in /networking/javaClient");
        } catch (IOException e){
			e.printStackTrace();
			System.out.println("IOException in request()");
        }
    }

    public int checkIfSocketClosed(int choice){
    	if(socket == null){
    		System.out.println("Client closed socket...ending communication.");
    		return 3;
    	}
		return choice;
    }
}
