package localsearch.multibinpacking2d;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

public class Test{
    int maxBin = 20;
    int minBin = 5;
    int nFiles = maxBin - minBin + 1;
    int maxW = 20, minW = 10;
    int maxH = 20, minH = 10;
    int maxw = 5, minw = 1;
    int maxh = 5, minh = 1;
    
    public void createData(String fn, int m){
        try{
            File outFile = new File(fn);
            PrintWriter out;
            out = new PrintWriter(outFile);
            Random r = new Random();
            int n = m + r.nextInt(3*m);
            for(int i = 0; i < m; i++){
                int W = minW + r.nextInt(maxW - minW + 1);
                int H = minH + r.nextInt(maxH - minH + 1);
                out.println(W + " " + H);
            }
            out.println(-1);
            for(int i = 0; i < n; i++){
                int w = minw + r.nextInt(maxw - minw + 1);
                int h = minh + r.nextInt(maxh - minh + 1);
                out.println(w + " " + h);
            }
            out.println(-1);
            out.close();
        }catch(IOException ex){
            ex.printStackTrace();
        }
    }
    
    public void createDatabase() {
        for(int id = 0; id < nFiles; id++) {
            String fn = "C:\\Users\\96chi\\Documents\\NetBeansProjects\\project\\src\\localsearch\\multibinpacking2d\\test\\" + id + ".txt";
            createData(fn, id + minBin);
        }
    }
    
    public void run(){
        try{  
            for(int algo = 0; algo < 1; algo++) {
                for(int id = 0; id < nFiles; id++){
                    File outFile;
                    switch(algo){
                        case 0:
                            outFile = new File("C:\\Users\\96chi\\Documents\\NetBeansProjects\\project\\src\\localsearch\\multibinpacking2d\\result\\TabuSearch" + id + ".txt");
                            break;
                        case 1:
                            outFile = new File("C:\\Users\\96chi\\Documents\\NetBeansProjects\\project\\src\\localsearch\\multibinpacking2d\\result\\SimulatedAnnealing" + id + ".txt");
                            break;
                        default:
                            outFile = new File("C:\\Users\\96chi\\Documents\\NetBeansProjects\\project\\src\\localsearch\\multibinpacking2d\\result\\IteraredLocalSearch" + id + ".txt");
                    }
                    PrintWriter out = new PrintWriter(outFile);
                    out.printf("%6s\t%6s\t%6s\t%8s\t%8s\t%6s\t%6s\t%8s\t%8s\n", "#", "min", "max", "avg", "std", "min2", "max2", "avg2", "std2");
                    
                    MultiBinPacking2D mbp = new MultiBinPacking2D(); 
                    mbp.readData("C:\\Users\\96chi\\Documents\\NetBeansProjects\\project\\src\\localsearch\\multibinpacking2d\\test\\" + id + ".txt");
                    int nTimes = 5;
                    int min1 = 10000, min2 = 10000;
                    int max1 = -10000, max2 = -10000;
                    int sum1 = 0, sum2 = 0;
                    int sum21 = 0, sum22 = 0; 
                    for(int i = 0; i < nTimes; i++){
                        mbp.solve(1000, algo);
                        int f1 = mbp.obj1.getValue();
                        int f2 = mbp.obj2.getValue();
                        min1 = Math.min(min1, f1);
                        min2 = Math.min(min2, f2);
                        max1 = Math.max(max1, f1);
                        max2 = Math.max(max2, f2);
                        sum1 += f1;
                        sum2 += f2;
                        sum21 += f1*f1;
                        sum22 += f2*f2;
                    }
                    double avg1 = sum1*1.0/nTimes;
                    double avg2 = sum2*1.0/nTimes;
                    double std1 = Math.sqrt(sum21*1.0/nTimes - (sum1*sum1)*1.0/(nTimes*nTimes));
                    double std2 = Math.sqrt(sum22*1.0/nTimes - (sum2*sum2)*1.0/(nTimes*nTimes));
                    out.printf("%6d\t%6d\t%6d\t%8.2f\t%8.2f\t%6d\t%6d\t%8.2f\t%8.2f\n", 1, min1, max1, avg1, std1, min2, max2, avg2, std2); 
                    out.close();
                }
            }
        }catch(IOException ex){
            ex.printStackTrace();
        }
    }
    
    public static void main(String args[]) {
        Test test = new Test();
        test.run();
    }
}
