package core;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class ArrayJobs {
	public static int remainder;
	public static List<String> jobCommand = new ArrayList<String>();
	public static int modelsPerJob ;
	public static void main(String[] args) {
		
		//For c=[0,5] and gamma=[3,10] and 5 jobs
		List<List<Point>> jobs = setUpJobs(0, 5, 3, 10, 5);
		
		System.out.println("We have the following jobs:");
		int cnt=0;
		for (List<Point> job : jobs){
			System.out.println("Job " + cnt + ": " + job.toString());
			cnt++;
		}
		
		for (String command : jobCommand){
			System.out.println(command);
		}
		
	}

	
	
	public static List<List<Point>> setUpJobs(int cStart, int cEnd, int gammaStart, int gammaEnd, int noParallelJobs) {
		// Compute dimensions. Number of models: c * gamma
		int cSize=(cEnd-cStart+1);
		int gammaSize=(gammaEnd-gammaStart + 1);
		int totalSize=cSize*gammaSize;

		//Prune number of jobs if not enough models to fill
		if (totalSize<noParallelJobs){
			System.out.println("Number of models less than number oj jobs. Pruned number of jobs to: " + totalSize);
			noParallelJobs=totalSize;
		}

		modelsPerJob = totalSize/noParallelJobs;

		System.out.println("Number of models to build: " + totalSize);
		System.out.println("Desired number of parrallel jobs: " + noParallelJobs);
		
		//If not summing up to 0, add one model per job
		if (modelsPerJob*noParallelJobs!=totalSize){
			modelsPerJob++;
			remainder = totalSize % noParallelJobs;
			}

		System.out.println("Number of models per job: " + modelsPerJob);

		//Set up list of jobs, each job is a list of points
		List<List<Point>> jobs = new ArrayList<List<Point>>();
		for (int i=0; i<noParallelJobs; i++){
			List<Point> job = new ArrayList<Point>();
			jobs.add(job);
		}
		int remainderCounter = 0;
		int currjobix=0;
		int currjobSize=0;

		for (int c = cStart; c <= cEnd; c++){
			for (int g = gammaStart; g <= gammaEnd; g++){
				Point p = new Point(c, g);
				if (currjobix < (noParallelJobs-1)){    //Do not increase if we are in last job, this can be larger than the other
					if (remainder != 0) {
					if (currjobix < remainder){
					if (currjobSize >= modelsPerJob){
						
						//Get next job
						currjobix++;
						currjobSize=0;
					}
					}
					else if ((currjobix >= remainder) & (remainder != 0)){
						if (currjobSize >= (modelsPerJob - 1)){
							
							//Get next job
							currjobix++;
							currjobSize=0;
						}
					}
					}
					else {
						if (currjobSize >= modelsPerJob){
							
							//Get next job
							currjobix++;
							currjobSize=0;
						}
					}
				}
				List<Point> currjob = jobs.get(currjobix);
				currjob.add(p);
				currjobSize++;
			}
		}
		
		for (int i =0 ; i < noParallelJobs ; i ++){
			String paramList = "";
			for(int j=0; j < jobs.get(i).size(); j++){
				paramList += jobs.get(i).get(j).x + "," + jobs.get(i).get(j).y + ";" ;	
			}
			jobCommand.add(paramList);
		}
		
		return jobs;
	}
	
	
}
