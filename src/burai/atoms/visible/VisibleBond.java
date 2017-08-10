/*
 * Copyright (C) 2016 Satomichi Nishihara
 *
 * This file is distributed under the terms of the
 * GNU General Public License. See the file `LICENSE'
 * in the root directory of the present distribution,
 * or http://www.gnu.org/copyleft/gpl.txt .
 */

package burai.atoms.visible;

import burai.atoms.design.AtomDesign;
import burai.atoms.design.AtomDesignAdaptor;
import burai.atoms.design.AtomDesignListener;
import burai.atoms.design.AtomsStyle;
import burai.atoms.design.Design;
import burai.atoms.element.ElementUtil;
import burai.atoms.model.Atom;
import burai.atoms.model.Bond;
import burai.atoms.model.event.AtomEvent;
import burai.atoms.model.event.BondEvent;
import burai.atoms.model.event.BondEventListener;
import javafx.geometry.Point3D;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;

public class VisibleBond extends Visible<Bond> implements BondEventListener, AtomDesignListener {

    private static final double CYLINDER_RADIUS_NORM = 0.10;
    private static final double CYLINDER_RADIUS_BOLD = 0.12;
    private static final int CYLINDER_DIV = 12;

    private static final double RMIN = 5.0e-3;
    private static final double RRMIN = RMIN * RMIN;

    private boolean boldMode;

    private Cylinder bondCylinder1;
    private Cylinder bondCylinder2;

    private double currentRadius1;
    private double currentRadius2;
    private Color currentColor1;
    private Color currentColor2;
    private boolean currentStyle1;
    private boolean currentStyle2;
    private double currentBond1;
    private double currentBond2;

    private AtomDesign atomDesign1;
    private AtomDesign atomDesign2;
    private AtomDesignAdaptor atomDesignAdaptor1;
    private AtomDesignAdaptor atomDesignAdaptor2;

    public VisibleBond(Bond bond, Design design) {
        this(bond, design, false);
    }

    public VisibleBond(Bond bond, Design design, boolean boldMode) {
        super(bond, design);

        this.model.addListener(this);

        this.boldMode = boldMode;

        double radius = this.boldMode ? CYLINDER_RADIUS_BOLD : CYLINDER_RADIUS_NORM;
        this.bondCylinder1 = new Cylinder(radius, 1.0, CYLINDER_DIV);
        this.bondCylinder2 = new Cylinder(radius, 1.0, CYLINDER_DIV);

        this.currentRadius1 = -1.0;
        this.currentRadius2 = -1.0;
        this.currentColor1 = null;
        this.currentColor2 = null;
        this.currentStyle1 = false;
        this.currentStyle2 = false;
        this.currentBond1 = -1.0;
        this.currentBond2 = -1.0;

        this.atomDesign1 = null;
        this.atomDesign2 = null;
        this.atomDesignAdaptor1 = null;
        this.atomDesignAdaptor1 = null;

        this.updateAtomDesign(true, true);
        this.updateVisibleCylinder();
        this.updateXYZOfCylinder();
        this.updateColorOfCylinder();
        this.getChildren().add(this.bondCylinder1);
        this.getChildren().add(this.bondCylinder2);
    }

    private void updateAtomDesign(boolean toDoAtom1, boolean toDoAtom2) {
        if (toDoAtom1) {
            this.atomDesign1 = null;
            if (this.design != null) {
                Atom atom1 = this.model.getAtom1();
                this.atomDesign1 = this.design.getAtomDesign(atom1.getName());
            }

            if (this.atomDesignAdaptor1 != null) {
                this.atomDesignAdaptor1.detach();
            }

            if (this.atomDesign1 != null) {
                this.atomDesignAdaptor1 = new AtomDesignAdaptor(this);
                this.atomDesign1.addAdaptor(this.atomDesignAdaptor1);
            }
        }

        if (toDoAtom2) {
            this.atomDesign2 = null;
            if (this.design != null) {
                Atom atom2 = this.model.getAtom2();
                this.atomDesign2 = this.design.getAtomDesign(atom2.getName());
            }

            if (this.atomDesignAdaptor2 != null) {
                this.atomDesignAdaptor2.detach();
            }

            if (this.atomDesign2 != null) {
                this.atomDesignAdaptor2 = new AtomDesignAdaptor(this);
                this.atomDesign2.addAdaptor(this.atomDesignAdaptor2);
            }
        }
    }

