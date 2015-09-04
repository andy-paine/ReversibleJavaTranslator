package com.company.Visitors;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.Statement;


/**
 * Created by Andy on 01/09/2015.
 */
public class CleanupVisitor extends CustomVisitor {

    public CleanupVisitor() { super(); }

    @Override
    public boolean visit(LabeledStatement labeledStatement) {
        if (labeledStatement.getLabel().getFullyQualifiedName().equals("out_of_scope") || labeledStatement.getLabel().getFullyQualifiedName().equals("break_save")) {
            Statement statement = (Statement) ASTNode.copySubtree(labeledStatement.getAST(), labeledStatement.getBody());
            rewriter.replace(labeledStatement, statement, null);
        }

        return true;
    }
}
