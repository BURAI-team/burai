/*
 * Copyright (C) 2016 Satomichi Nishihara
 *
 * This file is distributed under the terms of the
 * GNU General Public License. See the file `LICENSE'
 * in the root directory of the present distribution,
 * or http://www.gnu.org/copyleft/gpl.txt .
 */

package burai.app.project.editor.result.movie;

import java.io.IOException;

import burai.app.project.QEFXProjectController;
import burai.app.project.editor.result.QEFXResultEditor;
import burai.app.project.viewer.result.movie.QEFXMovieViewer;
import burai.project.Project;

public class QEFXMovieEditor extends QEFXResultEditor<QEFXMovieEditorController> {

    public QEFXMovieEditor(QEFXProjectController projectController, Project project, QEFXMovieViewer viewer) throws IOException {
        super("QEFXMovieEditor.fxml",
                new QEFXMovieEditorController(projectController, viewer == null ? null : viewer.getController(), project));
    }

}
