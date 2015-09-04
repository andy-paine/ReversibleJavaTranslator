package com.company.Visitors;

import com.company.TranslationUtil;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import java.util.Iterator;

public class NormalisedFirstPass extends CustomVisitor{

    public NormalisedFirstPass () {
        super();
    }

    @Override
    public void accept(MethodDeclaration declaration) {
        super.accept(declaration);
    }

    /**********************************************************
     Converts for statement to equivalent while statement
     *********************************************************/
    @Override
    public boolean visit(ForStatement forStatement) {

        Block replacementBlock = forStatement.getAST().newBlock();
        Iterator it = forStatement.initializers().iterator();

        /* for every initializer, create a new expression in the replacement block */
        while (it.hasNext()) {
            Expression exp = (Expression)ASTNode.copySubtree(forStatement.getAST(), ((ASTNode) it.next()));
            ExpressionStatement stmt = forStatement.getAST().newExpressionStatement(exp);
            replacementBlock.statements().add(stmt);
        }

        /* create an equivalent while loop with the same condition */
        WhileStatement whileStatement = forStatement.getAST().newWhileStatement();
        whileStatement.setExpression((Expression)ASTNode.copySubtree(forStatement.getAST(), forStatement.getExpression()));
        Block forStatementBody = (Block) ASTNode.copySubtree(forStatement.getAST(), forStatement.getBody());

        it = forStatement.updaters().iterator();

        /* for every variable update in the for loop declaration, create a new expression inside the while statement body */
        while (it.hasNext()) {
            Expression exp = (Expression)ASTNode.copySubtree(forStatement.getAST(), ((ASTNode) it.next()));
            ExpressionStatement stmt = forStatement.getAST().newExpressionStatement(exp);
            forStatementBody.statements().add(stmt);
        }

        /* assign this augmented body to the while statement */
        whileStatement.setBody(forStatementBody);

        /* add the while statement into the replacement block */
        replacementBlock.statements().add(whileStatement);

        /* replace the for statement with replacement block */
        ASTNode parentBlock = TranslationUtil.getContainingBlock(forStatement);
        ListRewrite listRewrite = rewriter.getListRewrite(parentBlock.getParent(), Block.STATEMENTS_PROPERTY);
        listRewrite.replace(forStatement, replacementBlock, null);

        return true;
    }

    /**********************************************************
     Converts do-while statement to equivalent while statement
     *********************************************************/
    @Override
    public boolean visit(DoStatement doStatement) {
        Block replacementBlock = doStatement.getAST().newBlock();

        /* create and equivalent while statement with the same condition */
        WhileStatement whileStatement = doStatement.getAST().newWhileStatement();
        whileStatement.setExpression((Expression) ASTNode.copySubtree(doStatement.getAST(), doStatement.getExpression()));

        Block doStatementBody = (Block) ASTNode.copySubtree(doStatement.getAST(), doStatement.getBody());
        Iterator it = doStatementBody.statements().iterator();

        /* copy every statement into the new block to ensure the body gets executed once */
        /* an iterator and statements().add() is used to prevent having extra braces surrounding this block */
        while (it.hasNext())
            replacementBlock.statements().add(ASTNode.copySubtree(doStatement.getAST(), (ASTNode)it.next()));

        /* assign the body to the while statement */
        whileStatement.setBody(doStatementBody);

        /* add the while statement to the block */
        replacementBlock.statements().add(whileStatement);

        /* replace the do statement with the new block */
        ASTNode parentBlock = TranslationUtil.getContainingBlock(doStatement);
        ListRewrite listRewrite = rewriter.getListRewrite(parentBlock.getParent(), Block.STATEMENTS_PROPERTY);
        listRewrite.replace(doStatement, replacementBlock, null);

        return true;
    }
}
