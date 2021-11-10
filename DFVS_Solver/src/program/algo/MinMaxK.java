package program.algo;

import program.model.Graph;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public abstract class MinMaxK {

    public static int minK(int n, int m){

        List<Integer> smallestKList = new ArrayList<>();

        for(int i = 0; i <= n * (n-1) / 2; i++){
            smallestKList.add(0);
        }

        for(int j = 1; j < n; j++){
            for(int i = 0; i < (n-j); i++){
                smallestKList.add(j);
            }
        }

        return smallestKList.get(m);
    }

    public static int maxK(int n, int m){

        if(m == n * (n-1)) return n - 1;

        return Math.min(n - 2, m / 2);
    }

}
