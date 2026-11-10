package br.com.mhr.backend.controller.admin;

import br.com.mhr.backend.dto.SistemaInfoDto;
import br.com.mhr.backend.service.SistemaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Informações de versão/ambiente para a tela "Versão" do admin (protegido por JWT, ver SecurityConfig). */
@RestController
@RequestMapping("/api/admin/sistema")
@RequiredArgsConstructor
public class SistemaAdminController {

    private final SistemaService service;

    @GetMapping("/versao")
    public SistemaInfoDto versao() {
        return service.obterInfo();
    }
}
