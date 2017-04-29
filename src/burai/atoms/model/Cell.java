/*
 * Copyright (C) 2016 Satomichi Nishihara
 *
 * This file is distributed under the terms of the
 * GNU General Public License. See the file `LICENSE'
 * in the root directory of the present distribution,
 * or http://www.gnu.org/copyleft/gpl.txt .
 */

package burai.atoms.model;

import java.util.ArrayList;
import java.util.List;

import burai.atoms.model.event.CellEvent;
import burai.atoms.model.event.CellEventListener;
import burai.atoms.model.exception.ZeroVolumCellException;
import burai.com.math.Lattice;
import burai.com.math.Matrix3D;

public class Cell extends Model<CellEvent, CellEventListener> {

    public static final int ATOMS_POSITION_WITH_LATTICE = 0;
    public static final int ATOMS_POSITION_SCALED = 1;
    public static final int ATOMS_POSITION_LEFT = 2;

    private static final int MAX_ATOMS_TO_RESOLVE = 2048;

    private static final double MIN_VOLUME = 1.0e-6;

    private static final double THR_LATTICE = 1.0e-4;

    private double[][] lattice;

    private double volume;

    private double[] normLattice;

    private double[][] recLattice;

    private List<Atom> atoms;

    private List<Bond> bonds;

    private int maxAtomsToResolve;

    private boolean resolverStopping;

    private AtomsResolver atomsResolver;

    private BondsResolver bondsResolver;

    public Cell(double[][] lattice) throws ZeroVolumCellException {
        this(lattice, MAX_ATOMS_TO_RESOLVE);
    }

    public Cell(double[][] lattice, int maxAtomsToResolve) throws ZeroVolumCellException {
        this.checkLattice(lattice);
        this.setupLattice(lattice);

        this.atoms = null;
        this.bonds = null;

        this.maxAtomsToResolve = Math.max(0, maxAtomsToResolve);

        this.resolverStopping = false;

        this.atomsResolver = new AtomsResolver(this);

        this.bondsResolver = null;
        if (this.maxAtomsToResolve > 0) {
            this.bondsResolver = new BondsResolver(this);
        }
    }

    private void checkLattice(double[][] lattice) throws ZeroVolumCellException {
        if (lattice == null || lattice.length < 3) {
            throw new IllegalArgumentException("lattice is null or too short.");
        }

        for (int i = 0; i < 3; i++) {
            if (lattice[i] == null || lattice.length < 3) {
                throw new IllegalArgumentException("lattice[" + i + "] is null or too short.");
            }
        }

        double volume = this.calcVolume(lattice);
        if (volume < MIN_VOLUME) {
            throw new ZeroVolumCellException();
        }
    }

    private void setupLattice(double[][] lattice) {
        this.lattice = Matrix3D.copy(lattice);
        this.calcVolume();
        this.calcNormLattice();
        this.calcRecLattice();
    }

    private double calcVolume(double[][] lattice) {
        return Math.abs(Matrix3D.determinant(lattice));
    }

    private void calcVolume() {
        this.volume = this.calcVolume(this.lattice);
    }

    private void calcNormLattice() {
        this.normLattice = new double[3];
        this.normLattice[0] = Matrix3D.norm(this.lattice[0]);
        this.normLattice[1] = Matrix3D.norm(this.lattice[1]);
        this.normLattice[2] = Matrix3D.norm(this.lattice[2]);
    }

    private void calcRecLattice() {
        this.recLattice = Matrix3D.inverse(this.lattice);
    }

    @Override
    protected CellEvent createEvent() {
        return new CellEvent(this);
    }

    protected double[][] getLattice() {
        return this.lattice;
    }

    protected double[][] getRecLattice() {
        return this.recLattice;
    }

    protected double[] getNormLattice() {
        return this.normLattice;
    }

    public double getVolume() {
        return this.volume;
    }

    public double[][] copyLattice() {
        return Matrix3D.copy(this.lattice);
    }

    public double getA() {
        return Lattice.getA(this.lattice);
    }

    public double getB() {
        return Lattice.getB(this.lattice);
    }

    public double getC() {
        return Lattice.getC(this.lattice);
    }

    public double getAlpha() {
        return Lattice.getAlpha(this.lattice);
    }

    public double getBeta() {
        return Lattice.getBeta(this.lattice);
    }

    public double getGamma() {
        return Lattice.getGamma(this.lattice);
    }

    private double[] convertToCartesianPosition(double a, double b, double c, double[][] lattice) {
        return Matrix3D.mult(new double[] { a, b, c }, lattice);
    }

