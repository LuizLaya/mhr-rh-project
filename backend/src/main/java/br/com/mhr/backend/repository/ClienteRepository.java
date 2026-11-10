package br.com.mhr.backend.repository;

import java.util.List;
import java.util.UUID;

import br.com.mhr.backend.model.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Acesso a dados de {@link Cliente}. */
public interface ClienteRepository extends JpaRepository<Cliente, UUID> {

    List<Cliente> findByExibirNoSiteTrueOrderByNomeEmpresa();

    // :busca vazio/nulo devolve tudo (paginado) - um unico metodo cobre "listar
    // paginado" e "buscar paginado", a tela de admin sempre manda os dois parametros.
    @Query("""
            select c from Cliente c
            where (:busca is null or :busca = ''
                or lower(c.nomeEmpresa) like lower(concat('%', :busca, '%'))
                or lower(c.contatoNome) like lower(concat('%', :busca, '%'))
                or lower(c.contatoEmail) like lower(concat('%', :busca, '%')))
            """)
    Page<Cliente> buscarPaginado(@Param("busca") String busca, Pageable pageable);
}
