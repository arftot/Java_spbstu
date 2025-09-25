package com.alina.taskmanager.repository.jpa;

import com.alina.taskmanager.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("db")
class SpringUserJpaRepositoryTest {

    @Autowired
    private SpringUserJpaRepository repository;

    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        testUser = new UserEntity();
        testUser.setUsername("testuser");
        testUser.setDisplayName("Test User");
        testUser.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void save_ShouldReturnSavedUser() {
        UserEntity saved = repository.save(testUser);

        assertThat(saved.getId()).isNotBlank();
        assertThat(saved.getUsername()).isEqualTo("testuser");
        assertThat(saved.getDisplayName()).isEqualTo("Test User");
    }

    @Test
    void findById_ShouldReturnUser_WhenExists() {
        UserEntity saved = repository.save(testUser);

        Optional<UserEntity> found = repository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("testuser");
    }

    @Test
    void findByUsername_ShouldReturnUser_WhenExists() {
        repository.save(testUser);

        Optional<UserEntity> found = repository.findByUsername("testuser");

        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("testuser");
    }
}
