package org.alz2019;

import org.alz2019.model.Card;
import org.alz2019.model.User;
import org.alz2019.session.Session;
import org.alz2019.session.SessionFactory;
import org.alz2019.session.impl.SimpleSessionFactory;
import org.checkerframework.checker.units.qual.C;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;

public class AppTest {
    public static void main(String[] args) {
        SessionFactory sessionFactory = new SimpleSessionFactory(initDataSource());
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

    private static DataSource initDataSource() {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setUser("test");
        return dataSource;
    }

    private static User createNewUser() {
        User user = new User();
        user.setId(42L);
        user.setFirstName("Jack");
        user.setLastName("Jackson");
        return user;
    }
}
