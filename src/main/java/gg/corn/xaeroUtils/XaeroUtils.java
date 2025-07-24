package gg.corn.xaeroUtils;

import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import net.kyori.adventure.text.Component;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.protocol.ProtocolManager;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSystemChatMessage;

public class XaeroUtils extends JavaPlugin implements Listener {

    private String mode;
    private boolean allowNether;
    private ProtocolManager protocolManager;

    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        //On Bukkit, calling this here is essential, hence the name "load"
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        FileConfiguration config = getConfig();
        mode = config.getString("mode", "full").toLowerCase();
        allowNether = config.getBoolean("allow-nether-cave-layers", false);

        if (!mode.equals("fairplay") && !mode.equals("none") && !mode.equals("full")) {
            getLogger().severe("Unknown minimap mode in config.yml: '" + mode + "'. " +
                    "Valid options: fairplay, none, full. Defaulting to 'full'.");
            mode = "full";
        }

        // Initialize PacketEvents
        PacketEvents.getAPI().init();
        protocolManager = PacketEvents.getAPI().getProtocolManager();

        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("XaeroUtils enabled with mode='" + mode + "', allow-nether-cave-layers=" + allowNether);
    }

    @Override
    public void onDisable() {
        //Terminate the instance (clean up process)
        PacketEvents.getAPI().terminate();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        long delay = 20L; // 1 second
        getServer().getScheduler().runTaskLater(this, () -> sendMinimapStatus(player), delay);
    }

    private void sendMinimapStatus(Player player) {
        switch (mode) {
            case "fairplay":
                sendSystemChat(player, "§f§a§i§r§x§a§e§r§o");
                if (allowNether) {
                    sendSystemChat(player, "§x§a§e§r§o§w§m§n§e§t§h§e§r§i§s§f§a§i§r");
                }
                break;
            case "none":
                sendSystemChat(player, "§n§o§m§i§n§i§m§a§p");
                break;
            case "full":
                sendSystemChat(player, "§r§e§s§e§t§x§a§e§r§o");
                break;
        }
    }

    private void sendSystemChat(Player player, String message) {
        Component comp = Component.text(message);
        WrapperPlayServerSystemChatMessage packet = new WrapperPlayServerSystemChatMessage(false, comp);
        Object channel = protocolManager.getChannel(player.getUniqueId());
        protocolManager.sendPacketSilently(channel, packet);
        getLogger().info("Sent system chat to " + player.getName() + ": " + message);
    }
}