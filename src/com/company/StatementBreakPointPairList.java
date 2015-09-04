package com.company;

import org.eclipse.jdt.core.dom.Statement;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Andy on 31/08/2015.
 */
public class StatementBreakPointPairList {
    private ArrayList<StatementBreakPointPair> pairs;

    public StatementBreakPointPairList() {
        this.pairs = new ArrayList<StatementBreakPointPair>();
    }

    public void add(Statement stmt, int newInt) {
        for (int i=0; i<pairs.size(); i++) {
            if (pairs.get(i).statement.equals(stmt)) {
                pairs.get(i).breakPoints.add(newInt);
                return;
            }
        }
        StatementBreakPointPair newPair = new StatementBreakPointPair(stmt, newInt);
        pairs.add(newPair);
    }

    public Iterator<StatementBreakPointPair> getIterator() {
        return pairs.iterator();
    }
}
