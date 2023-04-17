package com.example.gromit.repository;

import com.example.gromit.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;
import java.util.Optional;

@RepositoryRestResource
public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    Boolean existsByNickname(String nickname);

    Optional<UserAccount> findByEmailAndProviderAndIsDeleted(String email, String provider, boolean isDeleted);

    Optional<UserAccount> findByNicknameAndIsDeleted(String nickname, boolean isDeleted);

    List<UserAccount> findByIsDeleted(boolean isDeleted);

    Optional<UserAccount> findByIdAndIsDeleted(Long id, boolean isDeleted);
}
