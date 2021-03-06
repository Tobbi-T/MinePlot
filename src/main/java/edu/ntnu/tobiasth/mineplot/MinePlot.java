package edu.ntnu.tobiasth.mineplot;

import edu.ntnu.tobiasth.mineplot.canvas.Canvas;
import edu.ntnu.tobiasth.mineplot.canvas.ValueRange;
import edu.ntnu.tobiasth.mineplot.plot.Function;
import edu.ntnu.tobiasth.mineplot.plot.Plot;
import edu.ntnu.tobiasth.mineplot.plot.Point;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@SuppressWarnings("unused")
public class MinePlot extends JavaPlugin {
    private final HashMap<UUID, HashMap<String, Canvas>> canvases = new HashMap<>();
    private final HashMap<UUID, LocationSelection> locationSelections = new HashMap<>();
    private final ArrayList<UUID> activeTools = new ArrayList<>();

    /**
     * Built-in method that is called by the server when server is enabled.
     */
    @Override
    public void onEnable() {
        //Register the event listeners from the EventListener class.
        Bukkit.getPluginManager().registerEvents(new EventListener(this), this);
    }

    /**
     * Built-in method that is called by the server when a command sender issues a command.
     * @param sender Command sender. Not always a player.
     * @param command Command.
     * @param label Command label.
     * @param args Command arguments.
     * @return True if successful, false if not.
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command command, @NotNull String label, @NotNull String[] args) {
        if(sender instanceof Player) {
            try {
                handleCommand((Player) sender, args);
            }
            catch(Exception e) {
                sender.sendMessage(e.getMessage());
            }
        }
        else {
            sender.sendMessage(Message.SENDER_NOT_PLAYER);
        }

        return true;
    }

    /**
     * Handle a player issued command.
     * @param sender Player that sent the command.
     * @param args Command argument iterator.
     */
    private void handleCommand(@NotNull Player sender, @NotNull String[] args) {
        //Show version info on empty command.
        if(args.length == 0) {
            sender.sendMessage(Message.VERSION_INFO);
            return;
        }

        //Find the issued command.
        @NotNull String[] finalArgs = args;
        @Nullable Command command;
        try {
            command = (Command) Arrays.stream(Command.values()).filter(cmd -> {
                String[] identifierWords = cmd.getIdentifierWords();
                Iterator<String> sentWords = Arrays.stream(finalArgs).iterator();

                for(String word : identifierWords) {
                    if(!sentWords.hasNext() || !word.equalsIgnoreCase(sentWords.next())) {
                        return false;
                    }
                }

                return true;
            }).toArray()[0];
        }
        //If no command matched the user command throw an exception.
        catch(ArrayIndexOutOfBoundsException e) {
            sender.sendMessage(Message.UNKNOWN_COMMAND);
            return;
        }

        //Remove command words from args array.
        args = Arrays.copyOfRange(args, command.getIdentifierWords().length, args.length);

        //Execute the command.
        switch(command) {
            case CANVAS_ADD: {
                checkArgumentCount(args, 6);
                canvasAdd(sender, Arrays.stream(args).iterator());
                return;
            }
            case CANVAS_CLEAR: {
                checkArgumentCount(args, 1);
                canvasClear(sender, Arrays.stream(args).iterator());
                return;
            }
            case CANVAS_REMOVE: {
                checkArgumentCount(args, 1);
                canvasRemove(sender, Arrays.stream(args).iterator());
                return;
            }
            case CANVAS_LIST: {
                canvasList(sender);
                return;
            }
            case PLOT_ADD_FUNCTION: {
                checkArgumentCount(args, 5);
                plotAddFunction(sender, Arrays.stream(args).iterator());
                return;
            }
            case PLOT_ADD_POINT: {
                checkArgumentCount(args, 5);
                plotAddPoint(sender, Arrays.stream(args).iterator());
                return;
            }
            case PLOT_REMOVE: {
                checkArgumentCount(args, 2);
                plotRemove(sender, Arrays.stream(args).iterator());
                return;
            }
            case PLOT_LIST: {
                checkArgumentCount(args, 1);
                plotList(sender, Arrays.stream(args).iterator());
                return;
            }
            case TOOL: {
                toggleTool(sender);
                return;
            }
            case HELP: {
                //Redirect help comment to built-in help comment.
                StringBuilder helpCommand = new StringBuilder().append("help mp");
                Arrays.stream(args).forEach(arg -> helpCommand.append(" ").append(arg));
                sender.performCommand(helpCommand.toString());
            }
        }
    }

