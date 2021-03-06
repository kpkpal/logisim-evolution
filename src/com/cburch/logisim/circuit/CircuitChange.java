/**
 * This file is part of Logisim-evolution.
 *
 * Logisim-evolution is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Logisim-evolution is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with Logisim-evolution.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Original code by Carl Burch (http://www.cburch.com), 2011.
 * Subsequent modifications by:
 *   + Haute École Spécialisée Bernoise
 *     http://www.bfh.ch
 *   + Haute École du paysage, d'ingénierie et d'architecture de Genève
 *     http://hepia.hesge.ch/
 *   + Haute École d'Ingénierie et de Gestion du Canton de Vaud
 *     http://www.heig-vd.ch/
 *   + REDS Institute - HEIG-VD, Yverdon-les-Bains, Switzerland
 *     http://reds.heig-vd.ch
 * This version of the project is currently maintained by:
 *   + Kevin Walsh (kwalsh@holycross.edu, http://mathcs.holycross.edu/~kwalsh)
 */

package com.cburch.logisim.circuit;

import java.util.Collection;

import com.cburch.logisim.comp.Component;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.instance.StdAttr;
import com.cburch.logisim.std.wiring.Pin;
import com.cburch.logisim.std.hdl.VhdlContent;
import com.cburch.logisim.std.hdl.VhdlEntity;

class CircuitChange {
  public static CircuitChange add(Circuit circuit, Component comp) {
    return new CircuitChange(circuit, ADD, comp);
  }

  public static CircuitChange addAll(Circuit circuit,
      Collection<? extends Component> comps) {
    return new CircuitChange(circuit, ADD_ALL, comps);
  }

  public static CircuitChange clear(Circuit circuit,
      Collection<Component> oldComponents) {
    return new CircuitChange(circuit, CLEAR, oldComponents);
  }

  public static CircuitChange remove(Circuit circuit, Component comp) {
    return new CircuitChange(circuit, REMOVE, comp);
  }

  public static CircuitChange removeAll(Circuit circuit,
      Collection<? extends Component> comps) {
    return new CircuitChange(circuit, REMOVE_ALL, comps);
  }

  public static CircuitChange replace(Circuit circuit, ReplacementMap replMap) {
    return new CircuitChange(circuit, REPLACE, null, null, null, replMap);
  }

  public static CircuitChange set(Circuit circuit, Component comp,
      Attribute<?> attr, Object value) {
    return new CircuitChange(circuit, SET, comp, attr, null, value);
  }

  public static CircuitChange set(Circuit circuit, Component comp,
      Attribute<?> attr, Object oldValue, Object newValue) {
    return new CircuitChange(circuit, SET, comp, attr, oldValue, newValue);
  }

  public static CircuitChange setForCircuit(Circuit circuit,
      Attribute<?> attr, Object v) {
    return new CircuitChange(circuit, SET_FOR_CIRCUIT, null, attr, null, v);
  }

  public static CircuitChange setForCircuit(Circuit circuit,
      Attribute<?> attr, Object oldValue, Object newValue) {
    return new CircuitChange(circuit, SET_FOR_CIRCUIT, null, attr,
        oldValue, newValue);
  }

  public static CircuitChange setForVhdl(VhdlContent vhdl,
      Attribute<?> attr, Object v) {
    return new CircuitChange(vhdl, SET_FOR_VHDL, null, attr, null, v);
  }

  public static CircuitChange setForVhdl(VhdlContent vhdl,
      Attribute<?> attr, Object oldValue, Object newValue) {
    return new CircuitChange(vhdl, SET_FOR_VHDL, null, attr,
        oldValue, newValue);
  }

  static final int CLEAR = 0;
  static final int ADD = 1;
  static final int ADD_ALL = 2;
  static final int REMOVE = 3;
  static final int REMOVE_ALL = 4;
  static final int REPLACE = 5;
  static final int SET = 6;
  static final int SET_FOR_CIRCUIT = 7;
  static final int SET_FOR_VHDL = 8;

  private Circuit circuit;
  private VhdlContent vhdl;
  private int type;
  private Component comp;
  private Collection<? extends Component> comps;
  private Attribute<?> attr;
  private Object oldValue;
  private Object newValue;

  private CircuitChange(Circuit circuit, int type,
      Collection<? extends Component> comps) {
    this(circuit, type, null, null, null, null);
    this.comps = comps;
  }

  private CircuitChange(Circuit circuit, int type, Component comp) {
    this(circuit, type, comp, null, null, null);
  }

  private CircuitChange(Circuit circuit, int type, Component comp,
      Attribute<?> attr, Object oldValue, Object newValue) {
    this.circuit = circuit;
    this.type = type;
    this.comp = comp;
    this.attr = attr;
    this.oldValue = oldValue;
    this.newValue = newValue;
  }

  private CircuitChange(VhdlContent vhdl, int type, Component comp,
      Attribute<?> attr, Object oldValue, Object newValue) {
    this.vhdl = vhdl;
    this.type = type;
    this.comp = comp;
    this.attr = attr;
    this.oldValue = oldValue;
    this.newValue = newValue;
  }

