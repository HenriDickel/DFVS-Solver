import log.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static String PATH_SYNTHETIC_FOLDER = "src/inputs/synthetic/";
    public static String PATH_SYNTHETIC_SOLUTION_FOLDER = "src/solutions/synthetic/";
    public static String PATH_COMPLEX_FOLDER = "src/inputs/complex/";
    public static String PATH_COMPLEX_SOLUTION_FOLDER = "src/solutions/complex/";
    public static String PATH_TEST_SOLUTION_FOLDER = "src/solutions/test/";

    public static void main(String[] args) {

        // Custom Tests
        if(true) {

            List<List<Node>> components = Preprocessing.findAllCycles(TestCreator.createSimpleNonDAG());
            components = Preprocessing.findAllCycles(TestCreator.createSimpleNonDAG2());
            components = Preprocessing.findAllCycles(TestCreator.createSimpleNonDAG3());
            components = Preprocessing.findAllCycles(TestCreator.createSimpleNonDAG4());
            components = Preprocessing.findAllCycles(TestCreator.createSimpleNonDAG5());
            components = Preprocessing.findAllCycles(TestCreator.createK3Test());

            //solveSelectedGraphs();
            return;
        }

        //As args
        if(args.length > 0){
            String fileName = args[0];
            Graph graph = GraphFileManager.CreateFromFile("", fileName);
            List<Node> nodes = Solver.dfvs_solve(graph);
            GraphFileManager.SaveSolution("", fileName, nodes);
            return;
        }


        //LogLevel
        Log.Clear();
        Log.type = Log.LogType.File;
        Log.detail = Log.LogDetail.Important;


        //solveSynthetics();
        solveComples();

    }

    public static void solveSynthetics() {
        List<Graph> Synthetics = GraphFileManager.CreateFromFolder(PATH_SYNTHETIC_FOLDER);
        Synthetics.forEach(x -> {
            List<Node> nodes = Solver.dfvs_solve(x);
            GraphFileManager.SaveSolution(PATH_SYNTHETIC_SOLUTION_FOLDER, x.name, nodes);
            verify(x.name, PATH_SYNTHETIC_FOLDER + x.name, PATH_SYNTHETIC_SOLUTION_FOLDER + x.name);
        } );
    }

    public static void solveComples() {
        List<Graph> Complex = GraphFileManager.CreateFromFolder(PATH_COMPLEX_FOLDER);
        Complex.forEach(x -> {
            List<Node> nodes = Solver.dfvs_solve(x);
            GraphFileManager.SaveSolution(PATH_COMPLEX_SOLUTION_FOLDER, x.name, nodes);
            verify(x.name, PATH_COMPLEX_FOLDER + x.name, PATH_COMPLEX_SOLUTION_FOLDER + x.name);
        } );
    }

    // For testing the performance with a few graphs, that take a while
    public static void solveSelectedGraphs() {
        List<Graph> graphs = new ArrayList<>();
        graphs.add(GraphFileManager.CreateFromFile(PATH_SYNTHETIC_FOLDER, "synth-n_30-m_111-k_8-p_0.2.txt"));
        graphs.add(GraphFileManager.CreateFromFile(PATH_SYNTHETIC_FOLDER, "synth-n_30-m_117-k_10-p_0.2.txt"));
        graphs.add(GraphFileManager.CreateFromFile(PATH_SYNTHETIC_FOLDER, "synth-n_40-m_114-k_15-p_0.1.txt"));
        graphs.add(GraphFileManager.CreateFromFile(PATH_SYNTHETIC_FOLDER, "synth-n_40-m_192-k_10-p_0.2.txt"));
        graphs.add(GraphFileManager.CreateFromFile(PATH_SYNTHETIC_FOLDER, "synth-n_40-m_192-k_15-p_0.2.txt"));
        graphs.add(GraphFileManager.CreateFromFile(PATH_SYNTHETIC_FOLDER, "synth-n_40-m_203-k_8-p_0.2.txt"));
        graphs.add(GraphFileManager.CreateFromFile(PATH_SYNTHETIC_FOLDER, "synth-n_50-m_154-k_8-p_0.1.txt"));
        graphs.add(GraphFileManager.CreateFromFile(PATH_SYNTHETIC_FOLDER, "synth-n_50-m_159-k_10-p_0.1.txt"));
        graphs.add(GraphFileManager.CreateFromFile(PATH_SYNTHETIC_FOLDER, "synth-n_50-m_192-k_20-p_0.1.txt"));
        graphs.add(GraphFileManager.CreateFromFile(PATH_SYNTHETIC_FOLDER, "synth-n_50-m_195-k_15-p_0.1.txt"));
        graphs.add(GraphFileManager.CreateFromFile(PATH_SYNTHETIC_FOLDER, "synth-n_50-m_302-k_6-p_0.2.txt"));
        graphs.add(GraphFileManager.CreateFromFile(PATH_SYNTHETIC_FOLDER, "synth-n_50-m_320-k_15-p_0.2.txt"));
        graphs.add(GraphFileManager.CreateFromFile(PATH_SYNTHETIC_FOLDER, "synth-n_50-m_327-k_10-p_0.2.txt"));
        graphs.add(GraphFileManager.CreateFromFile(PATH_SYNTHETIC_FOLDER, "synth-n_50-m_334-k_8-p_0.2.txt"));
        graphs.add(GraphFileManager.CreateFromFile(PATH_SYNTHETIC_FOLDER, "synth-n_50-m_357-k_20-p_0.2.txt"));

        graphs.forEach(graph -> {
            List<Node> nodes = Solver.dfvs_solve(graph);
            GraphFileManager.SaveSolution(PATH_SYNTHETIC_SOLUTION_FOLDER, graph.name, nodes);
            verify(graph.name, PATH_SYNTHETIC_FOLDER + graph.name, PATH_SYNTHETIC_SOLUTION_FOLDER + graph.name);
        } );
    }

    public static void solveTests() {
        Solver.dfvs_solve(TestCreator.createSimpleDAG());
        Solver.dfvs_solve(TestCreator.createSimpleNonDAG());
        Solver.dfvs_solve(TestCreator.createComplexNonDAG());
        Solver.dfvs_solve(TestCreator.createK3Test());
        Solver.dfvs_solve(TestCreator.createSimpleNonDAG2());
        Solver.dfvs_solve(TestCreator.createSimpleNonDAG3());

    }

    public static boolean verify(String graphName, String inputGraph, String solutionPath){
        try{

            String command = "py src/python/dfvs-verify.py " + inputGraph + " " + solutionPath;
            Process p = Runtime.getRuntime().exec(command);

            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String ret = in.readLine();
            Log.log(Log.LogDetail.Important, graphName, 0, ret != null ? "Verified" : "Not Verified");
        }catch(Exception e){e.printStackTrace();}

        return false;
    }
}
