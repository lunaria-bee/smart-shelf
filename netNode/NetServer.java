package netNode;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Vector;

import db.Db;
import netNode.StartServerSocket;

public class NetServer implements Runnable {

    IOException ioException;
    
    UnknownHostException unknownHostException;
    @SuppressWarnings({ "unchecked", "rawtypes" })
	Vector<Socket> socket = new Vector();
    @SuppressWarnings({ "unchecked", "rawtypes" })
	Vector<OutputStream> out = new Vector();
    @SuppressWarnings({ "unchecked", "rawtypes" })
	Vector<String> identity = new Vector();
    
    StartServerSocket startServerSocket = null;
    SocketServer socketServer = null;
    Db db = null;
    
    int count = -1;
    
	Queue<String> queue = new LinkedList<>();

    public NetServer(StartServerSocket startServerSocket, Db db, SocketServer socketServer) {
        this.startServerSocket = startServerSocket;
        this.socketServer = socketServer;
        this.db = db;
    }
	
    public void run() {
		/*starts loop for user input in new thead. Not needed in final implementation, as they'll
		  be calling the functions of their own accord.*/
    	MainServer mainServer = new MainServer();
    	mainServer.main(this, db);
    	
    	//System.out.println("exited out of mainServer.main...");
    }
    
    public void setSocket(Socket socket, String client, int num){
        try {
        	//System.out.println("getting output stream for NetServer...");
        	this.socket.add(num, socket);
        	out.add(num, socket.getOutputStream());
        	identity.add(num, client);
        	count = num;

        } catch (UnknownHostException e) {
            this.unknownHostException = e;
            System.err.println("UnknownHostException in socket creation");
            return;
        } catch (IOException e) {
            this.ioException = e;
            System.err.println("IOException in socket creation");
            return;
        }
    }
    
    public String getIdentity(int num){
    	return identity.get(num);
    }
    
    public int getCount(){
    	return count;
    }
    
    public boolean sendString(String str, int num){
    	//create request string
        String intent = "SendString~";
        if(str != "close"){
	        if(out != null){
			    try {
			    	out.get(num).flush();
			    	out.get(num).write(intent.getBytes());
			    	out.get(num).flush();
			        //System.out.println("sendString intent sent...");
			        
			        //send string
			        out.get(num).flush();
			        out.get(num).write(str.getBytes());
			        out.get(num).flush();
			        out.get(num).write("~".getBytes());
			        out.get(num).flush();
			        //System.out.println("string sent: " + str);
			        return true;
			       
			    } catch (IOException e){
					e.printStackTrace();
					System.err.println("IOException in sendString()");
					return false;
			    }
	        }else{
	        	System.out.println("No socket connected at" + num +".");
	        	return false;
	        }
        }else{
        	try {
    	        out.get(num).flush();
            	out.get(num).write(str.getBytes());
            	out.get(num).flush();
            	out.get(num).write("~".getBytes());
            	out.get(num).flush();
    	        //System.out.println("string sent: " + str);
    	        return true;
    	        
            } catch (IOException e){
    			e.printStackTrace();
    			System.err.println("IOException in sendString()");
    			return false;
            }
        }     
    }

    public String pop(){
    	if(queue.isEmpty())
    	{
    		return "empty";
    	}else{
    		return queue.remove();
    	}
    }
    
    void addStringToQueue(String str){
    	queue.add(str);
    }
    
    public void close(int num){
    	if(!socket.get(num).isClosed()){
    		try {
    			identity.set(num, "OFFLINE");
    			socket.get(num).close();

				//System.out.println("netServer: Closed socket #" + num + ".");
				//System.out.println("identity of #" + num + " set to " + identity.get(num));
			} catch (IOException e) {
				System.err.println("IOException closing socket #" + num + ".");
			}
    	}
    }
    
    public void exit(){
    	if(count < 0){
    		System.out.println("No sockets to close.");
    	}
    	for(int i = count; i >= 0; i--){
    		sendString("close", i);
    		close(i);	
    	}
    	socketServer.closeServer();
    }
}