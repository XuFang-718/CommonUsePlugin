package top.touchstudio.cup;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.plugin.java.JavaPlugin;
import top.touchstudio.cup.configs.ModuleConfig;
import top.touchstudio.cup.configs.PlayerConfig;
import top.touchstudio.cup.modules.ModuleManager;
import top.touchstudio.cup.modules.money.MoneyDatabase;

import java.io.IOException;

/**
 * @Autho TouchStudio
 * @Github https://github.com/TouchStudio
 * @Date 2024-07-30 22:00
 */

public final class CommonUsePlugin extends JavaPlugin {
    public static CommonUsePlugin instance;

    @Override
    public void onEnable() {

        instance = this;
        ModuleManager moduleManager = new ModuleManager();
        moduleManager.onServerStart(this);

        PlayerConfig playerConfig = new PlayerConfig();
        try {
            playerConfig.onServerStart(this);
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }

        // 初始化 Money SQLite 数据库
        MoneyDatabase.getInstance().init();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        ModuleConfig moduleConfig = new ModuleConfig();
        try {
            moduleConfig.onServerDisable();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // 关闭 Money 数据库连接
        MoneyDatabase.getInstance().close();
    }

}