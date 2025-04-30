package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserDbStorage.class})
class UserDbStorageTest {
    private User user;

    @Autowired
    private final UserDbStorage userDbStorage;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        user = new User(1L, "vlad@mail.ru", "Vladislav", "WhatIsLove", LocalDate.of(2000, 5, 10), new HashSet<>());
    }

    @AfterEach
    void tearDown() {
        log.info("Очищаю базу данных");
        jdbcTemplate.execute("DELETE FROM users");
        log.info("База данных очищена");
        jdbcTemplate.execute("ALTER TABLE users ALTER COLUMN id RESTART WITH 1");
        log.info("Счетчик сброшен");
    }

    @Test
    public void testCreateAndGetUserById() {
        userDbStorage.create(user);
        Optional<User> userOptional = Optional.ofNullable(userDbStorage.findById(1L));

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("id", 1)
                )
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("name", "WhatIsLove")
                );
    }

    @Test
    public void testFindAll() {
        userDbStorage.create(user);
        List<User> users = List.of(user);
        List<User> users1 = userDbStorage.findAll();
        assertEquals(users, users1);
    }

    @Test
    public void testUpdate() {
        userDbStorage.create(user);
        User user1 = new User(1L, "newEmail@mail.ru", "newLogin", "NewName",
                LocalDate.of(2000, 5, 10), new HashSet<>());
        userDbStorage.update(user1);
        Optional<User> userOptional = Optional.ofNullable(userDbStorage.findById(1L));
        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("id", 1))
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("name", "NewName"))
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("login", "newLogin"))
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("email", "newEmail@mail.ru"))
        ;
    }

    @Test
    public void testDelete() {
        userDbStorage.create(user);
        userDbStorage.delete(1L);
        assertEquals(0, userDbStorage.findAll().size());
    }
}