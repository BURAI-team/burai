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
import java.util.List;

import burai.atoms.design.Design;
import burai.atoms.model.Atom;
import burai.atoms.model.Cell;
import burai.atoms.model.CellProperty;
import burai.atoms.viewer.logger.AtomsLogger;
import burai.atoms.viewer.logger.AtomsLoggerPFactory;
import burai.atoms.viewer.operation.ViewerEventManager;
import burai.atoms.visible.VisibleAtom;
import burai.atoms.visible.VisibleCell;
import burai.com.math.Lattice;
import javafx.event.EventHandler;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;

public class AtomsViewer extends AtomsViewerBase<Group> {

    private boolean compassMode;

    private ViewerCell viewerCell;
    private ViewerSample viewerSample;
    private ViewerXYZAxis viewerXYZAxis;
    private ViewerCompass viewerCompass;

    private AtomsLogger logger;

    private Design design;
    private List<Node> backgroundNodes;

    private boolean busyLinkedViewers;
    private List<AtomsViewer> linkedViewers;

    public AtomsViewer(Cell cell, double size) {
        this(cell, size, size);
    }

    public AtomsViewer(Cell cell, double size, boolean silent) {
        this(cell, size, size, silent);
    }

    public AtomsViewer(Cell cell, double width, double height) {
        this(cell, width, height, false);
    }

    public AtomsViewer(Cell cell, double width, double height, boolean silent) {
        super(cell, width, height);

        this.compassMode = false;

        this.viewerCell = new ViewerCell(this, this.cell);
        this.viewerSample = new ViewerSample(this, this.cell);
        this.viewerXYZAxis = new ViewerXYZAxis(this);
        if (!silent) {
            this.viewerCompass = new ViewerCompass(this.viewerCell);
        } else {
            this.viewerCompass = null;
        }

        if (!silent) {
            this.logger = new AtomsLogger(this.cell);
        } else {
            this.logger = null;
        }

        this.createDesign();
        this.backgroundNodes = null;

        this.busyLinkedViewers = false;
        this.linkedViewers = null;

        this.sceneRoot.getChildren().add(this.viewerCell.getNode());
        this.sceneRoot.getChildren().add(this.viewerSample.getNode());
        this.sceneRoot.getChildren().add(this.viewerXYZAxis.getNode());
        if (!silent) {
            this.sceneRoot.getChildren().add(this.viewerCompass.getNode());
        }

        ViewerEventManager viewerEventManager = new ViewerEventManager(this, silent);
        this.subScene.setOnMousePressed(viewerEventManager.getMousePressedHandler());
        this.subScene.setOnMouseDragged(viewerEventManager.getMouseDraggedHandler());
        this.subScene.setOnMouseReleased(viewerEventManager.getMouseReleasedHandler());
        this.subScene.setOnScroll(viewerEventManager.getScrollHandler());

        this.subScene.setOnKeyPressed(event -> {
            EventHandler<KeyEvent> handler = viewerEventManager.getKeyPressedHandler();
            if (handler != null) {
                handler.handle(event);
            }
            if (this.subKeyHandler != null) {
                subKeyHandler.handle(event);
            }
        });

        this.initialRotation();
    }

    private void createDesign() {
        this.design = new Design();

        this.subScene.setFill(this.design.getBackColor());
        this.design.setOnBackColorChanged(color -> {
            if (color == null) {
                return;
            }

            this.subScene.setFill(color);

            if (this.backgroundNodes != null && !this.backgroundNodes.isEmpty()) {
                String strColor = color.toString();
                strColor = strColor == null ? null : strColor.replaceAll("0x", "#");
                if (strColor != null) {
                    for (Node node : this.backgroundNodes) {
                        node.setStyle("-fx-background-color: " + strColor);
                    }
                }
            }
        });

        this.viewerSample.getNode().setVisible(this.design.isShowingLegend());
        this.design.setOnShowingLegendChanged(showing -> {
            this.viewerSample.getNode().setVisible(showing);
        });

        this.viewerXYZAxis.getNode().setVisible(this.design.isShowingAxis());
        this.design.setOnShowingAxisChanged(showing -> {
            this.viewerXYZAxis.getNode().setVisible(showing);
        });
    }

