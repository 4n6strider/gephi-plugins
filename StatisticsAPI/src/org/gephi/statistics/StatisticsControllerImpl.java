/*
Copyright 2008-2010 Gephi
Authors : Patick J. McSweeney <pjmcswee@syr.edu>,
Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.statistics;

import org.gephi.statistics.spi.StatisticsBuilder;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.api.*;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.project.api.ProjectController;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.longtask.api.LongTaskExecutor;
import org.gephi.utils.longtask.api.LongTaskListener;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceListener;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 * @author Patrick J. McSweeney
 */
@ServiceProvider(service = StatisticsController.class)
public class StatisticsControllerImpl implements StatisticsController {

    private final StatisticsBuilder[] statisticsBuilders;
    private StatisticsModelImpl model;

    public StatisticsControllerImpl() {
        statisticsBuilders = Lookup.getDefault().lookupAll(StatisticsBuilder.class).toArray(new StatisticsBuilder[0]);

        //Workspace events
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.addWorkspaceListener(new WorkspaceListener() {

            public void initialize(Workspace workspace) {
                workspace.add(new StatisticsModelImpl());
            }

            public void select(Workspace workspace) {
                model = workspace.getLookup().lookup(StatisticsModelImpl.class);
                if (model == null) {
                    model = new StatisticsModelImpl();
                    workspace.add(model);
                }
            }

            public void unselect(Workspace workspace) {
            }

            public void close(Workspace workspace) {
            }

            public void disable() {
                model = null;
            }
        });

        if (pc.getCurrentWorkspace() != null) {
            model = pc.getCurrentWorkspace().getLookup().lookup(StatisticsModelImpl.class);
            if (model == null) {
                model = new StatisticsModelImpl();
                pc.getCurrentWorkspace().add(model);
            }
        }
    }

    public void execute(final Statistics statistics, LongTaskListener listener) {
        StatisticsBuilder builder = getBuilder(statistics.getClass());
        LongTaskExecutor executor = new LongTaskExecutor(true, "Statistics " + builder.getName(), 10);
        if (listener != null) {
            executor.setLongTaskListener(listener);
        }
        LongTask task = statistics instanceof LongTask ? (LongTask) statistics : null;

        executor.execute(task, new Runnable() {

            public void run() {
                execute(statistics);
            }
        }, builder.getName(), null);
    }

    public void execute(Statistics statistics) {
        final GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        final GraphModel graphModel = graphController.getModel();
        final AttributeModel attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel();
        statistics.execute(graphModel, attributeModel);
        model.addReport(statistics);
    }

    public StatisticsBuilder getBuilder(Class<? extends Statistics> statisticsClass) {
        for (StatisticsBuilder b : statisticsBuilders) {
            if (b.getStatisticsClass().equals(statisticsClass)) {
                return b;
            }
        }
        return null;
    }

    public StatisticsModelImpl getModel() {
        return model;
    }

    public StatisticsModel getModel(Workspace workspace) {
        StatisticsModel statModel = workspace.getLookup().lookup(StatisticsModelImpl.class);
        if (statModel == null) {
            statModel = new StatisticsModelImpl();
            workspace.add(statModel);
        }
        return statModel;
    }
}
