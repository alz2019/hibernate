package org.alz2019.session.impl;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.alz2019.collection.LazyList;
import org.alz2019.util.EntityKey;

import javax.sql.DataSource;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static org.alz2019.util.EntityUtil.*;
import static org.alz2019.util.SqlUtil.*;

@RequiredArgsConstructor
@Log4j2
public class EntityOperations {
    private final DataSource dataSource;
    private final StatefulSession statefulSession;
    private final PersistenceContext persistenceContext;

    public <T> T findById(Class<T> entityType, Object id) {
        EntityKey<?> entityKey = EntityKey.of(entityType, id);
        Object cached = persistenceContext.getEntity(entityKey);
        if (cached != null) {
            log.trace("Returning cached entity: " + cached);
            return entityType.cast(cached);
        }
        Field idField = getIdField(entityType);
        return findOne(entityType, idField, id);
    }

    private <T> T findOne(Class<T> entityType, Field idField, Object id) {
        List<T> result = findAll(entityType, idField, id);
        if (result.size() != 1) {
            throw new IllegalStateException("Result must be a single row");
        }
        return result.get(0);
    }

    @SneakyThrows
    private <T> List<T> findAll(Class<T> entityType, Field field, Object value) {
        List<T> list = new ArrayList<>();
        try (Connection connection = dataSource.getConnection()) {
            String tableName = resolveTableName(entityType);
            log.trace("Table name: " + tableName);
            String columnName = resolveColumnName(field);
            String select = String.format(SELECT, tableName, columnName);
            try (PreparedStatement statement = connection.prepareStatement(select)) {
                statement.setObject(1, value);
                log.trace("Select statement: " + statement);
                ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    T entity = createEntity(entityType, resultSet);
                    list.add(entity);
                }
            }
        }
        return list;
    }

    @SneakyThrows
    public <T> T insert(T entity) {
        log.trace("Inserting: " + entity);
        Class<?> entityClass = entity.getClass();
        try (Connection connection = dataSource.getConnection()) {
            String tableName = resolveTableName(entityClass);
            log.trace("Table name: " + tableName);
            String columns = commaSeparatedColumns(entityClass);
            String params = commaSeparatedParams(entityClass);
            String insert = String.format(INSERT_INTO, tableName, columns, params);
            try (PreparedStatement statement = connection.prepareStatement(insert)) {
                fillInsertParams(statement, entity);
                log.trace("Insert statement: " + statement);
                statement.executeUpdate();
            }
        }
        return entity;
    }

    private <T> void fillInsertParams(PreparedStatement statement, T entity) {
        Field[] columnFields = getColumnFields(entity.getClass());
        setParams(statement, entity, columnFields);
    }

    private <T> void fillUpdateParams(PreparedStatement statement, T entity) {
        Field[] columnFields = getUpdatableFields(entity.getClass());
        setParams(statement, entity, columnFields);
    }

    @SneakyThrows
    private void setParams(PreparedStatement statement, Object entity, Field[] fields) {
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            field.setAccessible(true);
            Object value = field.get(entity);
            statement.setObject(i + 1, value);
        }
    }

    @SneakyThrows
    private <T> T createEntity(Class<T> entityType, ResultSet resultSet) {
        Constructor<T> constructor = entityType.getConstructor();
        T entity = constructor.newInstance();
        for (Field field : entityType.getDeclaredFields()) {
            field.setAccessible(true);
            if (isSimpleColumnField(field)) {
                log.trace("Simple field: " + field.getName());
                String columnName = resolveColumnName(field);
                log.trace("Column name: " + columnName);
                Object value = resultSet.getObject(columnName);
                log.trace("Column value: " + value);
                field.set(entity, value);
            } else if (isEntityField(field)) {
                log.trace("Entity field: " + field.getName());
                Class<?> type = field.getType();
                String columnName = resolveColumnName(field);
                log.trace("Column name: " + columnName);
                Object value = resultSet.getObject(columnName);
                log.trace("JoinColumn name: " + columnName);
                Object related = findById(type, value);
                field.set(entity, related);
            } else if (isEntityCollectionField(field)) {
                log.trace("Entity collection field: " + field.getName());
                Class<?> entityCollections = getEntityCollections(field);
                log.trace("Entity collection field type: " + entityCollections.getSimpleName());
                Field associatedEntityField = getAssociatedEntityField(entityType, entityCollections);
                log.trace("Associated entity field: " + associatedEntityField.getName());
                Object id = getId(entity);
                LazyList<?> lazyList = createLazyList(entityCollections, associatedEntityField, id);
                log.trace("Set lazy list to field " + field.getName());
                field.set(entity, lazyList);
            }
        }
        return persistenceContext.manageEntity(entity);
    }

    private <T> LazyList<T> createLazyList(Class<T> entityType, Field field, Object entityId) {
        Supplier<List<T>> supplier = () -> {
            statefulSession.checkIsOpen();
            return findAll(entityType, field, entityId);
        };
        return new LazyList<>(supplier);
    }

    @SneakyThrows
    public <T> T update(T entity) {
        log.trace("Updating entity: " + entity);
        Class<?> entityClass = entity.getClass();
        try (Connection connection = dataSource.getConnection()) {
            String tableName = resolveTableName(entityClass);
            log.trace("Table name: " + tableName);
            String columns = commaSeparatedSetters(entityClass);
            String idColumn = resolveIdColumnName(entityClass) + " = ?";
            String update = String.format(UPDATE, tableName, columns, idColumn);
            try (PreparedStatement statement = connection.prepareStatement(update)) {
                fillUpdateParams(statement, entity);
                int index = getUpdatableFields(entityClass).length + 1;
                statement.setObject(index, getId(entity));
                log.trace("Update statement: " + statement);
                statement.executeUpdate();
            }
        }
        return entity;
    }

    @SneakyThrows
    public <T> T delete(T entity) {
        log.trace("Deleting entity: " + entity);
        Class<?> entityClass = entity.getClass();
        try (Connection connection = dataSource.getConnection()) {
            String tableName = resolveTableName(entityClass);
            log.trace("Table name: " + tableName);
            String columnName = resolveIdColumnName(entityClass);
            String delete = String.format(DELETE, tableName, columnName);
            try (PreparedStatement statement = connection.prepareStatement(delete)) {
                Object id = getId(entity);
                statement.setObject(1, id);
                log.trace("Delete statement: " + statement);
                statement.executeUpdate();
            }
        }
        return entity;
    }
}
