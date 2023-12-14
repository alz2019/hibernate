package org.alz2019.util;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.alz2019.annotation.*;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;

@UtilityClass
public class EntityUtil {
    public static String resolveColumnName(Field field) {
        return Optional.ofNullable(field.getAnnotation(Column.class))
                .map(Column::value)
                .orElse(field.getName());
    }

    public static String resolveIdColumnName(Class<?> entityType) {
        Field idField = getIdField(entityType);
        return resolveColumnName(idField);
    }

    public static <T> String resolveTableName(Class<T> entityType) {
        return Optional.ofNullable(entityType.getAnnotation(Table.class))
                .map(Table::value)
                .orElse(entityType.getSimpleName());
    }

    @SneakyThrows
    public static Object[] entityToSnapshot(Object entity) {
        ArrayList<Object> columnValues = new ArrayList<>();
        for (Field field : entity.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            if (isSimpleColumnField(field)) {
                Object columnValue = field.get(entity);
                columnValues.add(columnValue);
            } else if (isEntityField(field)) {
                Object object = field.get(entity);
                Object objectId = getId(object);
                columnValues.add(objectId);
            }
        }
        return columnValues.toArray();
    }

    @SneakyThrows
    public static Object getId(Object entity) {
        Class<?> entityClass = entity.getClass();
        Field idField = getIdField(entityClass);
        idField.setAccessible(true);
        return idField.get(entity);
    }

    public static <T> Field getIdField(Class<T> entityClass) {
        return Arrays.stream(entityClass.getDeclaredFields())
                .filter(EntityUtil::isIdField)
                .findAny()
                .orElseThrow(() -> new RuntimeException("Cannot find a identifier in class " + entityClass.getSimpleName()));
    }

    public static boolean isIdField(Field field) {
        return field.isAnnotationPresent(Id.class);
    }

    public static boolean isEntityField(Field field) {
        return field.isAnnotationPresent(ManyToOne.class);
    }

    public static boolean isEntityCollectionField(Field field) {
        return field.isAnnotationPresent(OneToMany.class);
    }

    public static boolean isSimpleColumnField(Field field) {
        return !isEntityField(field) && !isEntityCollectionField(field);
    }

    public static boolean isColumnField(Field field) {
        return !isEntityCollectionField(field);
    }

    public static <T> Field getAssociatedEntityField(Class<T> fromEntity, Class<?> toEntity) {
        return Arrays.stream(toEntity.getDeclaredFields())
                .filter(f -> f.getType().equals(fromEntity))
                .findAny()
                .orElseThrow();
    }

    public static Field[] getFields(Class<?> entityType, Predicate<Field> fieldPredicate) {
        return Arrays.stream(entityType.getDeclaredFields())
                .filter(fieldPredicate)
                .toArray(Field[]::new);
    }

    public static Field[] getColumnFields(Class<?> entityType) {
        return getFields(entityType, EntityUtil::isColumnField);
    }

    public static Field[] getUpdatableFields(Class<?> entityType) {
        Predicate<Field> fieldPredicate = field -> isColumnField(field) && !isIdField(field);
        return getFields(entityType, fieldPredicate);
    }

    public static Class<?> getEntityCollections(Field field) {
        ParameterizedType type = (ParameterizedType) field.getGenericType();
        Type[] arguments = type.getActualTypeArguments();
        return (Class<?>) arguments[0];
    }
}
