package com.dcasadevall.jiratodos.editor;

import com.dcasadevall.jiratodos.config.JiraConfiguration;
import com.google.common.base.Strings;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.TextRange;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;

public class CreateTaskFromTodoEditorPopupAction extends AnAction {
  @Override
  public void actionPerformed(@NotNull AnActionEvent event) {
    final Project project = event.getProject();
    final Editor editor = event.getData(CommonDataKeys.EDITOR);
    if (project == null || editor == null) {
      return;
    }

    Caret caret = editor.getCaretModel().getPrimaryCaret();
    final Document document = editor.getDocument();
    String line =
        document.getText(new TextRange(caret.getVisualLineStart(), caret.getVisualLineEnd()));
    if (!line.contains("TODO")) {
      return;
    }

    // Let's make the following assumptions:
    // - TODOs start with TODO and a colon after it.
    // - There may be a space between the TODO: and the task.
    // - Users may add their LDAP / Username with parentheses. i.e: TODO(dpino):
    // - A TODO ends when the current comment ends.
    // - We assume only Java.. so a comment can be ended by either:
    //     - No more // before text on a new line.
    //     - A */ in the current or new line.
    //       (We will simplify this check and assume that if we see a */, we are currently inside a
    //        block started by /*)
    // - A TODO will never be added on the last line of a document, with no newline after it.
    Pattern pattern = Pattern.compile(".*TODO.*:\\s*(.*)\\s*");
    Matcher matcher = pattern.matcher(line);
    if (!matcher.matches() || matcher.groupCount() == 0) {
      return;
    }

    String summary = matcher.group(1);
    String remainingText =
        document.getText(new TextRange(caret.getVisualLineEnd() + 1, document.getTextLength() - 1));
    String[] lines = remainingText.split("\n");
    StringBuilder stringBuilder = new StringBuilder();
    for (String commentLine : lines) {
      CommentLineParseResult commentLineParseResult = tryReadCommentedLine(commentLine);
      if (commentLineParseResult.isCommentEnd) {
        break;
      }

      stringBuilder.append(commentLineParseResult.comment);
      stringBuilder.append("\n");
    }

    JiraConfiguration jiraConfiguration = ServiceManager.getService(project, JiraConfiguration.class);
    String username = jiraConfiguration.getUsername();
    if (Strings.isNullOrEmpty(username)) {
      username = Messages.showInputDialog("Please enter your JIRA username", "JIRA Username", null);
      jiraConfiguration.setUserName(username);
    }

    int projectId = jiraConfiguration.getProjectId();
    if (projectId <= 0) {
      String projectIdString =
          Messages.showInputDialog(
              "Please enter your Project Id. (Go to Project Details page and look a the value of 'PID' in the URL",
              "Project ID",
              null);
      try {
        projectId = Integer.parseInt(projectIdString);
        jiraConfiguration.setProjectId(projectId);
      } catch (Exception ignored) {
        return;
      }
    }

    String baseUrl = jiraConfiguration.getBaseUrl();
    if (Strings.isNullOrEmpty(baseUrl)) {
      baseUrl = Messages.showInputDialog("Please enter your JIRA base url", "JIRA Base Url", null);
      jiraConfiguration.setBaseUrl(baseUrl);
    }

    if (projectId <= 0 || Strings.isNullOrEmpty(username) || Strings.isNullOrEmpty(baseUrl)) {
      return;
    }

    String description = stringBuilder.toString();
    BrowserUtil.browse(
        String.format(
            "https://%s/secure/CreateIssueDetails!init.jspa?summary=%s"
                + "&description=%s"
                + "&pid=10093"
                + "&issuetype=10002"
                + "&reporter=%s"
                + "&assignee=%s",
            baseUrl, summary, description, username, username));
  }

  @Override
  public void update(@NotNull final AnActionEvent event) {
    // Get required data keys
    final Project project = event.getProject();
    final Editor editor = event.getData(CommonDataKeys.EDITOR);
    if (project == null || editor == null) {
      event.getPresentation().setEnabledAndVisible(false);
      return;
    }

    Caret caret = editor.getCaretModel().getPrimaryCaret();
    String line =
        editor
            .getDocument()
            .getText(new TextRange(caret.getVisualLineStart(), caret.getVisualLineEnd()));
    if (!line.contains("TODO")) {
      event.getPresentation().setEnabledAndVisible(false);
      return;
    }

    event.getPresentation().setEnabledAndVisible(true);
  }

  private CommentLineParseResult tryReadCommentedLine(String line) {
    Pattern pattern = Pattern.compile(".*\\s?//\\s?(.*)?");
    Matcher matcher = pattern.matcher(line);
    if (!matcher.matches() || matcher.groupCount() < 1) {
      return tryReadAsteriskCommentedLine(line);
    }

    return new CommentLineParseResult(matcher.group(1), false);
  }

  private CommentLineParseResult tryReadAsteriskCommentedLine(String line) {
    Pattern pattern = Pattern.compile(".*?\\*(.*)(\\*/)");
    Matcher matcher = pattern.matcher(line);
    if (!matcher.matches() || matcher.groupCount() < 1) {
      return new CommentLineParseResult("", true);
    }

    boolean isCommentEnd = matcher.groupCount() == 2;
    return new CommentLineParseResult(matcher.group(1), isCommentEnd);
  }

  private class CommentLineParseResult {
    public final String comment;
    public final boolean isCommentEnd;

    private CommentLineParseResult(String comment, boolean isCommentEnd) {
      this.comment = comment;
      this.isCommentEnd = isCommentEnd;
    }
  }
}
