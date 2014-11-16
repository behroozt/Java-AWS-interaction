package core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Server {
	private InstanceManager manager = new InstanceManager();
	public List<String> startedInstancesIds;
	public List<String> startedInstancesDNS;
	public final Object SEMAPHORE = new Object();
	BlockingQueue<String> commands = new LinkedBlockingQueue<String>();
	private int numOfInstances = 0;
	private final static String KEY_PAIR   = "ec2-keypair-new";
	
	//small instance
	//private static final String IMAGE_ID   = "ami-e560a98c";
	//private static final String INSTANCE_TYPE = "m1.small";
	
	//large instance
	//private static final String IMAGE_ID   = "ami-df6aa3b6";
	//private static final String INSTANCE_TYPE = "m1.large";
	
	//large instance
	private static final String IMAGE_ID   = "ami-89c4b4e0";
	private static final String INSTANCE_TYPE = "m1.large";
	
	private static InstanceManager.NewInstances runningInstances = new InstanceManager.NewInstances();
	public boolean onlyLastLine = false;
	private static ScpTo scpManager = new ScpTo();
		
	public Server(int numOf, String fileName, String keypair ) throws Exception {
		this.numOfInstances = numOf;
		manager.startInstance(1, numOfInstances, KEY_PAIR, IMAGE_ID, INSTANCE_TYPE);
		startedInstancesDNS = runningInstances.publicDNS ;
		
		
		if (numOf == 0) {
			stopAll();
			return;
		}
	}
	
	/**
	 * @param commands
	 * @return
	 */
	public List<String[]> runCommands(List<String> commandsToBeRun, String fileName, String keypair) {

		if (numOfInstances==0) {
			throw new IllegalStateException(
			    "There are no running instances" );
		}
		
		this.commands.addAll(commandsToBeRun);
		synchronized (this.commands) {
			this.commands.notifyAll();
		}
		
		List<String[]> result = Collections.synchronizedList( 
				                  new ArrayList<String[]>() );
		
		List<Thread> workerThreads = new ArrayList<Thread>();
		List<Worker> workers = new ArrayList<Worker>();
		
		for (String pubDNSAdress : startedInstancesDNS){
			Worker w = new Worker(result, this, pubDNSAdress,fileName, keypair);
			workers.add(w);
			Thread t = new Thread(w);
			workerThreads.add(t);
			t.start();
		}
		
		synchronized (SEMAPHORE) {
			try {
				while ( result.size() < commandsToBeRun.size() ) {
					SEMAPHORE.wait();
				}
			}
			catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		for ( Worker w : workers ) {
			w.poisoned = true;
		}
		for ( Thread t : workerThreads ) {
			t.interrupt();
		}
		
		return result;
	}

	public void stopAll() {
		
		startedInstancesIds = runningInstances.instancesIds ;
		for (String instanceId : startedInstancesIds){
			manager.stopInstance(instanceId);
		}
			
	}
}
