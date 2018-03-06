package kg.akoikelov.prbubble;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.awt.RelativePoint;
import kg.akoikelov.prbubble.exception.PackageNotFoundException;
import kg.akoikelov.prbubble.exception.PypiUnknownException;
import kg.akoikelov.prbubble.pypi.PackageInfoRetriever;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.io.IOException;

/**
 * Created by akoikelov
 */
public class PackageInfoBubbleAction extends AnAction {

    private StatusBar statusBar;

    public PackageInfoBubbleAction() {
        super();
    }

    @Override
    public void update(AnActionEvent e) {
        Project project = e.getData(CommonDataKeys.PROJECT);
        Editor editor = e.getData(CommonDataKeys.EDITOR);

        e.getPresentation().setEnabled(project != null && editor != null && editor.getSelectionModel().hasSelection());
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Editor editor = e.getRequiredData(CommonDataKeys.EDITOR);
        SelectionModel selectionModel = editor.getSelectionModel();
        String packageName = selectionModel.getSelectedText();

        if (statusBar == null) {
            statusBar = WindowManager.getInstance().getStatusBar(e.getProject());
        }

        ApplicationManager.getApplication().invokeLater(() -> {
            try {
                JSONObject info = PackageInfoRetriever.getInfo(packageName);

                showInfo(info.getJSONObject("info"));
            } catch (IOException e1) {
                showErrorMessage("Could not connect to pypi web site");
            } catch (PypiUnknownException e1) {
                showErrorMessage("Unknown error on pypi web site");
            } catch (PackageNotFoundException e1) {
                showErrorMessage("Given package does not exist");
            } catch (JSONException e1) {
                showErrorMessage("Internal error: JSONException");
            }
        });
    }

    private void showInfo(JSONObject info) throws JSONException {
        String packageName = info.getString("name");
        String version = info.getString("version");
        String description = info.getString("summary");

        String html = String.format("<p>Description: %s</p><p>Name: %s</p><p>Version: %s</p> <br />",
                                    description, packageName, version);

        JBPopupFactory.getInstance()
                .createHtmlTextBalloonBuilder(html, MessageType.INFO, null)
                .createBalloon()
                .show(RelativePoint.getCenterOf(statusBar.getComponent()), Balloon.Position.atRight);
    }

    private void showErrorMessage(String message) {
        Messages.showErrorDialog(message, "Error");
    }

}
