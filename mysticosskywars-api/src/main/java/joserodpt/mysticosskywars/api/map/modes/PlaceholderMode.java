package joserodpt.mysticosskywars.api.map.modes;

/*
 *   _____            _  _____ _
 *  |  __ \          | |/ ____| |
 *  | |__) |___  __ _| | (___ | | ___   ___      ____ _ _ __ ___
 *  |  _  // _ \/ _` | |\___ \| |/ / | | \ \ /\ / / _` | '__/ __|
 *  | | \ \  __/ (_| | |____) |   <| |_| |\ V  V / (_| | |  \__ \
 *  |_|  \_\___|\__,_|_|_____/|_|\_\\__, | \_/\_/ \__,_|_|  |___/
 *                                   __/ |
 *                                  |___/
 *
 * Licensed under the MIT License
 * @author José Rodrigues © 2019-2025
 * @link https://github.com/joserodpt/MysticosSkywars
 */

import joserodpt.mysticosskywars.api.cages.MSWCage;
import joserodpt.mysticosskywars.api.config.TranslatableLine;
import joserodpt.mysticosskywars.api.map.MSWMap;
import joserodpt.mysticosskywars.api.map.modes.teams.MSWTeam;
import joserodpt.mysticosskywars.api.player.MSWPlayer;
import joserodpt.mysticosskywars.api.utils.Itens;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;

public class PlaceholderMode extends MSWMap {
    public PlaceholderMode(String nome) {
        super(nome);
    }

    @Override
    public String forceStart(MSWPlayer p) {
        return null;
    }

    @Override
    public boolean canStartMap() {
        return false;
    }

    @Override
    public void removePlayer(MSWPlayer p) {
    }

    @Override
    public void addPlayer(MSWPlayer gp) {
    }

    @Override
    public void resetArena(OperationReason rr) {
    }

    @Override
    public void checkWin() {
    }

    @Override
    public GameMode getGameMode() {
        return null;
    }

    @Override
    public Collection<MSWCage> getCages() {
        return null;
    }

    @Override
    public Collection<MSWTeam> getTeams() {
        return null;
    }

    @Override
    public int getMaxTeamsNumber() {
        return 0;
    }

    @Override
    public int getMaxTeamsMembers() {
        return 0;
    }

    @Override
    public int getMaxGameTime() {
        return 0;
    }

    @Override
    public void forceStartMap() {
    }

    @Override
    public int minimumPlayersToStartMap() {
        return 0;
    }

    @Override
    public void removeCage(Location loc) {
    }

    @Override
    public void addCage(Location location) {
    }

    @Override
    public ItemStack getIconForPlayer(MSWPlayer p) {
        return Itens.createItem(Material.DEAD_BUSH, 1, TranslatableLine.ITEM_MAP_NOTFOUND_NAME.get(p));
    }

    @Override
    public MSWMap duplicate(String newName) {
        return null;
    }
}
