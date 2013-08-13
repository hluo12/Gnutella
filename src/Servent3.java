// **************************************************************************************************************/ 
// 
// Servent3 Program:
// ------- ---------
//
// Overview:
// ---------
// Servent3 Mode :
// 	- Recieves input msg
//    - Calls appropriate function as per request.(Pls find each function description below)
// 
// User Mode :
//    - Gets file name from the user. 
//    - If it is not available local system, send query message to all of its neighbors.
//    - Connect with local server in a special leased port.
//    - If hitmessage is recived by localserver, it will be informed to user.
//    - Then user can connect with specific file server & process it.
//    - Otherwise, user should close its connection after specific cutoff time and inform user.
//
// Input: 
// ------
// NOTE: The mode should be given to the program as inline argument as given below. 
//       <server mode> Java Servent3 server 
//       <user mode> Java Servent3 user 
//
// During user execution, user has to feed the file name.
//
// Assumptions:
// ------------
// 	- No neighbors will be added after the system started.
//
// **************************************************************************************************************/
// Created By : Navaneetha Krishnan
// Version    : 1.0
// Date       : 10/21/2012
// Course     : Advanced Operating System
// **************************************************************************************************************/

import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;

public class Servent3 extends Thread {

    // Variables which are used throughout in the program
	 public static final int ServerPORT = 3333;
	 public static int ClientPORT = ServerPORT+1;
    public static final int BUFSIZE = 1024;
	 public int TTR = 0;
	 public final String entry_mode;
	 public String Mode;
	 public final String dirpath="E:\\Navanee\\JGrasp Projects\\Consistent Gnutella\\test\\Servent3\\Master\\";
	 public final String recvdir="E:\\Navanee\\JGrasp Projects\\Consistent Gnutella\\test\\Servent3\\Recievedfiles\\";
	 public static int seqnum=0;
	 public String[] nbraddr; 
	 
	 // Associated array lists
 	 ArrayList<String> hitmsgid_list  = new ArrayList<String>();
	 ArrayList<String> msgid_list 	 = new ArrayList<String>();
    ArrayList<String> invalid_list 	 = new ArrayList<String>();
	 ArrayList<String> selfmsgid_list = new ArrayList<String>();
	 ArrayList<String> peer_list 		 = new ArrayList<String>();
	 ArrayList<String> pollip_list    = new ArrayList<String>();
	 ArrayList<String> pollreq_list   = new ArrayList<String>();
 
	 // Getting input entry_mode of the program
	 public Servent3(String mode){
	 	 entry_mode=mode.toUpperCase();
	 }	

