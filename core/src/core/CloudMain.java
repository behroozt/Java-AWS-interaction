package core;

import java.awt.Point;
//import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.amazonaws.services.ec2.model.InstanceState;

import core.InstanceManager.NewInstances;

public class CloudMain {
	
	private static String moleculeFilename;
	private static Integer number ;
	private static Integer gammaStart;
	private static Integer gammaEnd;
	private static Integer cStart;
	private static Integer cEnd;
	private static String activityProperty;
	private static String positiveValue;
	private static String keypair;
		
	static List<String> instancesIds = new ArrayList<String>();
	static List<String> instancesPubDNS = new ArrayList<String>();
//	private static InstanceManager manager = new InstanceManager();
//	private static InstanceManager.NewInstances idHolder = new InstanceManager.NewInstances();
	private static ArrayJobs jobs = new ArrayJobs();
//	private static ScpTo scpManager = new ScpTo();
	
	
	private static UserAuthPubKey runner = new UserAuthPubKey();
	
	/**
	 * @param args
	 * @throws ParseException 
	 * @throws Exception 
	 */
	public static void main(String[] args) throws ParseException, Exception {
		checkArgs(args);
		System.out.println("File " + moleculeFilename + " has been selected.");
		System.out.println("Running on " + number + " instances.");
		
		int pos = moleculeFilename.lastIndexOf('/');
		String pureName = moleculeFilename.substring(pos+1);
		
		String commandToBeRun = "time java -jar signmodel-withdeps.jar -i " + pureName + " -ap \"" + activityProperty + 
		"\" -c true -pa \"" + positiveValue + "\" -o ./opt_results/ -folds 5 -trainfinal false -optimize array -optarray ";
		
		Server server = new Server(number,moleculeFilename, keypair);
		
		List<List<Point>> jobList = jobs.setUpJobs(cStart, cEnd, gammaStart, gammaEnd, number);
		List<String> commands = new ArrayList<String>() ;
		
		System.out.println("We have the following jobs:");
		int cnt=0;
		int numJobs = jobList.size();
		System.out.println("num of jobs: " + numJobs);
		
		for (List<Point> job : jobList){
			System.out.println("Job " + cnt + ": " + job.toString());
			String paramList = "";
			
			for(int i=0; i < job.size(); i++){
				paramList += job.get(i).x + "," + job.get(i).y + ";" ;	
			}
			

			paramList = paramList.substring(0, (paramList.length() - 1));
			System.out.println(paramList);
			commands.add(cnt, commandToBeRun + "\"" + paramList + "\"");
			cnt++;
		}
		
		cnt = 0 ;
		for (String command : commands) {
			System.out.println("command " + cnt + ": " + command);
			cnt ++;
		}
		
		List<String[]> finalResults = server.runCommands(commands, moleculeFilename, keypair);
		for (String[] result : finalResults) {
			int count = 0;
			for (String r : result) {
				if (count++ != 0 ) {
					System.out.print("\t");
				}
				System.out.print(r);
			}
			System.out.println();
		}
		
		float bestValue ;
		int finalResultsSize = finalResults.size();
		bestValue = Float.parseFloat(finalResults.get(0)[0]);
		
		for (int i=1; i < finalResultsSize ; i ++) {
			if (Float.parseFloat(finalResults.get(i)[0]) > bestValue){
				bestValue = Float.parseFloat(finalResults.get(i)[0]);
			}
			
		}
		System.out.println("the best optimum value is: " + bestValue);
		
		float bestGamma = 0 ;
		bestGamma = Float.parseFloat(finalResults.get(0)[1]);
		for (int i=1; i < finalResultsSize ; i ++) {
			if (Float.parseFloat(finalResults.get(i)[1]) > bestGamma){
				bestGamma = Float.parseFloat(finalResults.get(i)[1]);
			}
//			
		}
		System.out.println("the best Gamma value is: " + bestGamma);
		
		float bestC = 0 ;
		bestC = Float.parseFloat(finalResults.get(0)[2]);
		for (int i=1; i < finalResultsSize ; i ++) {
			if (Float.parseFloat(finalResults.get(i)[2]) > bestC){
				bestC = Float.parseFloat(finalResults.get(i)[2]);
			}
			
		}
		System.out.println("the best C value is: " + bestC);
		
		System.out.println("the elapsed time is: " + finalResults.get((finalResultsSize - 1))[3].toString());
		
		
		System.out.println("gamma start value: " + gammaStart);
		System.out.println("gamma end value: " + gammaEnd);
		System.out.println("c start value: " + cStart);
		System.out.println("c end value: " + cEnd);
		server.stopAll();
		System.exit(1);
	}
	 public static void checkArgs(String[] args) throws ParseException {
	        
	        CommandLineParser parser = new GnuParser();
	        Options options = new Options();
	        
	        final String MOLECULE_FILE = "moleculeFile";
	        final String INSTANCES= "instances";
	        final String G_START = "gStart";
	        final String G_END = "gEnd";
	        final String C_START = "cStart";
	        final String C_END = "cEnd";
	        final String AP = "ap";
	        final String PA = "pa";
	        final String KEYPAIR_PATH = "keypair";
	        
	        Option molFile= OptionBuilder.withArgName( "file" )
	                                  .hasArg()
	                                  .withDescription("SD file with molecules")
	                                  .create( MOLECULE_FILE );
	        options.addOption( molFile );
	        
	        Option activProperty = OptionBuilder.withArgName( "actProperty" )
            .hasArg()
            .withDescription("Activity Property in the SDFile")
            .create( AP );
	        options.addOption( activProperty );

	        Option posValue = OptionBuilder.withArgName( "value" )
            .hasArg()
            .withDescription("positive value for the Activity Property")
            .create( PA );
	        options.addOption( posValue );
	        
	        Option numOfInstances= OptionBuilder.withArgName( "instancesNum" )
            .hasArg()
            .withDescription("number of instances to run")
            .create( INSTANCES );
	        options.addOption( numOfInstances );
	        
	        Option gamma_Start= OptionBuilder.withArgName( "gammaStart" )
            .hasArg()
            .withDescription("start value for gamma")
            .create( G_START );
	        options.addOption( gamma_Start );
	        
	        Option gamma_End= OptionBuilder.withArgName( "gammaEnd" )
            .hasArg()
            .withDescription("end value for gamma")
            .create( G_END );
	        options.addOption( gamma_End );
	        
	        Option c_Start= OptionBuilder.withArgName( "cStart" )
            .hasArg()
            .withDescription("start value for c")
            .create( C_START );
	        options.addOption( c_Start );
	        
	        Option c_End= OptionBuilder.withArgName( "cEnd" )
            .hasArg()
            .withDescription("end value for c")
            .create( C_END );
	        options.addOption( c_End );
	        
	        Option keypair_Path= OptionBuilder.withArgName( "keypairPath" )
            .hasArg()
            .withDescription("path to the keypair")
            .create( KEYPAIR_PATH );
	        options.addOption( keypair_Path );
	        
	        
	        if ( args.length == 0 ) {
	            HelpFormatter formatter = new HelpFormatter();
	            formatter.printHelp( "CloudMain", options );
	        }
	        
	        CommandLine cmd = parser.parse( options, args);
	        if ( cmd.hasOption(MOLECULE_FILE) ) {
	            moleculeFilename = cmd.getOptionValue( MOLECULE_FILE );
	        }
	        else {
	            throw new IllegalArgumentException( "Give " + MOLECULE_FILE );
	        }
	        if ( cmd.hasOption(INSTANCES) ) {
	            number = Integer.parseInt(cmd.getOptionValue( INSTANCES ));
	        }
	        else {
	            throw new IllegalArgumentException( "Give " + INSTANCES );
	        }
	        if ( cmd.hasOption(G_START) ) {
	            gammaStart = Integer.parseInt(cmd.getOptionValue( G_START ));
	        }
	        else {
	            throw new IllegalArgumentException( "Give " + G_START );
	        }
	        if ( cmd.hasOption(G_END) ) {
	            gammaEnd = Integer.parseInt(cmd.getOptionValue( G_END ));
	        }
	        else {
	            throw new IllegalArgumentException( "Give " + G_END );
	        }
	        if ( cmd.hasOption(C_START) ) {
	            cStart = Integer.parseInt(cmd.getOptionValue( C_START ));
	        }
	        else {
	            throw new IllegalArgumentException( "Give " + C_START );
	        }
	        if ( cmd.hasOption(C_END) ) {
	            cEnd = Integer.parseInt(cmd.getOptionValue( C_END ));
	        }
	        else {
	            throw new IllegalArgumentException( "Give " + C_END );
	        }
	        if ( cmd.hasOption(PA) ) {
	            positiveValue = cmd.getOptionValue( PA );
	        }
	        else {
	            throw new IllegalArgumentException( "Give " + PA );
	        }
	        if ( cmd.hasOption(AP) ) {
	            activityProperty = cmd.getOptionValue( AP );
	        }
	        else {
	            throw new IllegalArgumentException( "Give " + AP );
	        }
	        if ( cmd.hasOption(KEYPAIR_PATH) ) {
	            keypair = cmd.getOptionValue( KEYPAIR_PATH );
	        }
	        else {
	            throw new IllegalArgumentException( "Give " + KEYPAIR_PATH );
	        }
	    }
}
