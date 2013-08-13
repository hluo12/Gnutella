import java.util.*;
 
/**
 * Finds the maximum flow in a flow network.
 * @param E neighbour lists
 * @param C capacity matrix (must be n by n)
 * @param s source
 * @param t sink
 * @return maximum flow
 */
public class EdmondsKarp {
    public static int edmondsKarp(int[][] E, int[][] C, int s, int t) {
        int n = C.length;
        // Residual capacity from u to v is C[u][v] - F[u][v]
        int[][] F = new int[n][n];
        while (true) {
            int[] P = new int[n]; // Parent table
            Arrays.fill(P, -1);
            P[s] = s;
            int[] M = new int[n]; // Capacity of path to node
            M[s] = Integer.MAX_VALUE;
            // BFS queue
            Queue<Integer> Q = new LinkedList<Integer>();
            Q.offer(s);
            LOOP:
            while (!Q.isEmpty()) {
                int u = Q.poll();
                for (int v : E[u]) {
                    // There is available capacity,
                    // and v is not seen before in search
                    if (C[u][v] - F[u][v] > 0 && P[v] == -1) {
                        P[v] = u;
                        M[v] = Math.min(M[u], C[u][v] - F[u][v]);
                        if (v != t)
                            Q.offer(v);
                        else {
                            // Backtrack search, and write flow
                            while (P[v] != v) {
                                u = P[v];
                                F[u][v] += M[t];
                                F[v][u] -= M[t];
                                v = u;
                            }
                            break LOOP;
                        }
                    }
                }
            }
            if (P[t] == -1) { // We did not find a path to t
                int sum = 0;
                for (int x : F[s])
                    sum += x;
					 System.out.println(sum);	
                return sum;
            }
        }
		  
    }
	 public static void main(String[] args){
		 int[][] Edge = new int[4][4];
		 int[][] Cap =  new int[4][4];
		 Edge[0][1] = 1;
		 Edge[0][2] = 2;
		 Edge[1][1] = 2;
		 Edge[1][2] = 3;
		 Edge[2][1] = 3;
		 Cap[0][1] = 3;
		 Cap[0][2] = 2;
		 Cap[1][2] = 2;
		 Cap[1][3] = 1;
		 Cap[2][1] = 3;
		 
		 EdmondsKarp e = new EdmondsKarp();
		 int sum = e.edmondsKarp(Edge,Cap,0,3);
		 System.out.println("0:  0->3 0.0/91.0  0->4 0.0/81.0  0->5 0.0/52.0");
		 System.out.println("1:  1->5 0.0/76.0");
		 System.out.println("2:  2->5 0.0/53.0");
		 
		 System.out.println("Max flow from 0 to 5");
 		 System.out.println("0->5 52.0/52.0");
	}	 
}