    // Multithreaded methods
    public void run() {
		 	
		  // Read neighbors address from config file 
		  try {
		  		read_configfile();	 
			  }
		  catch (Exception e) {
          		  e.printStackTrace();
					  System.out.println("Error while reading configuration file");
		  }			  
		  	
		  if (entry_mode.equals("SERVENT"))
		  {	
		  		// Servent3 Mode	
     		   try {
						
					  String msg;		  
					  String file;
					  
					  File f = new File(dirpath+"MasterCatalog.txt");
					  if (!(f.exists()))
	 						Mastercatlg_builder();  
					  
					  // Server sockets declaration
       		     ServerSocket serverSckt 		 = new ServerSocket(ServerPORT);
					  ServerSocket localserverSckt = new ServerSocket(ClientPORT);
	  				  Socket 		s			 		 = new Socket();								  	
					 
					  System.out.println("Server (" + serverSckt.getLocalSocketAddress()+") is initiated. Listening.. " );	
				  	  System.out.println(" ");	
					
         		 while (true) {
						
						  try{	
        				  // Client Socket is received.	   		     
						  Socket sckt = serverSckt.accept();
						  DataInputStream in_svr = new DataInputStream(sckt.getInputStream());
						  
						  // Recieving request as msg.
	       		     msg=in_svr.readUTF();
						  System.out.println("Client socket is recieved from "+sckt.getRemoteSocketAddress()+" .");
							
						  // Handling Query request	      		
					     if (msg.indexOf("Query") > -1)
						  {
						      String[] msgarray  = msg.split("[$]+");
								
 						  		// Checking to know whether the msgid is already seen or its own user request  	
	  							if (msgid_list.indexOf(msgarray[1]) == -1 && (msgarray[1].indexOf(sckt.getLocalAddress()+":"+ServerPORT)== -1))
	 	 						{
									System.out.println("Request type: Query  Received From:"+sckt.getRemoteSocketAddress());
									System.out.println("Message :"+msg);
       				        	System.out.println(" ");	
									QueryHandler(msgarray,sckt.getRemoteSocketAddress());
								}
								
								// Expecting local client & Connecting to it
								if ((msgarray[1].indexOf(sckt.getLocalAddress()+":"+ServerPORT)> -1) && (selfmsgid_list.indexOf(msgarray[1])==-1)){
								selfmsgid_list.add(msgarray[1].toString());
								s = localserverSckt.accept(); 
								s.setSoTimeout(18000);
								}
						  } 

						  // Handling Query request	      		
					     if (msg.indexOf("Invalidate") > -1)
						  {
						      String[] msgarray  = msg.split("[$]+");
								
 						  		// Checking to know whether the msgid is already seen or its own user request  	
	  							if (invalid_list.indexOf(msgarray[1]) == -1 && (msgarray[1].indexOf(sckt.getLocalAddress()+":"+ServerPORT)== -1))
	 	 						{
									System.out.println("Request type: InvalidMsg  Received From:"+sckt.getRemoteSocketAddress());
									System.out.println("Message :"+msg);
       				        	System.out.println(" ");	
									InvalidMsgHandler(msg,msgarray,sckt.getRemoteSocketAddress());
								}								
						  }

						  // Handling HitQuery request     
	  				     if (msg.indexOf("Hitquery") > -1)
						  {
						  
						      String[] hmsgarray  = msg.split("[$]+");
						  			
								
								if (hitmsgid_list.size() >= 50)
									hitmsgid_list.subList(1,26).clear();
								
								// Reciving reply for its user request
								if (msg.indexOf((sckt.getLocalAddress().toString())+":"+ServerPORT)>-1)
								{  
 	 								if (hitmsgid_list.indexOf(hmsgarray[1]) == -1)	  
									{
 		  								hitmsgid_list.add(hmsgarray[1].toString());

										System.out.println("Forwarding the message to client...");
										System.out.println("Message :"+msg);
										System.out.println(" ");
										
										// Sending information to its user
										if (s.isClosed()||(!(s.isConnected())))
											s = localserverSckt.accept();
										
										DataOutputStream out = new DataOutputStream(s.getOutputStream());
										out.writeUTF(msg);
										out.close(); 
										s.close();
				  					   s = new Socket();								  
									}
								}
								
								else {
									// Check in associated list if it is not already sent. The msg will be forwarded.
									if (hitmsgid_list.indexOf(hmsgarray[1]) == -1)	  
									{
										System.out.println("Request type: HitQuery  Received From:"+sckt.getRemoteSocketAddress());
										System.out.println("Message :"+msg);
	 			               	System.out.println(" ");	
									
 		  								hitmsgid_list.add(hmsgarray[1].toString());
										HitQueryHandler(hmsgarray,sckt.getRemoteSocketAddress());
									}	
								}
						  } 	
	  			
						  // Handling obtain request	
				        if (msg.indexOf("Obtain") > -1)
						  {
								System.out.println("Request type: Obtain  Received From:"+sckt.getRemoteSocketAddress());
								file=msg.substring((msg.indexOf("$")+1));
								System.out.println("File Requested :"+file);
		               	System.out.println(" ");	
								Send_file(sckt,file);
						  } 
						  
						  if (msg.indexOf("Pollreq") > -1)
						  {
						  		System.out.println("Request type: Query  Received From:"+sckt.getRemoteSocketAddress());
								System.out.println("Message :"+msg);
       				      System.out.println(" ");	
						  
						  		String[] pollreqarr = msg.split("[$]+");
								String pollmsg = " ";
								File poll_file = new File(dirpath+pollreqarr[1]);
								long rcvdvalue = Long.valueOf(pollreqarr[2]);
								String ipaddr  = pollreqarr[3].substring(1,(pollreqarr[3].indexOf(":")));
								int port = Integer.parseInt(pollreqarr[3].substring((pollreqarr[3].indexOf(":")+1),(pollreqarr[3].trim()).length()));	

								if (rcvdvalue < poll_file.lastModified())
									pollmsg="Pollreply$"+pollreqarr[1]+"$invalid$"+TTR;
								else
								   pollmsg="Pollreply$"+pollreqarr[1]+"$valid$"+TTR;
								
								Forward_msg(pollmsg,ipaddr,port,"pollreply");								
								
						  }
						  
						  if (msg.indexOf("Pollreply") > -1)
						  {
						  		System.out.println("Request type: Query  Received From:"+sckt.getRemoteSocketAddress());
								System.out.println("Message :"+msg);
       				      System.out.println(" ");	

						  		String[] pollreparr = msg.split("[$]+");
								String newvalue = "$"+pollreparr[2];

								int check = FileCatalog_Updater(pollreparr[1], "$TTRExpired",newvalue, 3);
						  }
			
						  		
						}catch (SocketTimeoutException ex) {
          		    DataOutputStream out = new DataOutputStream(s.getOutputStream());
						 out.writeUTF("msg");
						 out.close();
						 s.close();
 						 s = new Socket();								  
						 }
        		
					}	  
        		} catch (Exception e) {
          		  e.printStackTrace();
        		}
			}
			
			else if(entry_mode.equals("USER"))
			{
				try {
					
					String User_Choice	= null;	
					System.out.println("***************** Servent3 User Interface ******************  ");  
					
					
					do{
						
						Scanner scanner 		= new Scanner (System.in);
						String filename 		= null;
						String nbripaddr;
						String In_msg;
						String selection;
						int 	 nbrport;	
						long   Start_time;
						long   End_time;   
						int    chk 				= 0;

						// Getting the filename from the user 
						System.out.println(" ");  
						System.out.println("Enter the filename to be searched:");  
						filename = scanner.nextLine();  
					   
						Start_time = System.currentTimeMillis();
						
						// Checking the existence of file in local system
						String filec = dirpath+filename;
						File   fi	 = new File(filec);
						boolean exists = fi.exists();
						
						if (exists){
							System.out.println("Requested File is available in Master directory:"+filec);
						}
											
						else{
								String check = Version_Lookup(filename,1);
								
								if (check.equalsIgnoreCase("invalid")) {
								
									String msgid = String.valueOf(System.currentTimeMillis())+"$"+filename;
									// Sending query message to all its neighbors
									for(int nbritr=0; nbritr<nbraddr.length; nbritr++)
									{
								  	  if ((nbraddr[nbritr].trim()).length() > 0)
									  {							
										nbripaddr=nbraddr[nbritr].substring(0,(nbraddr[nbritr].indexOf(":")));
										nbrport  =Integer.parseInt(nbraddr[nbritr].substring((nbraddr[nbritr].indexOf(":")+1),(nbraddr[nbritr].trim()).length()));	
					 	        
										Forward_msg(msgid,nbripaddr,nbrport,"qrymsg");
								 	 }
  									}
							
									System.out.println("Query messages are forwarded to neighbors");
									System.out.println(" ");
									
									// Waiting for local server to intimate if a hit message is received
									Socket recvsocket = new Socket( "localhost" , ClientPORT);
									recvsocket.setSoTimeout(20000);
						 		   DataInputStream inrecv_socket = new DataInputStream(recvsocket.getInputStream());						 		

									try{
																
										// Recieving hitmessage from the server								
							 			In_msg=inrecv_socket.readUTF();
										System.out.println(In_msg);
										inrecv_socket.close();
										recvsocket.close();						
									
										// Issuing a request directly to the file server
										if (In_msg.indexOf("Hitquery")>-1){
											String[] msgarray  = In_msg.split("[$]+");
											nbripaddr = msgarray[4].substring(1);
											nbrport   = Integer.parseInt(msgarray[5]);
									
											String obt_msg=filename;
											Forward_msg(obt_msg,nbripaddr,nbrport,"obtainmsg");
											System.out.println("Request : "+obt_msg+" is sent to "+nbripaddr);
										
										}
									}
									catch(SocketTimeoutException e)
									{
										// Intimating user about file unavailability
										inrecv_socket.close();
										recvsocket.close();						
									
										System.out.println("File is not available in the system");
										chk=1;
									}
								} 
								else 
									System.out.println("Requested File is available in local directory:"+recvdir+filename);
						
						}			 
						
						End_time = System.currentTimeMillis();
						System.out.println(" ");
						
						System.out.println("Request Elapsed time (in millisecs): "+(End_time-Start_time));
						System.out.println("Request Elapsed time (in Secs): "+((End_time-Start_time)/1000));						 
			

 						System.out.println(" ");
						System.out.println("Do you wish to search another file (Y/N)?");
  						User_Choice=scanner.next(); 

					 }while(User_Choice.equalsIgnoreCase("y"));	
						
					} catch (Exception e) {
				  		 System.out.println(e);
        	    }
			}
			
			else if(entry_mode.equalsIgnoreCase("coordinator"))
			{
				try{
				
					if (Mode.equalsIgnoreCase("PUSH")){
						
						String filename 		= null;
						String User_Choice	= null;
						String nbripaddr;
						String In_msg;
						int 	 nbrport;	 
						int    chk 				= 0;


						do{
						Scanner scanner 		= new Scanner (System.in);					
						System.out.println(" "); 
						System.out.println("Enter the filename to be modified:");  
						filename = scanner.nextLine();
					
						String filec = dirpath+filename;
						String Data  = "update";
						File   fi	 = new File(filec);
						boolean exists = fi.exists();
						
						if (exists){	
							
							File_Append(filec,Data);
							int version = FileCatalog_Updater(filename," "," ",0);
							String msgid = String.valueOf(System.currentTimeMillis())+"$"+filename+"$"+version;
								
							// Sending query message to all its neighbors
							for(int nbritr=0; nbritr<nbraddr.length; nbritr++)
							{
							  	  if ((nbraddr[nbritr].trim()).length() > 0)
								  {							
										nbripaddr=nbraddr[nbritr].substring(0,(nbraddr[nbritr].indexOf(":")));
										nbrport  =Integer.parseInt(nbraddr[nbritr].substring((nbraddr[nbritr].indexOf(":")+1),(nbraddr[nbritr].trim()).length()));	
						 	        
										Forward_msg(msgid,nbripaddr,nbrport,"invalidmsg");
								  }
 	 						}
					  }	
					  else 
					  		System.out.println("File doesn't exist in the master folder");
							
					  System.out.println(" ");
					  System.out.println("Do you wish to modify another file (Y/N)?");
  					  User_Choice=scanner.next(); 

					 }while(User_Choice.equalsIgnoreCase("y"));
					}
					
					if (Mode.equalsIgnoreCase("PULL")){
					
						String filec = recvdir+"filecatalog.txt";
						File   fi	 = new File(filec);
						boolean exists = false;
						long lastrun=0;
						
						while(true){
							fi	 	 = new File(filec);
 							exists = fi.exists();			
						
							if ((exists) && (System.currentTimeMillis() > (lastrun+300000))){
							
								int check = FileCatalog_Updater("###NOT-TO-SEARCH####", "$valid", "$TTRExpired" , 2);
								lastrun=System.currentTimeMillis();
								
								String ipaddress=" ";
								String ipaddr=" ";
								int port=0;
								
								for (int i=0;i<pollreq_list.size();i++){
									ipaddress  = pollip_list.get(i);
									ipaddr=ipaddress.substring(0,(ipaddress.indexOf(":")));
							 		port = Integer.parseInt(ipaddress.substring((ipaddress.indexOf(":")+1),(ipaddress.trim()).length()));	
	
								   Forward_msg(pollreq_list.get(i),ipaddr,port,"pollreq");
								}	
								
								pollip_list.clear();
								pollreq_list.clear();
 							}      
  						}
  
						
					}
				}catch (Exception e){
					System.out.println(e);
				}	
 			}
			else{
				System.out.println("Improper entry_mode");
			}
		
    }

// **************************************************************************************************************/
// Name: read_configfile
//
// Function:
//     1. Locates config file & read the contents (i.e neighbor peers addresses) from it.
//     2. Store those neighbor ipaddress in a public array called nbraddr
//
// **************************************************************************************************************/

