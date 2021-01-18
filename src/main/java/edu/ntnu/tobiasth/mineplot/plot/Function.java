package edu.ntnu.tobiasth.mineplot.plot;

import edu.ntnu.tobiasth.mineplot.Message;
import edu.ntnu.tobiasth.mineplot.canvas.Canvas;
import org.bukkit.Material;

public class Function extends Plot {
    private final Expression expression;

    public Function(String name, Material material, String expression, char variable) {
        super(name, material);
        this.expression = new Expression(expression, variable);
    }

    @Override
    public void draw(Canvas canvas) throws IllegalArgumentException {
        draw(canvas, this.material);
    }

    @Override
    public void destroy(Canvas canvas) {
        draw(canvas, canvas.getMaterial());
    }

    private void draw(Canvas canvas, Material material) throws IllegalArgumentException {
        double startValue = canvas.getValueRange().getMinX();
        double endValue = canvas.getValueRange().getMaxX();
        double step = (endValue - startValue) / canvas.getBlocksX();

        for(double x = startValue; x < endValue; x += step) {
            try {
                double roundX = Math.round(x * 1e5) / 1e5;
                canvas.drawValue(roundX, expression.getValue(roundX), material);
            }
            catch(IllegalArgumentException e) { /* This happens if one of the points in a function is outside the graph. */ }
            catch(Expression.MalformedExpressionException e) {
                throw new IllegalArgumentException(Message.INVALID_EXPRESSION);
            }
        }
    }

    @Override
    public String toString() {
        return String.format("Function '%s' (%s) plotting '%s'.", name, material.toString().toLowerCase(), expression.toString());
    }
}