package com.entertainment.bamboo.plugins.buildmanagement.tasks;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.configuration.ConfigurationMap;
import com.atlassian.bamboo.plan.PlanExecutionManager;
import com.atlassian.bamboo.plan.PlanResultKey;
import com.atlassian.bamboo.task.*;
import com.atlassian.bamboo.variable.CustomVariableContext;
import com.atlassian.bamboo.variable.VariableDefinitionContext;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.entertainment.bamboo.plugins.buildmanagement.tasks.utils.GroovyUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by dwang on 11/2/16.
 */
public class StopBuildManager implements TaskType {

    @ComponentImport
    private CustomVariableContext customVariableContext;

    @ComponentImport
    private PlanExecutionManager planExecutionManager;


    public StopBuildManager(@NotNull final CustomVariableContext customVariableContext, @NotNull final PlanExecutionManager planExecutionManager) {
        this.customVariableContext=customVariableContext;
        this.planExecutionManager= planExecutionManager;
    }
    @NotNull
    public TaskResult execute(@NotNull TaskContext taskContext) throws TaskException {
        final BuildLogger buildLogger = taskContext.getBuildLogger();
        ConfigurationMap configurationMap = taskContext.getConfigurationMap();
        PlanResultKey planResultKey=taskContext.getBuildContext().getPlanResultKey();

        Map<String, VariableDefinitionContext> variables = this.customVariableContext.getVariableContexts();
        //Map<String, VariableDefinitionContext> variables = taskContext.getBuildContext().getVariableContext().getEffectiveVariables();
        Collection<VariableDefinitionContext> existingVariables =variables.values();
        Map<String, String> passVariables = new HashMap<String, String>();
        for(VariableDefinitionContext vcontext :existingVariables) {
            String key = vcontext.getKey();
            String value = vcontext.getValue();
            passVariables.put(key,value);
        }

        String stopExpr = configurationMap.get(StopBuildManagerConfigurator.DYNMGR_STOP_EXPR);

        if (StringUtils.isNotBlank(stopExpr)) {
            buildLogger.addBuildLogEntry("Evaluating groovy logical expression: " +stopExpr);
            boolean isToStop = GroovyUtil.eval(passVariables, stopExpr);

            if (isToStop) {
                buildLogger.addBuildLogEntry("Stop plan execution, other jobs in current stage may still run since jobs in stage can be run in parallel.");
                this.planExecutionManager.stopPlan(planResultKey,false, "admin");
            }
        }
        //this.planExecutionManager.stopPlan(planResultKey,false, "admin");
        return success(taskContext);
    }
    public TaskResult success(TaskContext taskContext) {
        return TaskResultBuilder.newBuilder(taskContext).success().build();
    }

    public TaskResult failed(TaskContext taskContext) {
        return TaskResultBuilder.newBuilder(taskContext).failed().build();
    }
}