    private void read_configfile() throws Exception{
	 
		FileInputStream rdstream  = new FileInputStream(dirpath+"configfile.txt");
  		FileReader fileReader = new FileReader(dirpath+"configfile.txt");
      BufferedReader in_rdfile = new BufferedReader(fileReader);
      List<String> lines = new ArrayList<String>();
  
  		String strLine;
		Mode=" ";
		
  		//Read File Line By Line
  		while ((strLine = in_rdfile.readLine()) != null)   {
			
			if (Mode.equals(" "))
				Mode=strLine;
			else{
				if (strLine.contains("TTR"))
					TTR=Integer.parseInt(strLine.substring(3));
				else	
		  	 		lines.add(strLine);		  
			}		
  		}
  		//Close the input stream
  		in_rdfile.close();
		
		nbraddr = lines.toArray(new String[lines.size()]);
    
  }
  
// **************************************************************************************************************/
// Name: HitQueryHandler
//
//	Input:
//     1. HitQuery message in array
//		 2. Sender's address
// Function:
//     1. Reduce TTL value.
//     2. Find the upstream peer for the request msg id.
//		 3. Forward the message to that peer.
// **************************************************************************************************************/
    
	 private void HitQueryHandler(String[] msgarray, SocketAddress UpstrAddress) throws Exception{
	 
		int 	 TTL;
		String htfwdmsg 	  = null;
		
		// TTL value reduction
		TTL = Integer.parseInt(msgarray[2].toString()); 
		TTL--;
		
		// Forwarding hitquery message to upstream peer
		if (TTL>0){
		
	  		msgarray[2]=Integer.toString(TTL);
				
			for (int i=0; i<msgarray.length; i++)
			{					
				htfwdmsg +=msgarray[i]+"$";
			}	
		    
			htfwdmsg =htfwdmsg.substring(4,((htfwdmsg.length())-1));
		
			// Find address of upstream peer
			int peerindex=msgid_list.indexOf(msgarray[1].toString());
			String tosendaddr = peer_list.get(peerindex);
			
			String htfwdipaddr=tosendaddr.substring(1,tosendaddr.indexOf(":"));
			int 	 htfwdport  =Integer.parseInt(tosendaddr.substring((tosendaddr.indexOf(":")+1),tosendaddr.length()));	
		
			Forward_msg(htfwdmsg,htfwdipaddr,htfwdport,"htfwdmsg");
		}	
		 
	 }

// **************************************************************************************************************/
// Name: QueryHandler
//
// Input:
//     1. Splitted Msg in array
//     2. Upstream address
//  
// Function:
//     1. Add msg id & upstream address with its server port in associated array
//     2. Reduce TTL value
//     3. If TTL value is more that zero, propagate Query message to its neighbors.
//     4. Check whether request file is in local directory.
//     5. If it exists, frame hitquery message & send it to upstream address through its server port. 
//
// **************************************************************************************************************/
	 
