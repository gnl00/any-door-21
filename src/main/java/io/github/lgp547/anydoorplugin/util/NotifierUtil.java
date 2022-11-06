package io.github.lgp547.anydoorplugin.util;

import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;

public class NotifierUtil {

  public static void notifyError(@Nullable Project project,
                                 String content) {
    NotificationGroupManager.getInstance()
            .getNotificationGroup("AnyDoor")
            .createNotification(content, NotificationType.ERROR)
            .notify(project);
  }

  public static void notifyInfo(@Nullable Project project,
                                 String content) {
    NotificationGroupManager.getInstance()
            .getNotificationGroup("AnyDoor")
            .createNotification(content, NotificationType.INFORMATION)
            .notify(project);
  }

}