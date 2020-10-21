import java.io.*;
import java.util.*;
import java.net.*;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;

// Server class
public class Server
{

	// Vector to store active clients
	static Vector<ClientHandler> ar = new Vector<>();

	// counter for clients
	static int i = 0;
	static String roll = "";
	static List<Integer> blocks = new ArrayList<>();

	public static void main(String[] args) throws IOException
	{
		// server is listening on port 1234
		ServerSocket ss = new ServerSocket(1234);
        Scanner scan = new Scanner(System.in);
		System.out.println("Please enter roll no:");
		roll = scan.nextLine();

		Socket s;

		// running infinite loop for getting
		// client request
		while (true)
		{
			// Accept the incoming request
			s = ss.accept();

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

	private static void sendClientRoll(int id, DataOutputStream dos) throws IOException {
		String name = "c"+i;
		int sizB = blocks.size();
		sizB = sizB==0?0:sizB-1;
		for (ClientHandler mc : ar)
		{
			if (mc.name.equals(name) && mc.isloggedin==true)
			{
				blocks.add(i);
				dos.writeUTF(roll+"#"+(blocks.size()-1));
				System.out.println(mc.name+"#"+(blocks.size()-1));
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