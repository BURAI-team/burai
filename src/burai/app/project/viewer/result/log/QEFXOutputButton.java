/*
 * Copyright (C) 2016 Satomichi Nishihara
 *
 * This file is distributed under the terms of the
 * GNU General Public License. See the file `LICENSE'
 * in the root directory of the present distribution,
 * or http://www.gnu.org/copyleft/gpl.txt .
 */

package burai.app.project.viewer.result.log;

import java.io.File;

import burai.app.project.QEFXProjectController;
import burai.app.project.viewer.result.QEFXResultButtonWrapper;
import burai.project.Project;

public class QEFXOutputButton extends QEFXLogButton {

    private static final String BUTTON_TITLE = "LOG";
    private static final String BUTTON_FONT_COLOR = "-fx-text-fill: ivory";
    private static final String BUTTON_BACKGROUND = "-fx-background-color: derive(deepskyblue, -12.0%)";

    public static QEFXResultButtonWrapper<QEFXOutputButton> getWrapper(QEFXProjectController projectController, Project project, String ext) {
        if (projectController == null) {
            return null;
        }

        String dirPath = project == null ? null : project.getDirectoryPath();
        String fileName = project == null ? null : project.getLogFileName(ext);

        File file = null;
        if (dirPath != null && fileName != null) {
            file = new File(dirPath, fileName);
        }

        try {
            if (file != null && file.isFile() && (file.length() > 0L)) {
                final File file_ = file;
                return () -> new QEFXOutputButton(projectController, file_, ext);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private QEFXOutputButton(QEFXProjectController projectController, File file, String ext) {
        super(projectController, BUTTON_TITLE, "." + ext, file);

        this.setIconStyle(BUTTON_BACKGROUND);
        this.setLabelStyle(BUTTON_FONT_COLOR);
    }
}
