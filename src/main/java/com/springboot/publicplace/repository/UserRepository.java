package com.springboot.publicplace.repository;

import com.springboot.publicplace.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,Long> {
    User findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickName);

    boolean existsByPhoneNumber(String phoneNumber);
}