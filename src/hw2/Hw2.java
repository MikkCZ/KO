package hw2;

import java.io.*;
import java.util.Arrays;

import static hw2.FordFulkerson.BAC_ARC_INDEX;
import static hw2.FordFulkerson.FOR_ARC_INDEX;

public class Hw2 {

    private static int C, P;

    public static void main(String[] args) throws IOException {

        int[] l, u, v;
        int[][] customerProducts;

        // read input
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(args[0]))))) {
            String[] counts = br.readLine().split("\\s+");

            C = Integer.parseInt(counts[0]);
            l = new int[C];
            u = new int[C];
            customerProducts = new int[C][];
            for (int i = 0; i<C; i++) {
                String[] customer = br.readLine().split("\\s+");
                l[i] = Integer.parseInt(customer[0]);
                u[i] = Integer.parseInt(customer[1]);
                customerProducts[i] = new int[customer.length - 2];
                for (int j = 0; j<customer.length-2; j++) {
                    customerProducts[i][j] = Integer.parseInt(customer[j+2]) - 1; // products are indexed starting 1
                }
            }

            P = Integer.parseInt(counts[1]);
            v = new int[P];
            String[] products = br.readLine().split("\\s+");
            for (int i = 0; i<P; i++) {
                v[i] = Integer.parseInt(products[i]);
            }
        }

        // prepare original graph
        final int oGraphNodes = C+P+2;
        final int[][][] oGraphE = new int[oGraphNodes][2][];
        final int[][] oGraphU = new int[oGraphNodes][oGraphNodes];
        final int[][] oGraphL = new int[oGraphNodes][oGraphNodes];

        final int oGraphS = sourceNode();
        oGraphE[oGraphS][FOR_ARC_INDEX] = new int[C];
        for (int i=0; i<C; i++) { // forward arcs from source to customers
            int cNode = mapCustomerToNode(i);
            oGraphE[oGraphS][FOR_ARC_INDEX][i] = cNode;
            oGraphU[oGraphS][cNode] = u[i];
            oGraphL[oGraphS][cNode] = l[i];
            oGraphE[cNode][BAC_ARC_INDEX] = new int[] {oGraphS};
        }

        final int oGraphT = targetNode();
        oGraphE[oGraphT][BAC_ARC_INDEX] = new int[P];
        for (int i=0; i<P; i++) { // backward arcs from products to target
            int pNode = mapProductToNode(i);
            oGraphE[oGraphT][BAC_ARC_INDEX][i] = pNode;
            oGraphU[pNode][oGraphT] = Integer.MAX_VALUE;
            oGraphL[pNode][oGraphT] = v[i];
            oGraphE[pNode][FOR_ARC_INDEX] = new int[] {oGraphT};
        }

        int[] productsFrequency = new int[P];
        for (int i=0; i<C; i++) { // forward arcs from customers to products
            int cNode = mapCustomerToNode(i);
            int[] cProducts = customerProducts[i];
            oGraphE[cNode][FOR_ARC_INDEX] = new int[cProducts.length];
            for (int j=0; j<cProducts.length; j++) {
                productsFrequency[cProducts[j]]++;
                int pNode = mapProductToNode(cProducts[j]);
                oGraphE[cNode][FOR_ARC_INDEX][j] = pNode;
                oGraphU[cNode][pNode] = 1;
                oGraphL[cNode][pNode] = 0;
            }
        }

        for (int i=0; i<P; i++) { // backward arcs from products to customers
            int pNode = mapProductToNode(i);
            int customerCount = productsFrequency[i];
            oGraphE[pNode][BAC_ARC_INDEX] = new int[customerCount];
            int index = 0;
            for (int j=0; j<C && index<customerCount; j++) {
                int cNode = mapCustomerToNode(j);
                if (oGraphU[cNode][pNode] == 1) {
                    oGraphE[pNode][BAC_ARC_INDEX][index] = cNode;
                    index++;
                }
            }
        }

        // prepare changed graph
        // [OK] pridej s' a t'
        // [OK] pro kazdou hranu (u,v) s nenulovym lowerboundem
        // [OK]     - pridej hrany (s',v) a (u,t') s kapacitou podle lowerboundu
        // [OK]     - hrane (u,v) sniz upperbound o lowerbound a lowerbound vynuluj
        // [OK] sloucime duplicitni hrany z s' a do t' a jejich kapacity secteme
        // [] pridej hranu s nekonecnou kapacitou z puvodniho t do s
        final int tGraphNodes = oGraphNodes + 2;
        final int tGraphS = oGraphS+2;
        final int tGraphT = oGraphT+2;

        final int[][][] tGraphE = new int[tGraphNodes][2][];
        final int[][] tGraphU = new int[tGraphNodes][tGraphNodes];

        final boolean[] tGraphSArcTo = new boolean[oGraphNodes];
        int tGraphSeindex = 0;
        final boolean[] tGraphTArcFrom = new boolean[oGraphNodes];
        int tGraphTeindex = 0;

        tGraphE[tGraphS][FOR_ARC_INDEX] = new int[oGraphNodes];
        tGraphE[tGraphT][BAC_ARC_INDEX] = new int[oGraphNodes];
        for (int i=0; i<oGraphNodes; i++) {
            for (int j=0; j<oGraphNodes; j++) {
                final int lij = oGraphL[i][j];
                if (lij == 0) {
                    tGraphU[i][j] = oGraphU[i][j];
                } else { // lowerbound is not zero
                    tGraphU[tGraphS][j] += lij; // add new (s',j) upperbound (increment reduces duplicated arcs)
                    if (!tGraphSArcTo[j]) {
                        tGraphE[tGraphS][FOR_ARC_INDEX][tGraphSeindex++] = j;
                        tGraphSArcTo[j] = true;
                    }
                    tGraphU[i][tGraphT] += lij; // add new (i,t') upperbound (increment reduces duplicated arcs)
                    if (!tGraphTArcFrom[i]) {
                        tGraphE[tGraphT][BAC_ARC_INDEX][tGraphTeindex++] = i;
                        tGraphTArcFrom[i] = true;
                    }
                    tGraphU[i][j] = oGraphU[i][j] - lij; // set new (i,j) upperbound
                }
            }
        }
        tGraphE[tGraphS][FOR_ARC_INDEX] = Arrays.copyOf(tGraphE[tGraphS][FOR_ARC_INDEX], tGraphSeindex);
        tGraphE[tGraphT][BAC_ARC_INDEX] = Arrays.copyOf(tGraphE[tGraphT][BAC_ARC_INDEX], tGraphTeindex);

        for (int i=0; i<oGraphNodes; i++) {
            if (!tGraphSArcTo[i]) {
                tGraphE[i][BAC_ARC_INDEX] = oGraphE[i][BAC_ARC_INDEX];
            } else { // add arc (s',i)
                tGraphE[i][BAC_ARC_INDEX] = Arrays.copyOf(oGraphE[i][BAC_ARC_INDEX], oGraphE[i][BAC_ARC_INDEX].length+1);
                tGraphE[i][BAC_ARC_INDEX][tGraphE[i][BAC_ARC_INDEX].length-1] = tGraphS;
            }
            if (!tGraphTArcFrom[i]) {
                tGraphE[i][FOR_ARC_INDEX] = oGraphE[i][FOR_ARC_INDEX];
            } else { // add arc (i,t')
                tGraphE[i][FOR_ARC_INDEX] = Arrays.copyOf(oGraphE[i][FOR_ARC_INDEX], oGraphE[i][FOR_ARC_INDEX].length+1);
                tGraphE[i][FOR_ARC_INDEX][tGraphE[i][FOR_ARC_INDEX].length-1] = tGraphT;
            }
        }

        tGraphE[oGraphT][FOR_ARC_INDEX] = new int[] {oGraphS};
        tGraphE[oGraphS][BAC_ARC_INDEX] = new int[] {oGraphT};
        tGraphU[oGraphT][oGraphS] = Integer.MAX_VALUE;

        final int[][] tGraphL = new int[tGraphNodes][tGraphNodes];
        final int[][] tGraphF = tGraphL;

        // run FordFulkerson on the changed graph
        FordFulkerson.fordFulkerson(tGraphE, tGraphU, tGraphL, tGraphS, tGraphT, tGraphF);

        // find the initial flow for the original graph
        boolean feasible = Arrays.equals(tGraphF[tGraphS], tGraphU[tGraphS]);
        final int[][] oGraphF = new int[oGraphNodes][oGraphNodes];
        for (int i=0; i<oGraphNodes; i++) {
            for (int j=0; j<oGraphNodes; j++) {
                oGraphF[i][j] = tGraphF[i][j] + oGraphL[i][j];
            }
        }
        //

        // run FordFulkerson on the original graph and initial flow
        if (feasible) {
            FordFulkerson.fordFulkerson(oGraphE, oGraphU, oGraphL, oGraphS, oGraphT, oGraphF);
        }

        // output
        try(PrintStream output = new PrintStream(new File(args[1]))) {
            if (feasible) {
                for (int i=0; i<C; i++) {
                    StringBuilder sb = new StringBuilder();
                    int cNode = mapCustomerToNode(i);
                    int[] cProducts = customerProducts[i];
                    for (int p : cProducts) {
                        int pNode = mapProductToNode(p);
                        if (oGraphF[cNode][pNode] == 1) {
                            sb.append(p+1).append(" "); // products are indexed starting 1
                        }
                    }
                    String customerReviews = sb.toString();
                    output.println(customerReviews.substring(0, customerReviews.length()-1));
                }
            } else {
                output.println(-1);
            }
        }
    }

    private static int mapCustomerToNode(int customer) {
        return customer;
    }

    private static int mapProductToNode(int product) {
        return product + C;
    }

    private static int sourceNode() {
        return C+P;
    }

    private static int targetNode() {
        return C+P+1;
    }
}
