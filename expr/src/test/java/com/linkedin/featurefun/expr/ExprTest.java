package com.linkedin.featurefun.expr;

import org.testng.Assert;
import org.testng.annotations.Test;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingFormatArgumentException;

/**
 *
 * Unit test for math expression parsing, evaluating and printing
 *
 * Created by Leo Tang <litang@linkedin.com> on 11/13/14.
 */
public class ExprTest {
    @Test
    public void simple() {
        Assert.assertTrue(Expression.evaluate("(!= 2 3)") == 1);
        Assert.assertTrue(Expression.evaluate("(* 2 3)") == 6);

        Assert.assertTrue(Expression.evaluate("(ln1plus 2)") == Math.log(1 + 2));

        Assert.assertTrue(Expression.evaluate("(if (>= 4 5) 1 2)") == 2);

        Assert.assertTrue(Expression.evaluate("(if (in 3 4 5) 2 1)") == 1);

        Assert.assertTrue(Expression.evaluate("(- 1)") == -1);

        double r = Expression.evaluate("(rand-int 5 10)");
        Assert.assertTrue(r >= 5 && r <= 10);

        Assert.assertTrue(Expression.evaluate("(+ 0.5 (* (/ 15 1000) (ln (- 55 12))))") == 0.5 + 15.0 / 1000.0 * Math.log(55.0 - 12.0));

        //big boss, time to show power
        Assert.assertTrue(Expression.evaluate("(* (if (&& (== 0 12) (&& 3 (&& (&& (>= 4 5) (<= 4 6)) (&& (>= 7 5 ) (<= 7 4))))) 0 (if (&& (== 3 0) (<= 55 3)) 0 (if (&& (== 3 0) (|| (|| (&& 2 (&& 3 1) ) 1) (&& 6 3))) 0 (if (<= 55 12) (/ (* 0.5 55) 12)(+ 0.5 (*(/ 15 1000) (ln (- 55 12)))))))) 1000)") == 1000 * (0.5 + 15.0 / 1000.0 * Math.log(55.0 - 12.0)));
    }

    @Test
    public void variables(){
        VariableRegistry variableRegistry=new VariableRegistry();

        Expr expression = Expression.parse("(sigmoid (+ (* a x) b))",variableRegistry);

        Variable x = variableRegistry.findVariable("x");
        Variable a = variableRegistry.findVariable("a");
        Variable b = variableRegistry.findVariable("b");

        x.setValue(1);
        a.setValue(2);
        b.setValue(3);

        Assert.assertTrue(expression.evaluate() == 1.0/(1+Math.exp(-a.getValue() * x.getValue() - b.getValue() )) );


        x.setValue(4);
        Assert.assertTrue(x.getValue()==4);
        a.setValue(5);
        Assert.assertTrue(a.getValue() == 5);
        b.setValue(6);
        Assert.assertTrue(b.getValue() == 6);

        Assert.assertTrue(expression.evaluate() == 1.0/(1+Math.exp(-a.getValue() * x.getValue() - b.getValue() )) );


        Map<String,Double> varMap = new HashMap<String,Double>();

        varMap.put("x",0.2);
        varMap.put("a",0.6);
        varMap.put("b",0.8);

        variableRegistry.refresh(varMap);

        Assert.assertTrue(x.getValue()==0.2);
        Assert.assertTrue(a.getValue()==0.6);
        Assert.assertTrue(b.getValue()==0.8);


        Assert.assertTrue(expression.evaluate() == 1.0 / (1 + Math.exp(-a.getValue() * x.getValue() - b.getValue())));

    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void operatorNotSupported() {
        Expression.evaluate("(atan 1)");   //triangle functions are not supported due to no foreseeable use cases
    }

    @Test(expectedExceptions = MissingFormatArgumentException.class)
    public void operantNotSupported() {
        Expression.evaluate("(+ 1+1)");
    }

    @Test
    public void testPrettyPrint(){
        VariableRegistry variableRegistry=new VariableRegistry();

        Expr expr =  Expression.parse("(+ 0.5 (* (/ 15 1000) (ln (- 55 12))))", variableRegistry);

        Assert.assertEquals(expr.toString(), "(0.5+((15.0/1000.0)*ln((55.0-12.0))))");

        Assert.assertEquals(Expression.prettyTree(expr),    "└── +\n" +
                                                            "    ├── 0.5\n" +
                                                            "    └── *\n" +
                                                            "        ├── /\n" +
                                                            "        |   ├── 15.0\n" +
                                                            "        |   └── 1000.0\n" +
                                                            "        └── ln\n" +
                                                            "            └── -\n" +
                                                            "                ├── 55.0\n" +
                                                            "                └── 12.0\n");

    }
}
