package edu.ntnu.tobiasth.mineplot;

public abstract class Message {
    private Message() {}

    public static final String VERSION_INFO = "This server runs MinePlot v. 1.1.1 by tobiasth.";

    public static final String UNKNOWN_COMMAND = "Unknown command. Please check your syntax.";
    public static final String SENDER_NOT_PLAYER = "This command can only be used by players.";

    public static final String CANVAS_ADD_SUCCESS = "Successfully added the new canvas.";
    public static final String CANVAS_CLEAR_SUCCESS = "Successfully cleared the canvas.";
    public static final String CANVAS_REMOVE_SUCCESS = "Successfully removed the canvas.";
    public static final String CANVAS_EXISTS = "A canvas with that name already exists.";
    public static final String NO_CANVASES = "There are no canvases to display.";
    public static final String POINT_OUTSIDE_CANVAS = "The given point was outside the canvas limits.";

    public static final String PLOT_ADD_SUCCESS = "Successfully added the new plot.";
    public static final String PLOT_REMOVE_SUCCESS = "Successfully removed the plot.";
    public static final String PLOT_EXISTS = "A plot with that name already exists on the given canvas.";
    public static final String NO_PLOTS = "There are no plots to display.";

    public static final String TOGGLE_ON = "Toggled on the coordinate selection tool.";
    public static final String TOGGLE_OFF = "Toggled off the coordinate selection tool.";
    public static final String SET_LEFT_SELECTION = "Set the left block selection.";
    public static final String SET_RIGHT_SELECTION = "Set the right block selection.";
    public static final String NO_SELECTION = "Could not find a selection, use /mp tool to define one.";

    public static final String INVALID_ARGUMENT_COUNT = "Wrong number of arguments for this command.";
    public static final String INVALID_VALUE_RANGE = "The given value range is not valid.";
    public static final String INVALID_NUMBER = "One of the given numbers is not valid.";
    public static final String INVALID_EXPRESSION = "The given expression is not valid.";
    public static final String INVALID_MATERIAL = "The given material does not exist.";
    public static final String INVALID_PLOT_NAME = "A plot with that name does not exist on the given canvas.";
    public static final String INVALID_CANVAS_NAME = "A canvas with that name does not exist.";
    public static final String INVALID_CANVAS_DIMENSIONS = "The given coordinates are not valid. The canvas must be one thick.";

    public static String CANVAS_LIST(String playerName) { return String.format("Canvases for player '%s':", playerName); }
    public static String PLOT_LIST(String canvasName) { return String.format("Plots for canvas '%s':", canvasName); }
    public static String TAB(String message) { return String.format("    %s", message); }
}