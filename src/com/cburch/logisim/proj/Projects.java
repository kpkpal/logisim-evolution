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
package com.cburch.logisim.proj;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.WeakHashMap;

import com.cburch.logisim.Main;
import com.cburch.logisim.file.Loader;
import com.cburch.logisim.gui.main.Frame;
import com.cburch.logisim.util.PropertyChangeWeakSupport;

public class Projects {

  private static class MyListener extends WindowAdapter {

    @Override
    public void windowActivated(WindowEvent event) {
      mostRecentFrame = (Frame) event.getSource();
      moveToFront(mostRecentFrame);
    }

    @Override
    public void windowDeiconified(WindowEvent event) {
      mostRecentFrame = (Frame) event.getSource();
      moveToFront(mostRecentFrame);
    }

    @Override
    public void windowIconified(WindowEvent event) {
      moveToBack((Frame)event.getSource());
    }

    private int findProject(Frame frame) {
      for (int i = 0; i < openProjects.size(); i++) {
        Project proj = openProjects.get(i);
        if (proj.getFrame() == frame)
          return i;
      }
      return -1;
    }

    private void moveToFront(Frame frame) {
      int i = findProject(frame);
      if (i < 0)
        return;
      Project proj = openProjects.remove(i);
      openProjects.add(0, proj);
    }

    private void moveToBack(Frame frame) {
      int i = findProject(frame);
      if (i < 0)
        return;
      Project proj = openProjects.remove(i);
      openProjects.add(proj);
    }

    @Override
    public void windowClosed(WindowEvent event) {
      Frame frame = (Frame) event.getSource();
      Project proj = frame.getProject();

      if (frame == proj.getFrame()) {
        projectRemoved(proj, frame, this);
      }
      if (openProjects.isEmpty() && !Main.HasWindowlessMenubar) {
        ProjectActions.doQuit();
      }
    }

    @Override
    public void windowClosing(WindowEvent event) {
      Frame frame = (Frame) event.getSource();
      frameClosing(frame);
    }

    @Override
    public void windowOpened(WindowEvent event) {
      Frame frame = (Frame) event.getSource();
      Project proj = frame.getProject();

      if (frame == proj.getFrame() && !openProjects.contains(proj)) {
        openProjects.add(proj);
        propertyChangeProducer.firePropertyChange(projectListProperty, null, null);
      }
    }
  }

  public static Project findProjectFor(File query) {
    for (Project proj : openProjects) {
      Loader loader = proj.getLogisimFile().getLoader();
      if (loader == null) {
        continue;
      }
      File f = loader.getMainFile();
      if (query.equals(f)) {
        return proj;
      }
    }
    return null;
  }

  public static Point getCenteredLoc(int width, int height) {
    int x, y;
    x = getTopFrame().getX() + getTopFrame().getWidth() / 2;
    x -= width / 2;
    y = getTopFrame().getY() + getTopFrame().getHeight() / 2;
    y -= height / 2;
    return new Point(x, y);
  }

  public static Point getLocation(Window win) {
    Point ret = frameLocations.get(win);
    return ret == null ? null : (Point) ret.clone();
  }

  public static List<Project> getOpenProjects() {
    return Collections.unmodifiableList(openProjects);
  }

  public static Frame getTopFrame() {
    Frame ret = mostRecentFrame;
    if (ret == null) {
      Frame backup = null;
      for (Project proj : openProjects) {
        Frame frame = proj.getFrame();
        if (ret == null) {
          ret = frame;
        }
        if (ret.isVisible()
            && (ret.getExtendedState() & Frame.ICONIFIED) != 0) {
          backup = ret;
        }
      }
      if (ret == null) {
        ret = backup;
      }
    }
    return ret;
  }

  private static void projectRemoved(Project proj, Frame frame, MyListener listener) {
    frame.removeWindowListener(listener);
    openProjects.remove(proj);
    proj.getSimulator().shutDown();
    propertyChangeProducer.firePropertyChange(projectListProperty, null, null);
  }

  static void windowCreated(Project proj, Frame oldFrame, Frame frame) {
    if (oldFrame != null) {
      projectRemoved(proj, oldFrame, myListener);
    }

    if (frame == null) {
      return;
    }

    // locate the window
    Point lowest = null;
    for (Project p : openProjects) {
      Frame f = p.getFrame();
      if (f == null) {
        continue;
      }
      Point loc = p.getFrame().getLocation();
      if (lowest == null || loc.y > lowest.y) {
        lowest = loc;
      }
    }
    if (lowest != null) {
      Dimension sz = frame.getToolkit().getScreenSize();
      int x = Math.min(lowest.x + 20, sz.width - 200);
      int y = Math.min(lowest.y + 20, sz.height - 200);
      if (x < 0) {
        x = 0;
      }
      if (y < 0) {
        y = 0;
      }
      frame.setLocation(x, y);
    }

    if (frame.isVisible() && !openProjects.contains(proj)) {
      openProjects.add(proj);
      propertyChangeProducer.firePropertyChange(projectListProperty, null, null);
    }
    frame.addWindowListener(myListener);
  }

  public static boolean windowNamed(String name) {
    for (Project proj : openProjects) {
      if (proj.getLogisimFile().getName().equals(name)) {
        return true;
      }
    }
    return false;
  }

  public static final String projectListProperty = "projectList";

  private static final WeakHashMap<Window, Point> frameLocations = new WeakHashMap<Window, Point>();

  private static final MyListener myListener = new MyListener();

  // openProjects is maintained in order of most recent activation order:
  //  - activate brings to front
  //  - minimize sends to back
  private static ArrayList<Project> openProjects = new ArrayList<Project>();

  private static Frame mostRecentFrame = null;

  private Projects() {
  }

  public static void frameClosing(Frame frame) {
    if ((frame.getExtendedState() & Frame.ICONIFIED) == 0) {
      mostRecentFrame = frame;
      try {
        Point pt = frame.getLocationOnScreen();
        if (pt != null)
          frameLocations.put(frame, pt);
      } catch (Exception t) {
      }
    }
  }

  public static final PropertyChangeWeakSupport.Producer propertyChangeProducer =
      new PropertyChangeWeakSupport.Producer() {
        PropertyChangeWeakSupport propListeners = new PropertyChangeWeakSupport(Projects.class);
        public PropertyChangeWeakSupport getPropertyChangeListeners() { return propListeners; }
      };
}
