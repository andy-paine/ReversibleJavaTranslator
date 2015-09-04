package com.company;

import org.eclipse.jdt.core.dom.Statement;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andy on 31/08/2015.
 */
public class StatementBreakPointPair {
    public Statement statement;
    public List<Integer> breakPoints;

    public StatementBreakPointPair(Statement stmt, Integer integer) {
        this.statement = stmt;
        this.breakPoints = new ArrayList<Integer>();
        this.breakPoints.add(integer);
    }
}
