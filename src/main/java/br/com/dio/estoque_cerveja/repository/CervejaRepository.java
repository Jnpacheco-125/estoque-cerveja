package br.com.dio.estoque_cerveja.repository;

import br.com.dio.estoque_cerveja.entity.Cerveja;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CervejaRepository extends JpaRepository<Cerveja, Long> {
    Optional<Cerveja> findByNome(String nome);
}
