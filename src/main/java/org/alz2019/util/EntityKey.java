package org.alz2019.util;

import static org.alz2019.util.EntityUtil.getId;

public record EntityKey<T>(Class<T> entityType, Object id) {
    public static <T> EntityKey<?> of(Class<T> entityType, Object id) {
        return new EntityKey<>(entityType, id);
    }

    public static <T> EntityKey<T> valueOf(T entity) {
        Object id = getId(entity);
        Class<?> entityClass = entity.getClass();
        return new EntityKey(entityClass, id);
    }
}
