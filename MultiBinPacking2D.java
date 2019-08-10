package localsearch.multibinpacking2d;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;
import localsearch.constraints.basic.AND;
import localsearch.constraints.basic.Implicate;
import localsearch.constraints.basic.IsEqual;
import localsearch.constraints.basic.LessOrEqual;
import localsearch.constraints.basic.OR;
import localsearch.functions.basic.FuncMult;
import localsearch.functions.basic.FuncPlus;
import localsearch.functions.basic.FuncVarConst;
import localsearch.functions.conditionalsum.ConditionalSum;
import localsearch.functions.sum.Sum;
import localsearch.model.ConstraintSystem;
import localsearch.model.IConstraint;
import localsearch.model.IFunction;
import localsearch.model.LocalSearchManager;
import localsearch.model.VarIntLS;
import localsearch.search.TabuSearch;

public class MultiBinPacking2D{
    public int m, n;                                    // m = number of bin, n = number of item
    public int[] W, H;                                  // W[j] and H[j] are width and height of bin j, respectively
    public int[] w, h;                                  // w[i] and h[i] are width and height of item i, respectively
    public int W_max, H_max;
    
    LocalSearchManager ls;
    ConstraintSystem S;
    VarIntLS[] x, y;                                    // Bottom left point of each item                                                           
    VarIntLS[] o;                                       // Orientation of each item     
    VarIntLS[] b;                                       // Bin assigned for each item
    VarIntLS[] q;                                       // = 1 if bin is used and = 0 otherwise
    ConditionalSum[] CS;
    IFunction obj1, obj2;
    
