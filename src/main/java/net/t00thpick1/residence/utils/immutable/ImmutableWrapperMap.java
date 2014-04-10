package net.t00thpick1.residence.utils.immutable;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * We use this to avoid unnecessary processing for creation of a true immutable map, all we need is
 * enough deterrent to keep people from unknowingly misusing our returned values.
 *
 * @author t00thpick1
 */
public class ImmutableWrapperMap<K, V> implements Map<K, V> {
    private Map<K, V> map;

    public ImmutableWrapperMap(Map<K, V> map) {
        this.map = map;
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return map.get(key);
    }

    @Override
    public V put(K key, V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<K> keySet() {
        return new ImmutableWrapperSet<K>(map.keySet());
    }

    @Override
    public Collection<V> values() {
        return new ImmutableWrapperCollection<V>(map.values());
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return new ImmutableWrapperSet<Entry<K, V>>(map.entrySet());
    }

}