    private void initialRotation() {
        if (this.cell == null) {
            return;
        }

        List<double[]> rotations = getInitialRotation(this.cell);
        if (rotations == null || rotations.isEmpty()) {
            return;
        }

        for (double[] rotation : rotations) {
            if (rotation != null && rotation.length > 3) {
                double angle = rotation[0];
                double axisX = rotation[1];
                double axisY = rotation[2];
                double axisZ = rotation[3];
                this.appendCellRotation(angle, axisX, axisY, axisZ);
            }
        }
    }

    public static List<double[]> getInitialRotation(Cell cell) {
        if (cell == null) {
            return null;
        }

        double[][] lattice = cell.copyLattice();
        if (lattice == null || lattice.length < 3) {
            return null;
        }
        for (int i = 0; i < 3; i++) {
            if (lattice[i] == null || lattice[i].length < 3) {
                return null;
            }
        }

        double x = Lattice.getXMax(lattice) - Lattice.getXMin(lattice);
        if (x <= 0.0) {
            return null;
        }

        double y = Lattice.getYMax(lattice) - Lattice.getYMin(lattice);
        if (y <= 0.0) {
            return null;
        }

        double z = Lattice.getZMax(lattice) - Lattice.getZMin(lattice);
        if (z <= 0.0) {
            return null;
        }

        String axis = null;
        if (cell.hasProperty(CellProperty.AXIS)) {
            axis = cell.stringProperty(CellProperty.AXIS);
        }

        if ("x".equalsIgnoreCase(axis)) {
            x = Double.MAX_VALUE;
        } else if ("y".equalsIgnoreCase(axis)) {
            y = Double.MAX_VALUE;
        } else if ("z".equalsIgnoreCase(axis)) {
            z = Double.MAX_VALUE;
        }

        List<double[]> rotations = new ArrayList<double[]>();

        if (y >= x && y >= z) {
            if (x >= z) {
                // y > x > z
                // NOP
            } else {
                // y > z > x
                rotations.add(new double[] { -90.0, 0.0, 1.0, 0.0 });
            }

        } else if (z >= x && z >= y) {
            if (x >= y) {
                // z > x > y
                rotations.add(new double[] { -90.0, 1.0, 0.0, 0.0 });
            } else {
                // z > y > x
                rotations.add(new double[] { -90.0, 1.0, 0.0, 0.0 });
                rotations.add(new double[] { 90.0, 0.0, 1.0, 0.0 });
            }

        } else if (x >= y && x >= z) {
            if (y >= z) {
                // x > y > z
                rotations.add(new double[] { 90.0, 0.0, 0.0, 1.0 });
                rotations.add(new double[] { 180.0, 1.0, 0.0, 0.0 });
            } else {
                // x > z > y
                rotations.add(new double[] { 90.0, 0.0, 0.0, 1.0 });
                rotations.add(new double[] { 180.0, 1.0, 0.0, 0.0 });
                rotations.add(new double[] { 90.0, 0.0, 1.0, 0.0 });
            }
        }

        return rotations;
    }

    @Override
    protected Group newSceneRoot() {
        return new Group();
    }

    @Override
    protected void onSceneResized() {
        if (this.viewerCell != null) {
            this.viewerCell.initialize(true);
        }

        if (this.viewerSample != null) {
            this.viewerSample.initialize();
        }

        if (this.viewerXYZAxis != null) {
            this.viewerXYZAxis.initialize(true);
        }

        if (this.viewerCompass != null) {
            this.viewerCompass.initialize(true);
        }
    }

    public boolean isCompassMode() {
        return this.compassMode;
    }

    public void setCompassMode(VisibleAtom targetAtom) {
        if (this.viewerCompass == null) {
            return;
        }

        this.compassMode = (targetAtom != null);
        this.viewerCompass.setTargetAtom(targetAtom);

        if (this.compassMode) {
            this.viewerCompass.initialize();
            this.viewerCompass.getNode().setVisible(true);
            this.startExclusiveMode();

        } else {
            this.viewerCompass.getNode().setVisible(false);
            this.stopExclusiveMode();
        }
    }

