package program.log;

import program.model.Cycle;

public abstract  class CycleCounter {

    private static int[] sizeAcc;
    private static int[] count;

    public static void init(int k) {
        sizeAcc = new int[k];
        count = new int[k];

        for(int i = 0; i < k; i++) {
            sizeAcc[i] = 0;
            count[i] = 0;
        }
    }

    public static void count(Cycle cycle, int level) {
        sizeAcc[level] += cycle.size();
        count[level]++;
    }

    public static float[] getAverageCycleSize() {
        float[] averageCycleSize = new float[sizeAcc.length];
        for(int i = 0; i < sizeAcc.length; i++) {
            averageCycleSize[i] = (float) sizeAcc[i] / count[i];
        }
        return averageCycleSize;
    }

    public static int[] getRecursiveSteps() {
        return count;
    }
}