    private void updateVisibleCylinder() {
        this.currentStyle1 = this.isBallStyle(this.atomDesign1);
        this.currentStyle2 = this.isBallStyle(this.atomDesign2);
        this.bondCylinder1.setVisible(!this.currentStyle1);
        this.bondCylinder2.setVisible(!this.currentStyle2);
    }

    private boolean isBallStyle(AtomDesign atomDesign) {
        return atomDesign != null && atomDesign.getAtomsStyle() == AtomsStyle.BALL;
    }

    private void updateXYZOfCylinder() {
        Atom atom1 = this.model.getAtom1();
        int anum1 = atom1.getAtomNum();
        double x1 = atom1.getX();
        double y1 = atom1.getY();
        double z1 = atom1.getZ();

        double rad1 = -1.0;
        if (this.atomDesign1 != null) {
            rad1 = this.atomDesign1.getRadius();
        }
        if (rad1 <= 0.0) {
            rad1 = atom1.getRadius();
        }

        this.currentRadius1 = rad1;
        rad1 = Math.sqrt(Math.max(rad1, 0.0));

        Atom atom2 = this.model.getAtom2();
        int anum2 = atom2.getAtomNum();
        double x2 = atom2.getX();
        double y2 = atom2.getY();
        double z2 = atom2.getZ();

        double rad2 = -1.0;
        if (this.atomDesign2 != null) {
            rad2 = this.atomDesign2.getRadius();
        }
        if (rad2 <= 0.0) {
            rad2 = atom2.getRadius();
        }

        this.currentRadius2 = rad2;
        rad2 = Math.sqrt(Math.max(rad2, 0.0));

        double dx = x2 - x1;
        double dy = y2 - y1;
        double dz = z2 - z1;
        double rr = dx * dx + dy * dy + dz * dz;
        double r = Math.sqrt(rr);

        double rate1 = rad1 / (rad1 + rad2);
        double rate2 = 1.0 - rate1;
        if (anum1 == anum2) {
            rate1 = 0.999;
            rate2 = 0.001;
        }

        Point3D ax1 = new Point3D(dz, 0.0, -dx);
        Point3D ax2 = new Point3D(-dz, 0.0, dx);
        double theta1 = Math.acos(Math.min(Math.max(-1.0, dy / r), 1.0));
        double theta2 = Math.PI - theta1;

        this.bondCylinder1.setHeight(rate1 * r);
        this.bondCylinder1.setTranslateX(x1 + 0.5 * rate1 * dx);
        this.bondCylinder1.setTranslateY(y1 + 0.5 * rate1 * dy);
        this.bondCylinder1.setTranslateZ(z1 + 0.5 * rate1 * dz);
        this.bondCylinder1.setRotationAxis(ax1);
        this.bondCylinder1.setRotate((180.0 / Math.PI) * theta1);

        this.bondCylinder2.setHeight(rate2 * r);
        this.bondCylinder2.setTranslateX(x2 - 0.5 * rate2 * dx);
        this.bondCylinder2.setTranslateY(y2 - 0.5 * rate2 * dy);
        this.bondCylinder2.setTranslateZ(z2 - 0.5 * rate2 * dz);
        this.bondCylinder2.setRotationAxis(ax2);
        this.bondCylinder2.setRotate((180.0 / Math.PI) * theta2);
    }

