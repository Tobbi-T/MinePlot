package edu.ntnu.tobiasth.mineplot.plot;

import java.util.Arrays;

/**
 * Class Expression:
 * Lets the user define and get values from string math functions.
 */
public class Expression {
    private final String exp;
    private final char variable;
    private static final boolean DEBUG = true;

    /**
     * Define a new math expression.

     * @param exp Math expression
     * @param variable Variable the expression is dependant on
     */
    public Expression(String exp, char variable) {
        this.exp = exp;
        this.variable = variable;
    }

    /**
     * Return the math expression as a string.
     *
     * @return Math expression
     */
    @Override
    public String toString() {
        return exp;
    }

    /**
     * Get the expression value for a specific value of the variable.
     *
     * @param variableValue Value to insert instead of the variable
     * @return Expression value
     * @throws MalformedExpressionException If the expression is malformed
     */
    public double getValue(double variableValue) throws MalformedExpressionException {
        try {
            String expression = this.exp.replaceAll(String.valueOf(variable), String.valueOf(variableValue));
            return getExpressionValue(expression);
        }
        catch(Exception e) {
            throw new MalformedExpressionException(e.getMessage());
        }
    }

    /**
     * Calculate an expression with just numbers and operators.
     *
     * @param expression Math expression
     * @return Result of calculations
     */
    private double getExpressionValue(String expression) {
        //Insert function value and replace and remove + and -.
        expression = removeUnnecessaryPlusSigns(expression);

        if(DEBUG)
            System.out.printf("Getting function value for %s%n", expression);

        //Replace all functions with their correct values.
        for(Function function : Function.values()) {
            while(expression.contains(function.getSymbol())) {
                //Get expression from inside the function's parentheses.
                int indexOfParentheses = expression.indexOf(function.getSymbol()) + function.getSymbol().length();
                String expressionInsideFunction = getExpressionFromParenthesesAtIndex(expression, indexOfParentheses);

                //Run function recursively to determine value inside parentheses.
                double expressionValue = getExpressionValue(expressionInsideFunction);

                //Run the current function on the value inside of its parentheses.
                double functionResult = function.use(expressionValue);

                //Replace the whole function with it's value.
                expression = expression.replace(String.format("%s(%s)", function.getSymbol(), expressionInsideFunction), String.valueOf(functionResult));

                if(DEBUG)
                    System.out.printf("Replaced function %s(%s) with %s%n", function.getSymbol(), expressionValue, functionResult);
            }
        }

        //Replace all parentheses with their correct values.
        while(expression.contains("(")) {
            String expressionFromParentheses = getExpressionFromParenthesesAtIndex(expression, expression.indexOf("("));

            //Run function recursively to determine value inside parentheses.
            double expressionValue = getExpressionValue(expressionFromParentheses);

            //Replace the whole parentheses with it's value.
            expression = expression.replace(String.format("(%s)", expressionFromParentheses), String.valueOf(expressionValue));

            if(DEBUG)
                System.out.printf("Replaced (%s) with %s", expressionFromParentheses, expressionValue);
        }

        //Replace minuses that have numbers of both sides with minus operator. Leave the others.
        expression = replaceMinusSignsWithOperator(expression);

        //Replace all operators with their correct values.
        for(Operator operator : Operator.values()) {
            while(expression.contains(operator.getSymbol())) {
                //Get operator index in expression.
                int operatorIndex = expression.indexOf(operator.getSymbol());

                //Get numbers before and after operator.
                String numBefore = getFirstDoubleBeforeIndex(expression, operatorIndex);
                String numAfter = getFirstDoubleAfterIndex(expression, operatorIndex);

                //Get the result of the operator
                double result = operator.use(Double.parseDouble(numBefore), Double.parseDouble(numAfter));

                //Replace the operator and the numbers with the value.
                expression = expression.replace(numBefore + operator.getSymbol() + numAfter, String.valueOf(result));

                if(DEBUG)
                    System.out.printf("Replaced %s with %s%n", numBefore + operator.getSymbol() + numAfter, result);
            }
        }

        //Replace double minus with nothing / plus.
        expression = expression.replace("--", "");

        if(DEBUG)
            System.out.printf("Returning value %s%n", expression);

        return Double.parseDouble(expression);
    }