    public void appendCellScale(double scale) {
        if (this.compassMode) {
            return;
        }

        if (this.busyLinkedViewers) {
            return;
        }

        if (this.viewerCell != null) {
            this.viewerCell.appendScale(scale);
        }

        this.busyLinkedViewers = true;

        if (this.linkedViewers != null) {
            for (AtomsViewer atomsViewer : this.linkedViewers) {
                if (atomsViewer != null) {
                    atomsViewer.appendCellScale(scale);
                }
            }
        }

        this.busyLinkedViewers = false;
    }

    public void appendCellRotation(double angle, double axisX, double axisY, double axisZ) {
        if (this.compassMode) {
            return;
        }

        if (this.busyLinkedViewers) {
            return;
        }

        if (this.viewerCell != null) {
            this.viewerCell.appendRotation(angle, axisX, axisY, axisZ);
        }

        if (this.viewerXYZAxis != null) {
            this.viewerXYZAxis.appendRotation(angle, axisX, axisY, axisZ);
        }

        this.busyLinkedViewers = true;

        if (this.linkedViewers != null) {
            for (AtomsViewer atomsViewer : this.linkedViewers) {
                if (atomsViewer != null) {
                    atomsViewer.appendCellRotation(angle, axisX, axisY, axisZ);
                }
            }
        }

        this.busyLinkedViewers = false;
    }

    public void appendCellTranslation(double x, double y, double z) {
        if (this.compassMode) {
            return;
        }

        if (this.busyLinkedViewers) {
            return;
        }

        double scale1 = 1.0;
        if (this.viewerCell != null) {
            scale1 = this.viewerCell.getScale();
            this.viewerCell.appendTranslation(x, y, z);
        }

        this.busyLinkedViewers = true;

        if (this.linkedViewers != null) {
            for (AtomsViewer atomsViewer : this.linkedViewers) {
                if (atomsViewer == null) {
                    continue;
                }

                double scale2 = 1.0;
                if (atomsViewer.viewerCell != null) {
                    scale2 = atomsViewer.viewerCell.getScale();
                }

                double x2 = x * scale2 / scale1;
                double y2 = y * scale2 / scale1;
                double z2 = z * scale2 / scale1;
                atomsViewer.appendCellTranslation(x2, y2, z2);
            }
        }

        this.busyLinkedViewers = false;
    }

    public void appendCompassRotation(double angle, double axisX, double axisY, double axisZ) {
        if (!this.compassMode) {
            return;
        }

        if (this.viewerCompass != null) {
            this.viewerCompass.appendRotation(angle, axisX, axisY, axisZ);
        }
    }

    public boolean addChild(Node node) {
        if (this.compassMode) {
            return false;
        }

        if (this.sceneRoot != null) {
            return this.sceneRoot.getChildren().add(node);
        }

        return false;
    }

    public boolean removeChild(Node node) {
        if (this.compassMode) {
            return false;
        }

        if (this.sceneRoot != null) {
            return this.sceneRoot.getChildren().remove(node);
        }

        return false;
    }

    public boolean hasChild(Node node) {
        if (this.sceneRoot != null) {
            return this.sceneRoot.getChildren().contains(node);
        }

        return false;
    }

    public List<VisibleAtom> getVisibleAtoms() {
        List<VisibleAtom> visibleAtoms = new ArrayList<VisibleAtom>();

        VisibleCell visibleCell = null;
        if (this.viewerCell != null) {
            visibleCell = this.viewerCell.getNode();
        }

        if (visibleCell != null) {
            List<Node> children = visibleCell.getChildren();
            for (Node child : children) {
                if (child instanceof VisibleAtom) {
                    visibleAtoms.add((VisibleAtom) child);
                }
            }
        }

        return visibleAtoms;
    }

    public boolean isInCell(double sceneX, double sceneY, double sceneZ) {
        if (this.viewerCell == null) {
            return false;
        }

        return this.viewerCell.isInCell(sceneX, sceneY, sceneZ);
    }

