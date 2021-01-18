package edu.ntnu.tobiasth.mineplot;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Objects;

class EventListener implements Listener {
    private final MinePlot mp;

    public EventListener(MinePlot plugin) {
        this.mp = plugin;
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if(mp.toolEnabled(player)) {
            Action playerAction = event.getAction();

            if(playerAction == Action.LEFT_CLICK_BLOCK) {
                Location currentLeft = mp.getLeftSelection(player);
                Location newLeft = Objects.isNull(event.getClickedBlock()) ? null : event.getClickedBlock().getLocation();
                if(!Objects.isNull(currentLeft) && currentLeft.equals(newLeft)) {
                    event.setCancelled(true);
                    return;
                }

                mp.setLeftSelection(player, Objects.requireNonNull(event.getClickedBlock()).getLocation());
                player.sendMessage(Message.SET_LEFT_SELECTION);
                event.setCancelled(true);
            }
            else if(playerAction == Action.RIGHT_CLICK_BLOCK) {
                Location currentRight = mp.getRightSelection(player);
                Location newRight = Objects.isNull(event.getClickedBlock()) ? null : event.getClickedBlock().getLocation();
                if(!Objects.isNull(currentRight) && currentRight.equals(newRight)) {
                    event.setCancelled(true);
                    return;
                }

                mp.setRightSelection(player, Objects.requireNonNull(event.getClickedBlock()).getLocation());
                player.sendMessage(Message.SET_RIGHT_SELECTION);
                event.setCancelled(true);
            }
        }
    }
}