	 private void QueryHandler(String[] msgarray, SocketAddress UpstrAddress) throws Exception{
		  
		  String   fwd_msg   = null;
		  String   nbripaddr = null;
		  String   Straddr   = (UpstrAddress.toString());
		  String   storeip   = Straddr.substring(0,Straddr.indexOf(":"));
  		  int      nbrport;
		  int      TTL;
		  int 	  msgexists = 0;	
		  
		  
		  // Arraylist cleanup
		  if (msgid_list.size()>=50)
		  {
		   System.out.println("Msg id Arraylists are cleanedup. Present size is "+msgid_list.size());  
		  	msgid_list.subList(1,26).clear();
		   peer_list.subList(1,26).clear();
		  }
		  
		  // Adding up msg info & upstream address in local maintained directory
		  msgid_list.add(msgarray[1].toString());
		  peer_list.add(storeip+":"+msgarray[4].toString());
		  		  
 	  	  // TTL value rduction 	   
		  TTL=Integer.parseInt(msgarray[2].toString());
  		  TTL--;
			 
		  if (TTL>0)
		  {
		  	  // Forwarding messag construction	
		     msgarray[2]=Integer.toString(TTL);
			  msgarray[4]=Integer.toString(ServerPORT);
			 
		  	  for (int i=0; i<msgarray.length; i++)
			  {					
					fwd_msg +=msgarray[i]+"$";
			  }	
		    
			  fwd_msg =fwd_msg.substring(4,((fwd_msg.length())-1));
		
			  // Fowarding Query message to neighbors 
			  for(int nbritr=0; nbritr<nbraddr.length; nbritr++)
			  {		
			  	  if ((nbraddr[nbritr].trim()).length() > 0)
				  {				
				   nbripaddr=nbraddr[nbritr].substring(0,nbraddr[nbritr].indexOf(":"));
				   nbrport  =Integer.parseInt(nbraddr[nbritr].substring((nbraddr[nbritr].indexOf(":")+1),(nbraddr[nbritr].trim()).length()));	

 				   System.out.println("Forwarding to:"+nbripaddr+":"+nbrport);				
				   Forward_msg(fwd_msg,nbripaddr,nbrport,"fwdmsg");
				  }
			  }
			   
			}		  
		  		  
		 	// Checking existence of the file
		  	String filec = dirpath+msgarray[3];
		  	File   fi	 = new File(filec);
   		boolean exists = fi.exists();
			int    peerint = 0;
			
			if (!(exists)){
				filec  = recvdir+msgarray[3];
				fi		 = new File(filec);
				exists = fi.exists();
				
				if (exists){
					String check = Version_Lookup(msgarray[3],1);
			
					if (check.equalsIgnoreCase("invalid"))
						exists=false;
				}		
			   peerint = 1; 
			}
		  
		  	if( exists)
		  	{
				// HitQuery formation
		   	String htqrymsg="Hitquery"+"$"+msgarray[1]+"$"+10+"$"+msgarray[3]+"$"+"#"+"$"+fi.lastModified()+"$"+peerint;
				
				// Find the upstream peer from array
				int peerindex=msgid_list.indexOf(msgarray[1].toString());
				String tosendaddr = peer_list.get(peerindex);
				
				String recvipaddr=tosendaddr.substring(1,(tosendaddr.indexOf(":")));
				int recvport =Integer.parseInt(tosendaddr.substring((tosendaddr.indexOf(":")+1),(tosendaddr.length())));	
				
				// Send HitQuery message to upstream peer
				Forward_msg(htqrymsg,recvipaddr,recvport,"htqrymsg");
				
			   hitmsgid_list.add(msgarray[1].toString());
		  }
		 
	} 

// **************************************************************************************************************/
// Name: InvalidMsgHandler
//
// Input:
//     1. Splitted Msg in array
//     2. Upstream address
//  
// Function:
//     1. Add msg id & upstream address with its server port in associated array
//     2. Reduce TTL value
//     3. If TTL value is more that zero, propagate Query message to its neighbors.
//     4. Check whether request file is in local directory.
//     5. If it exists, frame hitquery message & send it to upstream address through its server port. 
//
// **************************************************************************************************************/
	 
