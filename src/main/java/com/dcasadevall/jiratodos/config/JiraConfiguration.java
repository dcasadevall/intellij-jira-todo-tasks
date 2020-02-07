package com.dcasadevall.jiratodos.config;

public interface JiraConfiguration {
  String getUsername();
  void setUserName(String username);
  int getProjectId();
  void setProjectId(int projectId);

  String getBaseUrl();
  void setBaseUrl(String baseUrl);
}
