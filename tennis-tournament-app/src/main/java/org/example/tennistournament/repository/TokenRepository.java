package org.example.tennistournament.repository;

import org.example.tennistournament.model.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface TokenRepository extends JpaRepository<Token, Long> {

    @Query("SELECT t FROM Token t JOIN t.user u WHERE u.id = :userId AND t.expired = false AND t.revoked = false")
    List<Token> findAllValidTokenByUser(@Param("userId") Long userId);

    List<Token> findAllByToken(@Param("token") String token);
}