    /**
     * Add a new canvas. Takes 6 arguments.
     * @param sender Player that sent the command.
     * @param args Command arguments.
     */
    private void canvasAdd(Player sender, Iterator<String> args) {
        String name = args.next();

        double minValueX = parseDouble(args.next());
        double maxValueX = parseDouble(args.next());
        double minValueY = parseDouble(args.next());
        double maxValueY = parseDouble(args.next());
        ValueRange valueRange = new ValueRange(minValueX, maxValueX, minValueY, maxValueY);

        LocationSelection locations = locationSelections.get(sender.getUniqueId());
        if(Objects.isNull(locations) || Objects.isNull(locations.getLeft()) || Objects.isNull(locations.getRight()))
            throw new IllegalArgumentException(Message.NO_SELECTION);

        Material material = getMaterial(args.next());

        Canvas canvas = new Canvas(name, valueRange, locations.getLeft(), locations.getRight(), material);

        //Add canvas hashmap if user does not have one.
        canvases.putIfAbsent(sender.getUniqueId(), new HashMap<>());

        if(canvases.get(sender.getUniqueId()).containsKey(name))
            throw new IllegalArgumentException(Message.CANVAS_EXISTS);

        //Save and build canvas if it does not already exist.
        canvases.get(sender.getUniqueId()).put(name, canvas);
        canvas.build();

        sender.sendMessage(Message.CANVAS_ADD_SUCCESS);
    }

    /**
     * Clear a canvas. Takes 1 argument.
     * @param sender Player that sent the command.
     * @param args Command arguments.
     */
    private void canvasClear(Player sender, Iterator<String> args) {
        Canvas canvas = getCanvas(sender.getUniqueId(), args.next());
        canvas.clear();
        sender.sendMessage(Message.CANVAS_CLEAR_SUCCESS);
    }

    /**
     * Removes a canvas. Takes 1 arguments.
     * @param sender Player who sent the command.
     * @param args Command arguments.
     */
    private void canvasRemove(@NotNull Player sender, @NotNull Iterator<String> args) {
        String name = args.next();
        Canvas canvas = getCanvas(sender.getUniqueId(), name);

        //Remove blocks.
        canvas.destroy();

        //Remove canvas from list.
        canvases.get(sender.getUniqueId()).remove(name);

        sender.sendMessage(Message.CANVAS_REMOVE_SUCCESS);
    }

    /**
     * Lists all sender canvases. Takes no arguments.
     * @param sender Player who sent the command.
     */
    private void canvasList(@NotNull Player sender) {
        sender.sendMessage(Message.CANVAS_LIST(sender.getDisplayName()));

        //If sender has no canvases
        if(!canvases.containsKey(sender.getUniqueId()) || canvases.get(sender.getUniqueId()).isEmpty()) {
            sender.sendMessage(Message.TAB(Message.NO_CANVASES));
            return;
        }

        //List canvases
        for(@NotNull Canvas canvas : canvases.get(sender.getUniqueId()).values()) {
            sender.sendMessage(Message.TAB(canvas.toString()));
        }
    }

    /**
     * Add a new function plot. Takes 5 arguments.
     * @param sender Player who sent the command.
     * @param args Command arguments.
     */
    private void plotAddFunction(@NotNull Player sender, @NotNull Iterator<String> args) {
        @NotNull String name = args.next();
        @NotNull String expression = args.next();
        char variable = args.next().toCharArray()[0];
        @NotNull Canvas canvas = getCanvas(sender.getUniqueId(), args.next());
        @NotNull Material material = getMaterial(args.next());

        if(Arrays.stream(canvas.getPlots()).anyMatch(plot -> name.equals(plot.getName())))
            throw new IllegalArgumentException(Message.PLOT_EXISTS);

        @NotNull Plot plot = new Function(name, material, expression, variable);

        canvas.addPlot(plot);
        plot.draw(canvas);

        sender.sendMessage(Message.PLOT_ADD_SUCCESS);
    }

    /**
     * Add a new point plot. Takes 5 arguments.
     * @param sender Player who sent the command.
     * @param args Command arguments.
     */
    private void plotAddPoint(@NotNull Player sender, @NotNull Iterator<String> args) {
        String name = args.next();
        double x = parseDouble(args.next());
        double y = parseDouble(args.next());

        @NotNull Canvas canvas = getCanvas(sender.getUniqueId(), args.next());
        @NotNull Material material = getMaterial(args.next());

        if(Arrays.stream(canvas.getPlots()).anyMatch(plot -> name.equals(plot.getName())))
            throw new IllegalArgumentException(Message.PLOT_EXISTS);

        @NotNull Plot plot = new Point(name, material, x, y);
        canvas.addPlot(plot);
        plot.draw(canvas);

        sender.sendMessage(Message.PLOT_ADD_SUCCESS);
    }

    /**
     * Removes a plot from a canvas. Takes 2 arguments.
     * @param sender Player who sent the command.
     * @param args Command arguments.
     */
    private void plotRemove(@NotNull Player sender, @NotNull Iterator<String> args) {
        Canvas canvas = getCanvas(sender.getUniqueId(), args.next());
        String name = args.next();

        //Get the correct plot if it exists.
        Plot plot = getPlot(canvas, name);

        //Remove blocks.
        plot.destroy(canvas);

        //Remove canvas from list.
        canvas.removePlot(plot);

        //Redraw all other plots in case of an overlap.
        canvas.drawPlots();

        sender.sendMessage(Message.PLOT_REMOVE_SUCCESS);
    }

