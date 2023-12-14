package org.alz2019.collection;

import lombok.experimental.Delegate;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.function.Supplier;

@Log4j2
public class LazyList<T> implements List<T> {
    private final Supplier<List<T>> collectionSupplier;
    private List<T> internalList;


    public LazyList(Supplier<List<T>> collectionSupplier) {
        this.collectionSupplier = collectionSupplier;
    }

    @Delegate
    private List<T> getInternalList() {
        if (internalList == null) {
            log.trace("Initializing lazy list");
            internalList = collectionSupplier.get();
        }
        return internalList;
    }
}
