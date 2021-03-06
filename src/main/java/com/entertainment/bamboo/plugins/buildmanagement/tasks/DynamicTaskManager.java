package com.entertainment.bamboo.plugins.buildmanagement.tasks;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.chains.ChainExecutionManager;
import com.atlassian.bamboo.configuration.ConfigurationMap;
import com.atlassian.bamboo.plan.PlanExecutionManager;
import com.atlassian.bamboo.plan.PlanResultKey;
import com.atlassian.bamboo.task.*;
import com.atlassian.bamboo.task.runtime.RuntimeTaskDefinition;
import com.atlassian.bamboo.variable.CustomVariableContext;
import com.atlassian.bamboo.variable.VariableContext;
import com.atlassian.bamboo.variable.VariableDefinitionContext;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.entertainment.bamboo.plugins.buildmanagement.tasks.utils.GroovyUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.RegexValidator;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dwang on 9/28/16.
 */

@Scanned
public class DynamicTaskManager implements TaskType{

    // Define a custom variable context property at the class level
    @ComponentImport
    private CustomVariableContext customVariableContext;

    @ComponentImport
    private PlanExecutionManager planExecutionManager;

    public DynamicTaskManager(@NotNull final CustomVariableContext customVariableContext, @NotNull final PlanExecutionManager planExecutionManager) {
        this.customVariableContext=customVariableContext;
        this.planExecutionManager= planExecutionManager;
    }
    @NotNull
    public TaskResult execute(@NotNull TaskContext taskContext) throws TaskException {
        final BuildLogger buildLogger = taskContext.getBuildLogger();
        ConfigurationMap configurationMap = taskContext.getConfigurationMap();
        PlanResultKey planResultKey=taskContext.getBuildContext().getPlanResultKey();

        String deActivateExpr = configurationMap.get(DynamicTaskManagerConfigurator.DYNMGR_DEACTIVE_EXPR);
        String activateExpr = configurationMap.get(DynamicTaskManagerConfigurator.DYNMGR_ACTIVE_EXPR);
        String activateTasks = configurationMap.get(DynamicTaskManagerConfigurator.DYNMGR_TASKS_ACTIVATE);
        String deactivateTasks = configurationMap.get(DynamicTaskManagerConfigurator.DYNMGR_TASKS_DEACTIVATE);

        RegexValidator deactivateValidator = null;
        if (StringUtils.isNotBlank(deactivateTasks))
            deactivateValidator= new RegexValidator(deactivateTasks);
        RegexValidator activateValidator = null;
        if (StringUtils.isNotBlank(activateTasks))
            activateValidator= new RegexValidator(activateTasks);

        Map<String, VariableDefinitionContext> variables = this.customVariableContext.getVariableContexts();
        //Map<String, VariableDefinitionContext> variables = taskContext.getBuildContext().getVariableContext().getEffectiveVariables();
        Collection<VariableDefinitionContext> existingVariables =variables.values();
        Map<String, String> passVariables = new HashMap<String, String>();
        for(VariableDefinitionContext vcontext :existingVariables) {
            String key = vcontext.getKey();
            String value = vcontext.getValue();
            //buildLogger.addBuildLogEntry("Variable Context: "+key+"="+value);
            passVariables.put(key,value);
        }

        boolean isToDeactivate=false;
        if (StringUtils.isNotBlank(deActivateExpr)) {
            buildLogger.addBuildLogEntry("Evaluating groovy logical expression: " +deActivateExpr);
            isToDeactivate = GroovyUtil.eval(passVariables, deActivateExpr);
        }

        boolean isToActivate=false;
        if (StringUtils.isNotBlank(activateExpr)) {
            buildLogger.addBuildLogEntry("Evaluating groovy logical expression: " +deActivateExpr);
            isToActivate=GroovyUtil.eval(passVariables,activateExpr);
        }

        //TaskConfigurationServiceImpl.setTaskState
        //This get task list after/include current task
        List<RuntimeTaskDefinition> definitions=taskContext.getBuildContext().getRuntimeTaskDefinitions();
        for (RuntimeTaskDefinition def :definitions) {
            String taskDescription = def.getUserDescription();
            buildLogger.addBuildLogEntry("task description: " + taskDescription);
            //if(taskDescription.equalsIgnoreCase("TestBash")) def.setEnabled(false);
            if (isToDeactivate) {
                if (deactivateValidator.isValid(taskDescription) && def.isEnabled() ) {
                    buildLogger.addBuildLogEntry("Deactivating Enabled Task: " +taskDescription);
                    def.setEnabled(false);
                }
            }

            if (isToActivate) {
                if (activateValidator.isValid(taskDescription) && !def.isEnabled()) {
                    buildLogger.addBuildLogEntry("Activating Disabled Task: " +taskDescription);
                    def.setEnabled(true);
                }
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
