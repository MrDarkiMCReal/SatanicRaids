package org.mrdarkimc.satanicraids.utils;

import com.sk89q.worldedit.extension.platform.AbstractNonPlayerActor;
import com.sk89q.worldedit.session.SessionKey;
import com.sk89q.worldedit.util.auth.AuthorizationException;
import com.sk89q.worldedit.util.formatting.text.Component;

import java.util.Locale;
import java.util.UUID;

public class CustomActor extends AbstractNonPlayerActor {
    @Override
    public String getName() {
        return null;
    }

    @Override
    public void printRaw(String msg) {

    }

    @Override
    public void printDebug(String msg) {

    }

    @Override
    public void print(String msg) {

    }

    @Override
    public void printError(String msg) {

    }

    @Override
    public void print(Component component) {

    }

    @Override
    public Locale getLocale() {
        return null;
    }

    @Override
    public SessionKey getSessionKey() {
        return null;
    }

    @Override
    public UUID getUniqueId() {
        return null;
    }

    @Override
    public String[] getGroups() {
        return new String[0];
    }

    @Override
    public void checkPermission(String permission) throws AuthorizationException {

    }

    @Override
    public boolean hasPermission(String permission) {
        return false;
    }

    @Override
    public void setPermission(String permission, boolean value) {

    }
}
