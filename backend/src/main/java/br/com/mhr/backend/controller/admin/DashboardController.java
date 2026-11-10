package br.com.mhr.backend.controller.admin;

import br.com.mhr.backend.dto.DashboardGraficosDto;
import br.com.mhr.backend.dto.DashboardResumoDto;
import br.com.mhr.backend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Números e gráficos para a tela inicial do painel admin. Aberto a qualquer usuário
 * autenticado (ADMIN ou EDITOR); só a gestão de usuários exige ADMIN (ver
 * {@link br.com.mhr.backend.controller.admin.UsuarioAdminController}).
 */
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService service;

    @GetMapping
    public DashboardResumoDto resumo() {
        return service.resumo();
    }

    @GetMapping("/graficos")
    public DashboardGraficosDto graficos() {
        return service.graficos();
    }
}
