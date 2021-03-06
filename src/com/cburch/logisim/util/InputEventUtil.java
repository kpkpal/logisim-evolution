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

package com.cburch.logisim.util;
import static com.cburch.logisim.util.Strings.S;

import java.awt.Event;
import java.awt.event.InputEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

public class InputEventUtil {
  public static int fromDisplayString(String str) {
    int ret = 0;
    StringTokenizer toks = new StringTokenizer(str);
    while (toks.hasMoreTokens()) {
      String s = toks.nextToken();
      if (s.equals(S.get("ctrlMod")))
        ret |= InputEvent.CTRL_DOWN_MASK;
      else if (s.equals(S.get("altMod")))
        ret |= InputEvent.ALT_DOWN_MASK;
      else if (s.equals(S.get("shiftMod")))
        ret |= InputEvent.SHIFT_DOWN_MASK;
      else if (s.equals(S.get("button1Mod")))
        ret |= InputEvent.BUTTON1_DOWN_MASK;
      else if (s.equals(S.get("button2Mod")))
        ret |= InputEvent.BUTTON2_DOWN_MASK;
      else if (s.equals(S.get("button3Mod")))
        ret |= InputEvent.BUTTON3_DOWN_MASK;
      else
        throw new NumberFormatException("InputEventUtil");
    }
    return ret;
  }

  public static int fromString(String str) {
    int ret = 0;
    StringTokenizer toks = new StringTokenizer(str);
    while (toks.hasMoreTokens()) {
      String s = toks.nextToken();
      if (s.equals(CTRL))
        ret |= InputEvent.CTRL_DOWN_MASK;
      else if (s.equals(SHIFT))
        ret |= InputEvent.SHIFT_DOWN_MASK;
      else if (s.equals(ALT))
        ret |= InputEvent.ALT_DOWN_MASK;
      else if (s.equals(BUTTON1))
        ret |= InputEvent.BUTTON1_DOWN_MASK;
      else if (s.equals(BUTTON2))
        ret |= InputEvent.BUTTON2_DOWN_MASK;
      else if (s.equals(BUTTON3))
        ret |= InputEvent.BUTTON3_DOWN_MASK;
      else
        throw new NumberFormatException("InputEventUtil");
    }
    return ret;
  }

  public static String toDisplayString(int mods) {
    ArrayList<String> arr = new ArrayList<String>();
    if ((mods & InputEvent.CTRL_DOWN_MASK) != 0)
      arr.add(S.get("ctrlMod"));
    if ((mods & InputEvent.ALT_DOWN_MASK) != 0)
      arr.add(S.get("altMod"));
    if ((mods & InputEvent.SHIFT_DOWN_MASK) != 0)
      arr.add(S.get("shiftMod"));
    if ((mods & InputEvent.BUTTON1_DOWN_MASK) != 0)
      arr.add(S.get("button1Mod"));
    if ((mods & InputEvent.BUTTON2_DOWN_MASK) != 0)
      arr.add(S.get("button2Mod"));
    if ((mods & InputEvent.BUTTON3_DOWN_MASK) != 0)
      arr.add(S.get("button3Mod"));

    if (arr.isEmpty())
      return "";

    Iterator<String> it = arr.iterator();
    if (it.hasNext()) {
      StringBuilder ret = new StringBuilder();
      ret.append(it.next());
      while (it.hasNext()) {
        ret.append(" ");
        ret.append(it.next());
      }
      return ret.toString();
    } else {
      return "";
    }
  }

  public static String toKeyDisplayString(int mods) {
    ArrayList<String> arr = new ArrayList<String>();
    if ((mods & Event.META_MASK) != 0)
      arr.add(S.get("metaMod"));
    if ((mods & Event.CTRL_MASK) != 0)
      arr.add(S.get("ctrlMod"));
    if ((mods & Event.ALT_MASK) != 0)
      arr.add(S.get("altMod"));
    if ((mods & Event.SHIFT_MASK) != 0)
      arr.add(S.get("shiftMod"));

    Iterator<String> it = arr.iterator();
    if (it.hasNext()) {
      StringBuilder ret = new StringBuilder();
      ret.append(it.next());
      while (it.hasNext()) {
        ret.append(" ");
        ret.append(it.next());
      }
      return ret.toString();
    } else {
      return "";
    }
  }

  public static String toString(int mods) {
    ArrayList<String> arr = new ArrayList<String>();
    if ((mods & InputEvent.CTRL_DOWN_MASK) != 0)
      arr.add(CTRL);
    if ((mods & InputEvent.ALT_DOWN_MASK) != 0)
      arr.add(ALT);
    if ((mods & InputEvent.SHIFT_DOWN_MASK) != 0)
      arr.add(SHIFT);
    if ((mods & InputEvent.BUTTON1_DOWN_MASK) != 0)
      arr.add(BUTTON1);
    if ((mods & InputEvent.BUTTON2_DOWN_MASK) != 0)
      arr.add(BUTTON2);
    if ((mods & InputEvent.BUTTON3_DOWN_MASK) != 0)
      arr.add(BUTTON3);

    Iterator<String> it = arr.iterator();
    if (it.hasNext()) {
      StringBuilder ret = new StringBuilder();
      ret.append(it.next());
      while (it.hasNext()) {
        ret.append(" ");
        ret.append(it.next());
      }
      return ret.toString();
    } else {
      return "";
    }
  }

  public static String CTRL = "Ctrl";

  public static String SHIFT = "Shift";

  public static String ALT = "Alt";

  public static String BUTTON1 = "Button1";

  public static String BUTTON2 = "Button2";

  public static String BUTTON3 = "Button3";

  private InputEventUtil() {
  }
}
