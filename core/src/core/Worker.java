package core;

//import java.io.BufferedReader;
//import java.io.ByteArrayOutputStream;
//import java.io.InputStreamReader;
//import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class Worker implements Runnable {

	private List<String[]> result;
	private Server server;
	public volatile boolean poisoned;
	private String pubDNS;
	private String fileName;
	private static ExecCommand executer = new ExecCommand();
	private String[] output;
	private String keypair;
	
	public Worker(List<String[]> result, Server server, String pubDNS, String fileName, String keypair) {
		this.result = result;
		this.server = server;
		this.pubDNS = pubDNS;
		this.fileName = fileName;
		this.keypair = keypair ;
	}

	@Override
	public void run() {
		
		while (!poisoned) {
			String command;
			try {
				command = server.commands.take();
			} catch (InterruptedException e1) {
				continue;
			}
			try {
				
				int pos = fileName.lastIndexOf('/');
				String pureName = fileName.substring(pos+1);
				
					//System.out.println("public DNS: " + instance);
						
				String dnsAdres = pubDNS + ":" + pureName;
				String[] scpCommand = { fileName , "ec2-user@" + dnsAdres , keypair };
				System.out.println(pubDNS);
				boolean success = false;
				int numOfTimes = 0;
				while (!success) {
					Thread.sleep(15*1000);
					numOfTimes++;
					success = true;
					try {
						ScpTo.main(scpCommand);
					}
					catch (Exception error) {
						success = false;
					}
					if (numOfTimes > 15) {
						throw new TimeoutException("Tried 15 times to do SCP but failed");
					}
				}
				output = executer.RunCommand( keypair , pubDNS, command);
	        } 
			catch(Exception e) {
	            System.out.println(e.toString());
	            e.printStackTrace();
	        }

			result.add(output);
			synchronized (server.SEMAPHORE) {
				server.SEMAPHORE.notifyAll();
			}
		}
		// stop instance
	}
}
