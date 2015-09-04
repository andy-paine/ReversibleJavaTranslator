package com.company;

/**
 * Created by Andy on 20/08/2015.
 */
public class Algorithm {

    private Tape tape;

    public Algorithm (Tape tape) {
        this.tape = tape;
    }

    public int compute(int t) {
        t *= t;
        t++;
        return t;
    }
}
