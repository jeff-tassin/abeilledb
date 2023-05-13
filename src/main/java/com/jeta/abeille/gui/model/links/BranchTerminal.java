package com.jeta.abeille.gui.model.links;

import java.awt.*;

/**
 * The small branch icon drawn at the link destination
 */
public class BranchTerminal extends Terminal {
    private static final int HEIGHT = 6;
    private static final int WIDTH = 8;

    public BranchTerminal() {
        setBounds(0, 0, WIDTH, HEIGHT);
    }

    /**
     * Renders the arrow on the graphics context
     */
    public void draw(Graphics g) {
        Point pt = getTerminalPoint();
        Direction d = getDirection();
        int height = getHeight();
        int width = getWidth();

        g.drawLine(pt.x, pt.y - height/2 - 1, pt.x + d.getSign() * width, pt.y );
        g.drawLine(pt.x, pt.y + height/2 + 1, pt.x + d.getSign() * width, pt.y );
    }

    /**
     * Sets the terminal point for this object
     */
    public void setTerminalPoint(int x, int y, Direction d) {
        super.setTerminalPoint(x, y, d);
        resetTerminal(x, y, getWidth(), getHeight(), d);
    }

    private void resetTerminal(int x, int y, int width, int height, Direction d) {
        if (d.equals(Direction.EAST)) {
            setBounds(x, y - height / 2, width, height);
        } else {
            setBounds(x - width, y - height / 2, width, height);
        }
    }

    /**
     * Sets the maximum height for this terminal. This is used mainly when the
     * user changes the font size in the model view. We would like the terminal
     * sizes to be somewhat proportional to the font size.
     */
    public void setMaximumHeight(int height) {
        super.setMaximumHeight(height);
        Point pt = getTerminalPoint();
        height = height * 5 / 10;
        if (height < 2)
            height = 2;

        int width = height * 3 / 2;
        width = width - width % 2;

        resetTerminal(pt.x, pt.y, width, height, getDirection());
    }
}
