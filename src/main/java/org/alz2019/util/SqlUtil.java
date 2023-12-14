package org.alz2019.util;

import lombok.extern.log4j.Log4j2;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.stream.Collectors;

import static org.alz2019.util.EntityUtil.getColumnFields;
import static org.alz2019.util.EntityUtil.getUpdatableFields;

@Log4j2
public class SqlUtil {
    public static final String INSERT_INTO = "INSERT INTO %s (%s) VALUES (%s);";
    public static final String SELECT = "SELECT * FROM %s WHERE %s = ?;";
    public static final String UPDATE = "UPDATE %s SET %s WHERE %s;";
    public static final String DELETE = "DELETE FROM %s WHERE %s = ?;";

    public static String commaSeparatedColumns(Class<?> entityType) {
        Field[] insertableFields = getColumnFields(entityType);
        return Arrays.stream(insertableFields)
                .map(EntityUtil::resolveColumnName)
                .collect(Collectors.joining(", "));
    }

    public static String commaSeparatedParams(Class<?> entityType) {
        Field[] insertableFields = getColumnFields(entityType);
        return Arrays.stream(insertableFields)
                .map(f -> "?")
                .collect(Collectors.joining(","));
    }

    public static String commaSeparatedSetters(Class<?> entityType) {
        Field[] updatableFields = getUpdatableFields(entityType);
        return Arrays.stream(updatableFields)
                .map(EntityUtil::resolveColumnName)
                .map(column -> column + " = ?")
                .collect(Collectors.joining(", "));
    }
}
