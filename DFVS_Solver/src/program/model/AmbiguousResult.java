package program.model;

import java.util.List;

public class AmbiguousResult {
    Integer a;
    Integer b;
    Integer dependNode;

    public AmbiguousResult(Integer a, Integer b, Integer dependNode) {
        this.a = a;
        this.b = b;
        this.dependNode = dependNode;
    }

    public Integer getValue(List<Integer> S) {
        if(S.contains(dependNode)) {
            return b;
        } else {
            return a;
        }
    }
}
