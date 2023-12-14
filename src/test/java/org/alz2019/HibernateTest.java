package org.alz2019;

import org.alz2019.model.User;
import org.alz2019.session.Session;
import org.alz2019.session.SessionFactory;
import org.alz2019.session.impl.SimpleSessionFactory;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

public class HibernateTest {
    @Test
    @Order(1)
    public void testFind() {
        SessionFactory sessionFactory = new SimpleSessionFactory(initH2DataSource());
        Session session = sessionFactory.openSession();

        User foundUser = session.find(User.class, 1L);
        System.out.println("Found user: " + foundUser);
        foundUser.getCards().forEach(System.out::println);
    }

    @Test
    @Order(2)
    public void lifecycleTest() {
        SessionFactory sessionFactory = new SimpleSessionFactory(initH2DataSource());
        Session session = sessionFactory.openSession();

        User newUser = createNewUser();
        session.persist(newUser);

        User foundUser = session.find(User.class, 42L);
        System.out.println("Found user: " + foundUser);

        User cachedUser = session.find(User.class, 42L);
        System.out.println("Is user cached? " + (foundUser == cachedUser));

        cachedUser.setFirstName("James");
        User updatedUser = session.find(User.class, 42L);
        System.out.println("Changed user: " + updatedUser);

        session.remove(updatedUser);

        session.close();
    }

    private static DataSource initH2DataSource() {
        JdbcDataSource jdbcDataSource = new JdbcDataSource();
        jdbcDataSource.setURL("jdbc:h2:mem:demo;INIT=runscript from 'classpath:init.sql'");
        jdbcDataSource.setUser("sa");
        return jdbcDataSource;
    }

    private static User createNewUser() {
        User user = new User();
        user.setId(42L);
        user.setFirstName("Jack");
        user.setLastName("Jackson");
        return user;
    }
}