    public double[] convertToCartesianPosition(double a, double b, double c) {
        return this.convertToCartesianPosition(a, b, c, this.lattice);
    }

    private double[] convertToLatticePosition(double x, double y, double z, double[][] recLattice) {
        return Matrix3D.mult(new double[] { x, y, z }, recLattice);
    }

    public double[] convertToLatticePosition(double x, double y, double z) {
        return this.convertToLatticePosition(x, y, z, this.recLattice);
    }

    public boolean isInCell(double x, double y, double z) {
        double[] position = this.convertToLatticePosition(x, y, z);
        double a = position[0];
        double b = position[1];
        double c = position[2];

        boolean inCell = true;
        inCell = inCell && (0.0 <= a) && (a < 1.0);
        inCell = inCell && (0.0 <= b) && (b < 1.0);
        inCell = inCell && (0.0 <= c) && (c < 1.0);

        return inCell;
    }

    protected List<Atom> getAtoms() {
        return this.atoms;
    }

    public int numAtoms(boolean masterOnly) {
        if (this.atoms == null || this.atoms.isEmpty()) {
            return 0;
        }

        int natom = this.atoms.size();

        if (masterOnly) {
            for (Atom atom : this.atoms) {
                if (atom == null || atom.isSlaveAtom()) {
                    natom--;
                }
            }
        }

        return natom;
    }

    public int numAtoms() {
        return this.numAtoms(false);
    }

    public Atom[] listAtoms(boolean masterOnly) {
        if (this.atoms == null) {
            return null;
        }

        List<Atom> atoms2 = this.atoms;

        if (masterOnly) {
            atoms2 = new ArrayList<Atom>();
            for (Atom atom : this.atoms) {
                if (!atom.isSlaveAtom()) {
                    atoms2.add(atom);
                }
            }
        }

        return atoms2.toArray(new Atom[atoms2.size()]);
    }

    public Atom[] listAtoms() {
        return this.listAtoms(false);
    }

    protected List<Bond> getBonds() {
        return this.bonds;
    }

    public Bond[] listBonds() {
        if (this.bonds == null) {
            return null;
        }

        return this.bonds.toArray(new Bond[this.bonds.size()]);
    }

    public boolean isResolving() {
        return (this.numAtoms(true) <= this.maxAtomsToResolve);
    }

    public void stopResolving() {
        this.resolverStopping = true;

        if (this.atomsResolver != null) {
            this.atomsResolver.setAuto(false);
        }

        if (this.bondsResolver != null) {
            this.bondsResolver.setAuto(false);
        }
    }

    public void restartResolving() {
        this.resolverStopping = false;

        if (this.atomsResolver != null) {
            this.atomsResolver.setAuto(true);
            this.atomsResolver.resolve();
        }

        if (this.bondsResolver != null && this.isResolving()) {
            this.bondsResolver.setAuto(true);
            this.bondsResolver.resolve();
        }
    }

    public boolean equalsLattice(double lattice[][]) throws ZeroVolumCellException {
        this.checkLattice(lattice);

        return Matrix3D.equals(this.lattice, lattice, THR_LATTICE);
    }

    public boolean moveLattice(double lattice[][]) throws ZeroVolumCellException {
        return this.moveLattice(lattice, ATOMS_POSITION_WITH_LATTICE, null);
    }

    public boolean moveLattice(double lattice[][], int atomsPosition) throws ZeroVolumCellException {
        return this.moveLattice(lattice, atomsPosition, null);
    }

