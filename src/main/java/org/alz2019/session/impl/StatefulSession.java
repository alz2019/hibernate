package org.alz2019.session.impl;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.alz2019.action.EntityAction;
import org.alz2019.action.EntityDeleteAction;
import org.alz2019.action.EntityInsertAction;
import org.alz2019.action.EntityUpdateAction;
import org.alz2019.session.Session;
import org.alz2019.util.EntityKey;

import javax.sql.DataSource;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import static java.util.Comparator.comparing;

@Slf4j
public class StatefulSession implements Session {
    private EntityOperations operations;
    private PersistenceContext persistenceContext = new PersistenceContext();
    private Queue<EntityAction> actionQueue = new PriorityQueue<>(comparing(EntityAction::priority));
    @Getter
    private boolean closed;

    public StatefulSession(DataSource dataSource) {
        operations = new EntityOperations(dataSource, this, persistenceContext);
    }

    @Override
    public <T> void persist(T entity) {
        checkIsOpen();
        if (persistenceContext.contains(entity)) {
            throw new RuntimeException("Entity is already present in context");
        }
        persistenceContext.addEntity(entity);
        System.err.println("Adding insert action to action queue for entity: " + entity);
        actionQueue.add(new EntityInsertAction(entity, operations));
    }

    @Override
    public <T> T find(Class<T> entityType, Object id) {
        checkIsOpen();
        return operations.findById(entityType, id);
    }

    @Override
    public <T> void remove(T entity) {
        checkIsOpen();
        T managedEntity = persistenceContext.getEntity(EntityKey.valueOf(entity));
        if (managedEntity == null) {
            throw new RuntimeException("Entity is not present in current session");
        }
        actionQueue.add(new EntityDeleteAction(entity, operations));
    }

    @Override
    public void flush() {
        checkIsOpen();
        dirtyCheck();
        flushActionQueue();
    }

    private void dirtyCheck() {
        System.err.println("Running Dirty Checking");
        List<?> entities = persistenceContext.getDirtyEntities();
        List<EntityUpdateAction> actions = entities.stream()
                .map(e -> new EntityUpdateAction(e, operations))
                .toList();
        actionQueue.addAll(actions);
    }

    private void flushActionQueue() {
        System.err.println("Flushing");
        while (!actionQueue.isEmpty()) {
            EntityAction action = actionQueue.poll();
            action.execute();
        }
    }

    @Override
    public void close() {
        checkIsOpen();
        flush();
        persistenceContext.clear();
        closed = true;
        System.err.println("Closed session");
    }

    void checkIsOpen() {
        if (isClosed()) {
            throw new IllegalStateException("Session is closed");
        }
    }
}
