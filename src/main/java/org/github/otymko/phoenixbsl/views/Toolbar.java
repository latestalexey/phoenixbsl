package org.github.otymko.phoenixbsl.views;

import javafx.application.Platform;
import org.github.otymko.phoenixbsl.core.PhoenixApp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Toolbar {

  private static final String PATH_TO_ICON = "/phoenix.jpg";

  private PopupMenu popupMenu;

  public Toolbar() {
    init();
  }

  private void init() {
    popupMenu = new PopupMenu();

    var settingItem = new MenuItem("Настройки");
    popupMenu.add(settingItem);

    var exitItem = new MenuItem("Закрыть");
    exitItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Platform.exit();
        PhoenixApp.getInstance().stopBSL();
        System.exit(0);
      }
    });
    popupMenu.add(exitItem);

    var systemTray = SystemTray.getSystemTray();
    var icon = new ImageIcon(PhoenixApp.class.getResource(PATH_TO_ICON));
    var image = icon.getImage();

    TrayIcon trayIcon = new TrayIcon(image, "Phoenix BSL", popupMenu);
    trayIcon.setImageAutoSize(true);
    try {
      systemTray.add(trayIcon);
    } catch (AWTException e) {
      System.out.println(e.getMessage());
    }
  }

}
