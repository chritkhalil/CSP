/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CP;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.stream.Collectors;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;

/**
 *
 * @author pro
 */
public class QAP1 {

    
    int size;
    int[][] distances;
    
    
    int[][] weights;

    int min_wd = Integer.MAX_VALUE;
    int max_d, max_w = 0;
    
    public int[] MatrixToArray(int[][] matrix){
        int[] array = new int[size*size];
        for (int i = 0; i< size; i++){
            for (int j = 0; j< matrix[i].length; j++){
                array[i*matrix[i].length+j] = matrix[i][j];
            }
        }
      return array;  
    } 
    
    public static void main(String[] args) throws IOException{
        String instanceFile = "/Users/pro/Downloads/had12.dat";
        System.out.println("main started");
        QAP1 model = new QAP1();
        model.readInstance(instanceFile);
        model.S();
    }
    
    
     private void writeSolution(String fileName) throws IOException {
        try (PrintWriter output = new PrintWriter(fileName)) {
          //  output.println(size + " " + obj.getValue());
            //LSCollection pCollection = p.getCollectionValue();
            //for (int i = 0; i < n; i++)
                //output.print(pCollection.get(i) + " ");
            output.println();
        }
    }

    
    
    
    // Reads instance data
    private void readInstance(String fileName) throws IOException {
        try (Scanner input = new Scanner(new File(fileName))) {
            size = input.nextInt();
            System.out.println("Instance read: n="+size);

            distances = new int[size][size];
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    distances[i][j] = input.nextInt();
                }
            }

            weights = new int[size][size];
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    weights[i][j] = input.nextInt();
                }
            }
            
        }
    }
    
    
    void S() {
        Model model = new Model("QAP");

        IntVar[] variables = new IntVar[size*size];
        int[] d_array = MatrixToArray(distances);
        System.out.println(Arrays.toString(d_array));
        for (int i = 0; i < size*size; i++) {
            String varName = "X" + i;
            variables[i] = model.intVar(varName, d_array);
        }

        int[] d_1 = new int[size];
        int[] d_2 = new int[size * size];
        int[] w_1 = new int[size * size];
        for (int i = 0; i < size; i++) {
            d_1[i] = distances[i][i];
        }

        int c = 0;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (j != j) {
                    d_2[c] = distances[i][j];
                    c++;
                }
                w_1[c] = weights[i][j];
//                if(w[i][j]>max_w)
//                    max_w = w[i][j];
//                if(d[i][j]>max_d)
//                    max_d = d[i][j];
            }
        }
        
        IntVar WDSum = model.intVar("WDSum",0,max_d*max_w);
//
//        int k = 0;
//        for (int i = 0; i < size; i++) {
//            for (int j = 0; j < size; j++) {
//                IntVar d_ij;
//                if (i == j) {
//                    d_ij = model.intVar(d_1);
//                } else {
//                    d_ij = model.intVar(d_2);
//                }
//                d_[k] = d_ij;
//                k++;
//            }
//        }
       
        //model.allDifferent(d_).post();

        int[] distinct_d_array = Arrays.stream(d_array).distinct().toArray();
        IntVar[] indexes = new IntVar[distinct_d_array.length];

        System.out.println(Arrays.toString(distinct_d_array));
        model.nValues(variables, model.intVar(distinct_d_array.length)).post();

        for (int i = 0; i< size*size; i++){
            indexes[i%distinct_d_array.length] = model.intVar(0, size*size);
            model.element(variables[i], d_array, indexes[i%distinct_d_array.length]).post();
            //model.element
            //model.intvar
            //model.element
        }
        
        model.allDifferent(indexes).post();


        model.scalar(variables, w_1, "=", WDSum).post();
        //System.out.println(w_1);
        Solver solver = model.getSolver();
        
        System.out.println(solver.findSolution());


        model.setObjective(Model.MINIMIZE, WDSum);
        while(solver.solve()) {
            if (WDSum.getValue()<min_wd)
                min_wd = WDSum.getValue();
                //System.out.println(solver.findSolution());
        }
        System.out.println("Minimum Weighted Distance: "+min_wd);
 
        
    }
}