    public boolean moveLattice(double lattice[][], int atomsPosition, List<Atom> refAtoms) throws ZeroVolumCellException {
        if (this.equalsLattice(lattice)) {
            return false;
        }

        boolean orgAutoAtoms = false;
        if (this.atomsResolver != null) {
            orgAutoAtoms = this.atomsResolver.isAuto();
            this.atomsResolver.setAuto(false);
        }

        boolean orgAutoBonds = false;
        if (this.bondsResolver != null) {
            orgAutoBonds = this.bondsResolver.isAuto();
            this.bondsResolver.setAuto(false);
        }

        if (this.atoms != null) {
            if (atomsPosition == ATOMS_POSITION_WITH_LATTICE) {
                Atom[] atomList = this.listAtoms(true);
                boolean withRef = (refAtoms != null && atomList.length <= refAtoms.size());

                for (int i = 0; i < atomList.length; i++) {
                    Atom atom = null;
                    if (withRef) {
                        atom = refAtoms.get(i);
                        atom = (atom == null) ? atomList[i] : atom;
                    } else {
                        atom = atomList[i];
                    }

                    double[] position = null;
                    double x = atom.getX();
                    double y = atom.getY();
                    double z = atom.getZ();
                    position = this.convertToLatticePosition(x, y, z);
                    double a = position[0];
                    double b = position[1];
                    double c = position[2];
                    position = this.convertToCartesianPosition(a, b, c, lattice);
                    x = position[0];
                    y = position[1];
                    z = position[2];
                    atom.moveTo(x, y, z);
                }

            } else if (atomsPosition == ATOMS_POSITION_SCALED) {
                double oldScale = this.normLattice[0];
                double newScale = Matrix3D.norm(lattice[0]);
                Atom[] atomList = this.listAtoms(true);
                boolean withRef = (refAtoms != null && atomList.length <= refAtoms.size());

                for (int i = 0; i < atomList.length; i++) {
                    Atom atom = null;
                    if (withRef) {
                        atom = refAtoms.get(i);
                        atom = (atom == null) ? atomList[i] : atom;
                    } else {
                        atom = atomList[i];
                    }

                    double x = (newScale / oldScale) * atom.getX();
                    double y = (newScale / oldScale) * atom.getY();
                    double z = (newScale / oldScale) * atom.getZ();
                    atomList[i].moveTo(x, y, z);
                }

            } else if (atomsPosition == ATOMS_POSITION_LEFT) {
                if (refAtoms != null && !(refAtoms.isEmpty())) {
                    Atom[] atomList = this.listAtoms(true);

                    int natom = 0;
                    if (atomList.length <= refAtoms.size()) {
                        natom = atomList.length;
                    }

                    for (int i = 0; i < natom; i++) {
                        Atom atom = refAtoms.get(i);
                        atom = (atom == null) ? atomList[i] : atom;
                        double x = atom.getX();
                        double y = atom.getY();
                        double z = atom.getZ();
                        atomList[i].moveTo(x, y, z);
                    }
                }
            }
        }

        this.setupLattice(lattice);

        if (this.atomsResolver != null) {
            this.atomsResolver.setAuto(orgAutoAtoms);
        }

        if (this.bondsResolver != null) {
            this.bondsResolver.setAuto(orgAutoBonds);
        }

        if (this.listeners != null) {
            CellEvent event = new CellEvent(this);
            event.setLattice(lattice);
            for (CellEventListener listener : this.listeners) {
                listener.onLatticeMoved(event);
            }
        }

        return true;
    }

    public boolean hasAtomAt(double a, double b, double c) {
        double[] position = this.convertToCartesianPosition(a, b, c);
        double x = position[0];
        double y = position[1];
        double z = position[2];
        return this.hasAtomAt(new Atom(null, x, y, z));
    }

    public boolean hasAtomAt(Atom atom) {
        if (atom == null) {
            return false;
        }

        if (this.atoms == null || this.atoms.isEmpty()) {
            return false;
        }

        if (this.atomsResolver != null) {
            this.atomsResolver.packAtomIntoCell(atom);
        }

        for (Atom atom2 : this.atoms) {
            if (atom.equalsPosition(atom2)) {
                return true;
            }
        }

        return false;
    }

    public int indexOfAtom(Atom atom) {
        if (atom == null) {
            return -1;
        }

        if (this.atoms != null) {
            return this.atoms.indexOf(atom);
        }

        //int natom = this.atoms == null ? 0 : this.atoms.size();
        //for (int i = 0; i < natom; i++) {
        //    Atom atom2 = this.atoms.get(i);
        //    if (atom == atom2) {
        //        return i;
        //    }
        //}

        return -1;
    }

    public boolean addAtom(String name, double a, double b, double c) {
        double[] position = this.convertToCartesianPosition(a, b, c);
        double x = position[0];
        double y = position[1];
        double z = position[2];
        return this.addAtom(new Atom(name, x, y, z));
    }

    public boolean addAtom(Atom atom) {
        if (atom == null) {
            return false;
        }

        if (this.atoms == null) {
            this.atoms = new ArrayList<Atom>();
        }

        if (!atom.isSlaveAtom()) {
            if (this.atomsResolver != null) {
                this.atomsResolver.packAtomIntoCell(atom);
            }
        }

        if (this.atoms.contains(atom)) {
            return false;
        }

        boolean status = this.atoms.add(atom);
        if (!status) {
            return false;
        }

        if (this.bondsResolver != null && (!this.resolverStopping)) {
            boolean auto1 = this.bondsResolver.isAuto();
            boolean auto2 = this.isResolving();
            if (auto1 && (!auto2)) {
                this.bondsResolver.setAuto(false);
                //this.removeAllBonds();
            }
        }

        if (this.listeners != null) {
            CellEvent event = new CellEvent(this);
            event.setAtom(atom);
            for (CellEventListener listener : this.listeners) {
                listener.onAtomAdded(event);
            }
        }

        return true;
    }