  boolean concernsSupercircuit() {
    switch (type) {
    case CLEAR:
      return true;
    case ADD:
    case REMOVE:
      return comp.getFactory() instanceof Pin;
    case ADD_ALL:
    case REMOVE_ALL:
      for (Component comp : comps) {
        if (comp.getFactory() instanceof Pin)
          return true;
      }
      return false;
    case REPLACE:
      ReplacementMap repl = (ReplacementMap) newValue;
      for (Component comp : repl.getRemovals()) {
        if (comp.getFactory() instanceof Pin)
          return true;
      }
      for (Component comp : repl.getAdditions()) {
        if (comp.getFactory() instanceof Pin)
          return true;
      }
      return false;
    case SET:
      return comp.getFactory() instanceof Pin
          && (attr == StdAttr.WIDTH || attr == Pin.ATTR_TYPE);
    case SET_FOR_CIRCUIT:
      return attr == CircuitAttributes.APPEARANCE_ATTR
          || attr == CircuitAttributes.NAME_ATTR
          || attr == CircuitAttributes.CIRCUIT_LABEL_ATTR
          || attr == CircuitAttributes.CIRCUIT_LABEL_FACING_ATTR
          || attr == CircuitAttributes.CIRCUIT_LABEL_FONT_ATTR;
    case SET_FOR_VHDL:
      return attr == VhdlEntity.NAME_ATTR
          || attr == StdAttr.APPEARANCE; // note: always true so far
    default:
      return false;
    }
  }

  // fixme: remove? never happens...?
  boolean concernsSiblingComponents() {
    switch (type) {
    case SET:
      if ( (comp.getFactory() instanceof SubcircuitFactory && attr == CircuitAttributes.APPEARANCE_ATTR)
          || (comp.getFactory() instanceof VhdlEntity && attr == StdAttr.APPEARANCE)) { 
        System.out.println("yes, concerns sibling");
        return true;
      } else {
        return false;
      }
    default:
      return false;
    }
  }

  void execute(CircuitMutator mutator, ReplacementMap prevReplacements) {
    switch (type) {
    case CLEAR:
      if (circuit == null)
        throw new IllegalArgumentException("null circuit with change type " + type);
      mutator.clear(circuit);
      prevReplacements.reset();
      break;
    case ADD:
      if (circuit == null)
        throw new IllegalArgumentException("null circuit with change type " + type);
      prevReplacements.add(comp);
      break;
    case ADD_ALL:
      if (circuit == null)
        throw new IllegalArgumentException("null circuit with change type " + type);
      for (Component comp : comps)
        prevReplacements.add(comp);
      break;
    case REMOVE:
      if (circuit == null)
        throw new IllegalArgumentException("null circuit with change type " + type);
      prevReplacements.remove(comp);
      break;
    case REMOVE_ALL:
      if (circuit == null)
        throw new IllegalArgumentException("null circuit with change type " + type);
      for (Component comp : comps)
        prevReplacements.remove(comp);
      break;
    case REPLACE:
      if (circuit == null)
        throw new IllegalArgumentException("null circuit with change type " + type);
      prevReplacements.append((ReplacementMap) newValue);
      break;
    case SET:
      if (circuit == null)
        throw new IllegalArgumentException("null circuit with change type " + type);
      mutator.replace(circuit, prevReplacements);
      prevReplacements.reset();
      mutator.set(circuit, comp, attr, newValue);
      break;
    case SET_FOR_CIRCUIT:
      if (circuit == null)
        throw new IllegalArgumentException("null circuit with change type " + type);
      mutator.replace(circuit, prevReplacements);
      prevReplacements.reset();
      mutator.setForCircuit(circuit, attr, newValue);
      break;
    case SET_FOR_VHDL:
      if (vhdl == null)
        throw new IllegalArgumentException("null vhdl with change type " + type);
      mutator.setForVhdl(vhdl, attr, newValue);
      break;
    default:
      throw new IllegalArgumentException("unknown change type " + type);
    }
  }

  public Attribute<?> getAttribute() {
    return attr;
  }

  public Circuit getCircuit() {
    return circuit;
  }

  public VhdlContent getVhdl() {
    return vhdl;
  }

  public Component getComponent() {
    return comp;
  }

  public Object getNewValue() {
    return newValue;
  }

  public Object getOldValue() {
    return oldValue;
  }

  CircuitChange getReverseChange() {
    switch (type) {
    case CLEAR:
      return CircuitChange.addAll(circuit, comps);
    case ADD:
      return CircuitChange.remove(circuit, comp);
    case ADD_ALL:
      return CircuitChange.removeAll(circuit, comps);
    case REMOVE:
      return CircuitChange.add(circuit, comp);
    case REMOVE_ALL:
      return CircuitChange.addAll(circuit, comps);
    case SET:
      return CircuitChange.set(circuit, comp, attr, newValue, oldValue);
    case SET_FOR_CIRCUIT:
      return CircuitChange.setForCircuit(circuit, attr, newValue, oldValue);
    case SET_FOR_VHDL:
      return CircuitChange.setForVhdl(vhdl, attr, newValue, oldValue);
    case REPLACE:
      return CircuitChange.replace(circuit,
          ((ReplacementMap) newValue).getInverseMap());
    default:
      throw new IllegalArgumentException("unknown change type " + type);
    }
  }

  public int getType() {
    return type;
  }

  public String toString() {
    switch (type) {
    case CLEAR: return "CLEAR " + comps.size() + " comps";
    case ADD: return "ADD " + comp;
    case ADD_ALL: return "ADD_ALL " + comps.size() + " comps";
    case REMOVE: return "REMOVE " + comp;
    case REMOVE_ALL: return "REMOVE_ALL " + comps.size() + " comps";
    case REPLACE: return "REPLACE ...";
    case SET: return "SET " + attr + " = " + newValue +" from " + oldValue;
    case SET_FOR_CIRCUIT: return "SET_FOR_CIRCUIT " + attr + " = " + newValue +" from " + oldValue;
    case SET_FOR_VHDL: return "SET_FOR_VHDL " + attr + " = " + newValue +" from " + oldValue;
    default: return "CircuitChange("+type+")";
    }
  }
}
