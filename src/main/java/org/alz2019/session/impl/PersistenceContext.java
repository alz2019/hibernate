package org.alz2019.session.impl;

import lombok.extern.log4j.Log4j2;
import org.alz2019.util.EntityKey;
import org.alz2019.util.EntityUtil;

import java.util.*;

import static org.alz2019.util.EntityUtil.entityToSnapshot;

@Log4j2
public class PersistenceContext {
    private final Map<EntityKey<?>, Object> entitiesByKey = new HashMap<>();
    private final Map<EntityKey<?>, Object[]> entitiesSnapshotByKey = new HashMap<>();

    public <T> T getEntity(EntityKey<T> key) {
        Object entity = entitiesByKey.get(key);
        return key.entityType().cast(entity);
    }

    public <T> T addEntity(T entity) {
        EntityKey<Object> key = EntityKey.valueOf(entity);
        entitiesByKey.put(key, entity);
        entitiesSnapshotByKey.put(key, entityToSnapshot(entity));
        return entity;
    }

    public <T> boolean contains(T entity) {
        EntityKey<Object> key = EntityKey.valueOf(entity);
        return entitiesByKey.containsKey(key);
    }

    public void clear() {
        entitiesByKey.clear();
        entitiesSnapshotByKey.clear();
    }

    public List<?> getDirtyEntities() {
        List<Object> list = new ArrayList<>();
        for (Map.Entry<EntityKey<?>, Object> entity : entitiesByKey.entrySet()) {
            Object currentEntity = entity.getValue();
            Object[] currentSnapshot = entityToSnapshot(currentEntity);
            Object[] initialSnapshot = entitiesSnapshotByKey.get(entity.getKey());
            if (!Arrays.equals(currentSnapshot, initialSnapshot)) {
                log.trace("Dirty entity: " + currentEntity);
                log.trace("Snapshot copy: {}", initialSnapshot);
                list.add(currentEntity);
            }
        }
        return list;
    }

    @SuppressWarnings("unchecked")
    public <T> T manageEntity(T entity) {
        EntityKey<Object> key = EntityKey.valueOf(entity);
        Object cached = entitiesByKey.get(key);
        if (cached != null) {
            log.trace("Returning cached entity: " + entity);
            return (T) cached;
        } else {
            return addEntity(entity);
        }
    }
}
