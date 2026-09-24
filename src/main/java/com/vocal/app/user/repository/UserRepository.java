package com.vocal.app.user.repository;

import com.vocal.app.global.enums.SocialType;
import com.vocal.app.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<User> findBySocialTypeAndSocialUid(SocialType socialType, String socialUid);
}