    /**
     * Lists all canvas plots. Takes 1 arguments.
     * @param sender Player who sent the command.
     * @param args Command arguments.
     */
    private void plotList(@NotNull Player sender, @NotNull Iterator<String> args) {
        Canvas canvas = getCanvas(sender.getUniqueId(), args.next());
        sender.sendMessage(Message.PLOT_LIST(canvas.getName()));

        //If canvas has no plots
        if(canvas.getPlots().length == 0) {
            sender.sendMessage(Message.TAB(Message.NO_PLOTS));
            return;
        }

        for(Plot plot : canvas.getPlots()) {
            sender.sendMessage(Message.TAB(plot.toString()));
        }
    }

    /**
     * Activates or deactivates a players coordinate selection tool. Takes no arguments.
     * @param sender Player who sent the command.
     */
    private void toggleTool(Player sender) {
        UUID id = sender.getUniqueId();

        if(activeTools.contains(id)) {
            activeTools.remove(id);
            sender.sendMessage(Message.TOGGLE_OFF);
        }
        else {
            activeTools.add(id);
            sender.sendMessage(Message.TOGGLE_ON);
        }
    }

    /**
     * Return a players tool status.
     * @param player Player to check status for.
     * @return True if tool enabled, false if disabled.
     */
    protected boolean toolEnabled(Player player) {
        return activeTools.contains(player.getUniqueId());
    }

    /**
     * Set a players left selection.
     * @param player Player to change the selection for.
     * @param location Location to change the selection to.
     */
    protected void setLeftSelection(Player player, Location location) {
        locationSelections.putIfAbsent(player.getUniqueId(), new LocationSelection());
        locationSelections.get(player.getUniqueId()).setLeft(location);
    }

    /**
     * Set a players right selection.
     * @param player Player to change the selection for.
     * @param location Location to change the selection to.
     */
    protected void setRightSelection(Player player, Location location) {
        locationSelections.putIfAbsent(player.getUniqueId(), new LocationSelection());
        locationSelections.get(player.getUniqueId()).setRight(location);
    }

    /**
     * Get a players right location.
     * @param player Player to get the selection for.
     * @return Current right location.
     */
    protected Location getRightSelection(Player player) {
        LocationSelection ls = locationSelections.get(player.getUniqueId());
        return Objects.isNull(ls) ? null : ls.getRight();
    }

    /**
     * Get a players left location.
     * @param player Player to get the selection for.
     * @return Current left location.
     */
    protected Location getLeftSelection(Player player) {
        LocationSelection ls = locationSelections.get(player.getUniqueId());
        return Objects.isNull(ls) ? null : ls.getLeft();
    }

    /**
     * Checks if the number of arguments is incorrect.
     * @param args Argument list
     * @param minArgs Minimum number of arguments.
     * @throws IllegalArgumentException If the argument count is incorrect.
     */
    private void checkArgumentCount(String[] args, int minArgs) throws IllegalArgumentException {
        if(args.length < minArgs)
            throw new IllegalArgumentException(Message.INVALID_ARGUMENT_COUNT);
    }

    /**
     * Parses a double from the given string, and throws an exception if the number is invalid.
     * @param number Number to be parsed.
     * @return Double.
     * @throws IllegalArgumentException If the number format is not correct.
     */
    private double parseDouble(String number) throws IllegalArgumentException {
        try {
            return Double.parseDouble(number);
        }
        catch(NumberFormatException e) {
            throw new IllegalArgumentException(Message.INVALID_NUMBER);
        }
    }

    /**
     * Gets the given material, and is case insensitive.
     * @param name Material name.
     * @return Material.
     * @throws IllegalArgumentException If the material name is invalid.
     */
    private @NotNull Material getMaterial(@NotNull String name) throws IllegalArgumentException {
        @Nullable Material material = Material.getMaterial(name.toUpperCase());
        if(Objects.isNull(material))
            throw new IllegalArgumentException(Message.INVALID_MATERIAL);

        return material;
    }

    /**
     * Gets the given canvas.
     * @param playerID Player UUID.
     * @param name Canvas name.
     * @return Canvas.
     * @throws IllegalArgumentException If the canvas name is invalid.
     */
    private @NotNull Canvas getCanvas(UUID playerID, String name) throws IllegalArgumentException {
        @Nullable Canvas canvas = canvases.get(playerID).get(name);
        if(Objects.isNull(canvas))
            throw new IllegalArgumentException(Message.INVALID_CANVAS_NAME);

        return canvas;
    }

    /**
     * Gets the given plot.
     * @param canvas Parent canvas.
     * @param name Plot name.
     * @return Plot
     * @throws IllegalArgumentException If the plot name is invalid.
     */
    private @NotNull Plot getPlot(Canvas canvas, String name) throws IllegalArgumentException {
        Plot result = (Plot) Arrays.stream(canvas.getPlots()).filter(plot -> name.equals(plot.getName())).toArray()[0];

        if(Objects.isNull(result))
            throw new IllegalArgumentException(Message.INVALID_PLOT_NAME);

        return result;
    }
}