	 private void InvalidMsgHandler(String msg,String[] msgarray, SocketAddress UpstrAddress) throws Exception{
		  
		  String   fwd_msg   = null;
		  String   nbripaddr = null;
		  String   Straddr   = (UpstrAddress.toString());
		  String   storeip   = Straddr.substring(0,Straddr.indexOf(":"));
  		  int      nbrport;
		  int 	  msgexists = 0;	
		  
		  
		  // Arraylist cleanup
		  if (invalid_list.size()>=50)
		  {
		   System.out.println("Msg id Arraylists are cleanedup. Present size is "+invalid_list.size());  
		  	invalid_list.subList(1,26).clear();
		  }
		  
		  // Adding up msg info & upstream address in local maintained directory
		  invalid_list.add(msgarray[1].toString());		  		  			 
		
		  // Fowarding Query message to neighbors 
		  for(int nbritr=0; nbritr<nbraddr.length; nbritr++)
		  {		
		  	  if ((nbraddr[nbritr].trim()).length() > 0)
			  {				
			   nbripaddr=nbraddr[nbritr].substring(0,nbraddr[nbritr].indexOf(":"));
			   nbrport  =Integer.parseInt(nbraddr[nbritr].substring((nbraddr[nbritr].indexOf(":")+1),(nbraddr[nbritr].trim()).length()));	

			   System.out.println("Forwarding to:"+nbripaddr+":"+nbrport);				
			   Forward_msg(msg,nbripaddr,nbrport,"fwdmsg");
			  }
			   
			}		  
		  		  
		 	// Checking existence of the file
			int version = Integer.parseInt(msgarray[4]);
			version--;
			
		  	String searchkeywd = msgarray[3]+"$"+Integer.toString(version)+"$"+msgarray[2];
			System.out.println(searchkeywd);
			int samp = FileCatalog_Updater(searchkeywd,"$valid","$invalid",1);
		  
	} 


// **************************************************************************************************************/
// Name: Forward_msg
//
// Input:
//     1. Msg to be sent
//     2. Sender address
//		 3. Sender server port	
//		 4. Type of request 	
//  
// Function:
//     1. Create a socket & send the message to the given address
//     2. Request oriented changes like adding address & port for hitquery creation
//
// **************************************************************************************************************/

