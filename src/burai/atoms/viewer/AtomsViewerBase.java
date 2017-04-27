/*
 * Copyright (C) 2016 Satomichi Nishihara
 *
 * This file is distributed under the terms of the
 * GNU General Public License. See the file `LICENSE'
 * in the root directory of the present distribution,
 * or http://www.gnu.org/copyleft/gpl.txt .
 */

package burai.atoms.viewer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.scene.Camera;
import javafx.scene.DepthTest;
import javafx.scene.Node;
import javafx.scene.ParallelCamera;
import javafx.scene.Parent;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import burai.atoms.model.Cell;

public abstract class AtomsViewerBase<R extends Parent> extends AtomsViewerInterface {

    private static final Color BACKGROUND_COLOR = Color.DIMGRAY;

    protected Cell cell;

    protected double width;
    protected double height;

    private Camera camera;
    protected R sceneRoot;
    protected SubScene subScene;

    private List<NodeWrapper> exclusiveNodes;
    private Map<NodeWrapper, Boolean> exclusiveDisables;

    protected AtomsViewerBase(Cell cell, double width, double height) {
        super();

        if (cell == null) {
            throw new IllegalArgumentException("cell is null.");
        }

        if (width <= 0.0) {
            throw new IllegalArgumentException("width is not positive.");
        }

        if (height <= 0.0) {
            throw new IllegalArgumentException("height is not positive.");
        }

        this.cell = cell;

        this.width = width;
        this.height = height;

        this.camera = null;
        this.sceneRoot = null;
        this.subScene = null;

        this.exclusiveNodes = null;
        this.exclusiveDisables = null;

        this.createCamera();
        this.createSceneRoot();
        this.createSubScene();
        this.getChildren().add(this.subScene);
    }

    private void createCamera() {
        if (this.camera == null) {
            this.camera = new ParallelCamera();
        }

        double range = Math.min(this.width, this.height);
        this.camera.setFarClip(100.0 * range);
        this.camera.setNearClip(1.0e-4);
    }

    protected abstract R newSceneRoot();

    private void createSceneRoot() {
        this.sceneRoot = this.newSceneRoot();
        this.sceneRoot.setDepthTest(DepthTest.ENABLE);
    }

    private void createSubScene() {
        this.subScene = new SubScene(this.sceneRoot, this.width, this.height, true, SceneAntialiasing.BALANCED);
        this.subScene.getStyleClass().add("atoms-viewer");
        this.subScene.setFill(BACKGROUND_COLOR);
        this.subScene.setCamera(this.camera);
        this.subScene.setManaged(false);
        this.subScene.setOnMouseClicked(event -> this.subScene.requestFocus());
        this.subScene.widthProperty().addListener(o -> this.resizeScene());
        this.subScene.heightProperty().addListener(o -> this.resizeScene());
    }

    @Override
    public Cell getCell() {
        return this.cell;
    }

    @Override
    public double getSceneWidth() {
        return this.width;
    }

    @Override
    public double getSceneHeight() {
        return this.height;
    }

    @Override
    public void addExclusiveNode(Node node) {
        if (node == null) {
            return;
        }

        this.addExclusiveNode(() -> {
            return node;
        });
    }

    @Override
    public void addExclusiveNode(NodeWrapper nodeWrapper) {
        if (nodeWrapper == null) {
            return;
        }

        if (this.exclusiveNodes == null) {
            this.exclusiveNodes = new ArrayList<NodeWrapper>();
        }

        this.exclusiveNodes.add(nodeWrapper);
    }

    protected void stopExclusiveNodes() {
        if (this.exclusiveNodes == null) {
            return;
        }

        if (this.exclusiveDisables == null) {
            this.exclusiveDisables = new HashMap<NodeWrapper, Boolean>();
        }

        for (NodeWrapper nodeWrapper : this.exclusiveNodes) {
            if (nodeWrapper == null) {
                continue;
            }
            Node node = nodeWrapper.getNode();
            if (node == null) {
                continue;
            }
            this.exclusiveDisables.put(nodeWrapper, node.isDisable());
            node.setDisable(true);
        }
    }

    protected void restartExclusiveNodes() {
        if (this.exclusiveNodes == null) {
            return;
        }

        if (this.exclusiveDisables == null) {
            return;
        }

        for (NodeWrapper nodeWrapper : this.exclusiveNodes) {
            if (nodeWrapper == null) {
                continue;
            }
            Node node = nodeWrapper.getNode();
            if (node == null) {
                continue;
            }
            Boolean disable = this.exclusiveDisables.get(nodeWrapper);
            if (disable != null) {
                node.setDisable(disable);
            }
        }
    }

    protected abstract void onSceneResized();

    private void resizeScene() {
        double widthTmp = -1.0;
        double heightTmp = -1.0;

        if (this.subScene != null) {
            widthTmp = this.subScene.getWidth();
            heightTmp = this.subScene.getHeight();
        }

        if (widthTmp <= 0.0 || heightTmp <= 0.0) {
            return;
        }

        this.width = widthTmp;
        this.height = heightTmp;

        this.createCamera();

        this.onSceneResized();
    }

    @Override
    public void bindSceneTo(Pane pane) {
        if (pane == null) {
            return;
        }

        if (this.subScene == null) {
            return;
        }

        if (pane instanceof AnchorPane) {
            AnchorPane.setBottomAnchor(this.subScene, 0.0);
            AnchorPane.setTopAnchor(this.subScene, 0.0);
            AnchorPane.setLeftAnchor(this.subScene, 0.0);
            AnchorPane.setRightAnchor(this.subScene, 0.0);
        }

        this.subScene.widthProperty().bind(pane.widthProperty());
        this.subScene.heightProperty().bind(pane.heightProperty());

        Parent parent = this.subScene.getParent();
        if (parent == this) {
            List<Node> children = this.getChildren();
            if (children.contains(this.subScene)) {
                children.remove(this.subScene);
            }
        }

        pane.getChildren().add(this.subScene);
    }

    @Override
    public void unbindScene() {

        this.subScene.widthProperty().unbind();
        this.subScene.heightProperty().unbind();

        Parent parent = this.subScene.getParent();
        if (parent != null && parent instanceof Pane) {
            Pane pane = (Pane) parent;
            List<Node> children = pane.getChildren();
            if (children.contains(this.subScene)) {
                children.remove(this.subScene);
            }
        }

        this.getChildren().add(this.subScene);
    }
}