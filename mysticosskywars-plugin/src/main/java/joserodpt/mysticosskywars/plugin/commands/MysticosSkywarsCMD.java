package joserodpt.mysticosskywars.plugin.commands;

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

import dev.triumphteam.cmd.bukkit.annotation.Permission;
import dev.triumphteam.cmd.core.annotation.Command;
import dev.triumphteam.cmd.core.annotation.Default;
import dev.triumphteam.cmd.core.annotation.Optional;
import dev.triumphteam.cmd.core.annotation.SubCommand;
import dev.triumphteam.cmd.core.annotation.Suggestion;
import joserodpt.mysticosskywars.api.MysticosSkywarsAPI;
import joserodpt.mysticosskywars.api.chests.MSWChest;
import joserodpt.mysticosskywars.api.chests.TierViewer;
import joserodpt.mysticosskywars.api.config.MSWConfig;
import joserodpt.mysticosskywars.api.config.MSWLanguage;
import joserodpt.mysticosskywars.api.config.TranslatableLine;
import joserodpt.mysticosskywars.api.config.TranslatableList;
import joserodpt.mysticosskywars.api.kits.KitInventory;
import joserodpt.mysticosskywars.api.kits.MSWKit;
import joserodpt.mysticosskywars.api.managers.MapManagerAPI;
import joserodpt.mysticosskywars.api.managers.TransactionManager;
import joserodpt.mysticosskywars.api.managers.world.MSWWorld;
import joserodpt.mysticosskywars.api.map.MSWMap;
import joserodpt.mysticosskywars.api.player.MSWPlayer;
import joserodpt.mysticosskywars.api.shop.MSWBuyableItem;
import joserodpt.mysticosskywars.api.utils.ItemStackSpringer;
import joserodpt.mysticosskywars.api.utils.Text;
import joserodpt.mysticosskywars.api.utils.WorldEditUtils;
import joserodpt.mysticosskywars.plugin.gui.GUIManager;
import joserodpt.mysticosskywars.plugin.gui.guis.KitSettingsGUI;
import joserodpt.mysticosskywars.plugin.gui.guis.MapDashboardGUI;
import joserodpt.mysticosskywars.plugin.gui.guis.MapsListGUI;
import joserodpt.mysticosskywars.plugin.gui.guis.PlayerGUI;
import joserodpt.mysticosskywars.plugin.gui.guis.PlayerItemsGUI;
import joserodpt.mysticosskywars.plugin.gui.guis.SettingsGUI;
import joserodpt.mysticosskywars.plugin.gui.guis.ShopGUI;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

@Command(value = "mysticosskywars", alias = {"sw", "msw"})
public class MysticosSkywarsCMD extends BaseCommandWA {

    private final String onlyPlayer = "[MysticosSkywars] Only players can run this command.";
    public MysticosSkywarsAPI rs;

    public MysticosSkywarsCMD(MysticosSkywarsAPI rs) {
        this.rs = rs;
    }

    @Default
    @SuppressWarnings("unused")
    public void defaultCommand(final CommandSender commandSender) {
        if (commandSender instanceof Player) {
            MSWPlayer p = rs.getPlayerManagerAPI().getPlayer((Player) commandSender);
            if (p.getPlayer().isOp() || p.getPlayer().hasPermission("msw.admin")) {
                GUIManager.openPluginMenu(p, rs);
            } else {
                MapsListGUI v = new MapsListGUI(p);
                v.openInventory(p);
            }
        } else {
            Text.send(commandSender, "<white><bold>Mysticos<aqua>Skywars <gray>v<yellow>" + rs.getPlugin().getDescription().getVersion());
        }
    }

    @SubCommand(value = "reload", alias = "rl")
    @Permission("msw.admin")
    @SuppressWarnings("unused")
    public void reloadcmd(final CommandSender commandSender) {
        if (commandSender instanceof Player) {
            MSWPlayer p = rs.getPlayerManagerAPI().getPlayer((Player) commandSender);
            this.rs.reload();
            TranslatableLine.CMD_CONFIG_RELOAD.send(p, true);
        } else {
            this.rs.reload();
            commandSender.sendMessage("Reloaded MysticosSkywars configuration - version: " + rs.getPlugin().getDescription().getVersion());
            TranslatableLine.CMD_CONFIG_RELOAD.sendDefault(commandSender, true);
        }
    }

