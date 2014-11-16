package core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CloudServer {

	public final Object SEMAPHORE = new Object();
	boolean onlyLastLine;
	
	public CloudServer(boolean onlyLastLine) {
		this.onlyLastLine = onlyLastLine;
	}
	
	public CloudServer() {
		this(false);
	}
	
	/**
	 * @param commands
	 * @return
	 */
	public List<String> runCommands(List<String> commands) {

		List<String> result = Collections.synchronizedList( 
				                  new ArrayList<String>() );
		
		synchronized (SEMAPHORE) {
			try {
				while ( result.size() < commands.size() ) {
					SEMAPHORE.wait();
				}
			}
			catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		
		return result;
	}
}
