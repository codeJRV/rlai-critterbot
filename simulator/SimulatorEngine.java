/**
  * SimulatorEngine
  *
  * Defines the core of the simulator engine. This class should call the
  *  simulator components in turn, keep track of existing objects and agents, 
  *  etc.
  *
  * @author Marc G. Bellemare
  */

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class SimulatorEngine
{
  protected SimulatorState aState, aNextState;
  protected LinkedList<SimulatorComponent> aComponents;

  private long last_time;
  
  protected SimulatorVizEvents vizHandler;

  public SimulatorEngine()
  {
    aState = new SimulatorState();
    aComponents = new LinkedList<SimulatorComponent>();

    vizHandler = new SimulatorVizEvents();

    // Construct the simulator state by adding objects to it
    Wall w = new Wall("Wall", 1);
    w.addPoint(20,20);
    w.addPoint(20,480);
    w.addPoint(480,480);
    w.addPoint(480,20);
    w.addPoint(20,20);

    // Make a polygon for the wall as well
    Polygon wallShape = new Polygon();
    // Interior
    wallShape.addPoint(20,20);
    wallShape.addPoint(20,480);
    wallShape.addPoint(480,480);
    wallShape.addPoint(480,20);
    wallShape.addPoint(20,20);
    // Exterior
    wallShape.addPoint(0,0);
    wallShape.addPoint(0,500);
    wallShape.addPoint(500,500);
    wallShape.addPoint(500,0);
    wallShape.addPoint(0,0);
    // Note that this polygon self-intersects at the duplicated edge
    //  (0,0)-(20,20)
    // This polygon is also evil because everything falls within its bounding
    //  box
    w.setShape(wallShape);

    aState.addObject(w);

    SimulatorAgent sa = new SimulatorAgent("Anna Banana", 2);

    Polygon agentShape = new Polygon();
    agentShape.addPoint (-10,10);
    agentShape.addPoint (10,10);
    agentShape.addPoint (10,-40);
    agentShape.addPoint (6,-40);
    agentShape.addPoint (6,-10);
    agentShape.addPoint (-10,-10);

    sa.setShape(agentShape);

    sa.setPosition(new Vector2D(250,250));
    sa.setMass(4);
    sa.setMoment(2);
    // Give the agent a 'physics' state
    sa.addState(new ObjectStateKinematics());
    // Give the agent an omnidirectional drive
    sa.addState(new ObjectStateOmnidrive());

    aState.addObject(sa);
    
    // Add an hexagonal obstacle
    SimulatorObject hex = new SimulatorObject("Hex", 3);

    // This obstacle can be moved around
    hex.addState (new ObjectStateKinematics());

    // Create the hex polygon
    Polygon hexShape = new Polygon();
    hexShape.addPoint(0,0);
    hexShape.addPoint(-8,-6);
    hexShape.addPoint(-8,-16);
    hexShape.addPoint(0,-22);
    hexShape.addPoint(8,-16);
    hexShape.addPoint(8,-6);
    hexShape.translate(new Vector2D(0, 11));

    /*hexShape.addPoint(0,0);
    hexShape.addPoint(40,0);
    hexShape.addPoint(40,40);
    hexShape.addPoint(0, 40);
    hexShape.addPoint(0, 35);
    hexShape.addPoint(35,35);
    hexShape.addPoint(35, 5);
    hexShape.addPoint(0, 5);*/


    hex.setShape(hexShape);

    // Important - set position after setting shape
    hex.setPosition(new Vector2D(100,100));
    hex.setMass(1);
    hex.setMoment(2);
   
    aState.addObject(hex);


    // Clone the current state; we will use it to do state-state transitions
    aNextState = (SimulatorState)aState.clone();
  }

  /** Returns a list of existing agents */
  public List<SimulatorAgent> getAgentList() { 
    return Collections.unmodifiableList(aState.getAgents()); 
  }

  /** Returns a list of existing objects */
  public List<SimulatorObject> getObjectList() { 
    return Collections.unmodifiableList(aState.getObjects()); 
  }

  /** Returns a list of objects influenced by the given component
    *
    * @param pComponent The identifier of the component of interest
    * @return A list of objects as per SimulatorState.getObjects(pComponent)
    */
  public List<SimulatorObject> getObjects(String pComponent)
  {
    return aState.getObjects(pComponent);
  }

  /**
    * Returns the current state of the simulator. 
    */
  public SimulatorState getState()
  {
    return aState;
  }

  public void addComponent(SimulatorComponent pComponent)
  {
    aComponents.add(pComponent);
  }

  /** Takes one 'step' in the simulation: update positions, velocities, etc
    */
  public void step()
  {
    // @@@ remove this!!! 
	  long time = System.currentTimeMillis();
	  if(last_time == 0) {
		  last_time = time;
		  return;
	  }
	  int ms = (int)(time - last_time); 
	  
	  /** Don't run too fast */
	  if(ms < 10)
		  return;
	  last_time = time;
	  
	  SimulatorAgent test = aState.getAgents().getFirst();
	  
	  double forceX, forceY, torque;
	 
    // If any of the visualizer keys are pressed, we override the omnidrive
    //  @@@ This needs to be moved somewhere else or ...
    if (vizHandler.up > 0 || vizHandler.right > 0 || vizHandler.left > 0)
    {
	    forceX = vizHandler.up * 8 * Math.sin(test.aDir);
	    forceY = vizHandler.up * 8 * Math.cos(test.aDir);
	    torque = (vizHandler.right * -4  + vizHandler.left * 4);

      // Modify the agent's omni drive data 
      ObjectStateOmnidrive driveData = 
        (ObjectStateOmnidrive)test.getState(SimulatorComponentOmnidrive.NAME);
      driveData.setVelocity (new Vector2D(forceX, forceY));
      driveData.setAngVelocity (torque);
    }

    ObjectStateKinematics phys =
      (ObjectStateKinematics)test.getState(SimulatorComponentKinematics.NAME);

    LinkedList<SimulatorObject> stuff = new LinkedList<SimulatorObject>();

    SimulatorObject hex = aState.getObject(3);
    
    stuff.add (test);
    stuff.add (hex);

    // Ha ha ha.
    for (SimulatorObject o : stuff)
    {
      if (o.getState(SimulatorComponentKinematics.NAME) == null) continue;

      ObjectStateKinematics oPhys = (ObjectStateKinematics)
        o.getState(SimulatorComponentKinematics.NAME);
      
      if (o.aPos.y >= 500)
        oPhys.addForce(new Force(0, -1000));
      else if (o.aPos.y < 0)
        oPhys.addForce(new Force(0, 1000));
      if (o.aPos.x >= 500)
        oPhys.addForce(new Force(-1000, 0));
      else if (o.aPos.x < 0)
        oPhys.addForce(new Force(1000, 0));
    }

    // Take this out, Anna
    Vector2D iPoint = hex.getShape().intersects(test.getShape());
    if (iPoint != null)
    {
      System.err.println("Bang ("+iPoint.x+","+iPoint.y+")");
      Vector2D fv = hex.getPosition().minus(iPoint);
      System.err.println ("\t fv is "+fv.x+","+fv.y);
      fv.x = fv.x * 5;
      fv.y = fv.y * 5; 

      ObjectStateKinematics hexPhys =
        (ObjectStateKinematics)hex.getState(SimulatorComponentKinematics.NAME);
      hexPhys.addForce(new Force(fv,test.getPosition()));
    }

    /** Begin new (real) simulator code - everything above has to be moved
      *  (more or less) */
    // Apply each component in turn (order matters!)
    // @@@ oops Marc, you need to fix this
    aNextState = (SimulatorState)aState.clone();

    for (SimulatorComponent comp : aComponents)
    {
      comp.apply(aState, aNextState, ms);
    }

    SimulatorState tmpState = aState;

    // Replace the current state by the new state
    aState = aNextState;
    // Be clever and reuse what was the current state as our next 'new state'
    aNextState = tmpState;
  }
}
