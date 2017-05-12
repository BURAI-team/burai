/*
 * Copyright (C) 2016 Satomichi Nishihara
 *
 * This file is distributed under the terms of the
 * GNU General Public License. See the file `LICENSE'
 * in the root directory of the present distribution,
 * or http://www.gnu.org/copyleft/gpl.txt .
 */

package burai.app.project.viewer.result.graph;

import java.io.File;
import java.io.IOException;
import java.util.List;

import burai.app.project.QEFXProjectController;
import burai.app.project.viewer.result.QEFXResultButtonWrapper;
import burai.project.Project;
import burai.project.property.DosData;
import burai.project.property.ProjectDos;
import burai.project.property.ProjectEnergies;
import burai.project.property.ProjectProperty;

public class QEFXDosButton extends QEFXGraphButton<QEFXDosViewer> {

    private static final String FILE_NAME = ".burai.graph.dos";

    private static final String BUTTON_TITLE = "DOS";
    private static final String BUTTON_FONT_COLOR = "-fx-text-fill: ivory";
    private static final String BUTTON_BACKGROUND = "-fx-background-color: derive(lightslategrey, -45.0%)";

    public static QEFXResultButtonWrapper<QEFXDosButton> getWrapper(QEFXProjectController projectController, Project project) {
        if (projectController == null) {
            return null;
        }

        ProjectProperty projectProperty = project == null ? null : project.getProperty();
        if (projectProperty == null) {
            return null;
        }

        ProjectEnergies projectEnergies = projectProperty.getFermiEnergies();
        if (projectEnergies == null || projectEnergies.numEnergies() < 1) {
            return null;
        }

        ProjectDos projectDos = projectProperty.getDos();
        if (projectDos == null) {
            return null;
        }

        List<DosData> dosDataList = projectDos.listDosData();
        if (dosDataList == null || dosDataList.size() < 2) {
            return null;
        }

        String dirPath = project == null ? null : project.getDirectoryPath();
        String fileName = project == null ? null : (project.getPrefixName() + ".pdos_tot");

        File file = null;
        if (dirPath != null && fileName != null) {
            file = new File(dirPath, fileName);
        }

        try {
            if (file == null || (!file.isFile()) || (file.length() <= 0L)) {
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return () -> {
            QEFXDosButton button = new QEFXDosButton(projectController, projectProperty);

            String propPath = project == null ? null : project.getDirectoryPath();
            File propFile = propPath == null ? null : new File(propPath, FILE_NAME);
            if (propFile != null) {
                button.setPropertyFile(propFile);
            }

            return button;
        };
    }

    private ProjectProperty projectProperty;

    private QEFXDosButton(QEFXProjectController projectController, ProjectProperty projectProperty) {
        super(projectController, BUTTON_TITLE, null);

        if (projectProperty == null) {
            throw new IllegalArgumentException("projectProperty is null.");
        }

        this.projectProperty = projectProperty;

        this.setIconStyle(BUTTON_BACKGROUND);
        this.setLabelStyle(BUTTON_FONT_COLOR);
    }

    @Override
    protected QEFXDosViewer createGraphViewer() throws IOException {
        if (this.projectController == null) {
            return null;
        }

        return new QEFXDosViewer(this.projectController, this.projectProperty);
    }
}
