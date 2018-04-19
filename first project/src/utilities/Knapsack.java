package utilities;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;

import data.ChunkInfo;

/* From internet and adapted by the group */
public class Knapsack {
	
	// Minimum (Non exact cases)
	private long minimum = -Integer.MAX_VALUE;
	// Input
    private int n;
    // If the input array is sorted ascending, the shortest solution is
    // likely to be found somewhere at the end.
    // If the input array is sorted descending, the shortest solution is
    // likely to be found somewhere in the beginning.
    private ArrayList<ChunkInfo> input;

    // Shortest possibility
    private static Deque<ChunkInfo> shortest;
    // Number of possibilities
    private static int numberOfPossibilities;
    
    public Knapsack (int n, ArrayList<ChunkInfo> input) {
    	this.n = n;
    	this.input = input;
    }
    
    
    public Deque<ChunkInfo> solve()
    {
        calculate(0, n, new LinkedList<ChunkInfo>());
        return shortest;
    }

    private void calculate(int i, int left, Deque<ChunkInfo> partialSolution)
    {
        // If there's nothing left, we reached our target
        if (left == 0)
        {
            System.out.println(partialSolution);
            if (shortest == null || partialSolution.size() < shortest.size())
                shortest = new LinkedList<ChunkInfo>(partialSolution);
            numberOfPossibilities++;
            return;
        }
        // If we overshot our target, we'll check if there is an exact solution yet
        // Note that this could also be checked before making the
        // recursive call, but IMHO this gives a cleaner recursion step.
        if (left < 0 && numberOfPossibilities == 0) {
        	if (minimum < left) {
        		minimum = left;
                shortest = new LinkedList<ChunkInfo>(partialSolution);
        	} else if (minimum == left) {
        		minimum = left;
        		if (shortest == null || partialSolution.size() < shortest.size())
        			shortest = new LinkedList<ChunkInfo>(partialSolution);
        	}
            return;
        }
        // If there are no values remaining, we didn't reach our target
        if (i == input.size())
            return;

        // Uncomment the next two lines if you don't want to keep generating
        // possibilities when you know it can never be a better solution then
        // the one you have now.
//      if (shortest != null && partialSolution.size() >= shortest.size())
//          return;

        // Pick value i. Note that we are allowed to pick it again,
        // so the argument to calculate(...) is i, not i+1.
        partialSolution.addLast(input.get(i));
        calculate(i+1, left-input.get(i).getChunkSize(), partialSolution);
        // Don't pick value i. Note that we are not allowed to pick it after
        // all, so the argument to calculate(...) is i+1, not i.
        partialSolution.removeLast();
        calculate(i+1, left, partialSolution);
    }
}