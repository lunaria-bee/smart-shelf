package netNode;

import java.util.Scanner;

import netNode.NetUI;

public class TEST_NetUI {
   public static void main(String[] args) {
	   String ip = "";
	   int choice = 0;
	   Scanner scanner = new Scanner(System.in);
	   
	   /* need to manually input ip until ip scanner function is created */
       System.out.println("Input ip to connect to: ");
       ip = scanner.nextLine();
       
       //initialize socket in new thread
       NetUI netUI = new NetUI(ip);
	   netUI.start();
       
       /* choose network function */
       while(choice != 3){
	       System.out.println("(1) Send String, (2) Strings Retrieved, (3) End server communication: ");
	       choice = Integer.parseInt(scanner.nextLine());
	       if(choice == 1){
	    	   netUI.sendString("This is sent from TestServer.");
	    	   netUI.sendString("This is also from Test Server.");
	    	   netUI.sendString("Same.");
	       }else if(choice == 2){
	    	   System.out.println(netUI.pop());
	    	   System.out.println(netUI.pop());
	    	   System.out.println(netUI.pop());
	       }else if(choice == 3){
	    	   netUI.close();
	       }
       }
       scanner.close();
   }		
}
   