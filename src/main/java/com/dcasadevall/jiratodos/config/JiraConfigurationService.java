package com.dcasadevall.jiratodos.config;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.util.xmlb.XmlSerializerUtil;

@State(name = "JiraConfiguration")
class JiraConfigurationService
    implements PersistentStateComponent<JiraConfigurationService>, JiraConfiguration {
  private String username;
  private int projectId;
  private String baseUrl;

  public JiraConfigurationService getState() {
    return this;
  }

  public void loadState(JiraConfigurationService state) {
    XmlSerializerUtil.copyBean(state, this);
  }

  @Override
  public String getUsername() {
    return username;
  }

  @Override
  public void setUserName(String username) {
    this.username = username;
  }

  @Override
  public int getProjectId() {
    return projectId;
  }

  @Override
  public void setProjectId(int projectId) {
    this.projectId = projectId;
  }

  @Override
  public String getBaseUrl() {
    return baseUrl;
  }

  @Override
  public void setBaseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
  }
}
