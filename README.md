# IntelliJ Jira TODO Tasks

An IntelliJ plugin that allows the user to create a JIRA task from given TODOs.

This plugin will automatically open the JIRA task creation dialog based on the TODO currently selected in IntelliJ.
It can be accessed via the "Generate" menu (Command + N in Mac 10.5+ bindings) if the caret is currently selecting a line which contains a TODO.
i.e, the following code:

```
// TODO: Work on spawning npcs on the server side.
// See spec for further details.
// Needs design review.
```

Will generate a task with Summary: 'Work on spawning npcs on the server side.' and Description: 'See spec for further details. Needs design review'.

JIRA settings (username, base url, project ID) can be configured either via the Tools -&gt; Configure JIRA menu item, or when the "Jira Task" menu item is selected in the generate menu.
