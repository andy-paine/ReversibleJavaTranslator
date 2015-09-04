package com.company.Visitors;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

/**
 * Created by Andy on 12/08/2015.
 */
public class NormalisedSecondPass extends CustomVisitor{

    private int breakNum = 0;

    public NormalisedSecondPass () {
        super();
    }

    /**********************************************************
     Converts return statements to equivalent assignment and break statements
     *********************************************************/
    @Override
    public boolean visit(ReturnStatement returnStatement) {

        /* create new assignment from old returned expression to new variable */
        Expression exp = (Expression)ASTNode.copySubtree(returnStatement.getAST(), returnStatement.getExpression());
        Assignment assign = returnStatement.getAST().newAssignment();
        SimpleName name = returnStatement.getAST().newSimpleName("return_var");
        assign.setLeftHandSide(((SimpleName) ASTNode.copySubtree(returnStatement.getAST(), name)));
        assign.setOperator(Assignment.Operator.ASSIGN);
        assign.setRightHandSide(exp);
        ExpressionStatement expressionStatement = returnStatement.getAST().newExpressionStatement(assign);

        /* break the labelled block to jump to single return statement */
        BreakStatement breakStatement = returnStatement.getAST().newBreakStatement();
        breakStatement.setLabel(returnStatement.getAST().newSimpleName("return_label"));

        /* create variable to mark where we broke */
        Assignment breakAssign = returnStatement.getAST().newAssignment();
        breakAssign.setLeftHandSide(returnStatement.getAST().newSimpleName("broke_here"));
        breakAssign.setOperator(Assignment.Operator.ASSIGN);
        breakAssign.setRightHandSide(returnStatement.getAST().newNumberLiteral(String.valueOf(breakNum)));
        breakNum++;
        ExpressionStatement breakExpressionStatement = returnStatement.getAST().newExpressionStatement(breakAssign);

        /* if the return statement is already in a block, use a list rewriter to replace return statement */
        if (returnStatement.getLocationInParent().isChildListProperty()) {
            ListRewrite listRewrite = rewriter.getListRewrite(returnStatement.getParent(), (ChildListPropertyDescriptor)returnStatement.getLocationInParent());
            listRewrite.replace(returnStatement, expressionStatement, null);
            listRewrite.insertAfter(breakStatement, returnStatement, null);
            listRewrite.insertAfter(breakExpressionStatement, returnStatement, null);
        }
        /* otherwise, the statement is the only child so we need to create a new block */
        else {
            Block block = returnStatement.getAST().newBlock();

            /* add the two statements to a new block */
            block.statements().add(expressionStatement);
            block.statements().add(breakExpressionStatement);
            block.statements().add(breakStatement);

            /* assign the block as the new child */
            rewriter.set(returnStatement.getParent(), returnStatement.getLocationInParent(), block, null);
        }

        return true;
    }


}
