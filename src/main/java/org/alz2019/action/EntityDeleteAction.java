package org.alz2019.action;

import lombok.RequiredArgsConstructor;
import org.alz2019.session.impl.EntityOperations;

@RequiredArgsConstructor
public class EntityDeleteAction implements EntityAction {
    private final Object entity;
    private final EntityOperations operations;

    @Override
    public void execute() {
        operations.delete(entity);
    }

    @Override
    public int priority() {
        return 3;
    }
}
