package com.demigodsrpg.demigods.engine.helper;

public abstract class CensoredCentralizedClass {
    protected abstract int loadWorlds();

    protected abstract void loadListeners();

    protected abstract void loadCommands();

    protected abstract void loadPermissions(boolean load);
}
