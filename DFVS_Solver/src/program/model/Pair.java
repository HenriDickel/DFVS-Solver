package program.model;

public class Pair {
    Node A;
    Node B;

    public Pair(Node A, Node B) {
        this.A = A;
        this.B = B;
    }

    public boolean
    isForbidden(int level) {
        return A.deleted || B.deleted || A.forbidden < level || B.forbidden < level;
    }
}
