package org.alz2019.session.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.alz2019.session.Session;
import org.alz2019.session.SessionFactory;

import javax.sql.DataSource;

@RequiredArgsConstructor
@Log4j2
public class SimpleSessionFactory implements SessionFactory {
    private final DataSource dataSource;

    @Override
    public Session openSession() {
        log.trace("Opening a new session");
        return new StatefulSession(dataSource);
    }
}
