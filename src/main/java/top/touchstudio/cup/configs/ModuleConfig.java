package top.touchstudio.cup.configs;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import top.touchstudio.cup.CommonUsePlugin;
import top.touchstudio.cup.modules.ModuleManager;

import java.io.File;
import java.io.IOException;

/**
 * @Autho BaicaijunOvO
 * @Github https://github.com/BaicaijunOvO
 * @Date 2024-06 下午9:28
 * @Tips XuFang is Gay!
 */
public class ModuleConfig {
    public static File moduleConfigFile;
    public static YamlConfiguration moduleConfig;
    public static ConfigurationSection modulesSection;

    public void onServerStart(CommonUsePlugin plugin) throws IOException, InvalidConfigurationException {
        moduleConfigFile = new File(plugin.getDataFolder(), "ModuleConfig.yml");
        moduleConfig = new YamlConfiguration();
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        if (!moduleConfigFile.exists()) {
            moduleConfigFile.createNewFile();
        }

        moduleConfig.load(moduleConfigFile);

        if (!moduleConfig.contains("modules")) {
            moduleConfig.createSection("modules");
        }
        modulesSection = moduleConfig.getConfigurationSection("modules");

        // 添加新模块的默认配置
        for (String name : ModuleManager.ModuleList) {
            if (!modulesSection.contains(name + ".IsEnable")) {
                modulesSection.set(name + ".IsEnable", true);
            }
            ModuleManager.ModuleMap.put(name, modulesSection.getBoolean(name + ".IsEnable"));
        }

        // 清理已移除的模块配置
        for (String key : modulesSection.getKeys(false)) {
            if (!ModuleManager.ModuleList.contains(key)) {
                modulesSection.set(key, null);
            }
        }

        // home 特殊配置
        if (!modulesSection.contains("home.MaxHome")) {
            modulesSection.set("home.MaxHome", 5);
        }

        moduleConfig.save(moduleConfigFile);
    }

    public void onServerDisable() throws IOException {
        ModuleManager.ModuleMap.forEach((name, isenable) -> {
            moduleConfig.set("modules." + name + ".IsEnable",isenable);
        });
            moduleConfig.save(moduleConfigFile);
    }

    public void reloadConfig(){
        CommonUsePlugin.instance.getServer().reload();
    }
}
