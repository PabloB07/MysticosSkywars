package joserodpt.mysticosskywars.api.party;

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

import joserodpt.mysticosskywars.api.config.TranslatableLine;
import joserodpt.mysticosskywars.api.player.MSWPlayer;

import java.util.ArrayList;
import java.util.List;

public class MSWParty {

    private final MSWPlayer owner;
    private final List<MSWPlayer> members = new ArrayList<>();
    private boolean allowJoin;

    public MSWParty(MSWPlayer owner) {
        this.owner = owner;
    }

    public void playerJoin(MSWPlayer p) {
        p.joinParty(this.owner);
        this.members.add(p);

        this.owner.sendMessage(TranslatableLine.PARTY_JOIN.get(this.owner).replace("%player%", p.getDisplayName()));
        this.members.forEach(mswPlayer -> mswPlayer.sendMessage(TranslatableLine.PARTY_JOIN.get(mswPlayer).replace("%player%", p.getDisplayName())));
    }

    public void playerLeave(MSWPlayer p) {
        this.owner.sendMessage(TranslatableLine.PARTY_LEAVE.get(this.owner).replace("%player%", p.getDisplayName()));
        this.members.forEach(mswPlayer -> mswPlayer.sendMessage(TranslatableLine.PARTY_LEAVE.get(mswPlayer).replace("%player%", p.getDisplayName())));
    }

    public void kick(MSWPlayer p) {
        this.members.remove(p);
        this.owner.sendMessage(TranslatableLine.PARTY_KICK.get(this.owner).replace("%player%", p.getDisplayName()));
        this.members.forEach(mswPlayer -> mswPlayer.sendMessage(TranslatableLine.PARTY_KICK.get(mswPlayer).replace("%player%", p.getDisplayName())));
    }

    public void disband() {
        this.members.forEach(mswPlayer -> mswPlayer.sendMessage(TranslatableLine.PARTY_DISBAND.get(mswPlayer).replace("%player%", this.owner.getDisplayName())));
        this.members.forEach(MSWPlayer::leaveParty);
        this.members.clear();
        this.owner.sendMessage(TranslatableLine.PARTY_DISBAND.get(this.owner).replace("%player%", this.owner.getDisplayName()));
        this.owner.leaveParty();
    }

    public boolean isOwner(MSWPlayer p) {
        return this.owner == p;
    }

    public List<MSWPlayer> getMembers() {
        return this.members;
    }

    public void setAllowJoin(boolean b) {
        this.allowJoin = b;
    }

    public boolean allowJoin() {
        return this.allowJoin;
    }

    public void sendMessage(MSWPlayer p, String s) {
        this.owner.sendMessage("&3[PARTY] " + p.getDisplayName() + " - " + s);
        this.members.forEach(mswPlayer -> mswPlayer.sendMessage("&3[PARTY] " + p.getDisplayName() + " - " + s));
    }
}
