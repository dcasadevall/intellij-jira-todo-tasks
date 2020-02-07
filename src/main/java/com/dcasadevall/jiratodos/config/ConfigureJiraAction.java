package com.dcasadevall.jiratodos.config;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

public class ConfigureJiraAction extends AnAction {
  @Override
  public void update(AnActionEvent event) {}

  @Override
  public void actionPerformed(@NotNull AnActionEvent event) {
    Project project = event.getProject();
    if (project == null) {
      return;
    }

    JiraConfiguration jiraConfiguration =
        ServiceManager.getService(project, JiraConfiguration.class);
    String username = jiraConfiguration.getUsername();
    username =
        Messages.showInputDialog(
            "Please enter your JIRA username", "JIRA Username", null, username, null);
    jiraConfiguration.setUserName(username);

    int projectId = jiraConfiguration.getProjectId();
    String projectIdString =
        Messages.showInputDialog(
            "Please enter your Project Id. (Go to Project Details page and look a the value of 'PID' in the URL",
            "Project ID",
            null,
            Integer.toString(projectId),
            null);
    try {
      projectId = Integer.parseInt(projectIdString);
      jiraConfiguration.setProjectId(projectId);
    } catch (Exception ignored) {
    }
  }
}
