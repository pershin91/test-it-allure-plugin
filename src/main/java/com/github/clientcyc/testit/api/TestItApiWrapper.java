package com.github.clientcyc.testit.api;

import com.github.clientcyc.testit.api.client.TestItApiClientBuilder;
import com.github.clientcyc.testit.api.model.AutoTest;
import com.github.clientcyc.testit.api.model.WorkItem;
import com.github.clientcyc.testit.plugin.settings.PluginSettings;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;

public class TestItApiWrapper {
    private final ITestItApiClient testItApiClient;
    private final PluginSettings settings;

    public static TestItApiWrapper getTestItApiWrapper(Project project) {
        PluginSettings pluginSettings = PluginSettings.getInstance(project);
        return new TestItApiWrapper(pluginSettings);
    }

    public TestItApiWrapper(PluginSettings settings) {
        this.settings = settings;
        this.testItApiClient = TestItApiClientBuilder.buildTestItApiClient(settings.getServerUrl(), settings.getApiKey());
    }

    public AutoTest createAutoTest(AutoTest autoTest) {
        return testItApiClient.createAutoTest(autoTest);
    }

    public String getProjectUuid(int projectId) {
        return testItApiClient.getProjects()
                .stream()
                .filter(project -> project.getGlobalId().equals(String.valueOf(projectId)))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Test-It's project with 'globalId' = " + projectId + " isn't found"))
                .getId();
    }

    public WorkItem getWorkItem(String id) {
        try {
            return testItApiClient.getWorkItem(id);
        } catch (Exception e) {
            Notifications.Bus.notify(
                    new NotificationGroup("TestIt.Plugin", NotificationDisplayType.BALLOON, true)
                            .createNotification("Get Test-It manual test info", "There is no manual test with id: " + id, NotificationType.WARNING, null)
            );
            return null;
        }
    }
}
