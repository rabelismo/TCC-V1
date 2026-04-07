package com.ufc.tcc_gr.config;

import tools.jackson.databind.ObjectMapper;
import com.ufc.tcc_gr.model.AcceptanceCriterion;
import com.ufc.tcc_gr.model.Module;
import com.ufc.tcc_gr.repository.AcceptanceCriterionRepository;
import com.ufc.tcc_gr.repository.ModuleRepository;
import com.ufc.tcc_gr.repository.StudentProgressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.util.Arrays;
import java.util.Comparator;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final ObjectMapper objectMapper;

    @Bean
    CommandLineRunner initModules(ModuleRepository moduleRepo,
                                  AcceptanceCriterionRepository criterionRepo,
                                  StudentProgressRepository progressRepo) {
        return args -> {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath:modules/module-*.json");

            if (moduleRepo.count() == resources.length && criterionRepo.count() > 0) return;

            log.info("Reinicializando módulos ({} JSONs encontrados)...", resources.length);

            progressRepo.deleteAll();
            criterionRepo.deleteAll();
            moduleRepo.deleteAll();

            Arrays.sort(resources, Comparator.comparing(Resource::getFilename));

            for (Resource resource : resources) {
                ModuleData data = objectMapper.readValue(resource.getInputStream(), ModuleData.class);

                Module module = moduleRepo.save(Module.builder()
                        .orderIndex(data.getOrderIndex())
                        .title(data.getTitle())
                        .concept(data.getConcept())
                        .description(data.getDescription())
                        .theoryMarkdown(data.getTheoryMarkdown())
                        .starterCode(data.getStarterCode())
                        .expectedOutput(data.getExpectedOutput())
                        .build());

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

                log.info("Módulo carregado: {} - {}", data.getOrderIndex(), data.getTitle());
            }

            log.info("Total de módulos carregados: {}", moduleRepo.count());
        };
    }
}
