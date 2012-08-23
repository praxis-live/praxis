package org.jaudiolibs.jnajack.examples;

/**
 * PUBLIC DOMAIN
 */
import java.util.EnumSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jaudiolibs.jnajack.Jack;
import org.jaudiolibs.jnajack.JackClient;
import org.jaudiolibs.jnajack.JackClientRegistrationCallback;
import org.jaudiolibs.jnajack.JackException;
import org.jaudiolibs.jnajack.JackGraphOrderCallback;
import org.jaudiolibs.jnajack.JackOptions;
import org.jaudiolibs.jnajack.JackPortConnectCallback;
import org.jaudiolibs.jnajack.JackPortFlags;
import org.jaudiolibs.jnajack.JackPortRegistrationCallback;
import org.jaudiolibs.jnajack.JackPortType;
import org.jaudiolibs.jnajack.JackProcessCallback;
import org.jaudiolibs.jnajack.JackStatus;

/**\
 * Tests JNAJack Registration and Connection Tracking
 * @author Chuck Ritola
 *
 */

public class TestPortConnectionTracking {

	ExecutorService executor = Executors.newSingleThreadExecutor();
	public static final JackProcessCallback process = new JackProcessCallback()
		{
			@Override
			public boolean process(JackClient client, int nframes)
				{
				//meh.
				return true;
				}
		
		};
		
	private final JackClientRegistrationCallback clientReg = new JackClientRegistrationCallback()
		{
			@Override
			public void clientRegistered(JackClient invokingClient, String clientName)
				{
				System.out.println("client registered: "+clientName);
				printPortsAndConnections();
				}

			@Override
			public void clientUnregistered(JackClient invokingClient, String clientName)
				{
				System.out.println("client de-registered: "+clientName);
				printPortsAndConnections();
				}
		};
		
	private final JackPortRegistrationCallback portReg = new JackPortRegistrationCallback()
		{

			@Override
			public void portRegistered(JackClient invokingClient, String portFullName)
				{
				System.out.println("port registered: "+portFullName);
				printPortsAndConnections();
				}

			@Override
			public void portUnregistered(JackClient invokingClient, String portFullName)
				{
				System.out.println("port de-registered: "+portFullName);
				printPortsAndConnections();
				}
		};
		
	private final JackPortConnectCallback portConn = new JackPortConnectCallback()
		{
			@Override
			public void portsConnected(JackClient invokingClient, String port1FullName, String port2FullName)
				{
				System.out.println("ports connected: "+port1FullName+" and "+port2FullName);
				printPortsAndConnections();
				}
			@Override
			public void portsDisconnected(JackClient invokingClient, String port1FullName, String port2FullName)
				{
				System.out.println("ports disconnected: "+port1FullName+" and "+port2FullName);
				printPortsAndConnections();
				}
		};
		
	private final JackGraphOrderCallback graphOrder = new JackGraphOrderCallback()
		{
			@Override
			public void graphOrderChanged(JackClient invokingClient)
				{
				//System.out.println("Graph order changed.");
				printPortsAndConnections();
				}
		};
		
	private JackClient client;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception 
    {
    
    /**
     * EDITED:
     * org.jaudiolibs.jnajack.Jack
     * org.jaudiolibs.jnajack.JackPort
     * org.jaudiolibs.jnajack.JackClient
     * 
     * ADDS:
     * org.jaudiolibs.jnajack.JackClientRegistrationCallback
     * org.jaudiolibs.jnajack.JackPortRegistrationCallback
     * org.jaudiolibs.jnajack.JackPortConnectCallback
     * org.jaudiolibs.jnajack.JackGraphOrderChangeCallback
     * org.jaudiolibs.jnajack.GraphOrderRejectedException
     */
    
    new TestPortConnectionTracking();
    }
    
    public TestPortConnectionTracking() throws Exception
    	{
    	EnumSet<JackStatus> status = EnumSet.noneOf(JackStatus.class);
        client = Jack.getInstance().openClient("Test", EnumSet.of(JackOptions.JackNoStartServer), status);
        System.out.println("Status dump:");
        for(JackStatus s:status){System.out.println(s);}
        
//        client.setProcessCallback(process);
        client.setClientRegistrationCallback(clientReg);
        client.setPortRegistrationCallback(portReg);
        client.setPortConnectCallback(portConn);
        client.setGraphOrderCallback(graphOrder);
        client.activate();
        
        synchronized(client){client.wait();}//Wait for the universe to end.
    	}
    
    private void printPortsAndConnections()
    	{
    	executor.execute(new Runnable(){public void run()
    		{
    		try
    			{
    	       	 for(String portName:Jack.getInstance().getPorts(client, "", JackPortType.AUDIO, EnumSet.noneOf(JackPortFlags.class)))
    	       	    	{
    	       	    	System.out.println("Port: "+portName);
    	       	    	for(String connectionName:Jack.getInstance().getAllConnections(client, portName))
    	       	    		{System.out.println("	...connected to: "+connectionName);}
    	       	    	}//end for(ports)
    	       	 	System.out.println("==============================");
    	       		}
    		catch(JackException e){e.printStackTrace();}
    		}});
    	}//end printPortsAndConnections()
}//end TestPortConnectinTracking
