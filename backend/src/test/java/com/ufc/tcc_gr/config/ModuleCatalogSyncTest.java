package com.ufc.tcc_gr.config;

import com.ufc.tcc_gr.model.AcceptanceCriterion;
import com.ufc.tcc_gr.model.Module;
import com.ufc.tcc_gr.repository.AcceptanceCriterionRepository;
import com.ufc.tcc_gr.repository.ModuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ModuleCatalogSyncTest {

    @Mock
    private ModuleRepository moduleRepo;

    @Mock
    private AcceptanceCriterionRepository criterionRepo;

    private ModuleCatalogSync sync;

    @BeforeEach
    void setUp() {
        sync = new ModuleCatalogSync(JsonMapper.builder().build(), moduleRepo, criterionRepo);
    }

    private Resource json(String filename, String content) {
        return new ByteArrayResource(content.getBytes(StandardCharsets.UTF_8)) {
            @Override
            public String getFilename() {
                return filename;
            }
        };
    }

    private static final String MODULE_ONE = """
            {"orderIndex": 1, "title": "Olá", "theoryMarkdown": "# T",
             "criteria": [{"orderIndex": 1, "type": "OUTPUT_CONTAINS", "description": "d", "expectedOutput": "Olá"}]}
            """;

    @Test
    @DisplayName("JSON inválido interrompe a sincronização com o nome do arquivo")
    void sync_invalidJson_failsFast() {
        Resource broken = json("module-02.json", "{ isto não é json");

        assertThatThrownBy(() -> sync.sync(new Resource[]{broken}))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("module-02.json");
        verifyNoInteractions(criterionRepo);
    }

    @Test
    @DisplayName("Módulo sem orderIndex é rejeitado")
    void sync_missingOrderIndex_failsFast() {
        Resource noIndex = json("module-03.json", "{\"title\": \"x\"}");

        assertThatThrownBy(() -> sync.sync(new Resource[]{noIndex}))
                .hasMessageContaining("module-03.json");
    }

    @Test
    @DisplayName("Módulo novo é criado com seus critérios; hidden assume false")
    void sync_newModule_createsWithCriteria() {
        when(moduleRepo.findByOrderIndex(1)).thenReturn(Optional.empty());
        when(moduleRepo.save(any(Module.class))).thenAnswer(inv -> inv.getArgument(0));

        sync.sync(new Resource[]{json("module-01.json", MODULE_ONE)});

        ArgumentCaptor<AcceptanceCriterion> saved = ArgumentCaptor.forClass(AcceptanceCriterion.class);
        verify(criterionRepo).save(saved.capture());
        assertThat(saved.getValue().getHidden()).isFalse();
        verify(criterionRepo, never()).deleteByModuleId(any());
    }

    @Test
    @DisplayName("Módulo existente é atualizado no lugar e tem critérios recriados")
    void sync_existingModule_updatesInPlace() {
        Module existing = Module.builder().id(42L).orderIndex(1).title("antigo").build();
        when(moduleRepo.findByOrderIndex(1)).thenReturn(Optional.of(existing));
        when(moduleRepo.save(existing)).thenReturn(existing);

        sync.sync(new Resource[]{json("module-01.json", MODULE_ONE)});

        assertThat(existing.getTitle()).isEqualTo("Olá");
        verify(criterionRepo).deleteByModuleId(42L);
        verify(criterionRepo).save(any(AcceptanceCriterion.class));
    }
}