    @SubCommand("list")
    @Permission("msw.list")
    @SuppressWarnings("unused")
    public void listcmd(final CommandSender commandSender) {
        if (commandSender instanceof Player) {
            MSWPlayer p = rs.getPlayerManagerAPI().getPlayer((Player) commandSender);
            Collection<MSWMap> tmp = rs.getMapManagerAPI().getMaps(MapManagerAPI.MapGamemodes.ALL);
            p.sendMessage(TranslatableLine.CMD_MAPS.get(p).replace("%rooms%", "" + tmp.size()));
            for (MSWMap s : tmp) {
                TextComponent a = new TextComponent(Text.color("&7- &f" + s.getDisplayName()));
                a.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/msw map " + s.getName()));
                a.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Text.color("&fClick to open &b" + s.getDisplayName() + "&f settings!")).create()));
                p.getPlayer().spigot().sendMessage(a);
            }
        } else {
            commandSender.sendMessage(onlyPlayer);
        }
    }

    @SubCommand("kits")
    @Permission("msw.kits")
    @SuppressWarnings("unused")
    public void kitscmd(final CommandSender commandSender) {
        if (commandSender instanceof Player) {
            MSWPlayer p = rs.getPlayerManagerAPI().getPlayer((Player) commandSender);
            PlayerItemsGUI ds = new PlayerItemsGUI(p, MSWBuyableItem.ItemCategory.KIT);
            ds.openInventory(p);
        } else {
            commandSender.sendMessage(onlyPlayer);
        }
    }

    @SubCommand(value = "duplicate", alias = {"dup", "copy"})
    @Permission("msw.admin")
    @WrongUsage("&c/msw duplicate <map> <new_name>")
    @SuppressWarnings("unused")
    public void duplicatecmd(final CommandSender commandSender, @Suggestion("#maps") String originalName, String newName) {
        MSWMap original = rs.getMapManagerAPI().getMap(originalName);
        if (original == null) {
            TranslatableLine.CMD_NO_MAP_FOUND.sendDefault(commandSender, true);
            return;
        }
        if (rs.getMapManagerAPI().getMap(newName) != null) {
            Text.send(commandSender, TranslatableLine.MAP_EXISTS.getDefault());
            return;
        }
        try {
            rs.getMapManagerAPI().duplicateMap(original, newName);
            Text.send(commandSender, "&aMap duplicated with success!");
        } catch (Exception e) {
            Text.send(commandSender, "&cError duplicating map: " + e.getMessage());
        }
    }

    @SubCommand("kit")
    @Permission("msw.admin")
    @SuppressWarnings("unused")
    public void kitcmd(final CommandSender commandSender, KIT_OPERATION action, @Suggestion("#kits") String name, @Optional Double cost) {
        if (commandSender instanceof Player) {
            MSWPlayer p = rs.getPlayerManagerAPI().getPlayer((Player) commandSender);

            if (action == null) {
                p.sendMessage(rs.getLanguageManagerAPI().getPrefix() + "Unknown kit command syntax.");
                return;
            }
            switch (action) {
                case CREATE:
                    if (cost == null) {
                        p.sendMessage(rs.getLanguageManagerAPI().getPrefix() + "Cost value not accepted.");
                        return;
                    }

                    if (rs.getKitManagerAPI().getKit(name) == null) {
                        MSWKit k = new MSWKit(Text.strip(Text.color(name)), name, cost, new KitInventory(p.getPlayer().getInventory().getContents()));
                        KitSettingsGUI m = new KitSettingsGUI(k, p.getUUID());
                        m.openInventory(p);
                    } else {
                        TranslatableLine.KIT_EXISTS.send(p, true);
                    }
                    break;
                case DELETE:
                    MSWKit k2 = rs.getKitManagerAPI().getKit(name);
                    if (k2 != null) {
                        rs.getKitManagerAPI().unregisterKit(k2);
                        TranslatableLine.KIT_DELETE.send(p, true);
                    } else {
                        TranslatableLine.KIT_NOT_FOUND.send(p, true);
                    }
                    break;
                case GIVE:
                    MSWKit k3 = rs.getKitManagerAPI().getKit(name);
                    if (k3 != null) {
                        k3.give(p);
                        p.playSound(Sound.ENTITY_VILLAGER_YES, 50, 50);
                    } else {
                        TranslatableLine.KIT_NOT_FOUND.send(p, true);
                    }
                    break;
            }
        } else {
            commandSender.sendMessage(onlyPlayer);
        }
    }

    @SubCommand("shop")
    @Permission("msw.shop")
    @SuppressWarnings("unused")
    public void shopcmd(final CommandSender commandSender) {
        if (commandSender instanceof Player) {
            if (!MSWConfig.file().getBoolean("Config.Shops.Enable-Shop")) {
                return;
            }
            MSWPlayer p = rs.getPlayerManagerAPI().getPlayer((Player) commandSender);
            ShopGUI ss = new ShopGUI(p, MSWBuyableItem.ItemCategory.CAGE_BLOCK);
            ss.openInventory(p);
        } else {
            commandSender.sendMessage(onlyPlayer);
        }
    }

    @SubCommand(value = "spectatorshop", alias = "specshop")
    @Permission("msw.spectatorshop")
    @SuppressWarnings("unused")
    public void spectatorshopcmd(final CommandSender commandSender) {
        if (commandSender instanceof Player) {
            if (!MSWConfig.file().getBoolean("Config.Shops.Enable-Spectator-Shop")) {
                return;
            }

            MSWPlayer p = rs.getPlayerManagerAPI().getPlayer((Player) commandSender);
            ShopGUI ss = new ShopGUI(p, MSWBuyableItem.ItemCategory.SPEC_SHOP);
            ss.openInventory(p);
            p.getPlayer().playSound(p.getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 50, 50);
        } else {
            commandSender.sendMessage(onlyPlayer);
        }
    }

    @SubCommand("play")
    @SuppressWarnings("unused")
    public void playcmd(final CommandSender commandSender, MSWMap.GameMode type) {
        if (commandSender instanceof Player) {
            Player pobj = (Player) commandSender;
            if (MSWConfig.file().getBoolean("Config.Bungeecord.Enabled")) {
                pobj.kickPlayer(TranslatableLine.BUNGEECORD_KICK_MESSAGE.getSingle());
                return;
            }

            MSWPlayer p = rs.getPlayerManagerAPI().getPlayer(pobj);
            if (type != null && p != null && p.getPlayer() != null) {
                if (!(p.getState() == MSWPlayer.PlayerState.CAGE)) {
                    rs.getMapManagerAPI().findNextMap(p, type);
                } else {
                    TranslatableLine.CMD_ALREADY_IN_MATCH.send(p, true);
                }
            } else {
                TranslatableLine.CMD_NO_MAP_FOUND.send(p, true);
            }
        } else {
            commandSender.sendMessage(onlyPlayer);
        }
    }

    @SubCommand(value = "balance", alias = "bal")
    @Permission("msw.coins")
    @WrongUsage("&c/msw bal")
    @SuppressWarnings("unused")
    public void balancecmd(final CommandSender commandSender) {
        if (commandSender instanceof Player) {
            MSWPlayer p = rs.getPlayerManagerAPI().getPlayer((Player) commandSender);
            Text.send(p.getPlayer(), TranslatableLine.CMD_COINS.get(p).replace("%coins%", rs.getCurrencyAdapterAPI().getCoinsFormatted(p)));
        } else {
            commandSender.sendMessage(onlyPlayer);
        }
    }

    @SubCommand("coins")
    @Permission("msw.coins")
    @WrongUsage("&c/msw coins <send;add;set;remove> <name> <coins>")
    @SuppressWarnings("unused")
    public void coinscmd(final CommandSender commandSender, TransactionManager.Operations o, Player target, Double coins) {
        if (o == null) {
            Text.send(commandSender, MysticosSkywarsAPI.getInstance().getLanguageManagerAPI().getPrefix() + "Invalid operation type.");
            return;
        }

        MSWPlayer search = rs.getPlayerManagerAPI().getPlayer(target);

        if (commandSender instanceof Player) {
            MSWPlayer p = rs.getPlayerManagerAPI().getPlayer((Player) commandSender);

            if (search == null) {
                TranslatableLine.NO_PLAYER_FOUND.send(p, true);
                return;
            }

            new TransactionManager(search, p, coins, o, true);
        } else {
            if (search == null) {
                TranslatableLine.NO_PLAYER_FOUND.sendDefault(commandSender, true);
                return;
            }

            new TransactionManager(search, coins, o, true);
        }
    }

    @SubCommand("verifylang")
    @Permission("msw.admin")
    @SuppressWarnings("unused")
    public void verifylangcmd(final CommandSender commandSender) {
        Text.send(commandSender, "&aLanguage Verification Started.");

        for (MSWLanguage langObj : rs.getLanguageManagerAPI().getLanguages()) {
            String languageName = langObj.getKey();
            Text.send(commandSender, "&6Checking language &b" + languageName + "&6...");
            Text.send(commandSender, "&6Checking for missing translations in strings...");
            for (TranslatableLine value : TranslatableLine.values()) {
                if (value.getPath().startsWith(".")) {
                    String val = value.getInLanguage(languageName);
                    if (val.isEmpty()) {
                        Text.send(commandSender, "&cMissing translation for &9" + value.name() + " &cin language &b" + languageName);
                    }
                }
            }

            Text.send(commandSender, "&6Checking for missing translations in lists...");
            for (TranslatableList value : TranslatableList.values()) {
                List<String> tmp = value.getInLanguage(languageName);
                if (tmp == null || tmp.isEmpty()) {
                    Text.send(commandSender, "&cMissing translation for &9" + value.name() + " &cin language &b" + languageName);
                }
            }
        }

        Text.send(commandSender, "&fLanguage Verification Finished.");
    }

    @SubCommand("setspectator")
    @Permission("msw.admin")
    @SuppressWarnings("unused")
    public void setspectator(final CommandSender commandSender) {
        if (commandSender instanceof Player) {
            MSWPlayer p = rs.getPlayerManagerAPI().getPlayer((Player) commandSender);
            MSWMap sw = rs.getMapManagerAPI().getMap(p.getWorld());
            if (sw != null) {
                if (sw.isUnregistered()) {
                    sw.setSpectatorLocation(p.getLocation());
                    TranslatableLine.CMD_SPEC_SET.send(p, true);
                } else {
                    TranslatableLine.MAP_UNREGISTER_TO_EDIT.send(p, true);
                }
            } else {
                TranslatableLine.CMD_NO_MAP_FOUND.send(p, true);
            }
        } else {
            commandSender.sendMessage(onlyPlayer);
        }
    }

    @SubCommand(value = "settings", alias = "s")
    @Permission("msw.admin")
    @SuppressWarnings("unused")
    public void settings(final CommandSender commandSender) {
        if (commandSender instanceof Player) {
            MSWPlayer p = rs.getPlayerManagerAPI().getPlayer((Player) commandSender);
            SettingsGUI v = new SettingsGUI(p, rs);
            v.openInventory(p);
        } else {
            commandSender.sendMessage(onlyPlayer);
        }
    }

    @SubCommand("lobby")
    @Permission("msw.lobby")
    @SuppressWarnings("unused")
    public void lobbycmd(final CommandSender commandSender) {
        if (commandSender instanceof Player) {
            MSWPlayer p = rs.getPlayerManagerAPI().getPlayer((Player) commandSender);
            if (p.getMatch() == null) {
                rs.getLobbyManagerAPI().tpToLobby(p);
            } else {
                TranslatableLine.CMD_MATCH_CANCEL.send(p, true);
            }
        } else {
            commandSender.sendMessage(onlyPlayer);
        }
    }

    @SubCommand("forcestart")
    @Permission("msw.forcestart")
    @SuppressWarnings("unused")
    public void forcestartcmd(final CommandSender commandSender) {
        if (commandSender instanceof Player) {
            MSWPlayer p = rs.getPlayerManagerAPI().getPlayer(Bukkit.getPlayer(commandSender.getName()));
            if (p.isInMatch()) {
                p.sendMessage(p.getMatch().forceStart(p));
            } else {
                TranslatableLine.CMD_CNO_MATCH.send(p, true);
            }
        } else {
            commandSender.sendMessage(onlyPlayer);
        }
    }

    @SubCommand("addsharik")
    @Permission("msw.admin")
    @SuppressWarnings("unused")
    public void addsharik(final CommandSender commandSender) {
        if (commandSender instanceof Player) {
            MSWPlayer p = rs.getPlayerManagerAPI().getPlayer((Player) commandSender);
            if (p.isInMatch()) {
                p.getMatch().addPlayer(new MSWPlayer(true));
                p.sendMessage(Text.color("&6&lEXPERIMENTAL FEATURE. &eCAN RESULT IN SERVER & CLIENT CRASHES. &fAdded Null Player"));
            } else {
                TranslatableLine.CMD_CNO_MATCH.send(p, true);
            }
        } else {
            commandSender.sendMessage(onlyPlayer);
        }
    }

    @SubCommand("leave")
    @Permission("msw.leave")
    @SuppressWarnings("unused")
    public void leavecmd(final CommandSender commandSender) {
        if (commandSender instanceof Player) {
            MSWPlayer p = rs.getPlayerManagerAPI().getPlayer((Player) commandSender);
            if (p.isInMatch()) {
                p.getMatch().removePlayer(p);
            } else {
                TranslatableLine.CMD_CNO_MATCH.send(p, true);
            }
        } else {
            commandSender.sendMessage(onlyPlayer);
        }
    }

    @SubCommand("setlobby")
    @Permission("msw.admin")
    @SuppressWarnings("unused")
    public void setlobby(final CommandSender commandSender) {
        if (commandSender instanceof Player) {
            MSWPlayer p = rs.getPlayerManagerAPI().getPlayer((Player) commandSender);
            MSWConfig.file().set("Lobby.World", p.getLocation().getWorld().getName());
            MSWConfig.file().set("Lobby.X", p.getLocation().getX());
            MSWConfig.file().set("Lobby.Y", p.getLocation().getY());
            MSWConfig.file().set("Lobby.Z", p.getLocation().getZ());
            MSWConfig.file().set("Lobby.Yaw", p.getLocation().getYaw());
            MSWConfig.file().set("Lobby.Pitch", p.getLocation().getPitch());
            MSWConfig.save();
            rs.getLobbyManagerAPI().setLobbyLoc(p.getLocation());
            TranslatableLine.CMD_CLOBBY_SET.send(p, true);
        } else {
            commandSender.sendMessage(onlyPlayer);
        }
    }

    @SubCommand("finish")
    @Permission("msw.admin")
    @SuppressWarnings("unused")
    public void finishcmd(final CommandSender commandSender) {
        if (commandSender instanceof Player) {
            MSWPlayer p = rs.getPlayerManagerAPI().getPlayer((Player) commandSender);
            rs.getMapManagerAPI().finishMap(p);
        } else {
            commandSender.sendMessage(onlyPlayer);
        }
    }

    @SubCommand("maps")
    @SuppressWarnings("unused")
    public void mapscmd(final CommandSender commandSender) {
        if (commandSender instanceof Player) {
            MSWPlayer p = rs.getPlayerManagerAPI().getPlayer((Player) commandSender);
            if (p.getMatch() == null) {
                MapsListGUI v = new MapsListGUI(p);
                v.openInventory(p);
            } else {
                TranslatableLine.CMD_ALREADY_IN_MATCH.send(p, true);
            }
        } else {
            commandSender.sendMessage(onlyPlayer);
        }
    }

    @SubCommand("players")
    @Permission("msw.admin")
    @SuppressWarnings("unused")
    public void playerscmd(final CommandSender commandSender) {
        if (commandSender instanceof Player) {
            MSWPlayer p = rs.getPlayerManagerAPI().getPlayer((Player) commandSender);
            p.sendMessage(TranslatableLine.CMD_PLAYERS.get(p, true).replace("%players%", rs.getPlayerManagerAPI().getPlayers().size() + ""));
            for (MSWPlayer pair : rs.getPlayerManagerAPI().getPlayers()) {
                if (pair.getPlayer() != null) {
                    TextComponent a = new TextComponent(Text.color("&7- &f" + pair.getName()));
                    a.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/msw player " + pair.getName()));
                    a.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Text.color("&fClick here to inspect &b" + pair.getName())).create()));
                    p.getPlayer().spigot().sendMessage(a);
                }
            }
        } else {
            commandSender.sendMessage(onlyPlayer);
        }
    }

    @SubCommand("map")
    @Permission("msw.admin")
    @SuppressWarnings("unused")
    public void mapcmd(final CommandSender commandSender, @Suggestion("#maps") String name) {
        if (commandSender instanceof Player) {
            MSWPlayer p = rs.getPlayerManagerAPI().getPlayer((Player) commandSender);
            MSWMap sw = rs.getMapManagerAPI().getMap(name);
            if (sw != null) {
                MapDashboardGUI r = new MapDashboardGUI(sw, p.getUUID());
                r.openInventory(p);
            } else {
                TranslatableLine.CMD_NO_MAP_FOUND.send(p, true);
            }
        } else {
            commandSender.sendMessage(onlyPlayer);
        }
    }

    @SubCommand("tp")
    @Permission("msw.admin")
    @SuppressWarnings("unused")
    public void tpcmd(final CommandSender commandSender, @Suggestion("#maps") String name) {
        if (commandSender instanceof Player) {
            MSWPlayer p = rs.getPlayerManagerAPI().getPlayer((Player) commandSender);
            if (p != null) {
                p.getPlayer().setGameMode(org.bukkit.GameMode.CREATIVE);
                p.teleport(rs.getMapManagerAPI().getMap(name).getMSWWorld().getWorld().getSpawnLocation());
            }
        } else {
            commandSender.sendMessage(onlyPlayer);
        }
    }

    @SubCommand("set2chest")
    @Permission("msw.admin")
    @SuppressWarnings("unused")
    public void setchestcmd(final CommandSender commandSender, MSWChest.Tier tt, MSWChest.Type t) throws IOException {
        if (commandSender instanceof Player) {
            final Player p = (Player) commandSender;
            if (tt != null && t != null) {
                tt.set2Chest(t, Arrays.asList(IntStream.range(9, 35).boxed().map(p.getInventory()::getItem).filter(Objects::nonNull).toArray(ItemStack[]::new)));
                Text.send(commandSender, "Itens set for " + tt.name() + " (middle: " + t.name() + ")");
            } else {
                Text.send(commandSender, "Invalid chest tier and/or type.");
            }

            Text.send(commandSender, "Itens set for " + tt.name() + " (middle: " + t.name() + ")");
        } else {
            commandSender.sendMessage(onlyPlayer);
        }
    }

    @SubCommand("seetier")
    @Permission("msw.admin")
    @SuppressWarnings("unused")
    public void seetiercmd(final CommandSender commandSender, MSWChest.Tier tt, MSWChest.Type t) {
        if (commandSender instanceof Player) {
            Player p = (Player) commandSender;
            TierViewer tv = new TierViewer(rs.getPlayerManagerAPI().getPlayer(p), tt, t);
            tv.openInventory(p);
        } else {
            commandSender.sendMessage(onlyPlayer);
        }
    }

    @SubCommand("player")
    @Permission("msw.admin")
    @SuppressWarnings("unused")
    public void playercmd(final CommandSender commandSender, Player get) {
        if (commandSender instanceof Player) {
            MSWPlayer p = rs.getPlayerManagerAPI().getPlayer((Player) commandSender);
            MSWPlayer search = rs.getPlayerManagerAPI().getPlayer(get);
            if (search != null) {
                PlayerGUI playg = new PlayerGUI(rs.getPlayerManagerAPI().getPlayer(get), p.getUUID(), search);
                playg.openInventory(p);
            } else {
                TranslatableLine.NO_PLAYER_FOUND.send(p, true);
            }
        } else {
            commandSender.sendMessage(onlyPlayer);
        }
    }

    @SubCommand("create")
    @Permission("msw.admin")
    @WrongUsage("&c/msw create <name> <type> <players> or /msw create <name> <type> <number of teams> <players per team>")
    @SuppressWarnings("unused")
    public void createcmd(final CommandSender commandSender, @Suggestion("#createsuggestions") String mapname, @Suggestion("#worldtype") MSWWorld.WorldType wt, Integer maxPlayersandTeams, @Optional Integer teamPlayers) {
        if (commandSender instanceof Player) {
            MSWPlayer p = rs.getPlayerManagerAPI().getPlayer((Player) commandSender);

            if (wt == null) {
                Text.send(commandSender, rs.getLanguageManagerAPI().getPrefix() + "&cInvalid game generation type.");
                return;
            }

            if (wt.equals(MSWWorld.WorldType.SCHEMATIC) && !WorldEditUtils.schemFileExists(mapname)) {
                Text.send(commandSender, rs.getLanguageManagerAPI().getPrefix() + "&cNo " + mapname + "&c found in MysticosSkywars/maps. Did you forget to add .schem?");
                return;
            }

            if (MSWConfig.file().isSection("Lobby")) {
                MSWMap map = rs.getMapManagerAPI().getMap(mapname);
                if (map == null) {
                    if (teamPlayers == null) {
                        rs.getMapManagerAPI().setupSolo(p, Text.strip(mapname), mapname, wt, maxPlayersandTeams);
                    } else {
                        rs.getMapManagerAPI().setupTeams(p, Text.strip(mapname), mapname, wt, maxPlayersandTeams, teamPlayers);
                    }
                } else {
                    TranslatableLine.MAP_EXISTS.send(p, true);
                }
            } else {
                TranslatableLine.LOBBY_NOT_SET.send(p, true);
            }
        } else {
            commandSender.sendMessage(onlyPlayer);
        }
    }

    @SubCommand(value = "unregister", alias = "unreg")
    @Permission("msw.admin")
    @WrongUsage("&c/msw unregister <map>")
    @SuppressWarnings("unused")
    public void unregistercmd(final CommandSender commandSender, @Suggestion("#maps") String mapName) {
        MSWMap map = rs.getMapManagerAPI().getMap(mapName);
        if (map != null) {
            if (map.isUnregistered()) {
                TranslatableLine.MAP_ALREADY_UNREGISTERED.sendDefault(commandSender, true);
                return;
            }

            map.setUnregistered(true);
            TranslatableLine.MAP_UNREGISTERED.sendDefault(commandSender, true);
        } else {
            TranslatableLine.CMD_NO_MAP_FOUND.sendDefault(commandSender, true);
        }
    }

    @SubCommand(value = "register", alias = "ref")
    @Permission("msw.admin")
    @WrongUsage("&c/msw register <map>")
    @SuppressWarnings("unused")
    public void registercmd(final CommandSender commandSender, @Suggestion("#maps") String mapName) {
        MSWMap map = rs.getMapManagerAPI().getMap(mapName);
        if (map != null) {
            if (!map.isUnregistered()) {
                TranslatableLine.MAP_ALREADY_REGISTERED.sendDefault(commandSender, true);
                return;
            }

            map.setUnregistered(false);
            TranslatableLine.MAP_COMMAND_REGISTERED.sendDefault(commandSender, true);
        } else {
            TranslatableLine.CMD_NO_MAP_FOUND.sendDefault(commandSender, true);
        }
    }

    @SubCommand(value = "editmap", alias = "edit")
    @Permission("msw.admin")
    @WrongUsage("&c/msw edit <map>")
    @SuppressWarnings("unused")
    public void editmapcmd(final CommandSender commandSender, @Suggestion("#maps") String mapName) {
        if (commandSender instanceof Player) {
            MSWPlayer p = rs.getPlayerManagerAPI().getPlayer((Player) commandSender);
            MSWMap sw = rs.getMapManagerAPI().getMap(mapName);
            if (sw != null) {
                rs.getMapManagerAPI().editMap(p, sw);
            } else {
                TranslatableLine.CMD_NO_MAP_FOUND.send(p, true);
            }
        } else {
            commandSender.sendMessage(onlyPlayer);
        }
    }

    @SubCommand(value = "delete", alias = {"del", "cancel"})
    @Permission("msw.admin")
    @WrongUsage("&c/msw delete <map>")
    @SuppressWarnings("unused")
    public void deletecmd(final CommandSender commandSender, @Suggestion("#maps") String mapName) {
        MSWMap map = rs.getMapManagerAPI().getMap(mapName);
        if (map != null) {
            rs.getMapManagerAPI().deleteMap(map);
            TranslatableLine.MAP_DELETED.sendDefault(commandSender, true);
        } else {
            TranslatableLine.CMD_NO_MAP_FOUND.sendDefault(commandSender, true);
        }
    }

    @SubCommand(value = "rename", alias = "ren")
    @Permission("msw.admin")
    @WrongUsage("&c/msw rename <map>")
    @SuppressWarnings("unused")
    public void renamecmd(final CommandSender commandSender, @Suggestion("#maps") final String mapName, final String displayName) {
        MSWMap map = rs.getMapManagerAPI().getMap(mapName);
        if (map != null) {
            map.setDisplayName(displayName);
            map.save(MSWMap.Data.SETTINGS, true);
            TranslatableLine.MAP_RENAMED.sendDefault(commandSender, true);
        } else {
            TranslatableLine.CMD_NO_MAP_FOUND.sendDefault(commandSender, true);
        }
    }

    @SubCommand("reset")
    @Permission("msw.admin")
    @WrongUsage("&c/msw reset <map>")
    @SuppressWarnings("unused")
    public void resetcmd(final CommandSender commandSender, @Suggestion("#maps") String mapSTR) {
        MSWPlayer p = rs.getPlayerManagerAPI().getPlayer((Player) commandSender);
        MSWMap map = rs.getMapManagerAPI().getMap(mapSTR);
        if (map != null) {
            TranslatableLine.ARENA_RESET.send(p, true);
            map.reset();
            TranslatableLine.MAP_RESET_DONE.send(p, true);
        } else {
            TranslatableLine.CMD_NO_MAP_FOUND.send(p, true);
        }
    }

    @SubCommand("refreshLeaderboards")
    @Permission("msw.admin")
    @SuppressWarnings("unused")
    public void refreshLeaderboardscmd(final CommandSender commandSender) {
        rs.getLeaderboardManagerAPI().refreshLeaderboards();
        Text.send(commandSender, "Leaderboards Refreshed.");
    }

    @SubCommand("item2config")
    @Permission("msw.admin")
    @WrongUsage("&c/msw item2config")
    @SuppressWarnings("unused")
    public void item2configcmd(final CommandSender commandSender) {
        if (commandSender instanceof Player) {
            Player p = (Player) commandSender;
            MSWConfig.file().set("Config.Item", ItemStackSpringer.getItemSerialized(p.getInventory().getItemInMainHand()));
            MSWConfig.save();
        } else {
            commandSender.sendMessage(onlyPlayer);
        }
    }

    public enum KIT_OPERATION {CREATE, DELETE, GIVE}
}
