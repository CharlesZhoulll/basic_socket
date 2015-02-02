package project1;

import java.io.*;
import java.net.*;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class ScoketClient
{
	String HELLO = "cs5700spring2015 HELLO ";
	String STATUS;
	String SOLUTION = "cs5700spring2015 ";
	String BYE;

	private ScoketClient(String ID, String host, int port, boolean SSL)
			throws UnknownHostException, IOException
	{
		HELLO = HELLO + ID + "\n";
		/*
		 * System.out.println(ID); System.out.println(host);
		 * System.out.println(port); System.out.println(SSL);
		 */
/*		System.out.println(host);
		System.out.println(port);
		System.out.println(HELLO);*/
		// SSLSocket sslClient;
		if (!SSL)
		{
			Socket client = new Socket();
			SocketAddress sockAddr = new InetSocketAddress(host, port);
			client.connect(sockAddr, 1000); // Setup timeout 1s
			socketApp(client);
		}
		else
		{
			SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
			SSLSocket client = (SSLSocket) sslsocketfactory.createSocket();
			SocketAddress sockAddr = new InetSocketAddress(host, port);
			client.connect(sockAddr, 1000); // Setup timeout 1s
			socketApp(client);
		}
	}

	private void socketApp(SSLSocket client) throws IOException
	{
		Writer writer = new OutputStreamWriter(client.getOutputStream());
		
		//writer.write(stringToAscii(HELLO));
		writer.flush();
		Reader reader = new InputStreamReader(client.getInputStream());
		int intValueOfChar;
		client.setSoTimeout(10); // Time out timer: 10s
		try
		{
			while ((intValueOfChar = reader.read()) != -1)
			{
				STATUS += (char) intValueOfChar;
			}
		}
		catch (SocketTimeoutException e)
		{
			System.out.println("Timeout!");
		}
		intValueOfChar = 0; // reset message
		// System.out.println("STATUS: " + STATUS);
		String results = solveSolution(STATUS);
		SOLUTION = SOLUTION + results + "\n";
		System.out.println(SOLUTION);
		// System.out.println("SOLUTION: " + SOLUTION);
		writer.write(SOLUTION);
		writer.flush();
		try
		{
			while ((intValueOfChar = reader.read()) != -1)
			{
				BYE += (char) intValueOfChar;
			}
		}
		catch (SocketTimeoutException e)
		{
			System.out.println("Timeout!");
		}
		if (isValidBye(BYE))
		{
			System.out.println("BYE: " + BYE);
		}
		writer.close();
		reader.close();
		client.close();
	}

	private String stringToAscii(String str) throws UnsupportedEncodingException
	{
		byte[] bytes = str.getBytes("US-ASCII");
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

	private void socketApp(Socket client) throws IOException
	{
		Writer writer = new OutputStreamWriter(client.getOutputStream());
		//System.out.println("HELLO: " + HELLO);
		writer.write(stringToAscii(HELLO));
		writer.flush();
		BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
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
				SOLUTION = SOLUTION + results + "\n";
				writer.write(SOLUTION);
				writer.flush();
				SOLUTION = "cs5700spring2015 ";
			}
		}
		if (BYE == null)
		{
			throw new IllegalArgumentException("Program did not finish correctly");
		}
		else
		{
			System.out.println(BYE);
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
			return Integer.toString((int)Math.floor(operator1 + operator2));
		else if (tokens[3].equals("-"))
			return Integer.toString((int)Math.floor(operator1 - operator2));
		else if (tokens[3].equals("*"))
			return Integer.toString((int)Math.floor(operator1 * operator2));
		else if (tokens[3].equals("/"))
			return Integer.toString((int)Math.floor(operator1 / operator2));
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
		if (tokens.length != 3) return false;
		if (!tokens[0].equals("cs5700spring2015") || !tokens[2].equals("BYE")) return false;
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

	public static void main(String[] args) throws UnknownHostException, IOException
	{
		if (args.length < 2)
			throw new IllegalArgumentException("Please give me the host address and ID!");
		int port = -1;
		boolean SSL = false;
		String ID = args[args.length - 1];
		String host = args[args.length - 2];
		if (args[0].equals("-p"))
		{
			if (!isValidPortNumber(args[1]))
				throw new IllegalArgumentException("Invalid port number! (port number should be"
						+ "an integer with range (1024, 65535])");
			else
			{
				port = Integer.parseInt(args[1]);
			}
			if (args[2].equals("-s"))
			{
				SSL = true;
			}
		}
		else if (args[0].equals("-s"))
		{
			SSL = true;
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
		new ScoketClient(ID, host, port, SSL);
	}
}
