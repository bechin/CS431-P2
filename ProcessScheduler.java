import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.FileNotFoundException;

public class ProcessScheduler{

	private static final boolean READY = false;
	private static final boolean RUNNING = true;

	private static int masterPC = 0;
	private static int totalCycles = 0;
	private static Process curProc;
	private static List<Process> table = new ArrayList<>();

	public static void main(String[] args){
		if(args.length != 1)
			throw new IllegalArgumentException("Requires one filename argument.");
		processFile(args[0]);
		
		firstComeFirstServed();
		reset();
		shortestFirst();
		reset();
		roundRobin(50);
		reset();
		roundRobin(100);
		reset();
		random(50);
	}

	private static void processFile(String filename){
		try{
			Scanner fileScanner = new Scanner(new File(filename));
			while(fileScanner.hasNext()){
				String[] tokens = fileScanner.nextLine().split(",");
				table.add(new Process(Integer.parseInt(tokens[0]),
									  Integer.parseInt(tokens[1])));
				totalCycles += Integer.parseInt(tokens[1]);
			}
		}
		catch(FileNotFoundException e){
			e.printStackTrace();
		}
	}

	private static void reset(){
		masterPC = 0;
		for(Process p : table){
			p.pc = 0;
			p.status = READY;
			p.turnaroundTime = 0;
		}
		curProc = null;
		System.out.println();
	}

	private static void firstComeFirstServed(){
		System.out.println("Running First-come, first-served scheduler.");
		for(Process p : table){
			curProc = p;
			curProc.pc = curProc.cycles;
			masterPC += curProc.pc;
			System.out.printf("Process %d finishes on cycle %d.\n", curProc.id, masterPC);
			curProc.turnaroundTime = masterPC;
		}
		printAvgTurnTime();
	}

	private static void	shortestFirst(){
		System.out.println("Running shortest first scheduler.");
		int curProcNum = 0;
		while(masterPC < totalCycles){
			while(table.get(curProcNum).isDone())
				curProcNum++;
			curProc = table.get(curProcNum);
			for(int i = 1; i < table.size(); i++){
				Process next = table.get(i);
				if(!next.isDone() && next.cycles < curProc.cycles)
					curProc = next;
			}
			curProc.pc = curProc.cycles;
			masterPC += curProc.pc;
			System.out.printf("Process %d finishes on cycle %d.\n", curProc.id, masterPC);
			curProc.turnaroundTime = masterPC;
		}
		printAvgTurnTime();
	}

	private static void roundRobin(int quantum){
		System.out.printf("Running round robin scheduler with quantum %d.\n", quantum);
		int curProcNum = 0;
		curProc = table.get(curProcNum);
		while(masterPC < totalCycles){
			if(!curProc.isDone()){
				int curSched = Math.min(curProc.cyclesLeft(), quantum);
				curProc.pc += curSched;
				masterPC += curSched;
				if(curProc.isDone()){
					System.out.printf("Process %d finishes on cycle %d.\n", curProc.id, masterPC);
					curProc.turnaroundTime = masterPC;
				}
			}
			curProcNum = (++curProcNum)%table.size();
			curProc = table.get(curProcNum);
		}
		printAvgTurnTime();
	}

	private static void random(int quantum){
		System.out.printf("Running random scheduler with quantum %d.\n", quantum);
		int randomVal;
		while(masterPC < totalCycles){
			randomVal = (int)(Math.random()*(totalCycles - masterPC));
			int lastIntervalEnd = 0;
			int curProcNum = 0;
			curProc = table.get(curProcNum);
			while(randomVal >= lastIntervalEnd + curProc.cyclesLeft()){
				lastIntervalEnd += curProc.cyclesLeft();
				curProcNum++;
				curProc = table.get(curProcNum);
			}
			if(!curProc.isDone()){
				int curSched = Math.min(curProc.cyclesLeft(), quantum);
				curProc.pc += curSched;
				masterPC += curSched;
				if(curProc.isDone()){
					System.out.printf("Process %d finishes on cycle %d.\n", curProc.id, masterPC);
					curProc.turnaroundTime = masterPC;
				}
			}
		}
		printAvgTurnTime();
	}

	private static void printAvgTurnTime(){
		int totalTurnTime = 0;
		for(Process p : table){
			totalTurnTime += p.turnaroundTime;
		}
		System.out.printf("Average turnaround time: %.2f.\n", totalTurnTime/(double)table.size());
	}

	private static class Process{

		protected int id;
		protected int pc;
		protected boolean status;
		protected int cycles;
		protected int turnaroundTime;

		public Process(int id, int cycles){
			this.id = id;
			pc = 0;
			status = READY;
			this.cycles = cycles;
			turnaroundTime = 0;
		}

		public boolean isDone(){
			return pc == cycles;
		}

		public int cyclesLeft(){
			return cycles - pc;
		}

	}

}
