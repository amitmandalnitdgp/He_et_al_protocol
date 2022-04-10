import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.Writer;
import java.math.BigInteger;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Random;
import java.util.Scanner;


public class HeGenericUser3 {

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
		
		Instant responseStart = Instant.now();
		Instant responseEnd = Instant.now();
		long handshakeDuration = -1;
		long sendMsgSize = -1, receiveMsgSize = -1;;
		// memory usage before execution
		long beforeUsedMem=Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
		double Eelec = 50.0;
		double Eamp = 0.1;
		double d = 1.0;
		
		
		long count = 0, total = 0, avgElapsedTime = 0, n= 1;
		final String HOST = "127.0.0.1";
		final int PORT = 4085;
		
		 int IDi = 111;
	     int PWDi = 12345;
	     int SIDj = 2222;
	     int P = 5555;
	     
		String exitStatus= "";
		Socket socket = new Socket(HOST, PORT);
		DataInputStream indata=new DataInputStream(socket.getInputStream());  
		DataOutputStream outdata=new DataOutputStream(socket.getOutputStream());  
		BufferedReader brk=new BufferedReader(new InputStreamReader(System.in)); 
		
		while (count<n) {
			
			
			//exitStatus=brk.readLine();//keyboard input

			if (exitStatus.equalsIgnoreCase("stop")) {
				outdata.writeUTF(exitStatus);
				outdata.flush();
				break;
			}
			
			Instant start = Instant.now(); //time count
			
			////////////////Sending to trusted device //////////////////////
			String content = new Scanner(new File("SC.txt")).useDelimiter("\\Z").next();
    		System.out.println("\n----> "+content);
    		
    		String storeRead[] = content.split("<-->"); //MIi+"<-->"+ei+"<-->"+ri;
    		//System.out.println("\n----> length: "+recvd.length);
    		String MIi = storeRead[0];
    		String ei = storeRead[1];
    		String ri = storeRead[2];
    		
    		String MPi = getSha256(""+ri+PWDi);
    		String fi = XORDecodekey(MPi, ei).trim();
    		System.out.println("fi: "+ fi);
    		long T1 = System.currentTimeMillis();
    		String Yi = getSha256(fi+T1+SIDj);
    		
    		Random rnd = new SecureRandom();
			int a = BigInteger.probablePrime(15, rnd).intValue();
			int Ki = a*P;
			String Zi = XOREncode(Yi, ""+Ki);
			String Ni = getSha256(Yi+MIi+SIDj);
    		String m1 = MIi+"<-->"+Zi+"<-->"+Ni+"<-->"+T1;
    		
    		String sendsize = MIi+Zi+Ni+T1;
			sendMsgSize = sendsize.length()*16;
    		
    		responseStart = Instant.now(); // start of response time
    		
			outdata.writeUTF(m1);
			outdata.flush();
			System.out.println("Sent: " + m1);
			System.out.println("MIi: "+ MIi);
			System.out.println("Ni: "+ Ni);
			System.out.println("Yi: "+ Yi);
			System.out.println("Ki: "+ Ki);
				
////////////////Receiving from trusted device //////////////////////		
			String input2 = indata.readUTF(); 
			
			responseEnd = Instant.now(); // End of response time
			
			System.out.println("Received from D: "+input2);
			String DeviceReceived[] = input2.split("<-->"); //Rij+"<-->"+Ei+"<-->"+T4
			
			String receivesize = DeviceReceived[0]+DeviceReceived[1]+DeviceReceived[2];
			receiveMsgSize = receivesize.length()*16;
			
			String Rij = DeviceReceived[0];
			String Ei = DeviceReceived[1].trim();
			long T4 = Long.parseLong(DeviceReceived[2].trim());
			long T5 = System.currentTimeMillis();
			
			if ((T5-T4)>1000) {
        		System.out.println("System time out...."+(T5-T4));
        		break;
        	}
			
			String Eip = getSha256(fi+Ni);
			if(!Eip.equals(Ei)) {
				System.out.println("Parameter Mismatch in Ei & Eip...");
        		break;
			}
			String Rij1 = ""+Ki+T4;
			String Kj1 = XORDecodekey(Rij1, Rij);
			int Kj = Integer.parseInt(Kj1.trim());
			String SK = getSha256(""+(Ki*Ki/P)+Ki+Kj+MIi+SIDj+T1+T4);
			System.out.println("Kj: "+Kj);
			System.out.println("SK: "+SK);
			
			Instant finish = Instant.now(); // time count
			long timeElapsed = Duration.between(start, finish).toMillis();
			System.out.println("timeElapsed: "+timeElapsed+" milliseconds");
			
			count++;
			outdata.writeUTF("stop");
			outdata.flush();
			
			handshakeDuration = Duration.between(start, finish).toMillis();
			long afterUsedMem=Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
			double memKB = Math.round(((afterUsedMem/(8*1024))*100))/100.0 ;
			long actualMemUsed=afterUsedMem-beforeUsedMem;
			long responseTime = Duration.between(responseStart, responseEnd).toMillis();
			double sendEnergy = (Eelec*sendMsgSize)+(Eamp*sendMsgSize*d*d);
			double receiveEnergy = Eelec*receiveMsgSize;
			double totalEnergy = sendEnergy+receiveEnergy;
			
			System.out.println("\nresponse time: "+responseTime+" milliseconds");
			System.out.println("handshake duration: "+handshakeDuration+" milliseconds");
			System.out.println("memory usage: " + memKB + " KB");
			System.out.println("Communication cost (send message size): " + sendMsgSize + " bytes");
			System.out.println("receive message size: " + receiveMsgSize + " bytes");
			System.out.println("Sending Energy: " + sendEnergy + " nJ");
			System.out.println("Receiving Energy: " + receiveEnergy + " nJ");
			System.out.println("Total Energy: " + totalEnergy + " nJ");
			
			String store = responseTime+"\t"+handshakeDuration+"\t"+memKB+"\t"+sendMsgSize+"\t"+receiveMsgSize+"\t"+sendEnergy+"\t"+receiveEnergy+"\t"+totalEnergy;
			Writer output;
			output = new BufferedWriter(new FileWriter("Results.txt", true));  //clears file every time
			output.append(store+"\n");
			output.close();
		}
	} 

}
