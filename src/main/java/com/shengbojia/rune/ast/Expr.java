package com.shengbojia.rune.ast;

import com.shengbojia.rune.token.Token;

import java.util.List;

public abstract class Expr {

    public interface Visitor<R> {

        R visitAssignExpr(Assign expr);

        R visitBinaryExpr(Binary expr);

        R visitCallExpr(Call expr);

        R visitTernaryExpr(Ternary expr);

        R visitGroupingExpr(Grouping expr);

        R visitGetExpr(Get expr);

        R visitLiteralExpr(Literal expr);

        R visitLogicalExpr(Logical expr);

        R visitSetExpr(Set expr);

        R visitThisExpr(This expr);

        R visitUnaryExpr(Unary expr);

        R visitLambdaExpr(Lambda expr);

        R visitVariableExpr(Variable expr);
    }

    public static class Assign extends Expr {

        public Assign(Token name, Expr value) {
            this.name = name;
            this.value = value;
        }

        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitAssignExpr(this);
        }

        public final Token name;
        public final Expr value;
    }

    public static class Binary extends Expr {

        public Binary(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitBinaryExpr(this);
        }

        public final Expr left;
        public final Token operator;
        public final Expr right;
    }

    public static class Call extends Expr {

        public Call(Expr callee, Token paren, List<Expr> arguments) {
            this.callee = callee;
            this.paren = paren;
            this.arguments = arguments;
        }

        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitCallExpr(this);
        }

        public final Expr callee;
        public final Token paren;
        public final List<Expr> arguments;
    }

    public static class Ternary extends Expr {

        public Ternary(Expr left, Token firstOp, Expr middle, Token secondOp, Expr right) {
            this.left = left;
            this.firstOp = firstOp;
            this.middle = middle;
            this.secondOp = secondOp;
            this.right = right;
        }

        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitTernaryExpr(this);
        }

        public final Expr left;
        public final Token firstOp;
        public final Expr middle;
        public final Token secondOp;
        public final Expr right;
    }

    public static class Grouping extends Expr {

        public Grouping(Expr expression) {
            this.expression = expression;
        }

        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitGroupingExpr(this);
        }

        public final Expr expression;
    }

    public static class Get extends Expr {

        public Get(Expr object, Token name) {
            this.object = object;
            this.name = name;
        }

        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitGetExpr(this);
        }

        public final Expr object;
        public final Token name;
    }

    public static class Literal extends Expr {

        public Literal(Object value) {
            this.value = value;
        }

        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitLiteralExpr(this);
        }

        public final Object value;
    }

    public static class Logical extends Expr {

        public Logical(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitLogicalExpr(this);
        }

        public final Expr left;
        public final Token operator;
        public final Expr right;
    }

    public static class Set extends Expr {

        public Set(Expr object, Token name, Expr value) {
            this.object = object;
            this.name = name;
            this.value = value;
        }

        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitSetExpr(this);
        }

        public final Expr object;
        public final Token name;
        public final Expr value;
    }

    public static class This extends Expr {

        public This(Token keyword) {
            this.keyword = keyword;
        }

        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitThisExpr(this);
        }

        public final Token keyword;
    }

    public static class Unary extends Expr {

        public Unary(Token operator, Expr right) {
            this.operator = operator;
            this.right = right;
        }

        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitUnaryExpr(this);
        }

        public final Token operator;
        public final Expr right;
    }

    public static class Lambda extends Expr {

        public Lambda(List<Token> params, List<Stmt> body) {
            this.params = params;
            this.body = body;
        }

        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitLambdaExpr(this);
        }

        public final List<Token> params;
        public final List<Stmt> body;
    }

    public static class Variable extends Expr {

        public Variable(Token name) {
            this.name = name;
        }

        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitVariableExpr(this);
        }

        public final Token name;
    }

    public abstract <R> R accept(Visitor<R> visitor);
}
