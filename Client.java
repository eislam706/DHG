import java.io.*;
import java.util.*; 
import java.net.*; 
import java.util.Scanner; 
import java.math.BigInteger; 
import java.security.MessageDigest; 
import java.security.NoSuchAlgorithmException;

public class Client 
{ 
	final static int ServerPort = 1234; 

	public static void main(String args[]) throws UnknownHostException, IOException 
	{ 
		Scanner scn = new Scanner(System.in); 
		
		// getting localhost ip 
		InetAddress ip = InetAddress.getByName("localhost"); 
		
		// establish the connection 
		Socket s = new Socket(ip, ServerPort); 
		
		// obtaining input and out streams 
		DataInputStream dis = new DataInputStream(s.getInputStream()); 
		DataOutputStream dos = new DataOutputStream(s.getOutputStream()); 

		// sendMessage thread 
		Thread sendMessage = new Thread(new Runnable() 
		{ 
			@Override
			public void run() { 
				while (true) { 

					// read the message to deliver. 
					String msg = scn.nextLine(); 
					
					try { 
						// write on the output stream 
						dos.writeUTF(msg); 
					} catch (IOException e) { 
						e.printStackTrace(); 
					} 
				} 
			} 
		}); 
		
		// readMessage thread 
		Thread readMessage = new Thread(new Runnable() 
		{ 
			@Override
			public void run() { 

				while (true) { 
					try { 
						// read the message sent to this client 
						String msg = dis.readUTF(); 
						System.out.println(msg); 
						boolean b = checking(msg);
						dos.writeUTF(b?"1":"0");
					} catch (IOException e) { 

						e.printStackTrace(); 
					} 
				} 
			} 
		}); 

		sendMessage.start(); 
		readMessage.start(); 
	} 

	private static boolean checking(String msg) {
		StringTokenizer st = new StringTokenizer(msg, "#"); 
		String t = st.nextToken();
		int blockNo = Integer.parseInt(st.nextToken());
		int b = blockNo;
		
		int ii = b*4194304;

		for(;ii<4194304*(b+1);ii++) {
			blockNo = ii;

			// System.out.println(t+" "+blockNo);
			String s = "37";
			// String s = "00";

			if (t.length()<4){
				for(int i=0;i<4-t.length();i++){
				t="0"+t;
				}
			}
			s=s+t.substring(t.length()-3);
			String v = t+""+blockNo;
			// System.out.println(v);
			// System.out.println(s);

			String m = getMd5(v);
			// System.out.println(m);

			String firstFive = m.substring(0, 5);


			// System.out.println(firstFive);
			System.out.println(blockNo);

			if(firstFive.equals(s)) {
				return true;
			}
		}
		return false;
	}

	public static String getMd5(String input) 
    { 
        try { 
  
            // Static getInstance method is called with hashing MD5 
            MessageDigest md = MessageDigest.getInstance("MD5"); 
  
            // digest() method is called to calculate message digest 
            //  of an input digest() return array of byte 
            byte[] messageDigest = md.digest(input.getBytes()); 
  
            // Convert byte array into signum representation 
            BigInteger no = new BigInteger(1, messageDigest); 
  
            // Convert message digest into hex value 
            String hashtext = no.toString(16); 
            while (hashtext.length() < 32) { 
                hashtext = "0" + hashtext; 
            } 
            return hashtext; 
        }  
  
        // For specifying wrong message digest algorithms 
        catch (NoSuchAlgorithmException e) { 
            throw new RuntimeException(e); 
        } 
    }
} 
