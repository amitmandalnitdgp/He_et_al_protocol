import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Scanner;

import javax.crypto.SecretKey;

public class HeGateway3 {

	public static double acosh(double x) {
		return Math.log(x + Math.sqrt(x * x - 1.0));
	}

	public static double chebyshev(double x, int z, int n) {
		return Math.cosh(n * acosh(x) % z);
	}

	public static String XOREncode(String st, String key) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < key.length(); i++)
			sb.append((char) (st.charAt(i) ^ key.charAt(i)));
		String str = sb.toString();
		str = str + st.substring(key.length());
		// System.out.println(st.substring(key.length()));
		return str;
	}

	public static String XORDecodekey(String st, String key) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < key.length(); i++)
			sb.append((char) (st.charAt(i) ^ key.charAt(i)));
		String str = sb.toString();
		return str;
	}

	public static String XORDecodeString(String st, String key) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < key.length(); i++)
			sb.append((char) (st.charAt(i) ^ key.charAt(i)));
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

	public static void main(String[] args) throws IOException {
		
		final int PORT = 4086;
		int XGWN = 4321;
		
		ServerSocket serverSocket = new ServerSocket(PORT);
		Socket clientSocket = serverSocket.accept();
		DataInputStream din = new DataInputStream(clientSocket.getInputStream());
		DataOutputStream dout = new DataOutputStream(clientSocket.getOutputStream());
		
		String input = "", str2 = "";
		while (!input.equals("stop")) {
			
			////////////////receives from trusted device //////////////////////
			input = din.readUTF();
			System.out.println("Received at GW: "+ input);

			if (input.equalsIgnoreCase("stop")) {
				break;
			}
			else {
				
				////////////////sending to trusted device //////////////////////					
				String deviceReceived[] = input.split("<-->");//MIi+"<-->"+Ni+"<-->"+SIDj+"<-->"+Aj+"<-->"+T1+"<-->"+T2;	
				String MIi = deviceReceived[0].trim();
				String Ni = deviceReceived[1].trim();
				String SIDj = deviceReceived[2].trim();
				String Aj = deviceReceived[3].trim();
				long T1 = Long.parseLong(deviceReceived[4].trim());
				long T2 = Long.parseLong(deviceReceived[5].trim());
				System.out.println("MIi: "+ MIi);
				System.out.println("Ni: "+ Ni);
				
				long T3 = System.currentTimeMillis();
				if ((T3-T2)>1000) {
	        		System.out.println("System time out...."+(T3-T2));
	        		break;
	        	}
				
				String fjp = getSha256(SIDj+XGWN); 
				String Ajp = getSha256(fjp+Ni+T2);
				String fip = getSha256(MIi+XGWN);
				String Yip = getSha256(fip+T1+SIDj);
				String Nip = getSha256(Yip+MIi+SIDj);
				System.out.println("Yip: "+ Yip);
				if(!Ni.equals(Nip)) {
					System.out.println("Parameter Mismatch in Ni & Nip...");
	        		break;
				}
				if(!Aj.equals(Ajp)) {
					System.out.println("Parameter Mismatch in Aj & Ajp...");
	        		break;
				}
				
				String Fij1 = fjp+T3;
				String Fij = XOREncode(Fij1, Yip);
				String Hj = getSha256(Yip);
				String Ei = getSha256(fip+Nip);
				String m3 = Fij+"<-->"+Hj+"<-->"+Ei+"<-->"+T3;
				
				
				dout.writeUTF(m3); // send to trusted device
				dout.flush();
				System.out.println("Sent to Device from Gateway: "+m3);

			}
			
			
		}
		
	}

}
