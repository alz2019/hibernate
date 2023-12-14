package org.alz2019.session;

public interface Session {
    <T> void persist(T entity);

    <T> T find(Class<T> entityType, Object id);

    <T> void remove(T entity);

    void flush();

    void close();
}
