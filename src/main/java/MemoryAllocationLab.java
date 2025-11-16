import java.io.*;
import java.util.*;

public class MemoryAllocationLab {

	static class MemoryBlock {
		int start;
		int size;
		String processName; // null if free

		public MemoryBlock(int start, int size, String processName) {
			this.start = start;
			this.size = size;
			this.processName = processName;
		}

		public boolean isFree() {
			return processName == null;
		}

		public int getEnd() {
			return start + size - 1;
		}
	}

	static int totalMemory;
	static ArrayList<MemoryBlock> memory;
	static int successfulAllocations = 0;
	static int failedAllocations = 0;

	/**
	 * TODO 1, 2: Process memory requests from file
	 * <p>
	 * This method reads the input file and processes each REQUEST and RELEASE.
	 * <p>
	 * TODO 1: Read and parse the file - Open the file using BufferedReader - Read
	 * the first line to get total memory size - Initialize the memory list with one
	 * large free block - Read each subsequent line and parse it - Call appropriate
	 * method based on REQUEST or RELEASE
	 * <p>
	 * TODO 2: Implement allocation and deallocation - For REQUEST: implement
	 * First-Fit algorithm * Search memory list for first free block >= requested
	 * size * If found: split the block if necessary and mark as allocated * If not
	 * found: increment failedAllocations - For RELEASE: find the process's block
	 * and mark it as free - Optionally: merge adjacent free blocks (bonus)
	 */
	public static void processRequests(String filename) {
		memory = new ArrayList<>();
		
		try (BufferedReader read = new BufferedReader(new FileReader(filename))) {

			String lineOne = read.readLine();

			totalMemory = Integer.parseInt(lineOne.trim());

			System.out.println("Total Memory: " + totalMemory + " KB");
			System.out.println("---------------------------------------------");

			System.out.println();

			System.out.println("Processing requests...");

			System.out.println();

			memory = new ArrayList<>();

			memory.add(new MemoryBlock(0, totalMemory, null));

			String line;
			while ((line = read.readLine()) != null) {
				line = line.trim();
				if (line.isEmpty())
					continue;

				String[] parts = line.split("\\s+");
				String command = parts[0];

				if (command.equalsIgnoreCase("RELEASE")) {
					String processName = parts[1];
					deallocate(processName);
				}

				else if (command.equalsIgnoreCase("REQUEST")) {
					String processName = parts[1];
					int sizeKB = Integer.parseInt(parts[2]);
					allocate(processName, sizeKB);
				}

			}

		} catch (IOException e) {
			System.out.println("I can't read file: " + e.getMessage());
		}
	}

	private static void allocate(String processName, int size) {

		for (int i = 0; i < memory.size(); i++) {
			MemoryBlock block = memory.get(i);
			
			if (block.processName == null && block.size >= size) {
				int remaining = block.size - size;

				block.size = size;
				block.processName = processName;

				if (remaining > 0) {
					MemoryBlock freeBlock = new MemoryBlock(block.start + size, remaining, null);
					memory.add(i + 1, freeBlock);
				}

				successfulAllocations++;
				
				System.out.printf("REQUEST %s %d KB → SUCCESS%n", processName, size);
				return;
			}
		}

		failedAllocations++;
		
		System.out.printf("REQUEST %s %d KB → FAILED - Insufficient Memory. %n", processName, size);
	}

	private static void deallocate(String processName) {
		for (int i = 0; i < memory.size(); i++) {
			MemoryBlock block = memory.get(i);

			if (block.processName != null && block.processName.equals(processName)) {
				block.processName = null;
				System.out.printf("RELEASE %s → SUCCESS%n", processName);

				return;
			}
		}

		System.out.printf("RELEASE %s → FAILED Insufficient memory. %n", processName);
	}

	public static void displayStatistics() {
		System.out.println("\n========================================");
		System.out.println("Final Memory State");
		System.out.println("========================================");

		int blockNum = 1;
		for (MemoryBlock block : memory) {
			String status = block.isFree() ? "FREE" : block.processName;
			String allocated = block.isFree() ? "" : " - ALLOCATED";
			System.out.printf("Block %d: [%d-%d]%s%s (%d KB)%s\n", blockNum++, block.start, block.getEnd(),
					" ".repeat(Math.max(1, 10 - String.valueOf(block.getEnd()).length())), status, block.size,
					allocated);
		}

		System.out.println("\n========================================");
		System.out.println("Memory Statistics");
		System.out.println("========================================");

		int allocatedMem = 0;
		int freeMem = 0;
		int numProcesses = 0;
		int numFreeBlocks = 0;
		int largestFree = 0;

		for (MemoryBlock block : memory) {
			if (block.isFree()) {
				freeMem += block.size;
				numFreeBlocks++;
				largestFree = Math.max(largestFree, block.size);
			} else {
				allocatedMem += block.size;
				numProcesses++;
			}
		}

		double allocatedPercent = (allocatedMem * 100.0) / totalMemory;
		double freePercent = (freeMem * 100.0) / totalMemory;
		double fragmentation = freeMem > 0 ? ((freeMem - largestFree) * 100.0) / freeMem : 0;

		System.out.printf("Total Memory:           %d KB\n", totalMemory);
		System.out.printf("Allocated Memory:       %d KB (%.2f%%)\n", allocatedMem, allocatedPercent);
		System.out.printf("Free Memory:            %d KB (%.2f%%)\n", freeMem, freePercent);
		System.out.printf("Number of Processes:    %d\n", numProcesses);
		System.out.printf("Number of Free Blocks:  %d\n", numFreeBlocks);
		System.out.printf("Largest Free Block:     %d KB\n", largestFree);
		System.out.printf("External Fragmentation: %.2f%%\n", fragmentation);

		System.out.println("\nSuccessful Allocations: " + successfulAllocations);
		System.out.println("Failed Allocations:     " + failedAllocations);
		System.out.println("========================================");
	}

	/**
	 * Main method (FULLY PROVIDED)
	 */
	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("Usage: java MemoryAllocationLab <input_file>");
			System.out.println("Example: java MemoryAllocationLab memory_requests.txt");
			return;
		}

		System.out.println("========================================");
		System.out.println("Memory Allocation Simulator (First-Fit)");
		System.out.println("========================================\n");
		System.out.println("Reading from: " + args[0]);

		processRequests(args[0]);
		displayStatistics();
	}
}