		private void Forward_msg(String msg,String ipaddr,int port,String type) throws Exception{
			
			String[] msgarr; 
			
			Socket fwdsocket = new Socket(ipaddr, port);				
			DataOutputStream outfwd_socket = new DataOutputStream(fwdsocket.getOutputStream());
				
			// Adding up ipaddress & serverport with hitquery	
			if (type.equals("htqrymsg")){
				msgarr = msg.split("[#]+");
				msg =msgarr[0].toString()+fwdsocket.getLocalAddress()+"$"+ServerPORT+msgarr[1].toString(); 
			}
			
			//	Framing up query message
			if (type.equals("qrymsg")){
				msgarr = msg.split("[$]+");
				msg = "Query"+"$"+fwdsocket.getLocalAddress()+":"+ServerPORT+msgarr[0].toString()+"$"+10+"$"+msgarr[1].toString()+"$"+ServerPORT;
			} 
			
			if (type.equals("obtainmsg")){
				String filename=msg;
				msg ="Obtain"+"$"+filename; 
			}
			
			if (type.equals("invalidmsg")){
				msgarr = msg.split("[$]+");
				msg = "Invalidate"+"$"+fwdsocket.getLocalAddress()+":"+ServerPORT+msgarr[0].toString()+"$"+fwdsocket.getLocalAddress()+":"+ServerPORT+"$"+msgarr[1].toString()+"$"+msgarr[2].toString();
			}
			
			if (type.equalsIgnoreCase("pollreq")){
				String temp=msg;
				msg =temp+fwdsocket.getLocalAddress()+":"+ServerPORT;
			}
			
			outfwd_socket.writeUTF(msg);				
			
			//	Recieving file
			if (type.equals("obtainmsg")){
					String veraddr = Obtain_File(fwdsocket);
					String file=recvdir+"filecatalog.txt";
					File_Append(file,veraddr);
			}	

				outfwd_socket.close();
				fwdsocket.close();
			}

// **************************************************************************************************************/
// Name: Send_file
//
// Input:
//     1. Name of the file
//     2. Socket 
//  
// Function:
//     1. File is being located & send it to the requestor
//
// **************************************************************************************************************/
							 
   private void Send_file(Socket sckt,String filename) throws Exception{
		
		// Forming up absolute path of the file
		String sendfile = " ";	
		String Version=" ";
		String info = " ";
		File file = null;
		int type = 0;
		
		sendfile=dirpath+filename;	
		file = new File(sendfile);
		
		if (!(file.exists())){
			sendfile=recvdir+filename;	
			file = new File(sendfile);
		   type=1;
		}
			
		// Streams for transfer
  		ObjectInputStream ois_filesender = new ObjectInputStream(sckt.getInputStream());
  		ObjectOutputStream oos_filesender = new ObjectOutputStream(sckt.getOutputStream());
	  
		oos_filesender.writeObject(filename);	    
		  
		// Buffer creation
  		FileInputStream fis_filesender = new FileInputStream(file);
 		byte [] buffer = new byte[(int)file.length()];
		  	 
  		Integer bytesRd_ofile = 0;		  
		  
  		// Sending data to requestor 	    
  		while ((bytesRd_ofile = fis_filesender.read(buffer)) > 0) {
  		    oos_filesender.writeObject(bytesRd_ofile);
        	 oos_filesender.writeObject(Arrays.copyOf(buffer, buffer.length));
				
  		}
				
  		if (type==0){
			 Version=Version_Lookup(filename,0);
	  		 String pushmsg = filename+"$"+Version+"$"+sckt.getLocalAddress()+":"+ServerPORT+"$valid";
			 String pullmsg = sckt.getLocalAddress()+":"+ServerPORT+"$"+TTR+"$"+file.lastModified()+"$"+System.currentTimeMillis(); 
			 info = pushmsg+"#"+pullmsg;
		}	
		else
				info  = Version_Lookup(filename,2); 		
		
		oos_filesender.writeObject(info);			
		System.out.println("File "+filename+" is transferred... ");  		
  	   oos_filesender.close();
  		ois_filesender.close();
		
		 }  
		 
// **************************************************************************************************************/
// Name: Obtain_file
//
// Input:
//     1. Name of the file
//     2. Socket 
//  
// Function:
//     1. File is being recieved & stored in the local directory.
//
// **************************************************************************************************************/
	 
