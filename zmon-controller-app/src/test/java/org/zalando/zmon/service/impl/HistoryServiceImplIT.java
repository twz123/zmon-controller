package org.zalando.zmon.service.impl;

import java.util.List;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.zalando.zmon.domain.ActivityDiff;
import org.zalando.zmon.domain.AlertDefinition;
import org.zalando.zmon.domain.CheckDefinition;
import org.zalando.zmon.domain.CheckDefinitionImport;
import org.zalando.zmon.domain.HistoryReport;
import org.zalando.zmon.generator.AlertDefinitionGenerator;
import org.zalando.zmon.generator.CheckDefinitionImportGenerator;
import org.zalando.zmon.generator.DataGenerator;
import org.zalando.zmon.service.AlertService;
import org.zalando.zmon.service.HistoryService;
import org.zalando.zmon.service.ZMonService;

@ContextConfiguration(classes = ServiceTestConfiguration.class)
@Transactional
@DirtiesContext
public class HistoryServiceImplIT extends AbstractServiceIntegrationTest {

    @Autowired
    private HistoryService historyService;

    @Autowired
    private AlertService alertService;

    @Autowired
    private ZMonService service;

    private DataGenerator<CheckDefinitionImport> checkImportGenerator;
    private DataGenerator<AlertDefinition> alertGenerator;

    @Before
    public void setup() {
        checkImportGenerator = new CheckDefinitionImportGenerator();
        alertGenerator = new AlertDefinitionGenerator();
    }

    @Test
    public void testGetCheckDefinitionHistory() throws Exception {

        final CheckDefinition newCheckDefinition = service.createOrUpdateCheckDefinition(
                checkImportGenerator.generate());

        // TODO test history pagination
        // TODO improve this test: check each field
        final List<ActivityDiff> history = historyService.getCheckDefinitionHistory(newCheckDefinition.getId(), 10,
                null, null);

        MatcherAssert.assertThat(history, Matchers.hasSize(1));
    }

    @Test
    public void testGetAlertDefinitionHistory() throws Exception {

        final CheckDefinition newCheckDefinition = service.createOrUpdateCheckDefinition(
                checkImportGenerator.generate());

        AlertDefinition newAlertDefinition = alertGenerator.generate();
        newAlertDefinition.setCheckDefinitionId(newCheckDefinition.getId());
        newAlertDefinition = alertService.createOrUpdateAlertDefinition(newAlertDefinition);

        // TODO test history pagination
        // TODO improve this test: check each field
        final List<ActivityDiff> history = historyService.getAlertDefinitionHistory(newAlertDefinition.getId(), 10,
                null, null);

        MatcherAssert.assertThat(history, Matchers.hasSize(1));
    }

    @Test
    public void testHistoryReportWithoutTimeRange() throws Exception {
        final CheckDefinitionImport toImport = checkImportGenerator.generate();
        toImport.setOwningTeam("Platform/Software");

        final CheckDefinition newCheckDefinition = service.createOrUpdateCheckDefinition(toImport);

        AlertDefinition newAlertDefinition = alertGenerator.generate();
        newAlertDefinition.setTeam("Platform/Database");
        newAlertDefinition.setResponsibleTeam("Platform/System");
        newAlertDefinition.setCheckDefinitionId(newCheckDefinition.getId());
        newAlertDefinition = alertService.createOrUpdateAlertDefinition(newAlertDefinition);

        final List<HistoryReport> history = historyService.getHistoryReport(newAlertDefinition.getTeam(),
                newAlertDefinition.getResponsibleTeam(), null, null);

        // TODO improve this test: check each field
        MatcherAssert.assertThat(history, Matchers.hasSize(2));
    }

    @Test
    public void testHistoryReportWithinTimeRange() throws Exception {
        final CheckDefinitionImport toImport = checkImportGenerator.generate();
        toImport.setOwningTeam("Platform/Software");

        final CheckDefinition newCheckDefinition = service.createOrUpdateCheckDefinition(toImport);

        AlertDefinition newAlertDefinition = alertGenerator.generate();
        newAlertDefinition.setTeam("Platform/Database");
        newAlertDefinition.setResponsibleTeam("Platform/System");
        newAlertDefinition.setCheckDefinitionId(newCheckDefinition.getId());
        newAlertDefinition = alertService.createOrUpdateAlertDefinition(newAlertDefinition);

        final long from = newAlertDefinition.getLastModified().getTime() / 1000;
        final long to = from + 1;

        final List<HistoryReport> history = historyService.getHistoryReport(newAlertDefinition.getTeam(),
                newAlertDefinition.getResponsibleTeam(), from, to);

        // TODO improve this test: check each field
        MatcherAssert.assertThat(history, Matchers.hasSize(2));
    }

    @Test
    public void testHistoryReportOutsideTimeRange() throws Exception {
        final CheckDefinitionImport toImport = checkImportGenerator.generate();
        toImport.setOwningTeam("Platform/Software");

        final CheckDefinition newCheckDefinition = service.createOrUpdateCheckDefinition(toImport);

        AlertDefinition newAlertDefinition = alertGenerator.generate();
        newAlertDefinition.setTeam("Platform/Database");
        newAlertDefinition.setResponsibleTeam("Platform/System");
        newAlertDefinition.setCheckDefinitionId(newCheckDefinition.getId());
        newAlertDefinition = alertService.createOrUpdateAlertDefinition(newAlertDefinition);

        final long from = newAlertDefinition.getLastModified().getTime() / 10000;
        final long to = from + 1;

        final List<HistoryReport> history = historyService.getHistoryReport(newAlertDefinition.getTeam(),
                newAlertDefinition.getResponsibleTeam(), from, to);

        // TODO improve this test: check each field
        MatcherAssert.assertThat(history, Matchers.hasSize(0));
    }

    @Test
    public void testHistoryReportWithoutTimeRangeAndNonExistingTeams() throws Exception {

        final CheckDefinitionImport toImport = checkImportGenerator.generate();
        final CheckDefinition newCheckDefinition = service.createOrUpdateCheckDefinition(toImport);

        AlertDefinition newAlertDefinition = alertGenerator.generate();
        newAlertDefinition.setCheckDefinitionId(newCheckDefinition.getId());
        newAlertDefinition = alertService.createOrUpdateAlertDefinition(newAlertDefinition);

        final List<HistoryReport> history = historyService.getHistoryReport("FOO" + newAlertDefinition.getTeam(),
                "BAR" + newAlertDefinition.getResponsibleTeam(), null, null);

        // TODO improve this test: check each field
        MatcherAssert.assertThat(history, Matchers.hasSize(0));
    }
}
