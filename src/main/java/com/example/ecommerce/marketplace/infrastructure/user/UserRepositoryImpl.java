package com.example.ecommerce.marketplace.infrastructure.user;

import com.example.ecommerce.marketplace.domain.user.User;
import com.example.ecommerce.marketplace.domain.user.UserRepository;
import com.example.ecommerce.marketplace.domain.user.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of UserRepository using Spring Data JPA.
 * This adapter translates between domain and infrastructure layers.
 */
@Component
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final JpaUserRepository jpaRepository;

    @Override
    public User save(User user) {
        UserEntity entity = UserEntity.fromDomain(user);
        UserEntity savedEntity = jpaRepository.save(entity);
        return savedEntity.toDomain();
    }

    @Override
    public Optional<User> findById(Long id) {
        return jpaRepository.findById(id)
            .map(UserEntity::toDomain);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return jpaRepository.findByUsername(username)
            .map(UserEntity::toDomain);
    }

    @Override
    public List<User> findByRole(UserRole role) {
        return jpaRepository.findByRole(role).stream()
            .map(UserEntity::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public Optional<User> findByEntityIdAndRole(Long entityId, UserRole role) {
        return jpaRepository.findByEntityIdAndRole(entityId, role)
            .map(UserEntity::toDomain);
    }

    @Override
    public List<User> findByEnabled(Boolean enabled) {
        return jpaRepository.findByEnabled(enabled).stream()
            .map(UserEntity::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<User> findByAccountLocked(Boolean accountLocked) {
        return jpaRepository.findByAccountLocked(accountLocked).stream()
            .map(UserEntity::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<User> findAll() {
        return jpaRepository.findAll().stream()
            .map(UserEntity::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public boolean existsByUsername(String username) {
        return jpaRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEntityIdAndRole(Long entityId, UserRole role) {
        return jpaRepository.existsByEntityIdAndRole(entityId, role);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }

    @Override
    public long countByRole(UserRole role) {
        return jpaRepository.countByRole(role);
    }

    @Override
    public long countByEnabled(Boolean enabled) {
        return jpaRepository.countByEnabled(enabled);
    }

    @Override
    public long countByAccountLocked(Boolean accountLocked) {
        return jpaRepository.countByAccountLocked(accountLocked);
    }
}
