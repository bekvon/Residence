package net.t00thpick1.residence.utils.immutable;

import java.util.Collection;
import java.util.Iterator;

/**
 * We use this to avoid unnecessary processing for creation of a true immutable collection, all we need is
 * enough deterrent to keep people from unknowingly misusing our returned values.
 *
 * @author t00thpick1
 */
public class ImmutableWrapperCollection<V> implements Collection<V> {
    private Collection<V> collection;

    public ImmutableWrapperCollection(Collection<V> collection) {
        this.collection = collection;
    }

    @Override
    public int size() {
        return collection.size();
    }

    @Override
    public boolean isEmpty() {
        return collection.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return collection.contains(o);
    }

    @Override
    public Iterator<V> iterator() {
        return new ImmutableWrapperIterator<V>(collection.iterator());
    }

    @Override
    public Object[] toArray() {
        return collection.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return collection.toArray(a);
    }

    @Override
    public boolean add(V e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return collection.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends V> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }
}
