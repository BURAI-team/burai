/*
 * Copyright (C) 2016 Satomichi Nishihara
 *
 * This file is distributed under the terms of the
 * GNU General Public License. See the file `LICENSE'
 * in the root directory of the present distribution,
 * or http://www.gnu.org/copyleft/gpl.txt .
 */

package burai.atoms.viewer.operation.editor;

import javafx.scene.input.KeyCode;
import burai.atoms.viewer.AtomsViewer;
import burai.atoms.viewer.operation.ViewerEventManager;
import burai.com.keys.KeyName;

public class RedoMenuItem extends EditorMenuItem {

    private static final String ITEM_LABEL = "Redo [" + KeyName.getShortcutShift(KeyCode.Z) + "]";

    public RedoMenuItem(ViewerEventManager manager) {
        super(ITEM_LABEL, manager);
    }

    public RedoMenuItem(EditorMenu editorMenu) {
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

        atomsViewer.subRestoreCell();
    }
}
