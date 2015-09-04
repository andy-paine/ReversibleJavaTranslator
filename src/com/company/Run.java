package com.company;

import java.util.ArrayList;

/**
 * Created by Andy on 20/08/2015.
 */
public class Run {
    public static void main(String[] args) throws Exception {

        Tape tape = new Tape();
        Algorithm algorithm = new Algorithm(tape);

        ArrayList<Integer> vals = new ArrayList();
        for (int i=0; i<10; i++)
            vals.add(i);

        for (int val: vals) {
            int forward = algorithm.compute(val);
            //int backward = algorithm.rev_compute(forward);
            //assert backward == val;
        }


    }
}
