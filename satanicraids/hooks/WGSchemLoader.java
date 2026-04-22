package org.mrdarkimc.satanicraids.hooks;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import org.bukkit.Bukkit;
import org.mrdarkimc.SatanicLib.objectManager.interfaces.Reloadable;
import org.mrdarkimc.satanicraids.SatanicRaids;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
@Deprecated
public class WGSchemLoader implements Reloadable {
    public WGSchemLoader() {
        //loadSchematicsToCache();
        Reloadable.register(this);
    }

    public static File schemFolder = new File(SatanicRaids.getInstance().getDataFolder() + "/schems/");
    public static Map<String, Clipboard> clipboardMap = new HashMap<>(); //эта мапа содержит name + .schem
    public void loadSchematicsToCache(){
        clipboardMap.clear();
        if (!schemFolder.exists()) {
            Bukkit.getLogger().warning(" ");
            Bukkit.getLogger().warning("[WGSchemLoader] Папки со схемой не обнаружено!");
            Bukkit.getLogger().warning("[WGSchemLoader] Создаю папку!");

            try {
                schemFolder.mkdirs();
            } catch (Exception e) {
                Bukkit.getLogger().severe("[WGSchemLoader] Ошибка при создании папки: " + e.getMessage());
                throw new RuntimeException(e);
            }
            Bukkit.getLogger().warning("[WGSchemLoader] Успешно");
            Bukkit.getLogger().warning("[WGSchemLoader] Положите схемы в папку и /et reload");
            Bukkit.getLogger().warning(" ");
            return;
        }
        if (!schemFolder.isDirectory()) {
            Bukkit.getLogger().warning("[WGSchemLoader] Путь должен быть /plugins/SatanicRaids/schems/");
            return;
        }

        File[] schemlist = schemFolder.listFiles();
        if (schemlist == null || schemlist.length == 0) {
            Bukkit.getLogger().warning("[WGSchemLoader] Папка со схемами пуста. Ничего не было загружено в кэш");
            return;
        }


        //File[] schemlist = this.schemFolder.listFiles();
        for (File schem : schemlist) {
            Bukkit.getLogger().info("[WGSchemLoader] Загружаю в кэш схему: " + schem.getName());
            ClipboardFormat format = ClipboardFormats.findByFile(schem);
            try (ClipboardReader reader = format.getReader(new FileInputStream(schem))) {
                clipboardMap.put(schem.getName(), reader.read());
                Bukkit.getLogger().info("[WGSchemLoader] " + schem.getName() + " загружена в кэш.");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void reload() {
        loadSchematicsToCache();
    }
}
