package net.t00thpick1.residence.utils.uuid;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import net.t00thpick1.residence.Residence;
import net.t00thpick1.residence.api.ResidenceAPI;

import org.bukkit.scheduler.BukkitRunnable;

public class UUIDFetcherRunnable extends BukkitRunnable {
    private List<String> names;
    private Residence residence;

    public UUIDFetcherRunnable(List<String> names) {
        this.residence = Residence.getInstance();
        this.names = names;
    }

    public UUIDFetcherRunnable(String name) {
        this.residence = Residence.getInstance();
        this.names = new ArrayList<String>();
        this.names.add(name);
    }

    @Override
    public void run() {
        try {
            Map<String, UUID> returns = new UUIDFetcher(this.names).call();
            new CacheReturnedNames(returns).runTask(this.residence);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class CacheReturnedNames extends BukkitRunnable {
        private Map<String, UUID> returns;

        public CacheReturnedNames(Map<String, UUID> returns) {
            this.returns = returns;
        }

        @Override
        public void run() {
            for (Entry<String, UUID> entry : this.returns.entrySet()) {
                ResidenceAPI.getUsernameUUIDCache().cacheName(entry.getValue(), entry.getKey());
            }
        }
    }
}
