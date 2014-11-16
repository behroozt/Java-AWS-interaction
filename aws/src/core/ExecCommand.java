package core;


//import java.io.BufferedInputStream;
//import java.io.BufferedReader;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.util.ArrayList;
//import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

import core.UserAuthPubKey.MyUserInfo;

public class ExecCommand {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public static String[] RunCommand(String keypair, String host, String command){

		String[] optimumResults = new String[4];
		
	    try{
	      JSch jsch=new JSch();
	      jsch.addIdentity(keypair);
	      
	      String user = "ec2-user";

	      Session session=jsch.getSession(user, host, 22);
	      
	      // username and passphrase will be given via UserInfo interface.
	      UserInfo ui=new MyUserInfo();
	      session.setUserInfo(ui);
	      boolean success = false;
	      int count = 0;
	      while(!success) {
	    	  success = true;
	    	  if ( count > 0 ) {
	    		  Thread.sleep(10*1000);
	    	  }
	    	  try {
	    		  session.connect();
	    	  }
	    	  catch(Exception e) {
	    		  success= false;
	    		  System.out.println("trying session.connect failed, trying again in 10 sec ...");
	    	  }
	    	  if ( count++ > 15 ) {
	    		  throw new TimeoutException("Tried session.connect() 15 times wihtout success");
	    	  }
	      }
	      

	      Channel channel=session.openChannel("exec");
	      ((ChannelExec)channel).setCommand(command);

	      Scanner scanner = new Scanner(channel.getInputStream());
	      channel.connect();
	      Matcher matcherOptValues;
	      Matcher matcherRunTime;
	      Pattern patternOptValues = Pattern.compile("^Complete \\w+");
	      Pattern patternRunTimes = Pattern.compile("^Model building \\w+");
	      while(true){
	        while(scanner.hasNext()){
	        
	        	String line = scanner.nextLine();
	        	matcherRunTime = patternRunTimes.matcher(line);
				matcherOptValues = patternOptValues.matcher(line);
				if (matcherOptValues.find()){
					String[] prts = line.split(",");
					
					String[] prts2;
					
					prts2 = prts[0].split("=");
					Double Value = Double.parseDouble(prts2[1].trim());
					System.out.println("optimum Value= " + Value);
					
					prts2 = prts[1].split("=");
					Double Gamma = Double.parseDouble(prts2[1].trim());
					System.out.println("optimum Gamma= " + Gamma);
					
					prts2 = prts[2].split("=");
					prts2[1] = prts2[1].substring(0, prts2[1].indexOf("]"));
					Double C = Double.parseDouble(prts2[1].trim());
					System.out.println("optimum C= " + C);
					
					optimumResults[0]= Value.toString();
					optimumResults[1] = Gamma.toString();
					optimumResults[2] = C.toString();
									
				}
				else if(matcherRunTime.find()) {
					
					String[] timeLine = line.split(":");
					//String[] timeValue = timeLine[1].split("\\s");
					optimumResults[3] = timeLine[1].toString();
					System.out.println("elapsed time= " + timeLine[1].toString());
				}
			
	        }
	        if(channel.isClosed()){
	          System.out.println("exit-status: "+channel.getExitStatus());
	          break;
	        }
	        try{Thread.sleep(1000);}catch(Exception ee){}
	      }
	      channel.disconnect();
	      session.disconnect();
	      System.out.println("DONE");
	  }catch(Exception e){
	      e.printStackTrace();
	  }
		return optimumResults;
	}
}