	  private String Obtain_File(Socket socket) throws Exception {
      
		  ObjectOutputStream oos_ofile = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream ois_ofile  = new ObjectInputStream(socket.getInputStream());
        FileOutputStream fos_ofile   = null;
		  int version=0;
		  String reply = " ";
        byte [] buffer = new byte[BUFSIZE];

        // Read file name.
		  System.out.println("Filename is recieved.. ");
        Object obj_ofile = ois_ofile.readObject();

        if (obj_ofile instanceof String) {

		  		String file=obj_ofile.toString();
				String fullfile = recvdir+file;
				File f =  new File(fullfile);

		  		fos_ofile = new FileOutputStream(fullfile);
	     } else {
            throwException("Something is wrong");
        }

        // Read file to the end.
        Integer bytesRd_ofile = 0;

        do {
    			try{        
					obj_ofile = ois_ofile.readObject();
					System.out.println(obj_ofile);
					}
				catch(Exception e){
					System.err.println("Error at reading");
					System.err.println(e);
					}	

            if (!(obj_ofile instanceof Integer)) {
                throwException("Something is wrong");
            }
								
            bytesRd_ofile = (Integer)obj_ofile;

            obj_ofile = ois_ofile.readObject();
				
            if (!(obj_ofile instanceof byte[])) {
                throwException("Something is wrong");
            }

            buffer = (byte[])obj_ofile;
				System.out.println("Data receiveing. File write in progress... ");
				System.out.println(" ");

            // Write data to output file.
            fos_ofile.write(buffer, 0, bytesRd_ofile);
        } while (bytesRd_ofile == BUFSIZE);

        fos_ofile.close();
		  
		  obj_ofile = ois_ofile.readObject();
		  reply = (String)obj_ofile;        			
		  	  		  
        ois_ofile.close();
        oos_ofile.close();
		  
		  return reply;
    }

// **************************************************************************************************************/
// Name: FileCatalog_Updater
//
// Input:
//		 1. Searchkeyword 
//     2. Value to be updated
//     3. Newvalue
//		 4. Type 
//  
// Function:
//     1. To change consistency value of an entry in file catalog
//		 2. To update version number of an entry in catalog
//
// **************************************************************************************************************/
	
	 public int FileCatalog_Updater(String Searchkeywd, String oldvalue, String newvalue,int type)  {
	 
	 	String oldFileName=" ";
	 	String tmpFileName=" ";
		String   line;
		String[] msgarr;
		String[] pushmsg;
		String[] pullmsg;
		String   pollreqmsg=" ";	 
		int 	 version = 0;
	 
	 	if (type>0){
	      oldFileName = recvdir+"filecatalog.txt";
      	tmpFileName = recvdir+"temp.txt";}
		
		if (type==0){
			oldFileName = dirpath+"Mastercatalog.txt";
      	tmpFileName = dirpath+"temp.txt";}

      BufferedReader br = null;
      BufferedWriter bw = null;
      try {
				  
         br = new BufferedReader(new FileReader(oldFileName));
	      bw = new BufferedWriter(new FileWriter(tmpFileName));

         while ((line = br.readLine()) != null) {
            	
					if (type==2){
						msgarr  = line.split("[#]+");
						pushmsg = msgarr[0].split("[$]+");
						pullmsg = msgarr[1].split("[$]+");
						
						long TTR_Msg = Long.valueOf(pullmsg[1])+Long.valueOf(pullmsg[3]);
								
						if ((TTR_Msg < System.currentTimeMillis()) && (pushmsg[3].equalsIgnoreCase("valid")))
						{
							System.out.println("TTREXPIRED for "+pushmsg[0]);
							Searchkeywd = "$valid";	
							
							pollip_list.add(pullmsg[0].substring(1));

							pollreqmsg="Pollreq$"+pushmsg[0]+"$"+pullmsg[2]+"$";
							pollreq_list.add(pollreqmsg); 
						}	
					}						
					
					if (line.contains(Searchkeywd)){

						System.out.println(line);
						if (type==0){
							oldvalue	= line.substring((line.indexOf("$")+1),(line.trim()).length());
							version  = Integer.parseInt(oldvalue)+1;
							newvalue = Integer.toString(version);		
						}
										
						if (line.contains(oldvalue)){
							line = line.replace(oldvalue, newvalue);
						}
						
						if (type==3){
							line  = line.substring(0,(line.lastIndexOf('$')));	
							line +="$"+System.currentTimeMillis();
						}	
				  }		
				  
	         bw.write(line);
				bw.newLine();
				
         }
      } catch (Exception e) {
         return version;
      } finally {
         try {
            if(br != null)
               br.close();
         } catch (IOException e) {
            //
         }
         try {
            if(bw != null)
               bw.close();
         } catch (IOException e) {
            //
         }
      }
		
	   // Once everything is complete, delete old file..
 	   File oldFile = new File(oldFileName);
 	   oldFile.delete();
		
		long wait=System.currentTimeMillis()+2000;
		do{
		
		}while(System.currentTimeMillis()<wait);

	   // And rename tmp file's name to old file name
	   File newFile = new File(tmpFileName);
 	   newFile.renameTo(oldFile);
		
		File delFile = new File(tmpFileName);
 	   delFile.delete();

   return version;
	}

// **************************************************************************************************************/
// Name: Version_Lookup
//
// Input:
//		 1. Searchkeyword 
//     2. Value to be updated
//     3. Newvalue
//		 4. Type 
//  
// Function:
//     1. To change consistency value of an entry in file catalog
//		 2. To update version number of an entry in catalog
//
// **************************************************************************************************************/
	
