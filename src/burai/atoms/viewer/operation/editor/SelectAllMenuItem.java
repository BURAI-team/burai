/*
 * Copyright (C) 2016 Satomichi Nishihara
 *
 * This file is distributed under the terms of the
 * GNU General Public License. See the file `LICENSE'
 * in the root directory of the present distribution,
 * or http://www.gnu.org/copyleft/gpl.txt .
 */

package burai.atoms.viewer.operation.editor;

import java.util.List;

import javafx.scene.input.KeyCode;
import burai.atoms.viewer.AtomsViewer;
import burai.atoms.viewer.operation.ViewerEventManager;
import burai.atoms.visible.VisibleAtom;
import burai.com.keys.KeyName;

public class SelectAllMenuItem extends EditorMenuItem {

    private static final String ITEM_LABEL = "Select all atoms [" + KeyName.getShortcut(KeyCode.A) + "]";

    public SelectAllMenuItem(ViewerEventManager manager) {
        super(ITEM_LABEL, manager);
    }

    public SelectAllMenuItem(EditorMenu editorMenu) {
        super(ITEM_LABEL, editorMenu);
    }

    @Override
    protected void editAtoms() {
        if (this.manager == null) {
            return;
        }

        AtomsViewer atomsViewer = this.manager.getAtomsViewer();
        if (atomsViewer == null) {
            return;
        }

        List<VisibleAtom> visibleAtoms = atomsViewer.getVisibleAtoms();
        for (VisibleAtom visibleAtom : visibleAtoms) {
            if (visibleAtom != null) {
                visibleAtom.setSelected(true);
            }
        }

        this.manager.clearPrincipleAtom();
    }
}
