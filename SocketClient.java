package simpleSocket;

import java.io.*;
import java.net.*;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class SocketClient
{
	String PREFIX = "cs5700spring2015";
	String HELLO;
	String STATUS;
	String SOLUTION;
	String BYE;
	private static Socket client = null;

	private SocketClient(int port, boolean SSL, String host, String ID)
	{
		HELLO = PREFIX + " " + "HELLO" + " " + ID + "\n";
		/*
		 * System.out.println(ID); System.out.println(port);
		 * System.out.println(host); System.out.println(HELLO);
		 */
		try
		{
			if (!SSL)
			{
				client = new Socket();
			}
			else
			{
				System.setProperty("javax.net.ssl.trustStore", "./kclient.keystore");
				System.setProperty("javax.net.ssl.trustStorePassword", "*****");
				SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory
						.getDefault();
				client = (SSLSocket) sslsocketfactory.createSocket();
			}
			SocketAddress sockAddr = new InetSocketAddress(host, port);
			client.connect(sockAddr, 1000);
			socketApp(client);
		}
		catch (IOException e)
		{
			System.out.println("Cannot connect to the server!");
			e.printStackTrace();
		}
	}

	private String stringToAscii(String str)
	{
		byte[] bytes = null;
		try
		{
			bytes = str.getBytes("US-ASCII");
		}
		catch (UnsupportedEncodingException e)
		{
			System.out.println("HELLO message not follows ASCII standard");
			e.printStackTrace();
		}
		if ((bytes == null) || (bytes.length == 0))
		{
			return "";
		}
		char[] ascii = new char[bytes.length];
		for (int i = 0; i < bytes.length; i++)
		{
			ascii[i] = (char) bytes[i];
		}
		return new String(ascii);
	}

	private void socketApp(Socket client)
	{
		try
		{
			Writer writer = new OutputStreamWriter(client.getOutputStream());
			writer.write(stringToAscii(HELLO));
			writer.flush();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					client.getInputStream()));
			String message;
			while ((message = reader.readLine()) != null)
			{
				if (isValidBye(message))
				{
					BYE = message;
					writer.close();
					reader.close();
					client.close();
					break;
				}
				else
				{
					STATUS = message;
					String results = solveSolution(STATUS);
					SOLUTION = PREFIX + " " + results + "\n";
					writer.write(SOLUTION);
					writer.flush();
					SOLUTION = PREFIX + " ";
				}
			}
			if (BYE == null)
			{
				throw new IllegalArgumentException(
						"Program did not end correctly, please check hello message");
			}
			else
			{
				System.out.println(BYE);
			}
		}
		catch (IOException e)
		{
			System.out.println("Cannot write to or read from the server!");
			e.printStackTrace();
		}
	}

	private String solveSolution(String STATUS)
	{
		if (STATUS == null)
		{
			throw new IllegalArgumentException("Invalid status (Can not be null)!");
		}
		String delims = "[ ]+";
		String[] tokens = STATUS.split(delims);
		if (tokens.length != 5)
		{
			System.out.println("STATUS: " + STATUS);
			throw new IllegalArgumentException("Invalid status (Incorrect number of field)!");
		}
		if (!tokens[0].equals("cs5700spring2015") || !tokens[1].equals("STATUS"))
		{
			System.out.println("STATUS: " + STATUS);
			throw new IllegalArgumentException("Invalid status (Incorrect value of field)!");
		}
		float operator1 = Float.valueOf(tokens[2]);
		float operator2 = Float.valueOf(tokens[4]);
		if ((operator1 < 1 || operator1 > 1000) || (operator2 < 1 || operator2 > 1000))
			throw new IllegalArgumentException("Invalid status (Operators out of bounds [1,1000])!");
		if (tokens[3].equals("+"))
			return Integer.toString((int) Math.floor(operator1 + operator2));
		else if (tokens[3].equals("-"))
			return Integer.toString((int) Math.floor(operator1 - operator2));
		else if (tokens[3].equals("*"))
			return Integer.toString((int) Math.floor(operator1 * operator2));
		else if (tokens[3].equals("/"))
			return Integer.toString((int) Math.floor(operator1 / operator2));
		else
			throw new IllegalArgumentException("Invalid status (Operation not acceptable)");
	}

	private boolean isValidBye(String message)
	{
		if (message == null)
		{
			return false;
		}
		String delims = "[ ]+";
		String[] tokens = message.split(delims);
		if (tokens.length != 3)
			return false;
		if (!tokens[0].equals("cs5700spring2015") || !tokens[2].equals("BYE"))
			return false;
		return true;
	}

	private static boolean isValidPortNumber(String port)
	{
		for (int i = port.length(); --i >= 0;)
		{
			if (!Character.isDigit(port.charAt(i)))
			{
				return false; // if any char of string port is not digital,
								// return false
			}
		}
		// We can sure that port is at least an integer
		if (Integer.parseInt(port) < 1024 || Integer.parseInt(port) > 65535)
			return false;
		else
			return true;
	}

	private static int GetIndexOfArg(String[] args, String argument)
	{

		for (int i = 0; i < args.length; i++)
		{
			if (args[i].equals(argument))
				return i;
		}
		return -1;
	}

	public static void main(String[] args)
	{
		if (args.length < 2)
			throw new IllegalArgumentException("Please give me the host address and ID!");
		int port = -1;
		boolean SSL = false;
		String ID = args[args.length - 1];
		String host = args[args.length - 2];
		int indexOfPort = GetIndexOfArg(args, "-p");
		if (indexOfPort != -1)
		{
			if (!isValidPortNumber(args[indexOfPort + 1]))
				throw new IllegalArgumentException("Invalid port number! (port number should be"
						+ "an integer with range (1024, 65535])");
			else
			{
				port = Integer.parseInt(args[indexOfPort + 1]);
			}

		}
		if (GetIndexOfArg(args, "-s") != -1)
		{
			SSL = true;
		}
		else
		{
			SSL = false;
		}
		if (port == -1)
		{
			if (!SSL)
			{
				port = 27993;
			}
			else
			{
				port = 27994;

			}
		}
		new SocketClient(port, SSL, host, ID);
	}
}
