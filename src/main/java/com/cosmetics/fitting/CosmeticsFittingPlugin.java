package com.cosmetics.fitting;

import com.cosmetics.fitting.listeners.FittingRoomListener;
import com.cosmetics.fitting.listeners.PlayerConnectionListener;
import com.cosmetics.fitting.managers.CosmeticsManager;
import com.cosmetics.fitting.managers.FittingRoomManager;
import com.cosmetics.fitting.managers.MirrorManager;
import com.cosmetics.fitting.commands.FittingCommand;
import com.cosmetics.fitting.commands.FittingRoomAdminCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class CosmeticsFittingPlugin extends JavaPlugin {

    private static CosmeticsFittingPlugin instance;

    private CosmeticsManager cosmeticsManager;
    private FittingRoomManager fittingRoomManager;
    private MirrorManager mirrorManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        if (getServer().getPluginManager().getPlugin("ItemsAdder") == null) {
            getLogger().severe("ItemsAdder לא נמצא! מכבה את הפלאגין.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        if (getServer().getPluginManager().getPlugin("ProtocolLib") == null) {
            getLogger().severe("ProtocolLib לא נמצא! מכבה את הפלאגין.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.cosmeticsManager = new CosmeticsManager(this);
        this.fittingRoomManager = new FittingRoomManager(this);
        this.mirrorManager = new MirrorManager(this);

        getServer().getPluginManager().registerEvents(new FittingRoomListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerConnectionListener(this), this);

        getCommand("fitting").setExecutor(new FittingCommand(this));
        getCommand("fittingroom").setExecutor(new FittingRoomAdminCommand(this));

        mirrorManager.startTickTask();

        getLogger().info("CosmeticsFitting הופעל בהצלחה!");
    }

    @Override
    public void onDisable() {
        if (mirrorManager != null) {
            mirrorManager.removeAllMirrors();
        }
    }

    public static CosmeticsFittingPlugin getInstance() {
        return instance;
    }

    public CosmeticsManager getCosmeticsManager() {
        return cosmeticsManager;
    }

    public FittingRoomManager getFittingRoomManager() {
        return fittingRoomManager;
    }

    public MirrorManager getMirrorManager() {
        return mirrorManager;
    }
}
