/*
 * Copyright (C) 2018 Satomichi Nishihara
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package burai.app.project.editor.input.scf;

import java.io.IOException;

import burai.app.QEFXMainController;
import burai.app.project.editor.QEFXEditorComponent;
import burai.input.QEInput;

public class QEFXHubbard extends QEFXEditorComponent<QEFXHubbardController> {

    public QEFXHubbard(QEFXMainController mainController, QEInput input) throws IOException {
        super("QEFXHubbard.fxml", new QEFXHubbardController(mainController, input));
    }

    @Override
    public void notifyEditorOpened() {
        // TODO 自動生成されたメソ�?ド�?�スタ�?
    }

}