    public void readData(String fn){
        try{
            Scanner in = new Scanner(new File(fn));
   
            // load Bin
            ArrayList<Bin> B = new ArrayList<Bin>();
            while (true){
                int _W = in.nextInt();
                if (_W == -1)   break;
                int _H = in.nextInt();
                B.add(new Bin(_W, _H));
            }
            
            m = B.size(); 
            System.out.println("m = " + m);
            W = new int[m]; H = new int[m];
            W_max = -1; H_max = -1;
            for (int i = 0; i < m; i++){
                W[i] = B.get(i).W;
                if(W[i] > W_max)    W_max = W[i];
                H[i] = B.get(i).H;
                if(H[i] > H_max)    H_max = H[i];
            }
            
            // load Item
            ArrayList<Item> I = new ArrayList<Item>();
            while (true){
                int _w = in.nextInt();
                if (_w == -1)   break;
                int _h = in.nextInt();
                I.add(new Item(_w, _h));
            }
            
            n = I.size();
            System.out.println("n = " + n);
            w = new int[n]; h = new int[n];
            for (int i = 0; i < I.size(); i++){
                w[i] = I.get(i).w;
                h[i] = I.get(i).h;
            }
            
            in.close();
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }
    
    public void stateModel(){
        ls = new LocalSearchManager();
        S = new ConstraintSystem(ls);
        x = new VarIntLS[n];
        y = new VarIntLS[n];
        o = new VarIntLS[n];
        b = new VarIntLS[n];
        q = new VarIntLS[m];

        for (int i = 0; i < n; i++){
            x[i] = new VarIntLS(ls, 0, W_max);
            y[i] = new VarIntLS(ls, 0, H_max);
            o[i] = new VarIntLS(ls, 0, 1);
            b[i] = new VarIntLS(ls, 1, m);
        }

        for (int j = 0; j < m; j++){
            q[j] = new VarIntLS(ls, 0, 1);
        }
        
        CS = new ConditionalSum[m];
        for (int j = 0; j < m; j++){
            CS[j] = new ConditionalSum(b, j+1);
            S.post(new Implicate(new IsEqual(q[j], 0), new IsEqual(0, CS[j])));
        }
        
        for (int i = 0; i < n; i++){
            for (int j = 0; j < m; j++){
                S.post(new Implicate(new AND(new IsEqual(b[i], j + 1), new IsEqual(o[i], 0)), new LessOrEqual(
                        new FuncPlus(x[i], w[i]), W[j])));
                S.post(new Implicate(new AND(new IsEqual(b[i], j + 1), new IsEqual(o[i], 0)), new LessOrEqual(
                        new FuncPlus(y[i], h[i]), H[j])));
                S.post(new Implicate(new AND(new IsEqual(b[i], j + 1), new IsEqual(o[i], 1)), new LessOrEqual(
                        new FuncPlus(x[i], h[i]), W[j])));
                S.post(new Implicate(new AND(new IsEqual(b[i], j + 1), new IsEqual(o[i], 1)), new LessOrEqual(
                        new FuncPlus(y[i], w[i]), H[j])));
            }
        }

        for (int i1 = 0; i1 < n - 1; i1++){
            for (int i2 = i1 + 1; i2 < n; i2++){
                // o[i] = 0, o[j] = 0 (no orientation)
                IConstraint[] c = new IConstraint[4];
                c[0] = new LessOrEqual(new FuncPlus(x[i2], w[i2]), x[i1]); // l1.x > r2.x
                c[1] = new LessOrEqual(new FuncPlus(x[i1], w[i1]), x[i2]); // l2.x > r1.x
                c[2] = new LessOrEqual(new FuncPlus(y[i1], h[i1]), y[i2]); // l1.y < r2.y
                c[3] = new LessOrEqual(new FuncPlus(y[i2], h[i2]), y[i1]); // l2.y < r1.y
                S.post(new Implicate(new AND(new IsEqual(b[i1], b[i2]), new AND(new IsEqual(o[i1], 0), new IsEqual(
                        o[i2], 0))), new OR(c)));

                // o[i] = o, o[j] = 1
                c = new IConstraint[4];
                c[0] = new LessOrEqual(new FuncPlus(x[i2], h[i2]), x[i1]); // l1.x > r2.x
                c[1] = new LessOrEqual(new FuncPlus(x[i1], w[i1]), x[i2]); // l2.x > r1.x
                c[2] = new LessOrEqual(new FuncPlus(y[i1], h[i1]), y[i2]); // l1.y < r2.y
                c[3] = new LessOrEqual(new FuncPlus(y[i2], w[i2]), y[i1]); // l2.y < r1.y
                S.post(new Implicate(new AND(new IsEqual(b[i1], b[i2]), new AND(new IsEqual(o[i1], 0), new IsEqual(
                        o[i2], 1))), new OR(c)));

                // o[i] = 1, o[j] = 0
                c = new IConstraint[4];
                c[0] = new LessOrEqual(new FuncPlus(x[i2], w[i2]), x[i1]); // l1.x > r2.x
                c[1] = new LessOrEqual(new FuncPlus(x[i1], h[i1]), x[i2]); // l2.x > r1.x
                c[2] = new LessOrEqual(new FuncPlus(y[i1], w[i1]), y[i2]); // l1.y < r2.y
                c[3] = new LessOrEqual(new FuncPlus(y[i2], h[i2]), y[i1]); // l2.y < r1.y
                S.post(new Implicate(new AND(new IsEqual(b[i1], b[i2]), new AND(new IsEqual(o[i1], 1), new IsEqual(
                        o[i2], 0))), new OR(c)));

                // o[i] = 1, o[j] = 1
                c = new IConstraint[4];
                c[0] = new LessOrEqual(new FuncPlus(x[i2], h[i2]), x[i1]); // l1.x > r2.x
                c[1] = new LessOrEqual(new FuncPlus(x[i1], h[i1]), x[i2]); // l2.x > r1.x
                c[2] = new LessOrEqual(new FuncPlus(y[i1], w[i1]), y[i2]); // l1.y < r2.y
                c[3] = new LessOrEqual(new FuncPlus(y[i2], w[i2]), y[i1]); // l2.y < r1.y
                S.post(new Implicate(new AND(new IsEqual(b[i1], b[i2]), new AND(new IsEqual(o[i1], 1), new IsEqual(
                        o[i2], 1))), new OR(c)));
            }

        }

        obj1 = new Sum(q);

        obj2 = new FuncVarConst(ls, 0);
        for (int j = 0; j < m; j++)
            obj2 = new FuncPlus(obj2, new FuncMult(q[j], W[j]*H[j]));

        ls.close();
    }

    public void search(int timeLimit, int algo){
        IFunction[] tf = new IFunction[1];
        tf[0] = obj1;
        if(algo == 0){
            TabuSearch ts = new TabuSearch();
            ts.search(S, 50, timeLimit, 1000, 50);      
            ts.searchMaintainConstraintsMinimize(obj1, S, 50, timeLimit, 1000, 50);
            ts.searchMaintainConstraintsFunctionMinimize(obj2, tf, S, 50, timeLimit, 1000, 50);
            System.out.println("TABU-SEARCH: DONE");
        }else if(algo == 1){
            SimulatedAnnealing sa = new SimulatedAnnealing();
            sa.search(S, 200);
            sa.searchMaintainConstraintsMinimize(obj1, S, 200);
            sa.searchMaintainConstraintsFunctionMinimize(obj2, tf, S, 200);
            System.out.println("SIMULATED ANNEALING: DONE");
        }else{
            IteratedLocalSearch ils = new IteratedLocalSearch();
            ils.search(S, 200);
            ils.searchMaintainConstraintsMinimize(obj1, S, 200);
            ils.searchMaintainConstraintsFunctionMinimize(obj2, tf, S, 200);
            System.out.println("ITERATED LOCAL SEARCH: DONE");
        }
    }

    public void printResult(){
        for (int i = 0; i < n; i++){
            System.out.println("Item " + i + " is packed in bin " + b[i].getValue() + " at (" + x[i].getValue()
                    + ", " + y[i].getValue() + ") and orientation = " + o[i].getValue() + ".");
        }
    }

    public void outTableNew(String fn, int n, int[] w, int[] h, int[] x, int[] y, int[] o, int b[]){
        final String[] Color = new String[]{
            "#FFFF00", "#1CE6FF", "#FF34FF", "#FF4A46", "#008941", "#006FA6", "#A30059",
            "#FF0000", "#7A4900", "#0000A6", "#63FFAC", "#B79762", "#004D43", "#8FB0FF", "#997D87",
            "#5A0007", "#809693", "#1B4400", "#4FC601", "#3B5DFF", "#4A3B53", "#FF2F80",
            "#61615A", "#BA0900", "#6B7900", "#00C2A0", "#FFAA92", "#FF90C9", "#B903AA", "#D16100",
            "#FFDBE5", "#000035", "#7B4F4B", "#A1C299", "#300018", "#0AA6D8", "#013349", "#00846F",
            "#372101", "#FFB500", "#C2FFED", "#A079BF", "#CC0744", "#C0B9B2", "#C2FF99", "#001E09",
            "#00489C", "#6F0062", "#0CBD66", "#EEC3FF", "#456D75", "#B77B68", "#7A87A1", "#788D66",
            "#885578", "#FAD09F", "#FF8A9A", "#D157A0", "#BEC459", "#456648", "#0086ED", "#886F4C",
            "#34362D", "#B4A8BD", "#00A6AA", "#452C2C", "#636375", "#A3C8C9", "#FF913F", "#938A81",
            "#575329", "#00FECF", "#B05B6F", "#8CD0FF", "#3B9700", "#04F757", "#C8A1A1", "#1E6E00",
            "#7900D7", "#A77500", "#6367A9", "#A05837", "#6B002C", "#772600", "#D790FF", "#9B9700",
            "#549E79", "#FFF69F", "#201625", "#72418F", "#BC23FF", "#99ADC0", "#3A2465", "#922329",
            "#5B4534", "#FDE8DC", "#404E55", "#0089A3", "#CB7E98", "#A4E804", "#324E72", "#6A3A4C",
            "#83AB58", "#001C1E", "#D1F7CE", "#004B28", "#C8D0F6", "#A3A489", "#806C66", "#222800",
            "#BF5650", "#E83000", "#66796D", "#DA007C", "#FF1A59", "#8ADBB4", "#1E0200", "#5B4E51",
            "#C895C5", "#320033", "#FF6832", "#66E1D3", "#CFCDAC", "#D0AC94", "#7ED379", "#012C58"
        };
        try{
            File outFile = new File(fn);
            PrintWriter out;
            out = new PrintWriter(outFile);
            out.println("<!doctype html>\n<html>\n<head>\n<title>Multibinpacking2D</title>\n</head>\n<body>\n");

            for(int j = 0; j < m; j++){
                int count = 0;
                for(int i = 0; i < n; i++)
                    if(b[i] == j + 1) count++;
                
                int wj[] = new int[count];
                int hj[] = new int[count];
                int xj[] = new int[count];
                int yj[] = new int[count];
                int oj[] = new int[count];
                int bj[] = new int[count];
                int ij[] = new int[count];
                
                int index = 0;
                for(int i = 0; i < n; i++)
                    if(b[i] == j + 1){
                        wj[index] = w[i];
                        hj[index] = h[i];
                        xj[index] = x[i];
                        yj[index] = y[i];
                        oj[index] = o[i];
                        bj[index] = b[i];
                        ij[index] = i;
                        index++;
                    }
                
                
                boolean[] isIndex = new boolean[count + 2];
                int size = 650 / (Math.max(W[j], H[j]) + 1);
                out.println("<style type=\"text/css\">\n" + "table, td{\n"
                        + "\t\tborder : 1px solid black;\n"
                        + "\t\tborder-collapse: collapse;text-align : center;\n"
                        + "\t}\n"
                        + "\ttd{\n"
                        + "\t\twidth : +" + size + "px;\n"
                        + "\t\theight: +" + size + "px;\n"
                        + "\t}"
                );
                        
                for (int i = 0; i < count; i++){
                    out.println("td.class" + (i) + "{ \n background-color:" + Color[i] + ";  \n}");
                }
                out.println("</style>");

                out.println("<table>");
                for (int i = 0; i <= H[j]; i++){
                    out.println("<tr>");
                    for (int k = 0; k <= W[j]; k++){
                        if (i == 0){
                            if (k == 0){
                                out.print("<td>");
                                out.println("</td>");
                            } else{
                                out.print("<td>");
                                out.print(j);
                                out.println("</td>");
                            }
                        } else{
                            if (k == 0){
                                out.print("<td>");
                                out.print(i);
                                out.println("</td>");
                            } else{
                                boolean flag = false;
                                for (int l = 0; l < count; l++){
                                    int xl = xj[l];//x[k].getValue();
                                    int yl = yj[l];//y[k].getValue();
                                    int wl = wj[l];
                                    int hl = hj[l];
                                    //if (o[k].getValue() == 0){
                                    if (oj[l] == 0){
                                        if (k - 1 >= xl && k - 1 <= xl + wl - 1 && i - 1 >= yl && i - 1 <= yl + hl - 1){
                                            out.print("<td class='class" + l + "'>");

                                            if (!isIndex[l] && (k - 1) == (xl + xl + wl - 1) / 2 && (i - 1) == (yl + yl + hl - 1) / 2){
                                                out.print(ij[l]);
                                                isIndex[l] = true;
                                            }
                                            flag = true;
                                            break;
                                        }
                                    } else{
                                        if (k - 1 >= xl && k - 1 <= xl + hl - 1 && i - 1 >= yl && i - 1 <= yl + wl - 1){
                                            out.print("<td class='class" + l + "'>");
                                            if (!isIndex[l] && (k - 1) == (xl + xl + hl - 1) / 2 && (i - 1) == (yl + yl + wl - 1) / 2){
                                                out.print(ij[l]);
                                                isIndex[l] = true;
                                            }
                                            flag = true;
                                            break;
                                        }
                                    }
                                }
                                if (flag){
                                    out.println("</td>");
                                } else{
                                    out.print("<td>");
                                    out.println("</td>");
                                }
                            }
                        }

                    }
                    out.println("</tr>");
                }
                out.println("</table>");
                out.println("<br/>");
            }
            out.println("</body></html>");
            out.close();
        } catch (IOException exx){
            exx.printStackTrace();
        }
    }
    
    public void printResultHTML(String fn){
		int[] rx = new int[x.length];
		int[] ry = new int[y.length];
		int[] ro = new int[o.length];
                int[] rb = new int[b.length];
		for(int i = 0; i < x.length; i++){
			rx[i] = x[i].getValue();
			ry[i] = y[i].getValue();
			ro[i] = o[i].getValue();
                        rb[i] = b[i].getValue();
		}
		printResult();
		outTableNew(fn, n, w, h, rx, ry, ro, rb);
    }
    
    public boolean solve(int timeLimit, int algo){
        stateModel();
        search(timeLimit, algo);
        return S.violations() == 0;
    }

    public static void main(String[] args){
        MultiBinPacking2D mbp = new MultiBinPacking2D();
        Test test = new Test();
        mbp.readData("C:\\Users\\96chi\\Documents\\NetBeansProjects\\project\\src\\localsearch\\multibinpacking2d\\test\\6.txt");
        mbp.solve(1000, 0);
        mbp.printResultHTML("C:\\Users\\96chi\\Documents\\NetBeansProjects\\project\\src\\localsearch\\multibinpacking2d\\result\\multibinpacking2d.html");
    }
}

class Item{
    int w, h;
    public Item(int w, int h){
        this.w = w;
        this.h = h;
    }
}

class Bin{
    int W, H;
    public Bin(int W, int H){
        this.W = W;
        this.H = H;
    }
}
