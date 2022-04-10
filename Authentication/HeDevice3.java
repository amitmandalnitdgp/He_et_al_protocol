import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Random;
import java.util.Scanner;

import javax.crypto.SecretKey;

public class HeDevice3 {

	public int Di, PW, Rd, T, PID, PIN;
	public String com1;

	public static double acosh(double x)
	{
		return Math.log(x + Math.sqrt(x*x - 1.0));
	}

	public static double chebyshev(double x, int z, int n) {
		return Math.cosh(n*acosh(x)%z);
	}

	public static String XOREncode(String st, String key) {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < key.length(); i++)
			sb.append((char)(st.charAt(i) ^ key.charAt(i)));
		String str = sb.toString();
		str = str + st.substring(key.length());
		//System.out.println(st.substring(key.length()));
		return str;
	}

	public static String XORDecodekey(String st, String key) {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < key.length(); i++)
			sb.append((char)(st.charAt(i) ^ key.charAt(i)));
		String str = sb.toString();
		return str;
	}

	public static String XORDecodeString(String st, String key) {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < key.length(); i++)
			sb.append((char)(st.charAt(i) ^ key.charAt(i)));
		String str = sb.toString();
		str = str + st.substring(key.length());
		return str;
	}

	public static String getSha256(String str) {
		MessageDigest digest;
		String encoded = null;
		try {
			digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(str.getBytes(StandardCharsets.UTF_8));
			encoded = Base64.getEncoder().encodeToString(hash);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return encoded;
	} 

	public static void main(String[] args) throws Exception {
		
		double Eelec = 50.0;
		double Eamp = 0.1;
		double d = 1.0;
		long size1 = -1, size2 = -1, size3 = -1, size4 = -1;

		final String HOST = "127.0.0.1";
		final int PORTin = 4085;
		final int PORTout = 4086;
    	int Xgwnsj = 6543;
    	int P = 5555;
    	
/////////////////////// sockets for the new device ///////////////////////////////////////////////////////////////
		ServerSocket trustedServerSocket = new ServerSocket(PORTin);
		Socket trustedClientSocket = trustedServerSocket.accept();
		DataInputStream Device_indata=new DataInputStream(trustedClientSocket.getInputStream());  
		DataOutputStream Device_outdata=new DataOutputStream(trustedClientSocket.getOutputStream());  

/////////////////////// sockets for the Gateway ///////////////////////////////////////////////////////////////		
		
		Socket GWsocket = new Socket(HOST, PORTout);
		DataInputStream GWindata=new DataInputStream(GWsocket.getInputStream());  
		DataOutputStream GWoutdata=new DataOutputStream(GWsocket.getOutputStream()); 
		
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////		
		String input = "", input2 = "";
		while (!input.equals("stop")) {
			
			//////////////// receives from User //////////////////////
			input = Device_indata.readUTF();
			System.out.println("Received at D from U: "+ input);
			String content = new Scanner(new File("GWmemSj.txt")).useDelimiter("\\Z").next(); //reading memory
    		System.out.println("\n----> "+content);
    		String storeRead[] = content.split("<-->"); //SIDj+"<-->"+fi; 
    		String SIDj = storeRead[0].trim();
    		String fj = storeRead[1].trim();  		
    		System.out.println("fj: "+fj);
    		
    		
			if (input.equalsIgnoreCase("stop")) {
				GWoutdata.writeUTF(input);
				GWoutdata.flush();
				break;
				
			}else {
								
				
				////////////////Sending to Gateway //////////////////////
				String UserReceived[] = input.split("<-->");//MIi+"<-->"+Zi+"<-->"+Ni+"<-->"+T1;
				String MIi = UserReceived[0];
				String Zi = UserReceived[1];
				String Ni = UserReceived[2];
				long T1 = Long.parseLong(UserReceived[3].trim());
				
				size1 = (UserReceived[0].length()+UserReceived[1].length()+UserReceived[2].length()+UserReceived[3].length())*16;
				
				long T2 = System.currentTimeMillis();
				if ((T2-T1)>1000) {
	        		System.out.println("System time out...."+(T2-T1));
	        		break;
	        	}
				String Aj = getSha256(fj+Ni+T2);
				String m2 = MIi+"<-->"+Ni+"<-->"+SIDj+"<-->"+Aj+"<-->"+T1+"<-->"+T2;
				
				String sendtoGWsize = ""+MIi+Ni+SIDj+Aj+T1+T2;
				size2 = sendtoGWsize.length()*16;
				
				GWoutdata.writeUTF(m2);
				GWoutdata.flush();
				System.out.println("sent to gateway...");;
				
				////////////////receives from Gateway //////////////////////
				input2 = GWindata.readUTF(); // M6+"<-->"+M7+"<-->"+M8+"<-->"+M9+"<-->"+T3
				System.out.println("received from GW: "+ input2);
				String GWReceived[] = input2.split("<-->");//Fij+"<-->"+Hj+"<-->"+Ei+"<-->"+T3;
				String Fij = GWReceived[0];
				String Hj = GWReceived[1].trim();
				String Ei = GWReceived[2].trim();
				long T3 = Long.parseLong(GWReceived[3].trim());
				
				size3 = (GWReceived[0].length()+GWReceived[1].length()+GWReceived[2].length()+GWReceived[3].length())*16;
				
				String Fij1 = fj+T3;
				String Yip = XORDecodekey(Fij1, Fij).trim();
				System.out.println("Yip: "+ Yip);
				String Hjp = getSha256(Yip);
				int Ki = Integer.parseInt(XORDecodekey(Yip, Zi).trim());
				
				System.out.println("Ki: "+ Ki);
				if(!Hjp.equals(Hj)) {
					System.out.println("Parameter Mismatch in Hj & Hjp...");
	        		break;
				}
				long T4 = System.currentTimeMillis();
				
				if ((T4-T3)>1000) {
	        		System.out.println("System time out...."+(T4-T3));
	        		break;
	        	}
				
				Random rnd = new SecureRandom();
				int b = BigInteger.probablePrime(15, rnd).intValue();
				int Kj = b*P;
				String Rij1 = ""+Ki+T4;
				String Rij = XOREncode(Rij1, ""+Kj);				
				String SK = getSha256(""+(Ki*Ki/P)+Ki+Kj+MIi+SIDj+T1+T4);
				System.out.println("Kj: "+Kj);
				System.out.println("SK: "+SK);
				////////////////Sending to User //////////////////////
				String m4 = Rij+"<-->"+Ei+"<-->"+T4;
				
				String sizemsgtoGW = Rij+Ei+T4; 
				size4 = sizemsgtoGW.length()*16;
				
				Device_outdata.writeUTF(m4);
				Device_outdata.flush();
				System.out.println("Send from Device to User:");
				
				long receiveMsgSize = size1+size3;
				long sendMsgSize = size2+size4;
				long afterUsedMem=Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
				double memKB = Math.round(((afterUsedMem/(8*1024))*100))/100.0 ;
				double sendEnergy = (Eelec*sendMsgSize)+(Eamp*sendMsgSize*d*d);
				double receiveEnergy = Eelec*receiveMsgSize;
				double totalEnergy = sendEnergy+receiveEnergy;
				
				System.out.println("memory usage: " + memKB + " KB");
				System.out.println("Communication cost (send message size): " + sendMsgSize + " bytes");
				System.out.println("receive message size: " + receiveMsgSize + " bytes");
				System.out.println("Sending Energy: " + sendEnergy + " nJ");
				System.out.println("Receiving Energy: " + receiveEnergy + " nJ");
				System.out.println("Total Energy: " + totalEnergy + " nJ");
				
				String store = memKB+"\t"+sendMsgSize+"\t"+receiveMsgSize+"\t"+sendEnergy+"\t"+receiveEnergy+"\t"+totalEnergy;
				Writer output;
				output = new BufferedWriter(new FileWriter("Results.txt", true));  //clears file every time
				output.append(store+"\n");
				output.close();
				
			}
		}

	}

}
