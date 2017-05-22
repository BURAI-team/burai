/*
 * Copyright (C) 2016 Satomichi Nishihara
 *
 * This file is distributed under the terms of the
 * GNU General Public License. See the file `LICENSE'
 * in the root directory of the present distribution,
 * or http://www.gnu.org/copyleft/gpl.txt .
 */

package burai.app.project.viewer.inputfile;

import java.io.IOException;

import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import burai.app.QEFXAppComponent;
import burai.app.project.QEFXProjectController;
import burai.project.Project;

public class QEFXInputFile extends QEFXAppComponent<QEFXInputFileController> {

    public QEFXInputFile(QEFXProjectController projectController, Project project) throws IOException {
        super("QEFXInputFile.fxml", new QEFXInputFileController(projectController, project));

        if (this.node != null) {
            this.setupKeys(this.node);
        }
    }

    private void setupKeys(Node node) {
        if (node == null) {
            return;
        }

        node.setOnKeyPressed(event -> {
            if (event == null) {
                return;
            }

            if (event.isShortcutDown() && KeyCode.W.equals(event.getCode())) {
                // Ctrl + W
                this.controller.close();
                event.consume();
            }
        });
    }
}
