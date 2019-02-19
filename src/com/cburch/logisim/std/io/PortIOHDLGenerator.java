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
package com.cburch.logisim.std.io;

import com.bfh.logisim.hdlgenerator.HDLGenerator;
import com.bfh.logisim.hdlgenerator.HiddenPort;

public class PortIOHDLGenerator extends HDLGenerator {

  // todo: verilog support

  public PortIOHDLGenerator(HDLCTX ctx) {
    super(ctx, "PORTIO_${CIRCUIT}_${LABEL}", "PORTIO");
    
    for (InOutMap io : getPorts(_attrs)) {
      if (io.type == ALWAYSINPUT || io.type == ENABLE)
        inPorts.add(io.name, io.size, io.endNr, false);
      else if (io.type == TRISTATEINPUT_1 || io.type == TRISTATEINPUT_N)
        inPorts.add(io.name, io.size, io.endNr, null);
      else if (io.type == OUTPUT)
        outPorts.add(io.name, io.size, io.endNr, null);
      //else if (io.type == BUS)
      //  inOutPorts.add(io.name, io.size, io.endNr, null);
    }

    int n = attrs.getValue(PortIO.ATTR_SIZE);

    hiddenPort = HiddenPort.makeInOutport(n, 
        HiddenPort.PortIO /*, HiddenPort.Button, HiddenPort.Pin*/);
  }

  private class InOutMap {
    private int end, start, size, busNr, endNr;
    private int type;
    private String name;

    public InOutMap(int type, int end, int start, int busNr, int endNr) {
      this.type = type;
      this.end = end;
      this.start = start;
      this.size = (end - start) + 1;
      this.busNr = busNr;
      this.endNr = endNr;
      switch (type) {
      case ALWAYSINPUT:
      case TRISTATEINPUT_1:
      case TRISTATEINPUT_N:
        name = inBusName + busNr;
        break;
      case ENABLE:
        name = enBusName + busNr;
        break;
      case OUTPUT:
        name = outBusName + busNr;
        break;
      // case BUS:
      //   name = inOutBusName + busNr;
      //   break;
      }
    }
  }

  private static final int ENABLE = 0;
  private static final int TRISTATEINPUT_1 = 1;
  private static final int TRISTATEINPUT_N = 2;
  private static final int ALWAYSINPUT = 3;
  private static final int OUTPUT = 4;
  // private static final int BUS = 5;

  private static final String inBusName = "PIO_IN_BUS_";
  private static final String enBusName = "PIO_EN_BUS_";
  private static final String outBusName = "PIO_OUT_BUS_";
  private static final String inOutBusName = "PIO_INOUT_BUS_";

  private static ArrayList<InOutMap> getPorts(AttributeSet attrs) {
    // Note: PortIO.INPUT yields *output* from this entity (from off-chip
    // bus pins to circuit), while PortIO.OUTPUT yiels *input* to this
    // entity (from circuit to off-chip bus pins). We swap the direction
    // tags here to avoid having to swap them everywhere else in this file.
    String dir = attrs.getValue(PortIO.ATTR_DIR);
    int size = attrs.getValue(PortIO.ATTR_SIZE);
    ArrayList<InOutMap> ports = new ArrayList<>();
    int endNr = 0;
    if (dir == PortIO.INOUT_1)
      ports.add(new InOutMap(ENABLE, 0, 0, 0, endNr++));
    int n = size;
    for (int busNr = 0; busNr < (size - 1)/32 + 1; busNr++) {
      int e = (n > 32 ? 32 : n);
      // ports.add(new InOutMap(BUS, e - 1, 0, busNr, -1));
      if (dir == PortIO.INOUT_N) {
        ports.add(new InOutMap(ENABLE, e - 1, 0, busNr, endNr++));
        ports.add(new InOutMap(TRISTATEINPUT_N, e - 1, 0, busNr, endNr++));
      } else if (dir == PortIO.INOUT_1) {
        ports.add(new InOutMap(TRISTATEINPUT_1, e - 1, 0, busNr, endNr++));
      } else if (dir == PortIO.OUTPUT) {
        ports.add(new InOutMap(ALWAYSINPUT, e - 1, 0, busNr, endNr++));
      }
      n -= e;
    }
    n = size;
    for (int busNr = 0; busNr < (size - 1)/32 + 1; busNr++) {
      int e = (n > 32 ? 32 : n);
      if (dir == PortIO.INPUT || dir == PortIO.INOUT_1 || dir == PortIO.INOUT_N) {
        ports.add(new InOutMap(OUTPUT, e - 1, 0, busNr, endNr++));
      }
      n -= e;
    }
    return ports;
  }

  @Override
  protected void generateBehavior(Hdl out) {
    if (out.isVhdl) {
      for (InOutMap io : getPorts(_attrs)) {
        String fpgaBus = inOutBusName + io.busNr;
        String fpgaBusSlice;
        if (io.size == 1)
          fpgaBusSlice = String.format("%s(%d)", fpgaBus, io.end);
        else
          fpgaBusSlice = String.format("%s(%d downto %d)", fpgaBus, io.end, io.start);
        switch (io.type) {
        case OUTPUT:
          out.assign(io.name, fpgaBusSlice);
          break;
        case ALWAYSINPUT:
          out.assign(fpgaBusSlice, io.name);
          break;
        case TRISTATEINPUT_1:
          for (int i = io.end; i >= io.start; i--) {
            String bus = String.format("%s(%d)", fpgaBus, i);
            String enable = enBusName + 0;
            String input = String.format("%s(%d)", io.name, i);
            out.stmt("%s <= %s when %s = '1' else 'Z';", bus, input, enable);
          }
          break;
        case TRISTATEINPUT_N:
          for (int i = io.end; i >= io.start; i--) {
            String bus = String.format("%s(%d)", fpgaBus, i);
            String enable = String.format("%s%d(%d)", enBusName, io.busNr, i);
            String input = String.format("%s(%d)", io.name, i);
            out.stmt("%s <= %s when %s = '1' else 'Z';", bus, input, enable);
          }
          break;
        // case BUS:
        case ENABLE:
          // nothing
          break;
        }
      }
    }
  }

}
