package net.t00thpick1.residence.utils.immutable;

import java.util.Iterator;

/**
 * We use this to avoid unnecessary processing for creation of a true immutable iterator, all we need is
 * enough deterrent to keep people from unknowingly misusing our returned values.
 *
 * @author t00thpick1
 */
public class ImmutableWrapperIterator<K> implements Iterator<K> {
    private Iterator<K> iterator;

    public ImmutableWrapperIterator(Iterator<K> iterator) {
        this.iterator = iterator;
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public K next() {
        return iterator.next();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

}