    /**
     * Remove all plus signs that are not used as an operator (number in front and behind).
     *
     * @param expression Expression with unnecessary plus signs
     * @return Expression without unnecessary plus signs
     */
    private String removeUnnecessaryPlusSigns(String expression) {
        String[] chars = expression.split("");
        for(int i = 0; i < chars.length; i++) {
            boolean isPlus = "+".equals(chars[i]);
            boolean isFirst = (i == 0);
            boolean isLast = (i == chars.length - 1);

            if(isPlus) {
                if(isFirst || isLast)
                    chars[i] = "";
                else {
                    String allowed = "1234567890";
                    boolean hasDigitBefore = allowed.contains(chars[i - 1]);
                    boolean hasDigitAfter = allowed.contains(chars[i + 1]);

                    if(!(hasDigitBefore && hasDigitAfter)) {
                        chars[i] = "";
                    }
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        Arrays.stream(chars).forEach(sb::append);
        return sb.toString();
    }

    /**
     * Replace all minus signs that are used as operators (number in front and behind) with the minus operator.
     *
     * The minus operator has another codepoint than the dash. This distinction is made to make it easier
     * to see a minus sign means there is a negative number coming, and when is means subtract.
     *
     *
     * @param expression Expression with dash minuses
     * @return Expression with dash minuses and operator minuses
     */
    private String replaceMinusSignsWithOperator(String expression) {
        String[] chars = expression.split("");
        for(int i = 0; i < chars.length; i++) {
            boolean isMinus = "-".equals(chars[i]);
            boolean isFirst = (i == 0);
            boolean isLast = (i == chars.length - 1);

            if(isMinus) {
                if(isLast)
                    chars[i] = "";
                else if(!isFirst) {
                    String allowed = "1234567890";
                    boolean hasDigitBefore = allowed.contains(chars[i - 1]);
                    boolean hasDigitAfter = allowed.contains(chars[i + 1]);

                    if(hasDigitBefore && hasDigitAfter) {
                        chars[i] = "−"; //This is not a normal dash!
                    }
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        Arrays.stream(chars).forEach(sb::append);
        return sb.toString();
    }

    /**
     * Get the expression from inside the parentheses at the given index.
     *
     * @param expression Expression to get the parentheses expression from
     * @param index Index in expression to get parentheses expression from
     * @return Expression from inside parentheses
     */
    private String getExpressionFromParenthesesAtIndex(String expression, int index) {
        if(expression.charAt(index) != '(')
            throw new IllegalArgumentException("The given index does not match an opening parentheses.");

        int openParentheses = 1;
        for(int i = index; i < expression.length(); i++) {
            char current = expression.charAt(i);

            if(current == '(')
                openParentheses++;
            else if(current == ')')
                openParentheses--;

            if(openParentheses == 1 && current == ')') {
                return expression.substring(index + 1, i);
            }
        }

        throw new IllegalArgumentException("The parentheses of the given expression do not match.");
    }

    /**
     * Get the first double in front of the given index.
     *
     * This function skips the index and counts backwards until it hits a character
     * that does not in "0123456789.-".
     * The reason this function returns a string, is to make sure the number will still match
     * when it is supposed to be replaced.
     *
     * @param expression Expression to get double from
     * @param index Index to get double in front of.
     * @return String version of the resulting number.
     */
    private String getFirstDoubleBeforeIndex(String expression, int index) {
        char[] chars = expression.toCharArray();
        int startIndex = index - 1;

        String allowed = "0123456789.-";
        while(startIndex >= 0 && (allowed.contains(String.valueOf(chars[startIndex]))))
            startIndex--;

        return expression.substring(startIndex + 1, index);
    }

    /**
     * Get the first double after the given index.
     *
     * This function skips the index and counts forwards until it hits a character
     * that does not in "0123456789.-".
     * The reason this function returns a string, is to make sure the number will still match
     * when it is supposed to be replaced.
     *
     * @param expression Expression to get double from
     * @param index Index to get double behind.
     * @return String version of the resulting number.
     */
    private String getFirstDoubleAfterIndex(String expression, int index) {
        char[] chars = expression.toCharArray();
        int endIndex = index + 1;

        String allowed = "0123456789.-";
        while(endIndex < expression.length() && allowed.contains(String.valueOf(chars[endIndex])))
            endIndex++;

        return expression.substring(index + 1, endIndex);
    }

    /**
     * Enum Function:
     * Contains all supported math functions, and their order of execution.
     */
    enum Function {
        SQRT,
        CBRT,
        EXP,
        LOG10,
        LOG,
        ASIN,
        ACOS,
        ATAN,
        SIN,
        COS,
        TAN;

        /**
         * Get the text symbol for the function.
         *
         * @return Text symbol
         */
        public String getSymbol() {
            return this.name().toLowerCase();
        }

        /**
         * Use the math function on a number.
         *
         * @param a Number to perform the function on.
         * @return Result of the function
         */
        public double use(double a) {
            switch(this) {
                case SQRT: return Math.sqrt(a);
                case CBRT: return Math.cbrt(a);
                case EXP: return Math.exp(a);
                case LOG: return Math.log(a);
                case LOG10: return Math.log10(a);
                case SIN: return Math.sin(a);
                case COS: return Math.cos(a);
                case TAN: return Math.tan(a);
                case ASIN: return Math.asin(a);
                case ACOS: return Math.acos(a);
                case ATAN: return Math.atan(a);
                default: return 0;
            }
        }
    }

    /**
     * Enum Operator:
     * Contains all supported math operators, and their order of execution.
     */
    enum Operator {
        RAISE("^"),
        MULTIPLY("*"),
        DIVIDE("/"),
        ADD("+"),
        SUBTRACT("−"); //This is not a normal dash!

        private final String symbol;

        /**
         * Define a new operator.
         *
         * @param symbol Text symbol used for the operation.
         */
        Operator(String symbol) {
            this.symbol = symbol;
        }

        /**
         * Get the text symbol for the Operator.
         *
         * @return Text symbol
         */
        public String getSymbol() {
            return symbol;
        }

        /**
         * Use the math operation on two numbers.
         *
         * @param a First number to perform the operation on.
         * @param b Second number to perform operation on.
         * @return Result of the operation
         */
        public double use(double a, double b) {
            switch(this) {
                case RAISE: return Math.pow(a, b);
                case MULTIPLY: return a * b;
                case DIVIDE: return a / b;
                case ADD: return a + b;
                case SUBTRACT: return a - b;
                default: return 0;
            }
        }
    }

    /**
     * Class MalformedExpressionException:
     * Exception thrown if operation can't be performed because of a malformed expression.
     */
    public static class MalformedExpressionException extends Exception {
        public MalformedExpressionException(String message) {
            super(message);
        }
    }
}