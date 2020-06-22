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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.ParallelPortfolio;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainRandom;
import org.chocosolver.solver.search.strategy.selectors.variables.FirstFail;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;

/**
 *
 * @author pro
 */
public class QAP1 {

    
    private final static int TIME_LIMIT = 60;
    
    int maxWeightedSumValue;
    
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
    
    public HashMap<Integer, Integer> CountFrequencies(int[] list) 
    { 
        // hashmap to store the frequency of element 
        HashMap<Integer, Integer> hm = new HashMap<Integer, Integer>(); 
  
        for (int i : list) { 
            Integer j = hm.get(i); 
            hm.put(i, (j == null) ? 1 : j + 1); 
        } 
  
       return hm;
    } 
    
    
    
    public static void main(String[] args) throws IOException{
        // had12 sol = 1629
        String instanceFile = "/Users/pro/Downloads/tai12b.dat";
        
        // 248468 time: 4 minutes 55 seconds)
        
        System.out.println("main started");
        QAP1 model = new QAP1();
        model.readInstance(instanceFile);
        model.Solve();
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
    
    
    public int[] RemoveDuplicates(int[] arr){
        Set<Integer> set = new HashSet<Integer>();

        for (int num : arr) {
            set.add(num);
        }

        return set.stream().mapToInt(Number::intValue).toArray();
    }

    int getWeightedSum(int[] weights, int[] distances){
        int maxWeightedSumValue = 0;
        for (int i =0; i < size*size; i++) {
            maxWeightedSumValue += distances[i]*weights[i];
        }
        return maxWeightedSumValue;
    }
    
    int getWeightedSum(Variable[] weights, int[] distances){
        int maxWeightedSumValue = 0;
        for (int i =0; i < size*size; i++) {
            maxWeightedSumValue += distances[i]*weights[i].asIntVar().getValue();
        }
        return maxWeightedSumValue;
    }
    
    IntVar[] asIntVar(Variable[] vars){
        IntVar[] intvars = new IntVar[size*size];
        for (int i=0; i<vars.length;i++){
            if( vars[i].getName().startsWith("X_")) {
                intvars[i] = vars[i].asIntVar();
            }
        }
        return intvars;
    }
    
    Model makeModel(int n){
        Model model = new Model("QAP_"+n);

        IntVar[] variables = new IntVar[size*size];
        int[] w_array = MatrixToArray(weights);
        int[] d_array = MatrixToArray(distances);
        for (int i = 0; i < size*size; i++) {
            String varName = "X" + i;
            variables[i] = model.intVar(varName, RemoveDuplicates(w_array));
                        
            if ( w_array[i] > max_w ) {
                max_w = w_array[i];
            }
            
            if ( d_array[i] > max_d ) {
                max_d = d_array[i];
            }
        }

        
        maxWeightedSumValue = getWeightedSum(w_array, d_array);
        
       
        //System.out.println(maxWeightedSumValue);
        IntVar WDSum = model.intVar("WDSum",1,maxWeightedSumValue); //248543
        model.setObjective(Model.MINIMIZE, WDSum);
        
        model.nValues(variables, model.intVar(RemoveDuplicates(w_array).length)).post();


        
        HashMap<Integer, Integer> occurrences = CountFrequencies(w_array);

        for (Map.Entry<Integer, Integer> value : occurrences.entrySet()) { 
            int val = value.getKey();
            int occ = value.getValue();
            model.count(val,variables, model.intVar(occ)).post();

        } 
        
        model.scalar(variables, d_array, "=", WDSum).post();
        Solver solver = model.getSolver();
        solver.limitTime(TIME_LIMIT+"s");
        
        return model;
        
    }
    
    void Solve(){
        
        long time = System.currentTimeMillis();
        
        ParallelPortfolio portfolio = new ParallelPortfolio();
        portfolio.addModel(makeModel(0));
        portfolio.addModel(makeModel(1));
        portfolio.addModel(makeModel(2));
       
        
        while(portfolio.solve()) {

            System.out.println("Solution found (objective = "+portfolio.getBestModel().getSolver().getBestSolutionValue()+")");

        }
        
        int runtime = (int)((System.currentTimeMillis()-time)/1000);
        if(runtime < TIME_LIMIT) {
            System.out.println("Optimality proved in " + runtime + "s");
        }else{
            System.out.println(TIME_LIMIT+"s timeout reached");
        }
 
    }
    
}
