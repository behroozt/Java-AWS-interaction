package core;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeAvailabilityZonesResult;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceState;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;


public class InstanceManager {

	public static class NewInstances {
		
		static List<String> instancesIds = new ArrayList<String>();
		static List<String> publicDNS = new ArrayList<String>();
	}


	private AmazonEC2 ec2;
    private String    InstanceId;
    
    private void init() throws IOException {
        AWSCredentials credentials = new PropertiesCredentials(
                InstanceManager.class.getResourceAsStream("AwsCredentials.properties"));

        ec2 = new AmazonEC2Client(credentials);
    }
	
    public InstanceManager() {
	    	try {
	    		init();
	    	} catch (IOException e) {
	    		throw new IllegalStateException(e);
	    	}
    }
	
	

	public void startInstance(int minCount, int maxCount, String keyPair, String ImageId,String InstanceType) throws Exception{
		
		//List<String> instancesIds = new ArrayList<String>();
		NewInstances runningInstance = new NewInstances();
		try {
            DescribeAvailabilityZonesResult availabilityZonesResult = ec2.describeAvailabilityZones();
            System.out.println("You have access to " + availabilityZonesResult.getAvailabilityZones().size() +
                    " Availability Zones.");
            RunInstancesRequest runInstancesRequest = new RunInstancesRequest();
            runInstancesRequest.setImageId(ImageId);
            runInstancesRequest.setMinCount(minCount);
            runInstancesRequest.setMaxCount(maxCount);
            runInstancesRequest.setInstanceType(InstanceType);
            runInstancesRequest.setKeyName(keyPair);
            
			ec2.runInstances(runInstancesRequest);
			
            DescribeInstancesResult describeInstancesRequest = ec2.describeInstances();
            //describeInstancesRequest.getReservations().clear();
            List<Reservation> reservations = describeInstancesRequest.getReservations();
            Set<Instance> instances = new HashSet<Instance>();
            
            for (Reservation reservation : reservations) {
                instances.addAll(reservation.getInstances());
              
            }
                        
            int reservationSize = reservations.size();
            System.out.println("reservation size: " + reservationSize);
            
            int instancesSize = instances.size();
            
            
            if (reservationSize > 0) {
            	
            	for (int i = 0 ; i < reservationSize ; i ++) {
            		int resInstanceSize = describeInstancesRequest.getReservations().get(i).getInstances().size();
            		for (int j = 0 ; j < resInstanceSize ; j ++) {
            			String instanceStatus = describeInstancesRequest.getReservations().get(i).getInstances().get(j).getState().getName();
            			//System.out.println("status: " + instanceStatus);
            			while (instanceStatus.equals("pending")) {
            				Thread.currentThread().sleep(5*1000);
            				System.out.print(".");
            				describeInstancesRequest=ec2.describeInstances();
            				instanceStatus = describeInstancesRequest.getReservations().get(i).getInstances().get(j).getState().getName();
            			}
            			if (instanceStatus.equals("running")) {
            				String instanceId = describeInstancesRequest.getReservations().get(i).getInstances().get(j).getInstanceId();
            				//System.out.println("NEW: " + instanceId);
            				runningInstance.instancesIds.add(instanceId);
            				String pubDNS = describeInstancesRequest.getReservations().get(i).getInstances().get(j).getPublicDnsName().toString();
            				
            				runningInstance.publicDNS.add(pubDNS);
            			}
            		}
            	}
            	
            }

        } catch (AmazonServiceException ase) {
                System.out.println("Caught Exception: " + ase.getMessage());
                System.out.println("Reponse Status Code: " + ase.getStatusCode());
                System.out.println("Error Code: " + ase.getErrorCode());
                System.out.println("Request ID: " + ase.getRequestId());
        }
	        
	}
	
public void stopInstance(String id) {
		
	TerminateInstancesRequest terminateInstancesRequest = new TerminateInstancesRequest();
    terminateInstancesRequest.withInstanceIds(id);
    ec2.terminateInstances(terminateInstancesRequest);
	}


public void getRunningInstancesIds() {
	
	DescribeInstancesResult describeInstancesRequest = ec2.describeInstances();
	List<Reservation> reservations = describeInstancesRequest.getReservations();
    Set<Instance> instances = new HashSet<Instance>();
    
    for (Reservation reservation : reservations) {
        instances.addAll(reservation.getInstances());
    }
    
    int reservationSize = reservations.size();
    if (reservationSize > 0) {
    	for (int i = 0 ; i < reservationSize ; i ++) {
    		InstanceId = describeInstancesRequest.getReservations().get(i).getInstances().get(0).getInstanceId();
    		System.out.println("instance " + i + ": " + InstanceId);
    	}
    }
    
}

}
