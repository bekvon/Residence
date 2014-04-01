package net.t00thpick1.residence.api;

import java.io.IOException;
import java.util.UUID;

public interface UsernameUUIDCache {
    public UUID getCachedUUID(String name);
    public String getCachedName(UUID uuid);
    public void cacheName(UUID uuid, String name);
    public void save() throws IOException;
}
