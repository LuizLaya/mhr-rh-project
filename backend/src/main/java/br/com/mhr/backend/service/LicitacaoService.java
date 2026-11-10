package br.com.mhr.backend.service;

import java.util.List;
import java.util.UUID;

import br.com.mhr.backend.common.RecursoNaoEncontradoException;
import br.com.mhr.backend.dto.ConfiguracaoLicitacoesDto;
import br.com.mhr.backend.dto.DocumentoLicitacaoDto;
import br.com.mhr.backend.dto.LicitacaoDto;
import br.com.mhr.backend.dto.UsuarioResumoDto;
import br.com.mhr.backend.dto.request.SalvarDocumentoLicitacaoRequest;
import br.com.mhr.backend.dto.request.SalvarLicitacaoRequest;
import br.com.mhr.backend.mapper.DocumentoLicitacaoMapper;
import br.com.mhr.backend.mapper.LicitacaoMapper;
import br.com.mhr.backend.model.ConfiguracaoLicitacoes;
import br.com.mhr.backend.model.DocumentoLicitacao;
import br.com.mhr.backend.model.Licitacao;
import br.com.mhr.backend.repository.ConfiguracaoLicitacoesRepository;
import br.com.mhr.backend.repository.DocumentoLicitacaoRepository;
import br.com.mhr.backend.repository.LicitacaoRepository;
import br.com.mhr.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Regras de negócio de {@link Licitacao}: CRUD manual, status, responsável e o
 * checklist de documentos ({@link DocumentoLicitacao}). A importação automática
 * via PNCP fica em {@link PncpSyncService} — este service só cuida do que o
 * admin faz pelo painel.
 */
@Service
@RequiredArgsConstructor
public class LicitacaoService {

    private final LicitacaoRepository repository;
    private final LicitacaoMapper mapper;
    private final DocumentoLicitacaoRepository documentoRepository;
    private final DocumentoLicitacaoMapper documentoMapper;
    private final ConfiguracaoLicitacoesRepository configuracaoRepository;
    private final UsuarioRepository usuarioRepository;

    /** Para o seletor de "responsável" na tela de licitações — qualquer usuário ativo, qualquer papel. */
    public List<UsuarioResumoDto> listarResponsaveisDisponiveis() {
        return usuarioRepository.findByAtivoTrue().stream()
                .map(usuario -> new UsuarioResumoDto(usuario.getId(), usuario.getNome()))
                .toList();
    }

    public List<LicitacaoDto> listar(Licitacao.StatusLicitacao status) {
        List<Licitacao> licitacoes = status != null ? repository.findByStatus(status) : repository.findTop500ByOrderByCriadoEmDesc();
        return licitacoes.stream().map(mapper::toDto).toList();
    }

    public LicitacaoDto buscarPorId(UUID id) {
        return mapper.toDto(buscarEntidadeOuFalhar(id));
    }

    public LicitacaoDto criar(SalvarLicitacaoRequest request) {
        Licitacao licitacao = mapper.toEntity(request);
        licitacao.setOrigem(Licitacao.Origem.MANUAL);
        return mapper.toDto(repository.save(licitacao));
    }

    public LicitacaoDto atualizar(UUID id, SalvarLicitacaoRequest request) {
        Licitacao licitacao = buscarEntidadeOuFalhar(id);
        mapper.atualizar(request, licitacao);
        return mapper.toDto(repository.save(licitacao));
    }

    public void excluir(UUID id) {
        repository.delete(buscarEntidadeOuFalhar(id));
    }

    public LicitacaoDto atualizarStatus(UUID id, Licitacao.StatusLicitacao status) {
        Licitacao licitacao = buscarEntidadeOuFalhar(id);
        licitacao.setStatus(status);
        return mapper.toDto(repository.save(licitacao));
    }

    public LicitacaoDto atualizarResponsavel(UUID id, UUID responsavelId) {
        Licitacao licitacao = buscarEntidadeOuFalhar(id);
        licitacao.setResponsavelId(responsavelId);
        return mapper.toDto(repository.save(licitacao));
    }

    public List<DocumentoLicitacaoDto> listarDocumentos(UUID licitacaoId) {
        buscarEntidadeOuFalhar(licitacaoId);
        return documentoRepository.findByLicitacaoId(licitacaoId).stream().map(documentoMapper::toDto).toList();
    }

    public DocumentoLicitacaoDto criarDocumento(UUID licitacaoId, SalvarDocumentoLicitacaoRequest request) {
        buscarEntidadeOuFalhar(licitacaoId);
        DocumentoLicitacao documento = new DocumentoLicitacao();
        documento.setLicitacaoId(licitacaoId);
        documento.setNome(request.nome());
        return documentoMapper.toDto(documentoRepository.save(documento));
    }

    public DocumentoLicitacaoDto atualizarDocumento(UUID documentoId, boolean concluido) {
        DocumentoLicitacao documento = documentoRepository.findById(documentoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Documento nao encontrado: " + documentoId));
        documento.setConcluido(concluido);
        return documentoMapper.toDto(documentoRepository.save(documento));
    }

    public void excluirDocumento(UUID documentoId) {
        DocumentoLicitacao documento = documentoRepository.findById(documentoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Documento nao encontrado: " + documentoId));
        documentoRepository.delete(documento);
    }

    public ConfiguracaoLicitacoesDto obterConfiguracao() {
        ConfiguracaoLicitacoes configuracao = buscarConfiguracao();
        return new ConfiguracaoLicitacoesDto(configuracao.isBuscaPncpAtiva(), configuracao.getPalavrasChave());
    }

    public ConfiguracaoLicitacoesDto atualizarConfiguracao(boolean ativa, String palavrasChave) {
        ConfiguracaoLicitacoes configuracao = buscarConfiguracao();
        configuracao.setBuscaPncpAtiva(ativa);
        configuracao.setPalavrasChave(palavrasChave);
        configuracaoRepository.save(configuracao);
        return new ConfiguracaoLicitacoesDto(ativa, palavrasChave);
    }

    private ConfiguracaoLicitacoes buscarConfiguracao() {
        return configuracaoRepository.findById(ConfiguracaoLicitacoes.ID_UNICO)
                .orElseThrow(() -> new IllegalStateException(
                        "Linha de configuracao de licitacoes nao encontrada (migration V6 deveria te-la criado)"));
    }

    private Licitacao buscarEntidadeOuFalhar(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Licitacao nao encontrada: " + id));
    }
}
