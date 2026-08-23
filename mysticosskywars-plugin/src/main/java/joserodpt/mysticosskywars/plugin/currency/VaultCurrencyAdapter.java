package joserodpt.mysticosskywars.plugin.currency;

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


import joserodpt.mysticosskywars.api.MysticosSkywarsAPI;
import joserodpt.mysticosskywars.api.currency.CurrencyAdapterAPI;
import joserodpt.mysticosskywars.api.player.MSWPlayer;

public class VaultCurrencyAdapter implements CurrencyAdapterAPI {

    @Override
    public void transferCoins(MSWPlayer toPlayer, MSWPlayer fromPlayer, double amount) {
        removeCoins(fromPlayer, amount);
        addCoins(toPlayer, amount);
    }

    @Override
    public void addCoins(MSWPlayer p, double amount) {
        MysticosSkywarsAPI.getInstance().getVaultEconomy().depositPlayer(p.getPlayer(), amount);
    }

    @Override
    public boolean removeCoins(MSWPlayer p, double amount) {
        return MysticosSkywarsAPI.getInstance().getVaultEconomy().withdrawPlayer(p.getPlayer(), amount).transactionSuccess();
    }

    @Override
    public void setCoins(MSWPlayer p, double amount) {
        MysticosSkywarsAPI.getInstance().getVaultEconomy().withdrawPlayer(p.getPlayer(), getCoins(p));
        MysticosSkywarsAPI.getInstance().getVaultEconomy().depositPlayer(p.getPlayer(), amount);
    }

    @Override
    public double getCoins(MSWPlayer p) {
        return MysticosSkywarsAPI.getInstance().getVaultEconomy().getBalance(p.getPlayer());
    }

    @Override
    public String getCoinsFormatted(MSWPlayer p) {
        return MysticosSkywarsAPI.getInstance().getVaultEconomy().format(getCoins(p));
    }
}
