package br.com.dio.estoque_cerveja.entity;

import br.com.dio.estoque_cerveja.enums.TipoCerveja;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cervejas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cerveja {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String marca;
    private Integer maximo;
    private Integer quantidade;

    @Enumerated(EnumType.STRING)
    private TipoCerveja tipo;
}
