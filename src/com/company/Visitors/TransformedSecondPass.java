package com.company.Visitors;

import com.company.TranslationUtil;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

/**
 * Created by Andy on 16/08/2015.
 */
public class TransformedSecondPass extends CustomVisitor {

    private int ifcounter;

    public TransformedSecondPass() {
        super();
        this.ifcounter = 0;
    }

    /**********************************************************
     Introduces outcome variable for tracking which branch of the
     if statement executed
     *********************************************************/
    @Override
    public boolean visit(IfStatement ifStatement) {
        /* label statement as if statements need special handling in reversal */
        LabeledStatement labelledStatement = ifStatement.getAST().newLabeledStatement();
        labelledStatement.setLabel(ifStatement.getAST().newSimpleName("if_statement"));
        Block containingBlock = ifStatement.getAST().newBlock();

        /* introduce outcome variable */
        SimpleName name = ifStatement.getAST().newSimpleName("if_outcome_" + ifcounter);
        VariableDeclarationFragment fragment = ifStatement.getAST().newVariableDeclarationFragment();
        fragment.setName(name);
        VariableDeclarationStatement variableDeclarationStatement = ifStatement.getAST().newVariableDeclarationStatement(fragment);
        variableDeclarationStatement.setType(ifStatement.getAST().newSimpleType(ifStatement.getAST().newName("Boolean")));

        /* create expression of !!(ifcondition) to cast to boolean */
        PrefixExpression notExp = ifStatement.getAST().newPrefixExpression();
        notExp.setOperator(PrefixExpression.Operator.NOT);
        ParenthesizedExpression parenth = ifStatement.getAST().newParenthesizedExpression();
        parenth.setExpression((Expression) ASTNode.copySubtree(ifStatement.getAST(), ifStatement.getExpression()));
        notExp.setOperand(parenth);
        PrefixExpression notNotExp = ifStatement.getAST().newPrefixExpression();
        notNotExp.setOperator(PrefixExpression.Operator.NOT);
        notNotExp.setOperand(notExp);

        /* assign outcome variable to !! expression */
        Assignment assignment = ifStatement.getAST().newAssignment();
        assignment.setLeftHandSide((Expression) ASTNode.copySubtree(ifStatement.getAST(), name));
        assignment.setOperator(Assignment.Operator.ASSIGN);
        assignment.setRightHandSide(notNotExp);
        ExpressionStatement assignmentExpression = ifStatement.getAST().newExpressionStatement(assignment);
        assignmentExpression.setProperty("if_outcome", null);

        /* convert then and else to blocks if they aren't already */
        if (ifStatement.getThenStatement() instanceof ExpressionStatement) {
            Block newBlock = ifStatement.getAST().newBlock();
            newBlock.statements().add(ASTNode.copySubtree(ifStatement.getAST(), ifStatement.getThenStatement()));
            ifStatement.setThenStatement(newBlock);
        }

        if (ifStatement.getElseStatement() instanceof ExpressionStatement) {
            Block newBlock = ifStatement.getAST().newBlock();
            newBlock.statements().add(ASTNode.copySubtree(ifStatement.getAST(), ifStatement.getElseStatement()));
            ifStatement.setElseStatement(newBlock);
        }

        ifStatement.setExpression((Expression) ASTNode.copySubtree(ifStatement.getAST(), name));

        containingBlock.statements().add(variableDeclarationStatement);
        containingBlock.statements().add(assignmentExpression);
        containingBlock.statements().add(ASTNode.copySubtree(ifStatement.getAST(), ifStatement));
        labelledStatement.setBody(containingBlock);

        ASTNode block = TranslationUtil.getContainingBlock(ifStatement);

        ListRewrite listRewrite = rewriter.getListRewrite(block.getParent(), Block.STATEMENTS_PROPERTY);
        listRewrite.replace(ifStatement, labelledStatement, null);

        /* increment global tracker to avoid variable collision */
        ifcounter++;

        return true;
    }
}
