package trash;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import cat.lechuga.mql.Executor;

public class EntitiesLazyList<E> implements List<E> {

    List<E> list = null;

    final Executor<E> executor;

    public EntitiesLazyList(Executor<E> executor) {
        super();
        this.executor = executor;
    }

    protected void lazyLoad() {
        if (this.list == null) {
            this.list = new ArrayList<>();
            this.list.addAll(executor.load());
        }
    }

    public boolean isInitializated() {
        return this.list != null;
    }

    public List<E> getWrappedList() {
        return this.list;
    }

    // ============ delegate methods ===========

    @Override
    public int size() {
        lazyLoad();
        return list.size();
    }

    @Override
    public boolean isEmpty() {
        lazyLoad();
        return list.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        lazyLoad();
        return list.contains(o);
    }

    @Override
    public Iterator<E> iterator() {
        lazyLoad();
        return list.iterator();
    }

    @Override
    public Object[] toArray() {
        lazyLoad();
        return list.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        lazyLoad();
        return list.toArray(a);
    }

    @Override
    public boolean add(E e) {
        lazyLoad();
        return list.add(e);
    }

    @Override
    public boolean remove(Object o) {
        lazyLoad();
        return list.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        lazyLoad();
        return list.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        lazyLoad();
        return list.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        lazyLoad();
        return list.addAll(index, c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        lazyLoad();
        return list.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        lazyLoad();
        return list.retainAll(c);
    }

    @Override
    public void clear() {
        lazyLoad();
        list.clear();
    }

    @Override
    public boolean equals(Object o) {
        lazyLoad();
        return list.equals(o);
    }

    @Override
    public int hashCode() {
        lazyLoad();
        return list.hashCode();
    }

    @Override
    public E get(int index) {
        lazyLoad();
        return list.get(index);
    }

    @Override
    public E set(int index, E element) {
        lazyLoad();
        return list.set(index, element);
    }

    @Override
    public void add(int index, E element) {
        lazyLoad();
        list.add(index, element);
    }

    @Override
    public E remove(int index) {
        lazyLoad();
        return list.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        lazyLoad();
        return list.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        lazyLoad();
        return list.lastIndexOf(o);
    }

    @Override
    public ListIterator<E> listIterator() {
        lazyLoad();
        return list.listIterator();
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        lazyLoad();
        return list.listIterator(index);
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        lazyLoad();
        return list.subList(fromIndex, toIndex);
    }

    @Override
    public String toString() {
        lazyLoad();
        return list.toString();
    }
}