    public Point3D sceneToCell(double sceneX, double sceneY, double sceneZ) {
        if (this.viewerCell == null) {
            return null;
        }

        VisibleCell visibleCell = this.viewerCell.getNode();
        if (visibleCell == null) {
            return null;
        }

        return visibleCell.sceneToLocal(sceneX, sceneY, sceneZ);
    }

    public double getSceneZOnCompass(double sceneX, double sceneY) {
        if (!this.compassMode) {
            return 0.0;
        }

        if (this.viewerCompass == null) {
            return 0.0;
        }

        return this.viewerCompass.getSceneZ(sceneX, sceneY);
    }

    public void putAtom(String name, double sceneX, double sceneY, double sceneZ) {
        if (this.compassMode) {
            return;
        }

        if (this.viewerCell == null) {
            return;
        }

        VisibleCell visibleCell = this.viewerCell.getNode();
        if (visibleCell == null) {
            return;
        }

        Point3D point3d = visibleCell.sceneToLocal(sceneX, sceneY, sceneZ);
        double x = point3d.getX();
        double y = point3d.getY();
        double z = point3d.getZ();

        if (name != null && !name.isEmpty()) {
            visibleCell.getModel().addAtom(new Atom(name, x, y, z));
        }
    }

    public void setCellToCenter() {
        if (this.compassMode) {
            return;
        }

        if (this.busyLinkedViewers) {
            return;
        }

        if (this.viewerCell != null) {
            this.viewerCell.initialize();
        }

        if (this.viewerXYZAxis != null) {
            this.viewerXYZAxis.initialize();
        }

        this.initialRotation();

        this.busyLinkedViewers = true;

        if (this.linkedViewers != null) {
            for (AtomsViewer atomsViewer : this.linkedViewers) {
                if (atomsViewer != null) {
                    atomsViewer.setCellToCenter();
                }
            }
        }

        this.busyLinkedViewers = false;

    }

    public void setCompassToCenter() {
        if (!this.compassMode) {
            return;
        }

        if (this.viewerCompass != null) {
            this.viewerCompass.initialize();
        }
    }

    public void setLoggerPropertyFactory(AtomsLoggerPFactory propFactory) {
        if (this.logger != null) {
            this.logger.setPropertyFactory(propFactory);
        }
    }

    public void clearStoredCell() {
        if (this.logger != null) {
            this.logger.clearConfiguration();
        }
    }

    public void storeCell() {
        if (this.logger != null) {
            this.logger.storeConfiguration();
        }
    }

    public boolean canRestoreCell() {
        if (this.compassMode) {
            return false;
        }

        if (this.logger != null) {
            return this.logger.canRestoreConfiguration();
        }

        return false;
    }

    public boolean canSubRestoreCell() {
        if (this.compassMode) {
            return false;
        }

        if (this.logger != null) {
            return this.logger.canSubRestoreConfiguration();
        }

        return false;
    }

    public void restoreCell() {
        if (this.compassMode) {
            return;
        }

        if (this.logger != null) {
            this.logger.restoreConfiguration();
        }
    }

    public void subRestoreCell() {
        if (this.compassMode) {
            return;
        }

        if (this.logger != null) {
            this.logger.subRestoreConfiguration();
        }
    }

    public Design getDesign() {
        return this.design;
    }

    public void addBackgroundNode(Node node) {
        if (node == null) {
            return;
        }

        if (this.backgroundNodes == null) {
            this.backgroundNodes = new ArrayList<Node>();
        }

        Color color = this.design.getBackColor();
        String strColor = color == null ? null : color.toString();
        strColor = strColor == null ? null : strColor.replaceAll("0x", "#");
        if (strColor != null) {
            node.setStyle("-fx-background-color: " + strColor);
        }

        this.backgroundNodes.add(node);
    }

    public void linkAtomsViewer(AtomsViewer atomsViewer) {
        if (atomsViewer == null) {
            return;
        }

        if (this.linkedViewers == null) {
            this.linkedViewers = new ArrayList<AtomsViewer>();
        }

        if (this.linkedViewers.contains(atomsViewer)) {
            return;
        }

        this.linkedViewers.add(atomsViewer);

        atomsViewer.linkAtomsViewer(this);
    }
}
