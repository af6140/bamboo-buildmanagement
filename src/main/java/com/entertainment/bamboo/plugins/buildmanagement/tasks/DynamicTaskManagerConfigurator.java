package com.entertainment.bamboo.plugins.buildmanagement.tasks;

import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.task.AbstractTaskConfigurator;
import com.atlassian.bamboo.task.TaskConfiguratorHelper;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Created by dwang on 9/28/16.
 */
@Component
public class DynamicTaskManagerConfigurator extends AbstractTaskConfigurator{
    public static final String DYNMGR_TASKS_ACTIVATE = "dynMgrTasksActivate";
    public static final String DYNMGR_ACTIVE_EXPR="dynMgrActivateExp";
    public static final String DYNMGR_TASKS_DEACTIVATE = "dynMgrTasksDeActivate";
    public static final String DYNMGR_DEACTIVE_EXPR="dynMgrDeActivateExp";

    private static final Set<String> FIELDS = ImmutableSet.of(
            DYNMGR_TASKS_ACTIVATE,
            DYNMGR_TASKS_DEACTIVATE,
            DYNMGR_ACTIVE_EXPR,
            DYNMGR_DEACTIVE_EXPR
    );

    @Autowired
    public DynamicTaskManagerConfigurator(@ComponentImport final TaskConfiguratorHelper taskConfiguratorHelper) {
        this.taskConfiguratorHelper = taskConfiguratorHelper;
        //this.encryptionService = encryptionService;
    }
    @NotNull
    @Override
    public Map<String, String> generateTaskConfigMap(@NotNull final ActionParametersMap params, @Nullable final TaskDefinition previousTaskDefinition) {
        final Map<String, String> config = super.generateTaskConfigMap(params, previousTaskDefinition);
        taskConfiguratorHelper.populateTaskConfigMapWithActionParameters(config, params, FIELDS);
        return config;
    }

    @Override
    public void validate(@NotNull final ActionParametersMap params, @NotNull final ErrorCollection errorCollection) {
        super.validate(params, errorCollection);

        boolean activateTasks = params.getBoolean(DYNMGR_TASKS_ACTIVATE);

        if (activateTasks) {
            String activateExpr = params.getString(DYNMGR_ACTIVE_EXPR);

            if (StringUtils.isEmpty(activateExpr)) {
                errorCollection.addError(DYNMGR_ACTIVE_EXPR, "Regex is empty");
            } else {
                try {
                    Pattern.compile(activateExpr);
                } catch (PatternSyntaxException e) {
                    errorCollection.addError(DYNMGR_ACTIVE_EXPR, "Not a valid regex: " + e.getDescription());
                }
            }
        }

        boolean deactivateTasks = params.getBoolean(DYNMGR_TASKS_DEACTIVATE);

        if(deactivateTasks) {
            String deactivateExpr = params.getString(DYNMGR_DEACTIVE_EXPR);
            if(StringUtils.isEmpty(deactivateExpr)) {
                errorCollection.addError(DYNMGR_DEACTIVE_EXPR, "Regex is empty");
            }else {
                try {
                    Pattern.compile(deactivateExpr);
                } catch (PatternSyntaxException e) {
                    errorCollection.addError(DYNMGR_DEACTIVE_EXPR, "Not a valid regex:" + e.getDescription());
                }
            }
        }
    }
    @Override
    public void populateContextForView(@NotNull Map<String, Object> context, @NotNull TaskDefinition taskDefinition) {
        super.populateContextForView(context, taskDefinition);
        taskConfiguratorHelper.populateContextWithConfiguration(context, taskDefinition, FIELDS);
    }

    @Override
    public void populateContextForCreate(@NotNull final Map<String, Object> context) {
        super.populateContextForCreate(context);
    }

    @Override
    public void populateContextForEdit(@NotNull final Map<String, Object> context, @NotNull final TaskDefinition taskDefinition) {
        super.populateContextForEdit(context, taskDefinition);
        taskConfiguratorHelper.populateContextWithConfiguration(context, taskDefinition, FIELDS);
    }

}
