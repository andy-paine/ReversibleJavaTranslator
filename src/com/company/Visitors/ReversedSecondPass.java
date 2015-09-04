package com.company.Visitors;

import com.company.StatementBreakPointPair;
import com.company.StatementBreakPointPairList;
import com.company.TranslationUtil;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Andy on 13/08/2015.
 */
public class ReversedSecondPass extends CustomVisitor {

    StatementBreakPointPairList statementBreakPointPairList;

    public ReversedSecondPass() {
        super();
        this.statementBreakPointPairList = new StatementBreakPointPairList();
    }

    @Override
    public void accept(MethodDeclaration declaration) {
        super.accept(declaration);

        Iterator it = statementBreakPointPairList.getIterator();

        while (it.hasNext()) {
            StatementBreakPointPair pair = (StatementBreakPointPair)it.next();
            Statement stmt = pair.statement;

            if (!(stmt instanceof BreakStatement) && !(stmt instanceof ExpressionStatement && ((ExpressionStatement) stmt).getExpression() instanceof Assignment)
                    && !(stmt instanceof LabeledStatement && ((LabeledStatement) stmt).getLabel().getFullyQualifiedName().equals("out_of_scope"))) {
                IfStatement ifStatement = declaration.getAST().newIfStatement();
                List<InfixExpression> infixExpressions = new ArrayList<InfixExpression>();

                for (int i=0; i<pair.breakPoints.size(); i++) {
                    InfixExpression infixExpression = declaration.getAST().newInfixExpression();
                    infixExpression.setLeftOperand(declaration.getAST().newSimpleName("broke_here"));
                    infixExpression.setOperator(InfixExpression.Operator.NOT_EQUALS);
                    infixExpression.setRightOperand(declaration.getAST().newNumberLiteral(pair.breakPoints.get(i).toString()));
                    infixExpressions.add(infixExpression);
                }

                InfixExpression mainExpression = infixExpressions.get(0);
                for (int i=1; i<infixExpressions.size(); i++) {
                    InfixExpression subExpression = declaration.getAST().newInfixExpression();
                    subExpression.setLeftOperand(mainExpression);
                    subExpression.setOperator(InfixExpression.Operator.CONDITIONAL_AND);
                    subExpression.setRightOperand(infixExpressions.get(i));
                    mainExpression = subExpression;
                }

                ifStatement.setExpression((Expression)ASTNode.copySubtree(declaration.getAST(), mainExpression));
                Block ifBody = declaration.getAST().newBlock();
                ifBody.statements().add(ASTNode.copySubtree(declaration.getAST(), stmt));
                ifStatement.setThenStatement(ifBody);

                ASTNode parent = TranslationUtil.getContainingBlock(stmt);
                ListRewrite listRewrite = rewriter.getListRewrite(parent.getParent(), Block.STATEMENTS_PROPERTY);
                listRewrite.replace(stmt, ifStatement, null);
            }
        }
    }

    @Override
    public boolean visit(ReturnStatement returnStatement) {

        ASTNode block = TranslationUtil.getContainingBlock(returnStatement);
        ListRewrite listRewrite = rewriter.getListRewrite(block.getParent(), Block.STATEMENTS_PROPERTY);

        listRewrite.remove(returnStatement, null);
        return true;
    }

    @Override
    public boolean visit(BreakStatement breakStatement) {
        ASTNode parent = TranslationUtil.getContainingBlock(breakStatement);
        NumberLiteral breakNumber = TranslationUtil.getBreakPointValue(parent);

        /* go up a block and collect all statements before (after in forward) */
        /* keep going until the block to break has been reached */
        while (!(parent instanceof LabeledStatement) || !(((LabeledStatement) parent).getLabel().getFullyQualifiedName().equals("return_label"))) {
            if (!(parent instanceof WhileStatement) && !(parent instanceof IfStatement)) {
                //if we aren't in a while loop block, scan through the statements
                boolean reached = false;
                Block block = (Block)parent.getParent();
                Iterator it = block.statements().iterator();
                while (it.hasNext() && !reached) {
                    Statement stmt = (Statement)it.next();
                    if (stmt.equals(parent))
                        reached = true;
                    else {
                    /* if we haven't reached that statement yet, add this to the list */
                        if (!(stmt instanceof ExpressionStatement && ((ExpressionStatement) stmt).getExpression() instanceof PrefixExpression
                                && ((PrefixExpression) ((ExpressionStatement) stmt).getExpression()).getOperand() instanceof SimpleName
                                && ((SimpleName) ((PrefixExpression) ((ExpressionStatement) stmt).getExpression()).getOperand()).getFullyQualifiedName().matches("while_counter_\\d")))
                        statementBreakPointPairList.add(stmt, Integer.parseInt(breakNumber.getToken()));
                    }

                }
            }
            /* go up a block */
            parent = TranslationUtil.getContainingBlock(parent.getParent());

        }
        ASTNode container = TranslationUtil.getContainingBlock(breakStatement);
        ListRewrite listRewrite = rewriter.getListRewrite(container.getParent(), Block.STATEMENTS_PROPERTY);
        listRewrite.remove(breakStatement, null);

        return true;
    }

    @Override
    public boolean visit(LabeledStatement labeledStatement) {
        if (labeledStatement.getLabel().getFullyQualifiedName().equals("break_save")) {
            ASTNode container = TranslationUtil.getContainingBlock(labeledStatement);
            ListRewrite listRewrite = rewriter.getListRewrite(container.getParent(), Block.STATEMENTS_PROPERTY);
            listRewrite.remove(labeledStatement, null);
        }

        return true;
    }


}
