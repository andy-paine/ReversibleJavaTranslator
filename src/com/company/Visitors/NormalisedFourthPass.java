package com.company.Visitors;

import com.company.TranslationUtil;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import java.util.Iterator;

/**
 * Created by Andy on 13/08/2015.
 */
public class NormalisedFourthPass extends CustomVisitor {

    public NormalisedFourthPass () {
        this.rewriter = null;
    }

    /**********************************************************
     Move all variable initializations into a declaration and assignment
     *********************************************************/
    @Override
    public boolean visit(VariableDeclarationStatement statement) {
        Iterator it = statement.fragments().iterator();
        ListRewrite listRewrite = rewriter.getListRewrite(statement.getParent(), Block.STATEMENTS_PROPERTY);

        while (it.hasNext()) {

            VariableDeclarationFragment frag = (VariableDeclarationFragment) it.next();

            /* if the variable has an initializer */
            if (frag.getInitializer() != null) {

                Expression exp = (Expression) ASTNode.copySubtree(frag.getInitializer().getAST(), frag.getInitializer());

                /* create a new assignment for the initializer */
                Assignment assign = statement.getAST().newAssignment();
                assign.setLeftHandSide(statement.getAST().newSimpleName(frag.getName().toString()));
                assign.setOperator(Assignment.Operator.ASSIGN);
                assign.setRightHandSide(exp);
                ExpressionStatement expressionStatement = statement.getAST().newExpressionStatement(assign);

                /* insert assignment and remove initializer */
                listRewrite.insertAfter(expressionStatement, statement, null);
                rewriter.set(frag, VariableDeclarationFragment.INITIALIZER_PROPERTY, null, null);
            }
        }

        return true;
    }

}
