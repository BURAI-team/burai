/*
 * Copyright (C) 2016 Satomichi Nishihara
 *
 * This file is distributed under the terms of the
 * GNU General Public License. See the file `LICENSE'
 * in the root directory of the present distribution,
 * or http://www.gnu.org/copyleft/gpl.txt .
 */

package burai.app.project.viewer.result.movie;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.Group;
import burai.app.QEFXAppController;
import burai.app.project.QEFXProjectController;

public class QEFXMovieBarController extends QEFXAppController {

    private QEFXProjectController projectController;

    @FXML
    private Group baseGroup;

    public QEFXMovieBarController(QEFXProjectController projectController) {
        super(projectController == null ? null : projectController.getMainController());

        if (projectController == null) {
            throw new IllegalArgumentException("projectController is null.");
        }

        this.projectController = projectController;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // TODO
    }

}
