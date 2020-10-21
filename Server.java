import java.io.*;
import java.util.*;
import java.net.*;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

// Server class
public class Server
{
	static Vector<ClientHandler> ar = new Vector<>();			// Vector for store Client
	static int i = 0; 											// Client counter
	static String roll = "";									// Roll no take input from server use only single input
	static List<Integer> blocks = new ArrayList<>();			// List to keep track for all block which assigned to client
	static Date startDateTime = null;							// First client received date time

	public static void main(String[] args) throws IOException
	{
		ServerSocket ss = new ServerSocket(1234); 				// Server listening port 1234
        Scanner scan = new Scanner(System.in);					// Take input roll no
		System.out.println("Please enter roll no:");
		roll = scan.nextLine();

		Socket s;

		// running infinite loop for getting
		// client request
		while (true)
		{
			s = ss.accept(); 									// Accept the incoming request from client

			System.out.println("New client request received : " + s);

			// obtain input and output streams
			DataInputStream dis = new DataInputStream(s.getInputStream());
			DataOutputStream dos = new DataOutputStream(s.getOutputStream());

			System.out.println("Creating a new handler for this client...");

			// Create a new handler object for handling this request.
			ClientHandler mtch = new ClientHandler(s, "c" + i, i, dis, dos);

			// Create a new Thread with this object.
			Thread t = new Thread(mtch);

			System.out.println("Adding this client to active client list");

			// add this client to active clients list
			ar.add(mtch);

			// start the thread.
			t.start();

			sendClientRoll(i, dos);

			// increment i for new client.
			// i is used for naming only, and can be replaced
			// by any naming scheme
			i++;

		}
	}

	/**
	 * For send response to client
	 * @params - id
	 * @params - DataOutputStream
	 */
	private static void sendClientRoll(int id, DataOutputStream dos) throws IOException {
		String name = "c" + i;									// Setting client name
		for (ClientHandler mc : ar)
		{
			if (mc.name.equals(name) && mc.isloggedin == true)
			{
				blocks.add(i);									// Add client id to the list
				dos.writeUTF(roll+"#"+(blocks.size()-1));		// Send roll no and block number
				System.out.println(mc.name + "#" + (blocks.size() - 1));
				startDateTime = new Date();						// Setting first client response date time
				break;
			}
		}
	}
}

// ClientHandler class
class ClientHandler implements Runnable
{
	Scanner scn = new Scanner(System.in);
	public String name;
	final DataInputStream dis;
	public final DataOutputStream dos;
	Socket s;
	boolean isloggedin;
	public int id;

	// constructor
	public ClientHandler(Socket s, String name, int id,
							DataInputStream dis, DataOutputStream dos) {
		this.dis = dis;
		this.dos = dos;
		this.name = name;
		this.s = s;
		this.isloggedin=true;
		this.id = id;
	}

	@Override
	public void run() {

		String received;
		while (true)
		{
			try
			{
				// receive the string
				received = dis.readUTF();

				System.out.println(received);

				if(received.equals("logout")){
					this.isloggedin=false;
					this.s.close();
					break;
				}

				StringTokenizer st = new StringTokenizer(received, "#");
				String f = st.nextToken();
				String l=st.nextToken();
				int checkValue = Integer.parseInt(f);

				for (ClientHandler mc : Server.ar)
				{
					if (mc.name.equals(this.name) && mc.isloggedin == true)
					{
						if(Server.blocks.size() == 1023) {
							Date nowD =new Date();
							long diff = nowD.getTime() - Server.startDateTime.getTime();
							long diffSec = diff/1000%60;
							long diffMin = diff/(60*1000)%60;
							long diffHou = diff/(60*60*1000);

							System.out.println("Time: "+diffHou+" hours, "+diffMin+" minutes, "+diffSec+" seconds");
							break;
						}
						if (checkValue != 0) {
							System.out.println("["+this.name+"] " + l);
						}
						mc.dos.writeUTF(Server.roll+"#"+(Server.blocks.size()-1));
						Server.blocks.add(mc.id);
						System.out.println(mc.name+"#"+(Server.blocks.size()-1));
						break;
					}
				}


				// break the string into message and recipient part
				// StringTokenizer st = new StringTokenizer(received, "#");
				// String MsgToSend = st.nextToken();
				// String recipient = st.nextToken();

				// System.out.println(Server.roll);

				// for (ClientHandler mc : Server.ar)
				// {
					// if (mc.name.equals(this.name) && mc.isloggedin==true)
					// {
						// mc.dos.writeUTF(this.name+" : "+MsgToSend);
						// break;
					// }
				// }

			} catch (IOException e) {

				e.printStackTrace();
			}

		}
		try
		{
			// closing resources
			this.dis.close();
			this.dos.close();

		}catch(IOException e){
			e.printStackTrace();
		}
	}
}