/*
 * Copyright (C) 2017 Satomichi Nishihara
 *
 * This file is distributed under the terms of the
 * GNU General Public License. See the file `LICENSE'
 * in the root directory of the present distribution,
 * or http://www.gnu.org/copyleft/gpl.txt .
 */

package burai.atoms.design;

import java.util.ArrayList;
import java.util.List;

import burai.atoms.element.ElementUtil;
import javafx.scene.paint.Color;

public class AtomDesign {

    private static final int MAX_COUNT_LISTENERS = 32;

    private int countAdaptors;

    private List<AtomDesignAdaptor> adaptors;

    private double radius;

    private Color color;

    private AtomsStyle atomsStyle;

    private double bondWidth;

    public AtomDesign(double radius, Color color) {
        if (radius <= 0.0) {
            throw new IllegalArgumentException("radius is not positive.");
        }

        if (color == null) {
            throw new IllegalArgumentException("color is null.");
        }

        this.countAdaptors = 0;
        this.adaptors = null;

        this.radius = radius;
        this.color = color;
        this.atomsStyle = AtomsStyle.BALL_STICK;
        this.bondWidth = 1.0;
    }

    public AtomDesign(double radius, double red, double green, double blue) {
        this(radius, Color.color(red, green, blue));
    }

    public AtomDesign(String name) {
        this(ElementUtil.getCovalentRadius(name), ElementUtil.getColor(name));
    }

    public void addAdaptor(AtomDesignAdaptor adaptor) {
        if (adaptor == null) {
            return;
        }

        if (this.adaptors == null) {
            this.adaptors = new ArrayList<>();
        }

        this.refreshAdaptors();

        this.adaptors.add(adaptor);
    }

    private void refreshAdaptors() {
        if (this.adaptors == null || this.adaptors.isEmpty()) {
            return;
        }

        if (this.countAdaptors < MAX_COUNT_LISTENERS) {
            this.countAdaptors++;
            return;
        }

        this.countAdaptors = 0;

        int numAdaptors = this.adaptors.size();
        for (int i = (numAdaptors - 1); i >= 0; i--) {
            AtomDesignAdaptor adaptor = this.adaptors.get(i);
            if (adaptor == null || !adaptor.isAlive()) {
                this.adaptors.remove(i);
            }
        }
    }

    public double getRadius() {
        return this.radius;
    }

    public void setRadius(double radius) {
        if (radius <= 0.0) {
            return;
        }

        this.radius = radius;

        if (this.adaptors != null && !this.adaptors.isEmpty()) {
            for (AtomDesignAdaptor adaptor : this.adaptors) {
                AtomDesignListener listener = null;
                if (adaptor != null && adaptor.isAlive()) {
                    listener = adaptor.getListener();
                }
                if (listener != null) {
                    listener.onAtomicRadiusChanged(this, this.radius);
                }
            }
        }
    }

    public Color getColor() {
        return this.color;
    }

    public void setColor(Color color) {
        if (color == null) {
            return;
        }

        this.color = color;

        if (this.adaptors != null && !this.adaptors.isEmpty()) {
            for (AtomDesignAdaptor adaptor : this.adaptors) {
                AtomDesignListener listener = null;
                if (adaptor != null && adaptor.isAlive()) {
                    listener = adaptor.getListener();
                }
                if (listener != null) {
                    listener.onAtomicColorChanged(this, this.color);
                }
            }
        }
    }

    public AtomsStyle getAtomsStyle() {
        return this.atomsStyle;
    }

    public void setAtomsStyle(AtomsStyle atomsStyle) {
        if (atomsStyle == null) {
            return;
        }

        this.atomsStyle = atomsStyle;

        if (this.adaptors != null && !this.adaptors.isEmpty()) {
            for (AtomDesignAdaptor adaptor : this.adaptors) {
                AtomDesignListener listener = null;
                if (adaptor != null && adaptor.isAlive()) {
                    listener = adaptor.getListener();
                }
                if (listener != null) {
                    listener.onAtomsStyleChanged(this, this.atomsStyle);
                }
            }
        }
    }

    public double getBondWidth() {
        return this.bondWidth;
    }

    public void setBondWidth(double bondWidth) {
        if (bondWidth <= 0.0) {
            return;
        }

        this.bondWidth = bondWidth;

        if (this.adaptors != null && !this.adaptors.isEmpty()) {
            for (AtomDesignAdaptor adaptor : this.adaptors) {
                AtomDesignListener listener = null;
                if (adaptor != null && adaptor.isAlive()) {
                    listener = adaptor.getListener();
                }
                if (listener != null) {
                    listener.onBondWidthChanged(this, this.bondWidth);
                }
            }
        }
    }
}
