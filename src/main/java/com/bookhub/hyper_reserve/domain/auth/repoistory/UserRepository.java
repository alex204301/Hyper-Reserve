package com.bookhub.hyper_reserve.domain.auth.repoistory;

import com.bookhub.hyper_reserve.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // 로그인 시 이메일로 유저 조회
    Optional<User> findByEmail(String email);

    // 회원가입 시 이메일 중복 체크
    boolean existsByEmail(String email);
}