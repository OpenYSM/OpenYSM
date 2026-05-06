package com.elfmcys.yesstevemodel.geckolib3.core.molang.storage;

import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext;
import com.elfmcys.yesstevemodel.molang.runtime.Function;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class TempVariableStorage implements ITempVariableStorage {

    private static final int MAX_DEPTH = 32;

    private int baseOffset;

    private int currentSize;

    private int scopeStart;

    private int scopeSize;

    private Object[] elements = new Object[16];

    private final LongArrayList scopeStack = new LongArrayList(4);

    private final ElementListView listView = new ElementListView();

    private void ensureCapacity(int i) {
        Object[] objArr = this.elements;
        if (objArr.length < i) {
            int length = objArr.length;
            while (true) {
                int i2 = length * 2;
                if (i2 < i) {
                    length = i2;
                } else {
                    this.elements = Arrays.copyOf(objArr, i2);
                    return;
                }
            }
        }
    }

    @Override
    public Object getElement(int i) {
        if (i < this.currentSize) {
            return this.elements[this.baseOffset + i];
        }
        return null;
    }

    @Override
    public void setElement(int i, Object obj) {
        int i2 = i + 1;
        if (this.currentSize < i2) {
            this.currentSize = i2;
            ensureCapacity(this.baseOffset + i2);
        }
        this.elements[this.baseOffset + i] = obj;
    }

    public boolean pushScope(List<?> list) {
        if (this.scopeStack.size() < 32) {
            int i = this.baseOffset + this.currentSize;
            int size = list.size();
            int i2 = i + size;
            ensureCapacity(i2);
            Object[] objArr = this.elements;
            for (int i3 = 0; i3 < size; i3++) {
                objArr[i + i3] = list.get(i3);
            }
            this.scopeStack.add(((long) this.scopeSize << 32) | this.scopeStart);
            this.scopeStart = i;
            this.scopeSize = size;
            this.baseOffset = i2;
            this.currentSize = 0;
            return true;
        }
        return false;
    }

    public boolean pushScopeWithArgs(ExecutionContext<?> executionContext, Function.ArgumentCollection function) {
        if (this.scopeStack.size() < 32) {
            int i = this.baseOffset + this.currentSize;
            int i4 = function.size();
            int i2 = i + i4;
            ensureCapacity(i2);
            this.currentSize += function.size();
            for (int i3 = 0; i3 < i4; i3++) {
                this.elements[i + i3] = function.getValue(executionContext, i3);
            }
            this.scopeStack.add(((long) this.scopeSize << 32) | this.scopeStart);
            this.scopeStart = i;
            this.scopeSize = i4;
            this.baseOffset = i2;
            this.currentSize = 0;
            return true;
        }
        return false;
    }

    public void popScope() {
        LongArrayList longArrayList = this.scopeStack;
        if (!longArrayList.isEmpty()) {
            long jRemoveLong = longArrayList.removeLong(longArrayList.size() - 1);
            int i = this.scopeStart;
            int i2 = (int) (jRemoveLong & 4294967295L);
            int i3 = (int) (jRemoveLong >> 32);
            int i4 = i2 + i3;
            this.scopeStart = i2;
            this.scopeSize = i3;
            this.baseOffset = i4;
            this.currentSize = i - i4;
        }
    }

    public List<Object> asList() {
        return this.listView;
    }

    class ElementIterator implements Iterator<Object> {

        private int currentIndex;

        private final int endIndex;

        ElementIterator() {
            this.currentIndex = TempVariableStorage.this.scopeStart;
            this.endIndex = TempVariableStorage.this.baseOffset;
        }

        @Override
        public boolean hasNext() {
            return this.currentIndex < this.endIndex;
        }

        @Override
        public Object next() {
            if (this.currentIndex < this.endIndex) {
                Object[] objArr = TempVariableStorage.this.elements;
                int i = this.currentIndex;
                this.currentIndex = i + 1;
                return objArr[i];
            }
            return null;
        }
    }

    class ElementListView implements List<Object> {
        ElementListView() {
        }

        @Override
        public int size() {
            return TempVariableStorage.this.scopeSize;
        }

        @Override
        public boolean isEmpty() {
            return TempVariableStorage.this.scopeSize == 0;
        }

        @Override
        public Object get(int i) {
            if (i >= 0 && i < TempVariableStorage.this.scopeSize) {
                return TempVariableStorage.this.elements[TempVariableStorage.this.scopeStart + i];
            }
            return null;
        }

        @Override
        @NotNull
        public Iterator<Object> iterator() {
            return TempVariableStorage.this.new ElementIterator();
        }

        @Override
        public boolean contains(Object obj) {
            throw new UnsupportedOperationException();
        }

        @Override
        @NotNull
        public Object[] toArray() {
            throw new UnsupportedOperationException();
        }

        @Override
        @NotNull
        public <T> T[] toArray(@NotNull T[] tArr) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean add(Object obj) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean remove(Object obj) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean containsAll(@NotNull Collection<?> collection) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean addAll(@NotNull Collection<?> collection) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean addAll(int i, @NotNull Collection<? extends Object> collection) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean removeAll(@NotNull Collection<?> collection) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean retainAll(@NotNull Collection<?> collection) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object set(int i, Object obj) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(int i, Object obj) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object remove(int i) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int indexOf(Object obj) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int lastIndexOf(Object obj) {
            throw new UnsupportedOperationException();
        }

        @Override
        @NotNull
        public ListIterator<Object> listIterator() {
            throw new UnsupportedOperationException();
        }

        @Override
        @NotNull
        public ListIterator<Object> listIterator(int i) {
            throw new UnsupportedOperationException();
        }

        @Override
        @NotNull
        public List<Object> subList(int i, int i2) {
            throw new UnsupportedOperationException();
        }
    }
}