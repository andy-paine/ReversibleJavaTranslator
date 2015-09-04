package com.company;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by Andy on 20/08/2015.
 */
public class Tape {

    private List<Object> tape;

    public Tape() {
        tape = new ArrayList<Object>();
    }

    public <T> T RESTORE() {
        ListIterator it = tape.listIterator(tape.size());
        T ret = (T)it.previous();
        tape.remove(ret);

        return ret;
    }

    public <T> void SAVE(T type) {
        tape.add(type);
    }


}
