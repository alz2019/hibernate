package org.alz2019.session.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alz2019.session.Session;
import org.alz2019.session.SessionFactory;

import javax.sql.DataSource;

@RequiredArgsConstructor
@Slf4j
public class SimpleSessionFactory implements SessionFactory {
    private final DataSource dataSource;

    @Override
    public Session openSession() {
        System.err.println("Opening a new session");
        return new StatefulSession(dataSource);
    }
}
