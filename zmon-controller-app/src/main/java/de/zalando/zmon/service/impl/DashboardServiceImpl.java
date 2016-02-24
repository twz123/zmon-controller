package de.zalando.zmon.service.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Preconditions;
import com.google.common.primitives.Ints;

import de.zalando.zmon.domain.Dashboard;
import de.zalando.zmon.event.ZMonEventType;
import de.zalando.zmon.exception.ZMonException;
import de.zalando.zmon.persistence.DashboardOperationResult;
import de.zalando.zmon.persistence.DashboardSProcService;
import de.zalando.zmon.service.DashboardService;

@Service
@Transactional
public class DashboardServiceImpl implements DashboardService {

    private final Logger log = LoggerFactory.getLogger(DashboardServiceImpl.class);

    private static final Comparator<Dashboard> DASHBOARD_ID_COMPARATOR = new Comparator<Dashboard>() {

        @Override
        public int compare(final Dashboard o1, final Dashboard o2) {
            return Ints.compare(o1.getId(), o2.getId());
        }
    };

    private final NoOpEventLog eventLog;

    private final DashboardSProcService dashboardSProc;

    @Autowired
    public DashboardServiceImpl(NoOpEventLog eventLog, DashboardSProcService dashboardSProc) {
        this.eventLog = eventLog;
        this.dashboardSProc = dashboardSProc;
    }

    @Override
    public List<Dashboard> getDashboards(final List<Integer> dashboardIds) {
        final List<Dashboard> dashboards = dashboardSProc.getDashboards(dashboardIds);
        Collections.sort(dashboards, DASHBOARD_ID_COMPARATOR);

        return dashboards;
    }

    @Override
    public List<Dashboard> getAllDashboards() {
        final List<Dashboard> dashboards = dashboardSProc.getAllDashboards();
        Collections.sort(dashboards, DASHBOARD_ID_COMPARATOR);

        return dashboards;
    }

    @Override
    public Dashboard createOrUpdateDashboard(final Dashboard dashboard) throws ZMonException {
        Preconditions.checkNotNull(dashboard);
        log.info("Saving dashboard '{}' request from user '{}'", dashboard.getId(), dashboard.getLastModifiedBy());

        final DashboardOperationResult result = dashboardSProc.createOrUpdateDashboard(dashboard)
                                                              .throwExceptionOnFailure();
        final Dashboard entity = result.getEntity();

        eventLog.log(dashboard.getId() == null ? ZMonEventType.DASHBOARD_CREATED : ZMonEventType.DASHBOARD_UPDATED,
            entity.getId(), entity.getName(), entity.getWidgetConfiguration(), entity.getAlertTeams(),
            entity.getViewMode(), entity.getEditOption(), entity.getSharedTeams(), entity.getLastModifiedBy());

        return entity;
    }

    @Override
    public void deleteDashboard(final Integer dashboardId) {
        Preconditions.checkNotNull(dashboardId);
        log.info("Delete dashboard '{}' request from user '{}'", dashboardId);

        dashboardSProc.deleteDashboard(dashboardId);
    }
}