    private void updateColorOfCylinder() {
        Atom atom1 = this.model.getAtom1();

        Color color1 = null;
        if (this.atomDesign1 != null) {
            color1 = this.atomDesign1.getColor();
        }
        if (color1 == null) {
            color1 = ElementUtil.getColor(atom1.getName());
        }
        this.currentColor1 = color1;

        PhongMaterial material1 = new PhongMaterial();
        material1.setDiffuseColor(color1);
        material1.setSpecularColor(Color.SILVER);
        this.bondCylinder1.setMaterial(material1);

        Atom atom2 = this.model.getAtom2();

        Color color2 = null;
        if (this.atomDesign2 != null) {
            color2 = this.atomDesign2.getColor();
        }
        if (color2 == null) {
            color2 = ElementUtil.getColor(atom2.getName());
        }
        this.currentColor2 = color2;

        PhongMaterial material2 = new PhongMaterial();
        material2.setDiffuseColor(color2);
        material2.setSpecularColor(Color.SILVER);
        this.bondCylinder2.setMaterial(material2);
    }

    @Override
    public void onLinkedAtomRenamed(BondEvent event) {
        AtomEvent atomEvent = event == null ? null : event.getAtomEvent();
        if (atomEvent == null) {
            return;
        }

        String name1 = atomEvent.getOldName();
        String name2 = atomEvent.getName();
        if (name1 != null && name1.equals(name2)) {
            return;
        }

        Atom srcAtom = event.getAtom();
        if (srcAtom == this.model.getAtom1()) {
            this.updateAtomDesign(true, false);
        } else if (srcAtom == this.model.getAtom2()) {
            this.updateAtomDesign(false, true);
        }

        if ((!this.currentStyle1) || (!this.currentStyle2)) {
            this.updateXYZOfCylinder();
            this.updateColorOfCylinder();
        }
    }

    @Override
    public void onLinkedAtomMoved(BondEvent event) {
        AtomEvent atomEvent = event == null ? null : event.getAtomEvent();
        if (atomEvent == null) {
            return;
        }

        double dx = atomEvent.getDeltaX();
        double dy = atomEvent.getDeltaY();
        double dz = atomEvent.getDeltaZ();
        double rr = dx * dx + dy * dy + dz * dz;
        if (rr < RRMIN) {
            return;
        }

        if ((!this.currentStyle1) || (!this.currentStyle2)) {
            this.updateXYZOfCylinder();
        }
    }

    @Override
    public void onAtomicRadiusChanged(AtomDesign atomDesign, double radius) {
        if (radius <= 0.0) {
            return;
        } else if (atomDesign == this.atomDesign1) {
            if (Math.abs(radius - this.currentRadius1) < RMIN) {
                return;
            }
        } else if (atomDesign == this.atomDesign2) {
            if (Math.abs(radius - this.currentRadius2) < RMIN) {
                return;
            }
        } else {
            return;
        }

        if ((!this.currentStyle1) || (!this.currentStyle2)) {
            this.updateXYZOfCylinder();
        }
    }

    @Override
    public void onAtomicColorChanged(AtomDesign atomDesign, Color color) {
        if (color == null) {
            return;
        } else if (atomDesign == this.atomDesign1) {
            if (color.equals(this.currentColor1)) {
                return;
            }
        } else if (atomDesign == this.atomDesign2) {
            if (color.equals(this.currentColor2)) {
                return;
            }
        } else {
            return;
        }

        if ((!this.currentStyle1) || (!this.currentStyle2)) {
            this.updateColorOfCylinder();
        }
    }

    @Override
    public void onAtomsStyleChanged(AtomDesign atomDesign, AtomsStyle atomsStyle) {
        if (atomsStyle == null) {
            return;
        } else if (atomDesign == this.atomDesign1) {
            if (this.isBallStyle(atomDesign) == this.currentStyle1) {
                return;
            }
        } else if (atomDesign == this.atomDesign2) {
            if (this.isBallStyle(atomDesign) == this.currentStyle2) {
                return;
            }
        } else {
            return;
        }

        if (this.currentStyle1 && this.currentStyle2) {
            this.updateXYZOfCylinder();
            this.updateColorOfCylinder();
        }

        this.updateVisibleCylinder();
    }

    @Override
    public void onBondWidthChanged(AtomDesign atomDesign, double bondWidth) {
        // TODO 自動生成されたメソッド・スタブ
    }
}
