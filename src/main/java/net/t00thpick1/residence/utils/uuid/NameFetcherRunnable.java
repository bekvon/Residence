package net.t00thpick1.residence.utils.uuid;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import net.t00thpick1.residence.Residence;
import net.t00thpick1.residence.api.ResidenceAPI;

import org.bukkit.scheduler.BukkitRunnable;

public class NameFetcherRunnable extends BukkitRunnable {
    private List<UUID> uuids;
    private Residence residence;

    public NameFetcherRunnable(List<UUID> uuids) {
        this.residence = Residence.getInstance();
        this.uuids = uuids;
    }

    public NameFetcherRunnable(UUID id) {
        this.residence = Residence.getInstance();
        this.uuids = new ArrayList<UUID>();
        this.uuids.add(id);
    }

    @Override
    public void run() {
        try {
            Map<UUID, String> returns = new NameFetcher(this.uuids).call();
            new CacheReturnedNames(returns).runTask(this.residence);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class CacheReturnedNames extends BukkitRunnable {
        private Map<UUID, String> returns;

        public CacheReturnedNames(Map<UUID, String> returns) {
            this.returns = returns;
        }

        @Override
        public void run() {
            for (Entry<UUID, String> entry : this.returns.entrySet()) {
                ResidenceAPI.getUsernameUUIDCache().cacheName(entry.getKey(), entry.getValue());
            }
        }
    }
}
