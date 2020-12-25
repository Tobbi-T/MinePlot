package edu.ntnu.tobiasth.mineplot;

public enum Command {
    CANVAS_ADD("canvas add"),
    CANVAS_CLEAR("canvas clear"),
    CANVAS_REMOVE("canvas remove"),
    CANVAS_LIST("canvas list"),
    PLOT_ADD_FUNCTION("plot add function"),
    PLOT_ADD_POINT("plot add point"),
    PLOT_REMOVE("plot remove"),
    PLOT_LIST("plot list"),
    TOOL("tool"),
    HELP("help");

    private final String identifier;

    Command(String identifier) {
        this.identifier = identifier;
    }

    public String[] getIdentifierWords() {
        return identifier.split(" ");
    }
}