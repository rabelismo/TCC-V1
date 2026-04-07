package com.ufc.tcc_gr.config;

import tools.jackson.databind.ObjectMapper;
import com.ufc.tcc_gr.model.AcceptanceCriterion;
import com.ufc.tcc_gr.model.Module;
import com.ufc.tcc_gr.repository.AcceptanceCriterionRepository;
import com.ufc.tcc_gr.repository.ModuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final ObjectMapper objectMapper;

    @Bean
    CommandLineRunner initModules(ModuleRepository moduleRepo,
                                  AcceptanceCriterionRepository criterionRepo) {
        return args -> {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath:modules/module-*.json");

            if (resources.length == 0) {
                log.info("Nenhum JSON de módulo encontrado.");
                return;
            }

            Arrays.sort(resources, Comparator.comparing(Resource::getFilename));
            int created = 0, updated = 0;

            for (Resource resource : resources) {
                try {
                    ModuleData data = objectMapper.readValue(resource.getInputStream(), ModuleData.class);

                    Optional<Module> existing = moduleRepo.findByOrderIndex(data.getOrderIndex());
                    Module module;

                    if (existing.isPresent()) {
                        module = existing.get();
                        module.setTitle(data.getTitle());
                        module.setConcept(data.getConcept());
                        module.setDescription(data.getDescription());
                        module.setTheoryMarkdown(data.getTheoryMarkdown());
                        module.setStarterCode(data.getStarterCode());
                        module.setExpectedOutput(data.getExpectedOutput());
                        module.setSampleInput(data.getSampleInput());
                        module = moduleRepo.save(module);

                        criterionRepo.deleteByModuleId(module.getId());
                        updated++;
                    } else {
                        module = moduleRepo.save(Module.builder()
                                .orderIndex(data.getOrderIndex())
                                .title(data.getTitle())
                                .concept(data.getConcept())
                                .description(data.getDescription())
                                .theoryMarkdown(data.getTheoryMarkdown())
                                .starterCode(data.getStarterCode())
                                .expectedOutput(data.getExpectedOutput())
                                .sampleInput(data.getSampleInput())
                                .build());
                        created++;
                    }

                    if (data.getCriteria() != null) {
                        for (ModuleData.CriterionData cd : data.getCriteria()) {
                            criterionRepo.save(AcceptanceCriterion.builder()
                                    .module(module)
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

                } catch (Exception e) {
                    log.error("Erro ao processar {}: {}", resource.getFilename(), e.getMessage());
                }
            }

            log.info("Módulos sincronizados: {} criados, {} atualizados (progresso preservado)",
                    created, updated);
        };
    }
}
