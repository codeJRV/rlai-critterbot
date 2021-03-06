/* Copyright 2009 Marc G. Bellemare
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.rlcommunity.critterbot.javadrops.drops;

/**
  * CritterControlDrop
  *
  * This class encapsulates data received from DisCo about commands sent
  *  to the robot. Somewhat a clone of CritterControlDrop.{h,cpp}.
  */

import java.io.IOException;
import java.util.EnumSet;

import org.rlcommunity.critterbot.javadrops.InterfaceInputStream;
import org.rlcommunity.critterbot.javadrops.InterfaceOutputStream;

public class CritterControlDrop implements SimulatorDrop
{
  public enum MotorMode { WHEEL_SPACE, XYTHETA_SPACE, WHEEL_VOLTAGE };
  public enum LedMode { NONE, CLEAR, BATTERY, BALL, ERROR, EMERGENCY, BUSY, CUSTOM };

  /** The motor mode determining which fields are actually used */
  public MotorMode motor_mode;
  /** The LED mode (currently unused) */
  public LedMode   led_mode;

  /** Velocities used in XYTHETA_SPACE */
  public int x_vel, y_vel, theta_vel;
  /** Velocities used in WHEEL_SPACE */
  public int m100_vel, m220_vel, m340_vel;

  /** LED values in CUSTOM mode */
  public class led_struct
  {
    public int r,g,b;

    public void readData(InterfaceInputStream pIn) throws IOException
    {
      r = pIn.readByte();
      r = (r < 0 ? (r + 256) : r);
      g = pIn.readByte();
      g = (g < 0 ? (g + 256) : g);
      b = pIn.readByte();
      b = (b < 0 ? (b + 256) : b);
    }

    public void writeData(InterfaceOutputStream pOut) throws IOException
    {
      pOut.writeByte((byte)r);
      pOut.writeByte((byte)g);
      pOut.writeByte((byte)b);
    }

    public int getSize()
    {
      return 3 * Byte.SIZE;
    }
  }
  public led_struct[] LEDValues;

  public static final int LED_VALUES_SIZE = 16;


  /** Returns the size of the data contained in this drop.
    *  This must correspond to the amount of data written and read by
    *  writeData and readData, respectively. 
    * @return The number of bytes contained in this drop.
    */
  public int getSize()
  {
    return (Integer.SIZE + // motor_mode
           Integer.SIZE + // led_mode
           3 * Integer.SIZE + // x_vel + y_vel + theta_vel OR m_vel's
           LEDValues[0].getSize() * LED_VALUES_SIZE) // LEDValues
           / 8; // Divide by 8 because we're adding up bits
  }

  /** Creates a new CritterControlDrop and zeros LEDValues
   *   for posterity.
   */
  public CritterControlDrop() {
      LEDValues = new led_struct[LED_VALUES_SIZE];
      for(int i = 0; i < LED_VALUES_SIZE; i++) {
          LEDValues[i] = new led_struct();
      }
  }

  /** Write control drop data out to a stream. The order in which things 
    *  are sent must be:
    *
    *  motor_mode, led_mode (as ints)
    *  x_vel, y_vel, theta_vel OR
    *   m100_vel, m220_vel, m340_vel
    *
    * @param pOut The output stream to which the data should be written
    */
  public void writeData(InterfaceOutputStream pOut) throws IOException
  {
    // Write motor mode
    pOut.writeInt(motor_mode.ordinal());

    // Write 3 velocities
    switch (motor_mode)
    {
      case WHEEL_SPACE:
        pOut.writeInt(m100_vel);
        pOut.writeInt(m220_vel);
        pOut.writeInt(m340_vel);
        break;
      case XYTHETA_SPACE:
        pOut.writeInt(x_vel);
        pOut.writeInt(y_vel);
        pOut.writeInt(theta_vel);
        break;
      case WHEEL_VOLTAGE:
        pOut.writeInt(m100_vel);
        pOut.writeInt(m220_vel);
        pOut.writeInt(m340_vel);
        break;
      default:
        throw new IOException("Unknown motor mode.");
    }

    // Write LED mode
    pOut.writeInt(led_mode.ordinal());

    for(int i = 0; i < LED_VALUES_SIZE; i++)
        LEDValues[i].writeData(pOut);
  }
 
  /** Reverse of writeData; reads the drop from a DataInputStream
    *
    * @param pIn The input stream from which we read the data
    */
  public void readData(InterfaceInputStream pIn,int pDropSize) throws IOException
  {
    assert (pDropSize == getSize());

    // Read motor mode and convert it to an enum
    // Unfortunately, Java hates integer enums, so we have to circumvent this
    motor_mode = (MotorMode)EnumSet.range(MotorMode.WHEEL_SPACE, 
      MotorMode.WHEEL_VOLTAGE).toArray()[pIn.readInt()];

    // Read in the three velocities
    switch (motor_mode)
    {
      case WHEEL_SPACE:
        m100_vel = pIn.readInt();
        m220_vel = pIn.readInt();
        m340_vel = pIn.readInt();
        break;
      case XYTHETA_SPACE:
        x_vel = pIn.readInt();
        y_vel = pIn.readInt();
        theta_vel = pIn.readInt();
        break;
        case WHEEL_VOLTAGE:
        m100_vel = pIn.readInt();
        m220_vel = pIn.readInt();
        m340_vel = pIn.readInt();
        break;

      default:
        throw new IOException("Unknown motor mode.");
    }
   
    // Read LED mode
    led_mode = (LedMode)EnumSet.range(LedMode.NONE,
      LedMode.CUSTOM).toArray()[pIn.readInt()];

    for(int i = 0; i < LED_VALUES_SIZE; i++)
        LEDValues[i].readData(pIn);

  }

  @Override
  public String toString()
  {
    return "Motor mode: "+motor_mode+
      " XYT ("+x_vel+","+y_vel+","+theta_vel+")"+
      " WHE: ("+m100_vel+","+m220_vel+","+m340_vel+")"+
      " Led mode: "+led_mode;
  }
}
