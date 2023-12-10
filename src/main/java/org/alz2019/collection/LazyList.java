package org.alz2019.collection;

import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.function.Supplier;

@Slf4j
public class LazyList<T> implements List<T> {
    private final Supplier<List<T>> collectionSupplier;
    private List<T> internalList;


    public LazyList(Supplier<List<T>> collectionSupplier) {
        this.collectionSupplier = collectionSupplier;
    }

    @Delegate
    private List<T> getInternalList() {
        if (internalList == null) {
            System.err.println("Initializing lazy list");
            internalList = collectionSupplier.get();
        }
        return internalList;
    }
}
