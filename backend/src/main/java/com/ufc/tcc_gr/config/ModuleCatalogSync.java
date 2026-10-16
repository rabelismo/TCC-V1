package com.ufc.tcc_gr.config;

import com.ufc.tcc_gr.model.AcceptanceCriterion;
import com.ufc.tcc_gr.model.Module;
import com.ufc.tcc_gr.repository.AcceptanceCriterionRepository;
import com.ufc.tcc_gr.repository.ModuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Comparator;

@Slf4j
@Component
@RequiredArgsConstructor
public class ModuleCatalogSync implements ApplicationRunner {

    private static final String MODULES_PATTERN = "classpath:modules/module-*.json";

    private final ObjectMapper objectMapper;
    private final ModuleRepository moduleRepo;
    private final AcceptanceCriterionRepository criterionRepo;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws IOException {
        Resource[] resources = new PathMatchingResourcePatternResolver().getResources(MODULES_PATTERN);
        if (resources.length == 0) {
            log.info("Nenhum JSON de módulo encontrado.");
            return;
        }
        sync(resources);
    }

    void sync(Resource[] resources) {
        Resource[] sorted = resources.clone();
        Arrays.sort(sorted, Comparator.comparing(Resource::getFilename));
        int created = 0, updated = 0;

        for (Resource resource : sorted) {
            ModuleData data = read(resource);
            Module module = moduleRepo.findByOrderIndex(data.getOrderIndex()).orElse(null);

            if (module == null) {
                module = Module.builder().orderIndex(data.getOrderIndex()).build();
                created++;
            } else {
                criterionRepo.deleteByModuleId(module.getId());
                updated++;
            }

            module.setTitle(data.getTitle());
            module.setConcept(data.getConcept());
            module.setDescription(data.getDescription());
            module.setTheoryMarkdown(data.getTheoryMarkdown());
            module.setStarterCode(data.getStarterCode());
            module.setExpectedOutput(data.getExpectedOutput());
            module.setSampleInput(data.getSampleInput());
            Module saved = moduleRepo.save(module);

            if (data.getCriteria() != null) {
                for (ModuleData.CriterionData cd : data.getCriteria()) {
                    criterionRepo.save(AcceptanceCriterion.builder()
                            .module(saved)
                            .orderIndex(cd.getOrderIndex())
                            .type(cd.getType())
                            .description(cd.getDescription())
                            .input(cd.getInput())
                            .expectedOutput(cd.getExpectedOutput())
                            .hint(cd.getHint())
                            .hidden(cd.getHidden() != null ? cd.getHidden() : false)
                            .build());
                }
            }
        }

        log.info("Módulos sincronizados: {} criados, {} atualizados (progresso preservado)",
                created, updated);
    }

    private ModuleData read(Resource resource) {
        try (InputStream in = resource.getInputStream()) {
            ModuleData data = objectMapper.readValue(in, ModuleData.class);
            if (data.getOrderIndex() == null) {
                throw new IllegalStateException("orderIndex ausente");
            }
            return data;
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Módulo inválido em " + resource.getFilename() + ": " + e.getMessage(), e);
        }
    }
}
