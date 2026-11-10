package br.com.mhr.backend.controller.admin;

import java.util.List;
import java.util.UUID;

import br.com.mhr.backend.dto.ConfiguracaoLicitacoesDto;
import br.com.mhr.backend.dto.DocumentoLicitacaoDto;
import br.com.mhr.backend.dto.LicitacaoDto;
import br.com.mhr.backend.dto.UsuarioResumoDto;
import br.com.mhr.backend.dto.request.AtualizarConfiguracaoLicitacoesRequest;
import br.com.mhr.backend.dto.request.AtualizarDocumentoLicitacaoRequest;
import br.com.mhr.backend.dto.request.AtualizarResponsavelLicitacaoRequest;
import br.com.mhr.backend.dto.request.AtualizarStatusLicitacaoRequest;
import br.com.mhr.backend.dto.request.SalvarDocumentoLicitacaoRequest;
import br.com.mhr.backend.dto.request.SalvarLicitacaoRequest;
import br.com.mhr.backend.model.Licitacao;
import br.com.mhr.backend.service.LicitacaoService;
import br.com.mhr.backend.service.PncpSyncService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Acompanhamento de licitações (painel admin, protegido por JWT, ver SecurityConfig).
 * Inclui o CRUD manual, o checklist de documentos de cada licitação, e os
 * controles da busca automática no PNCP ({@link PncpSyncService}).
 */
@RestController
@RequestMapping("/api/admin/licitacoes")
@RequiredArgsConstructor
public class LicitacaoAdminController {

    private final LicitacaoService service;
    private final PncpSyncService pncpSyncService;

    @GetMapping
    public List<LicitacaoDto> listar(@RequestParam(required = false) Licitacao.StatusLicitacao status) {
        return service.listar(status);
    }

    @GetMapping("/responsaveis")
    public List<UsuarioResumoDto> listarResponsaveisDisponiveis() {
        return service.listarResponsaveisDisponiveis();
    }

    @GetMapping("/{id}")
    public LicitacaoDto buscarPorId(@PathVariable UUID id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LicitacaoDto criar(@Valid @RequestBody SalvarLicitacaoRequest request) {
        return service.criar(request);
    }

    @PutMapping("/{id}")
    public LicitacaoDto atualizar(@PathVariable UUID id, @Valid @RequestBody SalvarLicitacaoRequest request) {
        return service.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable UUID id) {
        service.excluir(id);
    }

    @PatchMapping("/{id}/status")
    public LicitacaoDto atualizarStatus(@PathVariable UUID id, @Valid @RequestBody AtualizarStatusLicitacaoRequest request) {
        return service.atualizarStatus(id, request.status());
    }

    @PatchMapping("/{id}/responsavel")
    public LicitacaoDto atualizarResponsavel(@PathVariable UUID id, @Valid @RequestBody AtualizarResponsavelLicitacaoRequest request) {
        return service.atualizarResponsavel(id, request.responsavelId());
    }

    @GetMapping("/{licitacaoId}/documentos")
    public List<DocumentoLicitacaoDto> listarDocumentos(@PathVariable UUID licitacaoId) {
        return service.listarDocumentos(licitacaoId);
    }

    @PostMapping("/{licitacaoId}/documentos")
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentoLicitacaoDto criarDocumento(@PathVariable UUID licitacaoId, @Valid @RequestBody SalvarDocumentoLicitacaoRequest request) {
        return service.criarDocumento(licitacaoId, request);
    }

    @PatchMapping("/documentos/{id}")
    public DocumentoLicitacaoDto atualizarDocumento(@PathVariable UUID id, @Valid @RequestBody AtualizarDocumentoLicitacaoRequest request) {
        return service.atualizarDocumento(id, request.concluido());
    }

    @DeleteMapping("/documentos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluirDocumento(@PathVariable UUID id) {
        service.excluirDocumento(id);
    }

    @GetMapping("/configuracao")
    public ConfiguracaoLicitacoesDto obterConfiguracao() {
        return service.obterConfiguracao();
    }

    @PatchMapping("/configuracao")
    public ConfiguracaoLicitacoesDto atualizarConfiguracao(@Valid @RequestBody AtualizarConfiguracaoLicitacoesRequest request) {
        return service.atualizarConfiguracao(request.buscaPncpAtiva(), request.palavrasChave());
    }

    @PostMapping("/sincronizar")
    public PncpSyncService.ResultadoSincronizacao sincronizar() {
        return pncpSyncService.sincronizarAgora();
    }
}
