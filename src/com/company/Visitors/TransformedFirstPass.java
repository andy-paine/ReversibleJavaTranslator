package com.company.Visitors;

import com.company.TranslationUtil;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

/**
 * Created by Andy on 12/08/2015.
 */
public class TransformedFirstPass extends CustomVisitor {

    private int whilecounter;

    public TransformedFirstPass() {
        super();
        this.whilecounter = 0;
    }

    /**********************************************************
     Introduces counter variable for tracking number of times
     while loop executed. Increments counter after each loop
     *********************************************************/
    @Override
    public boolean visit(WhileStatement whileStatement) {
        /* label statement as while loops need special handling in reversal */
        LabeledStatement labelledStatement = whileStatement.getAST().newLabeledStatement();
        labelledStatement.setLabel(whileStatement.getAST().newSimpleName("while_loop"));
        Block containingBlock = whileStatement.getAST().newBlock();

        /* create variable for counting */
        SimpleName name = whileStatement.getAST().newSimpleName("while_counter_" + whilecounter);
        VariableDeclarationFragment declarationFragment = whileStatement.getAST().newVariableDeclarationFragment();
        declarationFragment.setName(name);
        VariableDeclarationStatement variableDeclarationStatement = whileStatement.getAST().newVariableDeclarationStatement(declarationFragment);
        variableDeclarationStatement.setType(whileStatement.getAST().newSimpleType(whileStatement.getAST().newName("Integer")));

        /* intialize variable to 0 */
        Assignment assign = whileStatement.getAST().newAssignment();
        name = (SimpleName)ASTNode.copySubtree(whileStatement.getAST(), name);
        assign.setLeftHandSide(name);
        assign.setOperator(Assignment.Operator.ASSIGN);
        assign.setRightHandSide(whileStatement.getAST().newNumberLiteral("0"));
        ExpressionStatement assignmentStatement = whileStatement.getAST().newExpressionStatement(assign);

        /* increment variable after each loop */
        PostfixExpression postfixExpression = whileStatement.getAST().newPostfixExpression();
        postfixExpression.setOperand((SimpleName) ASTNode.copySubtree(whileStatement.getAST(), name));
        postfixExpression.setOperator(PostfixExpression.Operator.INCREMENT);
        ExpressionStatement postfixStatement = whileStatement.getAST().newExpressionStatement(postfixExpression);

        ((Block) whileStatement.getBody()).statements().add(0, postfixStatement);

        containingBlock.statements().add(variableDeclarationStatement);
        containingBlock.statements().add(assignmentStatement);
        containingBlock.statements().add(ASTNode.copySubtree(whileStatement.getAST(), whileStatement));

        labelledStatement.setBody(containingBlock);

        ASTNode block = TranslationUtil.getContainingBlock(whileStatement);
        ListRewrite listRewrite = rewriter.getListRewrite(block.getParent(), Block.STATEMENTS_PROPERTY);

        listRewrite.replace(whileStatement, labelledStatement, null);

        /* increment global tracker of number of while counters to avoid collisions of naming */
        whilecounter++;

        return true;
    }
}
