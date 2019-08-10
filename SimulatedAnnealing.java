package localsearch.multibinpacking2d;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import localsearch.model.IConstraint;
import localsearch.model.IFunction;
import localsearch.model.VarIntLS;

public class SimulatedAnnealing{
    public void search(IConstraint S, int maxIter){        
        VarIntLS[] x = S.getVariables();
        Random r = new Random();
        
        int best = S.violations();
        int[] x_best = new int[x.length];
        for (int i = 0; i < x.length; i++)
            x_best[i] = x[i].getValue();
        
        //initialize temperature
        double temperature = initTemperature();
        
        int it = 0;
        while(it < maxIter && S.violations() > 0){
            int maxTrials = 200;
            int sel_i = 0;
            int sel_v = x[sel_i].getValue();
            for(int k = 0; k < maxTrials; k++) {
                int delta = S.getAssignDelta(x[sel_i], sel_v);
                if(delta < 0) {
                    x[sel_i].setValuePropagate(sel_v);
                }
                //choose the neighborhood
                int i = r.nextInt(x.length);
                int v = x[i].getMinValue() + r.nextInt(x[i].getMaxValue() - x[i].getMinValue() + 1);
                int _delta = S.getAssignDelta(x[i], v);
                if(_delta <= 0 || r.nextDouble() < Math.exp(-_delta/temperature)){
                    sel_i = i; 
                    sel_v = v;   
                }
            }
            
            //if(f(s) < f(s*))
            if(S.violations() < best){
                best = S.violations();
                for(int i = 0; i < x.length; i++)
                    x_best[i] = x[i].getValue();
            }
//            System.out.println("Step " + it + ", S = " + S.violations() + ", best = " + best);
            
            //update temperature
            temperature = updateTemperature(temperature, 0.003);
            it++;
        }
        for(int i = 0; i < x.length; i++)
            x[i].setValuePropagate(x_best[i]);
    }
    
    public void searchMaintainConstraintsMinimize(IFunction f, IConstraint S, int maxIter){        
        VarIntLS[] x = S.getVariables();
        Random r = new Random();
        
        int best = f.getValue();
        int[] x_best = new int[x.length];
        for (int i = 0; i < x.length; i++)
            x_best[i] = x[i].getValue();
        
//        System.out.println(name() + "::searchMaintainConstraintsMinimize, init S = "
//				+ S.violations());
        //initialize temperature
        double temperature = initTemperature();
        
        int it = 0;
        while(it < maxIter){
            int maxTrials = 200;
            int sel_i = 0;
            int sel_v = x[sel_i].getValue();
            for(int k = 0; k < maxTrials; k++) {
                int deltaS = S.getAssignDelta(x[sel_i], sel_v);
                int deltaF = f.getAssignDelta(x[sel_i], sel_v);
                if(deltaS <= 0 && deltaF < 0) {
                    x[sel_i].setValuePropagate(sel_v);
                }
                //choose the neighborhood
                int i = r.nextInt(x.length);
                int v = x[i].getMinValue() + r.nextInt(x[i].getMaxValue() - x[i].getMinValue() + 1);
                int _deltaS = S.getAssignDelta(x[i], v);
                int _deltaF = f.getAssignDelta(x[i], v);
                if(_deltaF <= 0 || r.nextDouble() < Math.exp(_deltaF/temperature)){
                    sel_i = i; 
                    sel_v = v;   
                }
            }        
            //if(f(s) < f(s*))
            if(f.getValue() < best){
                best = f.getValue();
                for(int i = 0; i < x.length; i++)
                    x_best[i] = x[i].getValue();
            }
//            System.out.println(name()
//                            + "::searchMaintainConstraintsMinimize, Step " + it
//                            + ", S = " + S.violations() + ", f = " + f.getValue()
//                            + ", best = " + best);
            
            //update temperature
            temperature = updateTemperature(temperature, 0.003);
            it++;
        }
        for(int i = 0; i < x.length; i++)
            x[i].setValuePropagate(x_best[i]);
    }
    
    public void searchMaintainConstraintsFunctionMinimize(IFunction f1,
			IFunction[] f2, IConstraint S, int maxIter){
        HashSet<VarIntLS> _S = new HashSet<>();
        Random r = new Random();
        VarIntLS[] a = S.getVariables();
        for(int i = 0; i < a.length; i++)
            _S.add(a[i]);
        
        VarIntLS[] b = f1.getVariables();
        if(b != null) 
            for(int i = 0; i < b.length; i++)
                _S.add(b[i]);    
        
        for(int i = 0; i < f2.length; i++){
            VarIntLS[] c = f2[i].getVariables();
            if(c != null) 
                for(int j = 0; j < c.length; j++)
                    _S.add(c[j]);
        }
        
        VarIntLS[] x = new VarIntLS[_S.size()];
        int k = 0;
        for(VarIntLS e: _S){
            x[k] = e;
            k++;
        }
        
        int best = f1.getValue();
        int[] x_best = new int[x.length];
        for(int i = 0; i < x.length; i++)
            x_best[i] = x[i].getValue();
//        System.out.println(name()
//                        + "::searchMaintainConstraintsFunctionMinimize, init S = "
//                        + S.violations());
        
        //initialize temperature
        double temperature = initTemperature();
        int it = 0;
        while(it < maxIter){
            int maxTrials = 200;
            int sel_i = 0;
            int sel_v = x[sel_i].getValue();
            for(int j = 0; j < maxTrials; j++) {
                int deltaS = S.getAssignDelta(x[sel_i], sel_v);
                int deltaF = f1.getAssignDelta(x[sel_i], sel_v);
                int[] deltaF2 = new int[f2.length];
                for(int _j = 0; _j < f2.length; _j++)
                    deltaF2[_j] = f2[_j].getAssignDelta(x[sel_i], sel_v);
                Arrays.sort(deltaF2);
                
                if(deltaS <= 0 && deltaF2[f2.length - 1] <= 0 && deltaF < 0) {
                    x[sel_i].setValuePropagate(sel_v);
                }
                //choose the neighborhood
                int i = r.nextInt(x.length);
                int v = x[i].getMinValue() + r.nextInt(x[i].getMaxValue() - x[i].getMinValue() + 1);
                int _deltaF = f1.getAssignDelta(x[i], v);
                if(_deltaF <= 0 || r.nextDouble() < Math.exp(_deltaF/temperature)){
                    sel_i = i; 
                    sel_v = v;   
                }
            }        
            //if(f(s) < f(s*))
            if(f1.getValue() < best){
                best = f1.getValue();
                for(int i = 0; i < x.length; i++)
                    x_best[i] = x[i].getValue();
            }
//            System.out.println(name()
//                            + "::searchMaintainConstraintsFunctionMinimize, Step " + it
//                            + ", S = " + S.violations() + ", f2[0] = " + f2[0].getValue()
//                            + ", f1 = " + f1.getValue()
//                            + ", best = " + best);
            
            //update temperature
            temperature = updateTemperature(temperature, 0.003);
            it++;
        }
        for(int i = 0; i < x.length; i++)
            x[i].setValuePropagate(x_best[i]);
    }
    
    public double initTemperature(){
        return 10000;
    }
    
    public double updateTemperature(double t, double coolingRate){
        return t*(1 - coolingRate);
    }
    
    public String name(){
        return "Simulated Annealing";
    }
}