	 public String Version_Lookup(String Searchkeywd, int type)  {
	 
	 	String FileName=" ";
		String line;
		String version = "invalid";
	 
	 	if (type>0)
	      FileName = recvdir+"filecatalog.txt";

		if (type==0)
			FileName = dirpath+"Mastercatalog.txt";

      BufferedReader br = null;
      try {
				  
         br = new BufferedReader(new FileReader(FileName));
			
         while ((line = br.readLine()) != null) {
            if (line.contains(Searchkeywd)){

					if (type==0)
						version  = line.substring((line.indexOf("$")+1),(line.trim()).length());
					else{
							if (type==1){
								if (line.contains("$valid"))    
									version="valid";					
							}		
							else
								version=line;					
					}	
				}	

         }
      } catch (Exception e) {
         return version;
      } finally {
         try {
            if(br != null)
               br.close();
         } catch (IOException e) {
            //
         }
      }
		
   return version;
	}

// **************************************************************************************************************/
// Name: File_Append
//
// Input:
//     1. New entry (i.e filename, version id, 
//     2. Version of the file (to easily locate in the file) 
//     3. Value to be updated
//     4. Newvalue
//  
// Function:
//     1. To change consistency value of an entry in file catalog
//
// **************************************************************************************************************/
	
	 public void File_Append(String modfile, String data)
    {	
    	try{
 		
			//String FileCatalog = recvdir+"filecatalog.txt";
    		File file =new File(modfile);
 
    		//if file doesnt exists, then create it
    		if(!file.exists()){
    			file.createNewFile();
    		}
 
    		//true = append file
    		FileWriter fileWritter = new FileWriter(file,true);
   		BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
    	   bufferWritter.write(data);
			bufferWritter.newLine();
    	   bufferWritter.close();
 
	      System.out.println("Done");
 
    	}catch(IOException e){
    		e.printStackTrace();
    	}
    }

// **************************************************************************************************************/
// Name: Mastercatlg_builder
//
// Input:
//     1. Exception message
//
// **************************************************************************************************************/

	public void Mastercatlg_builder() 
	{
 
 	 String files;
  	 File mstrfldr = new File(dirpath);
	 File mstrcatlg = new File(dirpath+"MasterCatalog.txt");
  	 File[] MasterFiles = mstrfldr.listFiles(); 
	 try{
	 
	 	mstrcatlg.createNewFile();
	 	FileWriter fstream = new FileWriter(mstrcatlg);
  	 	BufferedWriter out = new BufferedWriter(fstream);
 
    	for (int i = 0; i < MasterFiles.length; i++) 
    	{
 
	     if ((MasterFiles[i].isFile())&& (!((MasterFiles[i].toString()).contains("configfile.txt")))) 
	     {	
	   	 files = MasterFiles[i].getName()+"$1";
	   	 out.write(files);
			 out.newLine();
	     }
	  	 }
		 out.close();
	}catch (Exception e){
		System.out.println(e);
	}		 
}

	 
// **************************************************************************************************************/
// Name: Exception Handler
//
// Input:
//     1. Exception message
//
// **************************************************************************************************************/
	 
    public static void throwException(String message) throws Exception {
        throw new Exception(message);
    }

// **************************************************************************************************************/
// Name: Main Function
//
// **************************************************************************************************************/

    public static void main(String[] args) {
	  		new Servent3(args[0]).start();  		
    }
}  