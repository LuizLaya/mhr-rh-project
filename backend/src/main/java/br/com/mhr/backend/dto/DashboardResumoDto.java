package br.com.mhr.backend.dto;

import br.com.mhr.backend.controller.admin.DashboardController;

/** Números de visão geral do painel admin (ver {@link DashboardController}). */
public record DashboardResumoDto(
        long vagasAbertas,
        long candidaturasTotal,
        long servicosPublicados,
        long depoimentosPublicados,
        long postsPublicados,
        long leadsNovos,
        long leadsTotal,
        long usuariosAtivos,
        long clientesTotal
) {
}
