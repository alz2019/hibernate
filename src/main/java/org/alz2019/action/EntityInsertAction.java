package org.alz2019.action;

import lombok.RequiredArgsConstructor;
import org.alz2019.session.impl.EntityOperations;

@RequiredArgsConstructor
public class EntityInsertAction implements EntityAction {
    private final Object entity;
    private final EntityOperations operations;

    @Override
    public void execute() {
        operations.insert(entity);
    }

    @Override
    public int priority() {
        return 1;
    }
}