    public boolean removeAtom(Atom atom) {
        if (atom == null) {
            return false;
        }

        if (this.atoms == null) {
            return false;
        }

        int index = this.atoms.indexOf(atom);
        if (index < 0) {
            return false;
        }

        Atom atom2 = this.atoms.remove(index);
        atom2.notDisplay();
        if (atom2.isSlaveAtom()) {
            atom2.setMasterAtom(null);
        }

        if (this.listeners != null) {
            CellEvent event = new CellEvent(this);
            event.setAtom(atom2);
            for (CellEventListener listener : this.listeners) {
                listener.onAtomRemoved(event);
            }
        }

        if (this.bondsResolver != null && (!this.resolverStopping)) {
            boolean auto1 = this.bondsResolver.isAuto();
            boolean auto2 = this.isResolving();
            if ((!auto1) && auto2) {
                this.bondsResolver.setAuto(true);
                this.bondsResolver.resolve();
            }
        }

        return true;
    }

    public void removeAllAtoms() {
        Atom[] atomList = this.listAtoms();
        if (atomList == null || atomList.length < 1) {
            return;
        }

        boolean orgAuto = false;
        if (this.bondsResolver != null) {
            orgAuto = this.bondsResolver.isAuto();
            this.bondsResolver.setAuto(false);
        }

        for (Atom atom : atomList) {
            this.removeAtom(atom);
        }

        if (this.bondsResolver != null) {
            this.bondsResolver.setAuto(orgAuto);
            this.bondsResolver.resolve();
        }
    }

    protected Bond pickBond(Atom atom1, Atom atom2) {
        if (this.bonds == null || this.bonds.isEmpty()) {
            return null;
        }

        for (Bond bond : this.bonds) {
            Atom refAtom1 = bond.getAtom1();
            Atom refAtom2 = bond.getAtom2();
            if (refAtom1 == atom1 && refAtom2 == atom2) {
                return bond;
            }
            if (refAtom1 == atom2 && refAtom2 == atom1) {
                return bond;
            }
        }

        return null;
    }

    protected boolean addBond(Bond bond) {
        if (bond == null) {
            return false;
        }

        if (this.bonds == null) {
            this.bonds = new ArrayList<Bond>();
        }

        if (this.bonds.contains(bond)) {
            return false;
        }

        boolean status = this.bonds.add(bond);
        if (!status) {
            return false;
        }

        if (this.listeners != null) {
            CellEvent event = new CellEvent(this);
            event.setBond(bond);
            for (CellEventListener listener : this.listeners) {
                listener.onBondAdded(event);
            }
        }

        return true;
    }

    protected boolean removeBond(Bond bond) {
        if (bond == null) {
            return false;
        }

        if (this.bonds == null) {
            return false;
        }

        int index = this.bonds.indexOf(bond);
        if (index < 0) {
            return false;
        }

        Bond bond2 = this.bonds.remove(index);
        bond2.notDisplay();
        bond2.detachFromAtoms();

        if (this.listeners != null) {
            CellEvent event = new CellEvent(this);
            event.setBond(bond2);
            for (CellEventListener listener : this.listeners) {
                listener.onBondRemoved(event);
            }
        }

        return true;
    }

    protected void removeAllBonds() {
        Bond[] bondList = this.listBonds();

        for (Bond bond : bondList) {
            this.removeBond(bond);
        }
    }

    @Override
    public void flushListeners() {
        super.flushListeners();

        if (this.atoms != null) {
            for (Atom atom : this.atoms) {
                atom.flushListeners();
            }
        }

        if (this.bonds != null) {
            for (Bond bond : this.bonds) {
                bond.flushListeners();
            }
        }
    }

    @Override
    public void display() {
        super.display();

        if (this.atoms != null) {
            for (Atom atom : this.atoms) {
                atom.display();
            }
        }

        if (this.bonds != null) {
            for (Bond bond : this.bonds) {
                bond.display();
            }
        }
    }

    @Override
    public void notDisplay() {
        super.notDisplay();

        if (this.atoms != null) {
            for (Atom atom : this.atoms) {
                atom.notDisplay();
            }
        }

        if (this.bonds != null) {
            for (Bond bond : this.bonds) {
                bond.notDisplay();
            }
        }
    }
}
