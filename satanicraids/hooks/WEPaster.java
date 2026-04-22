package org.mrdarkimc.satanicraids.hooks;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.mask.BlockTypeMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.mask.Masks;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.block.BlockType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;
import org.mrdarkimc.satanicraids.SatanicRaids;

import java.util.ArrayList;
import java.util.List;

public class WEPaster {
    private final List<EditSession> sessions = new ArrayList<>();

    public void undoAll() {
        sessions.forEach(s -> s.undo(s));
    }

    public WEPaster(Clipboard clipboard, int pasteActiveTime) {
        this.clipboard = clipboard;
        this.pasteActiveTime = pasteActiveTime;
    }

    private Clipboard clipboard;
    private BlockType[] globalContents = null; // поле инициализируется в классе Deserealizer
    int pasteActiveTime = 120;
    public void pasteAndRemove(Location loc) {

        double x = loc.getX();
        double y = loc.getY();
        double z = loc.getZ();

        com.sk89q.worldedit.world.World adapted = BukkitAdapter.adapt(loc.getWorld());
        try (EditSession editSession = WorldEdit.getInstance().newEditSession(adapted)) {

            //BlockType[] type = globalContents;
            //BlockTypeMask mask = new BlockTypeMask(editSession, type);
            //Mask reversed = Masks.negate(mask);

            //editSession.setMask(reversed);
            Operation operation = new ClipboardHolder(clipboard)
                    .createPaste(editSession)
                    .to(BlockVector3.at(x, y, z))
                    .ignoreAirBlocks(false)
                    .build();
            Operations.complete(operation);
            editSession.close();
            sessions.add(editSession);

            new BukkitRunnable() {
                @Override
                public void run() {
                    editSession.undo(editSession);
                    sessions.remove(editSession);
                }
            }.runTaskLater(SatanicRaids.getInstance(), pasteActiveTime * 20L);
        } catch (WorldEditException e) {
            Bukkit.getLogger().warning("WE exeption: location: " + loc);
            throw new RuntimeException(e);
        }
        Regions.createTimedProtectedRegion(loc, pasteActiveTime, 6);
    }
}
