package program.model;

public class GraphFile {

    public String path;
    public String name;
    public int optimalK;

    public GraphFile(String path, String name, int optimalK) {
        this.path = path;
        this.name = name;
        this.optimalK = optimalK;
    }
}
