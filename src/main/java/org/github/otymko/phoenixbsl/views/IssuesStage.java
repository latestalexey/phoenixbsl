package org.github.otymko.phoenixbsl.views;

import com.jfoenix.controls.JFXTreeTableColumn;
import com.jfoenix.controls.JFXTreeTableView;
import com.jfoenix.controls.RecursiveTreeItem;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeTableCell;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.github.otymko.phoenixbsl.core.PhoenixAPI;
import org.github.otymko.phoenixbsl.core.PhoenixApp;
import org.github.otymko.phoenixbsl.entities.Issue;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class IssuesStage extends Stage {

  private Map<DiagnosticSeverity, String> severityToStringMap = createSeverityToStringMap();

  private JFXTreeTableView<Issue> tree = new JFXTreeTableView<>();

  public int lineOffset = 0;

  private int countError = 0;
  private int countWarning = 0;
  private int countInfo = 0;

  private Label labelError;
  private Label labelWarning;
  private Label labelInfo;

  public IssuesStage() {

    // займемся ui
    setTitle("Phoenix");
    getIcons().add(new Image(PhoenixApp.class.getResourceAsStream("/phoenix.jpg")));

    tree.setPlaceholder(new Label("Замечаний нет"));

    JFXTreeTableColumn<Issue, String> descriptionColumn = new JFXTreeTableColumn<>("Описание");
    descriptionColumn.setPrefWidth(300);
    descriptionColumn.setCellFactory(param -> {
      TreeTableCell<Issue, String> cell = new TreeTableCell<>();
      Text text = new Text();
      cell.setGraphic(text);
      cell.setPrefHeight(Control.USE_COMPUTED_SIZE);
      text.textProperty().bind(cell.itemProperty());
      text.wrappingWidthProperty().bind(descriptionColumn.widthProperty());
      return cell;
    });
    descriptionColumn.setCellValueFactory(
      param -> new SimpleStringProperty(param.getValue().getValue().getDescription()));

    JFXTreeTableColumn<Issue, String> positionColumn = new JFXTreeTableColumn<>("стр.");
    positionColumn.setPrefWidth(50);
    positionColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getValue().getLocation()));
    positionColumn.setReorderable(false);

    JFXTreeTableColumn<Issue, String> typeColumn = new JFXTreeTableColumn<>("Тип");
    typeColumn.setPrefWidth(90);
    typeColumn.setCellValueFactory(param -> new SimpleStringProperty(severityToStringMap.get(param.getValue().getValue().getSeverity())));
    typeColumn.setReorderable(false);

    ObservableList<Issue> issues = FXCollections.observableArrayList();

    var recursiveTreeItem = new RecursiveTreeItem<>(issues, RecursiveTreeObject::getChildren);
    tree.setRoot(recursiveTreeItem);
    tree.setShowRoot(false);
    tree.setEditable(true);
    tree.getColumns().add(descriptionColumn);
    tree.getColumns().add(positionColumn);
    tree.getColumns().add(typeColumn);

    tree.setOnMouseClicked(event -> {
      if (event.getClickCount() != 2) {
        return;
      }

      var issue = tree.getSelectionModel().getSelectedItem().getValue();
      PhoenixAPI.gotoLineModule(issue.getStartLine(), PhoenixApp.getInstance().getFocusForm());

    });

    tree.setPrefSize(450, 520);

    GridPane main = new GridPane();
    main.setPadding(new Insets(10, 10, 10, 10));
    main.setHgap(20);
    main.setVgap(20);

    Label searchLabel = new Label("Поиск:");
    searchLabel.setPrefWidth(100);
    searchLabel.setPadding(new Insets(5, 0, 5, 10));

    TextField filterField = new TextField();
    filterField.setPrefWidth(420);
    filterField.setPadding(new Insets(5, 0, 5, 10));
    filterField.textProperty().addListener((o, oldVal, newVal) -> {
      tree.setPredicate(userProp -> {
        final Issue issue = userProp.getValue();
        return issue.getDescription().toLowerCase().contains(newVal.toLowerCase())
          || severityToStringMap.get(issue.getSeverity()).toLowerCase().contains(newVal.toLowerCase())
          || issue.getLocation().toLowerCase().contains(newVal.toLowerCase());
      });
    });

    GridPane searchPanel = new GridPane();
    searchPanel.add(searchLabel, 0, 0);
    searchPanel.add(filterField, 1, 0);

    main.add(searchPanel, 0, 0);
    main.add(tree, 0, 1);

    // сводка внизу
    GridPane infoPanel = new GridPane();
    infoPanel.setAlignment(Pos.CENTER);

    labelError = new Label();
    infoPanel.add(labelError, 0, 0);
    labelError.setPadding(new Insets(0, 50, 10, 10));

    labelWarning = new Label();
    infoPanel.add(labelWarning, 1, 0);
    labelWarning.setPadding(new Insets(0, 50, 10, 10));

    labelInfo = new Label();
    infoPanel.add(labelInfo, 2, 0);
    labelInfo.setPadding(new Insets(0, 10, 10, 10));

    updateIndicators();

    main.add(infoPanel, 0, 2);

    Scene scene = new Scene(main, 480, 600);

    setOnCloseRequest(event -> {
      setIconified(true);
      event.consume();
    });

    setScene(scene);
    setResizable(false);

  }

  public IssuesStage(Stage ownerStage) {
    this();
    initOwner(ownerStage);
  }

  public void updateIssues(List<Diagnostic> diagnostics) {

    countError = 0;
    countWarning = 0;
    countInfo = 0;

    ObservableList<Issue> issues = FXCollections.observableArrayList();
    diagnostics.stream().forEach(diagnostic -> {
      var range = diagnostic.getRange();
      var position = range.getStart();
      var startLine = position.getLine() + 1 + lineOffset;

      Issue issue = new Issue();
      issue.setDescription(diagnostic.getMessage());
      issue.setStartLine(startLine);
      issue.setLocation(String.valueOf(startLine));
      issue.setSeverity(diagnostic.getSeverity());
      issues.add(issue);

      if (diagnostic.getSeverity() == DiagnosticSeverity.Error) {
        countError++;
      } else if (diagnostic.getSeverity() == DiagnosticSeverity.Warning) {
        countWarning++;
      } else {
        countInfo++;
      }
    });

    updateIndicators();

    var recursiveTreeItem = new RecursiveTreeItem<Issue>(issues, RecursiveTreeObject::getChildren);
    tree.setRoot(recursiveTreeItem);
    tree.setShowRoot(false);

    this.toFront();
    this.setIconified(false);

  }

  private void updateIndicators() {

    labelError.setText("Ошибки: " + countError);
    labelWarning.setText("Предупреждения: " + countWarning);
    labelInfo.setText("Инфо: " + countInfo);

  }

  private Map<DiagnosticSeverity, String> createSeverityToStringMap() {
    Map<DiagnosticSeverity, String> map = new EnumMap<>(DiagnosticSeverity.class);
    map.put(DiagnosticSeverity.Error, "Ошибка");
    map.put(DiagnosticSeverity.Information, "Информация");
    map.put(DiagnosticSeverity.Hint, "Подсказка");
    map.put(DiagnosticSeverity.Warning, "Предупреждение");
    return map;
  }

}
