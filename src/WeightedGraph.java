import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This class describes a weighted directed graph represented by an adjacency
 * matrix. Most of the work to build the graph is done in the constructor; it
 * uses recursive methods to get the early and late stage times.
 * The only other methods are getters for the instance variables that describe
 * the early and late times within the graph. The class also contains a trivial
 * stack implementation to assist in determining the topological order. It will
 * throw an exception if the project that the matrix describes is infeasible.
 * This exception is automatically caught when thrown. Upon catching this
 * exception, an error message is printed out. If the graph is feasible,
 * it will print out a message saying so. The constructor also prints out its
 * current status to the console as it calculates the times of the project.
 */
class WeightedGraph {
    private final int[] est;
    private final int[] lst;
    private final int[] eat;
    private final int[] lat;
    private final int[][] adjMat;
    private final List<Integer> topOrder;
    private boolean feasible;

    // Constructor, builds the graph
    WeightedGraph(int[][] matrix) {
        adjMat = matrix;
        int len = adjMat[0].length;
        int[] predCount = new int[len];
        est = new int[len];
        lst = new int[len];

        CStack<Integer> tempStack = new CStack<>();
        topOrder = new ArrayList<>();

        // Get predecessor count
        for (int i=0;i<len;i++)
            for (int j=0;j<len;j++) if (adjMat[j][i] > 0) predCount[i]++;
        lat = new int[IntStream.of(predCount).sum()];
        eat = new int[IntStream.of(predCount).sum()];

        // Determine topological ordering
        for (int l=0;l<len;l++) {
            for (int i=1;i<=len;i++) if (predCount[i-1] == 0 & !topOrder.contains(i)) tempStack.push(i);

            while (tempStack.size() > 0) {
                int currentStage = tempStack.pop();
                int[] edges = adjMat[currentStage-1];
                topOrder.add(currentStage);

                for (int i=0;i<edges.length;i++) if (edges[i] > 0 & predCount[i] > 0) predCount[i]--;
            }
        }

        try {
            if (topOrder.size() < len) throw new IllegalArgumentException("Project is infeasible.\n");
            else {
                System.out.printf("Project is feasible.\n");
                feasible = true;
            }
        } catch (Exception e) {
            System.err.printf("%s\n", e.getMessage());
            return;
        }

        // Determine early/late stage times
        System.out.printf("Determining early stage times...\n");
        for (int i=0;i<len;i++) est[i] = EST(i+1);
        System.out.printf("Determining late stage times...\n");
        for (int j=len-1;j>=0;j--) lst[j] = LST(j+1);

        // Determine early/late activity times
        List<Integer> outgoing = new ArrayList<>();
        List<Integer> incoming = new ArrayList<>();
        List<Integer> costs = new ArrayList<>();

        System.out.printf("Building list of costs...\n");
        for (int i=0;i<len;i++)
            for (int j=0;j<len;j++)
                if (adjMat[i][j] > 0) {
                    outgoing.add(i+1);
                    incoming.add(j+1);
                    costs.add(adjMat[i][j]);
                }

        System.out.printf("Determining early/late activity times...\n");
        for (int k=0;k<outgoing.size();k++) {
            eat[k] = est[outgoing.get(k)-1];
            lat[k] = LST(incoming.get(k)) - costs.get(k);
        }
    }

    // This uses recursion to get the early stage time for an arbitrary stage
    private int EST(int stage) {
        int len = adjMat[0].length;
        List<Integer> cost = new ArrayList<>();
        List<Integer> incoming = new ArrayList<>();
        for (int i=1;i<=len;i++) if (adjMat[i-1][stage-1] > 0) incoming.add(i);

        if (incoming.size() == 0) return 0;
        cost.addAll(incoming.parallelStream().map(s -> adjMat[s-1][stage-1] + EST(s)).collect(Collectors.toList()));

        return cost.parallelStream().mapToInt(i -> i).max().getAsInt();
    }

    // This uses recursion to get the late stage time for an arbitrary stage
    private int LST(int stage) {
        int len = adjMat[0].length;
        List<Integer> cost = new ArrayList<>();
        List<Integer> outgoing = new ArrayList<>();
        for (int i=1;i<=len;i++) if (adjMat[stage-1][i-1] > 0) outgoing.add(i);

        if (outgoing.size() == 0) return EST(stage);
        cost.addAll(outgoing.parallelStream().map(s -> LST(s) - adjMat[stage-1][s-1]).collect(Collectors.toList()));

        return cost.parallelStream().mapToInt(i -> i).min().getAsInt();
    }

    // Getter for topOrder
    public String getTopOrder() {return topOrder.toString(); }

    // Getter for late stage time
    public int[] getLST() { return lst; }

    // Getter for late activity time
    public int[] getLAT() { return lat; }

    // Getter for early activity time
    public int[] getEAT() { return eat; }

    // Getter for early stage time
    public int[] getEST() { return est; }

    // Getter for feasibility
    public boolean isFeasible() { return feasible; }

    // Very simple stack implementation
    private class CStack<E> {
        private int size;
        private final LinkedList<E> stackList;

        // Constructor, initialises stackList and size
        CStack() {
            stackList = new LinkedList<>();
            size = 0;
        }

        // Add to top of stack
        public void push(E item) {
            stackList.addFirst(item);
            size++;
        }

        // Remove from top of stack
        public E pop() {
            size--;
            return stackList.removeFirst();
        }

        // Return size of stack
        public int size() {
            return size;
        }
    }
}
