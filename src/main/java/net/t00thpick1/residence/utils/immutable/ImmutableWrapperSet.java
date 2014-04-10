package net.t00thpick1.residence.utils.immutable;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * We use this to avoid unnecessary processing for creation of a true immutable set, all we need is
 * enough deterrent to keep people from unknowingly misusing our returned values.
 *
 * @author t00thpick1
 */
public class ImmutableWrapperSet<K> implements Set<K> {
    private Set<K> set;

    public ImmutableWrapperSet(Set<K> set) {
        this.set = set;
    }

    @Override
    public int size() {
        return set.size();
    }

    @Override
    public boolean isEmpty() {
        return set.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return set.contains(o);
    }

    @Override
    public Iterator<K> iterator() {
        return new ImmutableWrapperIterator<K>(set.iterator());
    }

    @Override
    public Object[] toArray() {
        return set.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return set.toArray(a);
    }

    @Override
    public boolean add(K e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return set.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends K> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

}
