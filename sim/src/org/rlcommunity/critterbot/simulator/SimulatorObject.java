/* Copyright 2009 Marc G. Bellemare, Michael Sokolsky and Anna Koop
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

package org.rlcommunity.critterbot.simulator;

/**
 * SimulatorObject
 *
 * Defines the basic properties and methods relevant to any simulator object,
 *  be it a wall, a tennis ball or a critterbot.
 *
 * @author Marc G. Bellemare
 * @author Mike Sokolsky
 * @author Anna Koop
 */

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.rlcommunity.critterbot.simulator.svg.ShapeDrawing;

public class SimulatorObject {
    /** Flag determining whether we should draw via SVG. Currently set by the
     *   SimulatorEngine, which is a really bad thing to do.
     */
    static protected boolean svgDrawing = false;
    
	/** Some properties of the object - position, velocity */
	protected Vector2D aPos;
	protected double aDir;

	/**
	 * A polygon describing the shape of the object; may be null if the object
	 * is an invisible source
	 */
	protected Polygon aShape;

	/** List of state components for this object */
	// protected LinkedList<ObjectState> aStates;
	protected Map<String, ObjectState> aStates = new TreeMap<String, ObjectState>();

	protected String aLabel;

	/**
	 * A unique identifier which is used to keep track of an object from state
	 * to state
	 */
	protected int aId;

	/** Parent of this object, in the object hierarchy; may be null */
	protected SimulatorObject aParent;
	/** Children of this object, in the object hierarchy; list may be empty */
	protected LinkedList<SimulatorObject> aChildren = new LinkedList<SimulatorObject>();

	private ShapeDrawing svgShapeDrawing = null;

	/**
	 * Creates a new object with a given label (e.g., "wall") and identifier,
	 * with no parent.
	 * 
	 * @param pLabel
	 *            The object label
	 * @param pId
	 *            The object's unique identifier
	 */
	public SimulatorObject(String pLabel, int pId) {
		aLabel = new String(pLabel);
		aId = pId;
		aPos = new Vector2D(0, 0);
		aDir = 0;

		aParent = null;
	}

	/**
	 * Adds an object as a child of this object, in the object hierarchy
	 * 
	 * @param pChild
	 *            The child to be added
	 * @throws IllegalArgumentException
	 *             If
	 */
	public void addChild(SimulatorObject pChild) {
		if (pChild == null)
			throw new IllegalArgumentException("Illegal parameter: null");
		else if (aChildren.contains(pChild))
			throw new IllegalArgumentException("Duplicate child");
		else if (pChild.getParent() != null)
			throw new IllegalArgumentException("Child to be added has a parent");

		aChildren.add(pChild);
		pChild.setParent(this);
	}

	/**
	 * Removes an object from this object's children.
	 * 
	 * @param pChild
	 *            The object to be removed
	 * @return Whether the child was contained in the children list
	 */
	public boolean removeChild(SimulatorObject pChild) {
		if (pChild == null)
			throw new IllegalArgumentException("Illegal parameter: null");

		// LinkedList.remove() tells us whether the list contains the removed
		// element
		boolean wasChild = aChildren.remove(pChild);
		if (wasChild)
			pChild.removeParent();

		return wasChild;
	}

	/**
	 * Returns a list of all nodes in this object's tree, in postorder traversal
	 * 
	 * @return List<SimulatorObject> A list of all objects in this object's tree
	 */
	public List<SimulatorObject> getChildren() {
		LinkedList<SimulatorObject> children = new LinkedList<SimulatorObject>();

		// Add each child's children
		for (SimulatorObject c : aChildren) {
			List<SimulatorObject> itsChildren = c.getChildren();
			// Add all of them
			children.addAll(itsChildren);
		}

		// To preserve postorder traversal, add the children after
		children.addAll(aChildren);

		return children;
	}

	/**
	 * Returns a list of all nodes in this object's tree, in postorder
	 * traversal, containing a particular ObjectState as given by its label.
	 * 
	 * @param pLabel
	 *            The label of the ObjectState of interest
	 * @return List<SimulatorObject> A list of objects from this object's tree
	 */
	public List<SimulatorObject> getChildren(String pLabel) {
		LinkedList<SimulatorObject> children = new LinkedList<SimulatorObject>();

		// Add each child's children
		for (SimulatorObject c : aChildren) {
			List<SimulatorObject> itsChildren = c.getChildren(pLabel);
			// Add all of them
			children.addAll(itsChildren);
		}

		// Add only the children that containing the property of interest
		// Preserve the postorder traversal! Do not merge these two 'for' loops
		for (SimulatorObject c : aChildren) {
			if (c.getState(pLabel) != null)
				children.add(c);
		}

		return children;
	}

	/**
	 * Returns collision information, as a Collision class, if this object is
	 * colliding with the given object. The returned Collision will be null if
	 * no collision actually happened. The meaning of the Collision information
	 * is somewhat undefined in the case where the two objects' polygons
	 * intersect at more than two points.
	 * 
	 * @param compObj
	 *            The object to be compared with
	 * @return Null if there is no collision between the two objects, or a
	 *         Collision object containing collision information otherwise.
	 */
	public Collision collidesWith(SimulatorObject compObj) {
    // Create two lists to compare
    List<SimulatorObject> myObjects = getChildren();
    List<SimulatorObject> compObjects = compObj.getChildren();
    
    myObjects.add(this);
    compObjects.add(compObj);

    // This is not recursive because we have two loops; recomputing the inner
    //   loop's list of objects everytime would be extra work.
    // Pairwise comparisons
    for (SimulatorObject leftObject : myObjects) {
      Polygon leftShape = leftObject.getShape();

      if (leftShape == null) continue;
      
      for (SimulatorObject rightObject : compObjects) {
        Polygon rightShape = rightObject.getShape();
        // @todo It might be worthwhile, here, to remove the elements from
        //  compObjects which have no attached Polygon
        if (rightShape == null) continue;

        // Get the first two intersections
        List<Polygon.Intersection> isects = leftShape.getIntersections(rightShape, 2);

        // No intersection, no collision
        if (isects.size() == 0) {
          continue;
        }
        else if (isects.size() == 1) {
          // @todo the case when we have one intersection is incorrect, as in
          // this case it's not clear what the normal is
          System.err.println("WARNING: Single-point intersection.");
          Collision col = new Collision();
          Polygon.Intersection i1 = isects.get(0);
          double alpha = i1.alpha;
          col.point = leftShape.getPoint(alpha);
          col.normal = leftShape.getNormal(alpha);
          col.alpha = i1.alpha;
          col.beta = i1.beta;

          return col;
        }
        else {
          Collision col = new Collision();
          // Look at the two intersections that we have
          Polygon.Intersection i1 = isects.get(0);
          Polygon.Intersection i2 = isects.get(1);

          // Ensure that the the second intersection comes after the first on the
          //   polygon, so that the normal to the alpha-related polygon is pointing
          //   outwards
          if (i1.alpha > i2.alpha) {
            Polygon.Intersection tmp = i1;
            i1 = i2;
            i2 = tmp;
          }

          Vector2D p1 = leftShape.getPoint(i1.alpha);
          Vector2D p2 = leftShape.getPoint(i2.alpha);

          // Keep the polygon coordinates of the collision point
          // This is an interpolation between the two points. Could be
          // very wrong, e.g. pacman-style shapes
			/* @todo For now, we return a single intersection point - this is
           *  because the interpolation of the two points might not be a good point
           *  of contact at all
           *
           * IF doing interpolation, remember how to interpolate at the boundaries
           *  (i.e. alpha = n-epsilon and alpha2 = epsilon interpolate to 0,
           *  not n/2
           */
          col.alpha = i1.alpha;
          col.beta = i1.beta;

          // this results in alpha = (n-1)/2 (when it should be n - 1/2)
          col.point = leftShape.getPoint(col.alpha);

          // The normal is the normal to p2-p1 (note: the normal itself is good,
          //   even if the point of contact would not be)
          col.normal = p2.minus(p1).rotate(Math.PI / 2);
          col.normal.normalize();

          return col;
        }
      }
    }

    return null;
	}

	/**
	 * Returns true of this object's shape intersects the parameter object's.
	 * This method is obsolete and should be removed at the earliest possible
	 * time.
	 * 
	 * @return True if the passed object intersects this object
	 * @deprecated - This method does not take the object hierarchy into account
	 **/
	public Vector2D intersects(SimulatorObject compObj) {
		if (aShape == null || compObj.getShape() == null)
			return null;
		else {
			Polygon.Intersection isect = aShape.intersects(compObj.getShape());
			if (isect == null)
				return null;
			else
				return aShape.getPoint(isect.alpha);
		}
	}

	public boolean corresponds(SimulatorObject o) {

		if (this.aId == o.aId)
			return true;
		else
			return false;
	}

	/**
	 * Sets the shape of the object (which is shapeless by default).
	 * 
	 * @param pShape
	 *            The polygon representing the shape. It is not copied over.
	 */
	public void setShape(Polygon pShape) {
		aShape = pShape;
	}

	public Polygon getShape() {
		return aShape;
	}

	/**
	 * Returns the partial state corresponding to a particular component,
	 * identified by pLabel.
	 * 
	 * @param pLabel
	 *            The identifier used by the SimulatorComponent and the
	 *            corresponding ObjectState.
	 * @return The corresponding ObjectState or null if this object does not
	 *         store this state information.
	 * 
	 */

	public ObjectState getState(String pLabel) {
		// We want to return null if the ObjectState does not exist
		return aStates.get(pLabel);
	}

    /** Removes the corresponding partial state from this object. Returns null
     *   if the object did not have the requested ObjectState, and the removed
     *   ObjectState otherwise.
     * 
     * @param pLabel The identifier used by the SimulatorComponent and the 
     *      corresponding ObjectState.
	 * @return The removed ObjectState or null if this object does not
	 *         store this state information.
     */
    public ObjectState removeState(String pLabel) {
        return aStates.remove(pLabel);
    }

	/**
	 * Add a new state component to the object; duplicates are not allowed!
	 * 
	 * @param pState
	 *            The new state component
	 */
	public void addState(ObjectState pState) {
		// @todo Automatically replaces duplicates.
		// @todo MGB - we probably don't want an assert here, but it is fine for
		// now; whether we should crash on duplicates is open to discussion
		assert (!aStates.containsKey(pState.getName()));
		aStates.put(pState.getName(), pState);
	}

	/**
	 * @return Returns the parent of this object, or null if it has no parent
	 */
	public SimulatorObject getParent() {
		return aParent;
	}

	/**
	 * Returns the root of the object tree for this object, defined to be the
	 * ancestor of this object which has no parent, or the object itself if it
	 * has no parent.
	 * 
	 * @return The root of the object tree to which this object belongs
	 */
	public SimulatorObject getRoot() {
		SimulatorObject root = this;

		for (SimulatorObject parent = root.getParent(); parent != null; root = parent, parent = root.getParent())
			;

		return root;
	}

	/**
	 * Set the parent of this object (as per the object hierarchy) aParent
	 * should be null.
	 * 
	 * @param pParent
	 *            The parent-to-be of the object
	 * @throws IllegalArgumentException
	 *             If pParent is null or aParent is not null
	 */
	public void setParent(SimulatorObject pParent) {
		if (aParent != null || pParent == null)
			throw new IllegalArgumentException(aLabel + " already has a parent");

		aParent = pParent;
	}

	/**
	 * Remove the parent of this object
	 * 
	 * @throws IllegalArgumentException
	 *             If this object has no paret
	 */
	public void removeParent() {
		if (aParent == null)
			throw new IllegalArgumentException(aLabel + " has no parent");
		aParent = null;
	}

	private void loadObject() {
		// Open files, lookup plabel, etc???
	}

	/**
	 * Draw the object on the provided canvas
	 * 
	 * @param g
	 *            The Graphics object used to draw
	 */
	public void draw(SimulatorGraphics g) {
        if (svgDrawing && (svgShapeDrawing != null))
			svgShapeDrawing.draw(g, getPosition(), getDirection());
		if (aShape != null)
			aShape.draw(g);

		// Draw the children in postorder traversal
		for (SimulatorObject c : aChildren)
			c.draw(g);

		// Draw each ObjectState in turn
		Set<Entry<String, ObjectState>> theEntrySet = aStates.entrySet();

		for (Entry<String, ObjectState> thisEntry : theEntrySet) {
			thisEntry.getValue().draw(g, this);
		}
	}

	/**
	 * Return the unique identifier of this object. The identifier is the same
	 * across time steps.
	 * 
	 * @return An integer uniquely identifying this object
	 */
	public int getId() {
		return aId;
	}

	public String getLabel() {
		return aLabel;
	}

	/**
	 * Sets this object's position, in global coordinates. When constructing
	 * object trees, setLocalPosition should be used instead.
	 * 
	 * @param newPos
	 *            The new global position of the object
	 */
	public void setPosition(Vector2D newPos) {
		Vector2D newLocalPos;

		// Find the local position of this object
		if (aParent == null)
			newLocalPos = newPos;
		else {
			// @todo replace this by a proper global-to-local conversion
			throw new UnsupportedOperationException("Not allowed to change the global position of a child object");
		}

		// If we have a shape, also translate it by the difference
		updateShape(newLocalPos.minus(aPos), 0, Vector2D.ZERO);

		aPos = newLocalPos;
	}

	/**
	 * Sets the global direction of the object
	 * 
	 * @param newDir The new direction
	 */
	public void setDirection(double newDir) {
        // First bound the direction
        newDir = boundDirection(newDir);
        
		double newLocalDir;

		if (aParent == null)
			newLocalDir = newDir;
		else {
			throw new UnsupportedOperationException("Not allowed to change the global direction of a child object");
		}

		// If we have a shape, also rotate it
		if (aShape != null)
			updateShape(Vector2D.ZERO, newLocalDir - aDir, getPosition());

		aDir = newLocalDir;
	}

    /**
     * Bounds a given direction so that it falls within a single period (between
     *   -PI and PI).
     *
     * @param pDir The original direction.
     * @return     The bounded direction (an equivalent angle between -PI and PI).
     */
    public double boundDirection(double pDir) {
        // Very naively loop until the direction falls within the range
        while (pDir >= Math.PI) {
            pDir -= Math.PI * 2;
        }
        while (pDir < -Math.PI) {
            pDir += Math.PI * 2;
        }

        return pDir;
    }

	/**
	 * Returns this object's position (in global coordinates)
	 * 
	 * @return The object's position
	 */
	public Vector2D getPosition() {
		if (aParent == null)
			return aPos;
		else {
			Vector2D parentPos = aParent.getPosition();
			double parentDir = aParent.getDirection();

			// Compute our position based on our parent's
			Vector2D pos = aPos.rotate(parentDir).plus(parentPos);

			return pos;
		}
	}

	/**
	 * Returns this object's direction, in the global frame of reference
	 * 
	 * @return The object's direction
	 */
	public double getDirection() {
		if (aParent == null)
			return aDir;
		else
			return aDir + aParent.getDirection();
	}

	/**
	 * Sets the object's local position (with respect to its parent's frame of
	 * reference)
	 * 
	 * @param pPos
	 *            The new local position of the object
	 */
	public void setLocalPosition(Vector2D pPos) {
		// In this case we update this object polygon's and its children
		updateShape(pPos.minus(aPos), 0, Vector2D.ZERO);
		aPos = pPos;
	}

	/**
	 * Returns the object's local position (with respect to its parent's frame
	 * of reference)
	 * 
	 * @return The object's local position
	 */
	public Vector2D getLocalPosition() {
		return aPos;
	}

	/**
	 * Sets the object's direction in its parent's frame of reference
	 * 
	 * @param pDir
	 *            The object's local direction
	 */
	public void setLocalDirection(double pDir) {
		updateShape(Vector2D.ZERO, pDir - aDir, getPosition());
		aDir = pDir;
	}

	/**
	 * Returns the object's direction in its parent's frame of reference
	 * 
	 * @return The object's local direction
	 */
	public double getLocalDirection() {
		return aDir;
	}

	/**
	 * Updates the shape with a translation (possibly 0) and a rotation (also
	 * 0), performed in this order. The center of rotation is provided, as it
	 * should remain fixed as the object tree is traversed. Also updates the
	 * Polygon of this object's children.
	 * 
	 * @param pTranslation
	 *            The translation to perform on the object
	 * @param pRotation
	 *            The rotation to perform on the object
	 * @param pRotCenter
	 *            The center of rotation, if needed
	 */
	public void updateShape(Vector2D pTranslation, double pRotation,
			Vector2D pRotCenter) {
	 /* @todo MGB: This is not quite clean - I don't like having to pass a center
	  *     of rotation around. There should be a better way.
	  */
		if (aShape != null) {
			aShape.translate(pTranslation);
			aShape.rotate(pRotation, pRotCenter);
		}

		for (SimulatorObject c : aChildren) {
			c.updateShape(pTranslation, pRotation, pRotCenter);
		}
	}

	/**
	 * Makes a copy of this object and modifies the label and ID of the new
	 * object. This should not be used for generating copies of the state, but
	 * rather for generating copies of the same object within a given state.
	 * 
	 * This method will fail if this object has any children, which would be
	 * cloned (but not re-id'ed) by this method.
	 */
	public SimulatorObject makeCopy(String pLabel, int pId) {
		if (!aChildren.isEmpty())
			throw new UnsupportedOperationException("Cannot make copy of an object with children");

		SimulatorObject newObj = (SimulatorObject) this.clone();
		newObj.aLabel = pLabel;
		newObj.aId = pId;

		return newObj;
	}

	/**
	 * Makes a copy of this object. For cloning purposes, the Object id remains
	 * identical. This method's purpose is to be used for copying objects
	 * between states.
	 */
	public Object clone() {
		SimulatorObject newObj = new SimulatorObject(this.aLabel, this.aId);

		newObj.copyFrom(this);

		// Clone the children
		for (SimulatorObject c : aChildren) {
			SimulatorObject childClone = (SimulatorObject) c.clone();
			newObj.addChild(childClone);
		}

		return newObj;
	}

	@Override
	public String toString() {
		return aLabel;
	}

	/**
	 * Compares the position and direction of the current object to the
	 * argument's position and direction
	 * 
	 * @param compObj SimulatorObject
	 **/
	public boolean geometryEquals(SimulatorObject compObj) {
		return (getPosition().equals(compObj.getPosition()) &&
            getDirection() == compObj.getDirection());
	}

	/**
	 * Sets the position and direction of the current object to the argument's
	 * position and direction
	 * 
	 * @param compObj
	 *            SimulatorObject
	 **/
	public void setGeometry(SimulatorObject compObj) {
		assert (compObj.aId == this.aId);

		this.setPosition(compObj.getPosition());
		this.setDirection(compObj.getDirection());
		if (compObj.getShape() != null)
			// @todo This fails - need to update the shape of object in our tree
			// In theory setting the position and direction already reset the
			// polygon to some machine epsilon precision
			this.aShape = (Polygon) (compObj.getShape().clone());
	}

	/**
	 * Copies the data from 'original' onto this object. This is used for
	 * cloning purposes.
	 */
	protected void copyFrom(SimulatorObject org) {
		this.aPos = (Vector2D) org.aPos.clone();
		this.aDir = org.aDir;

		// We must copy the shape around, because the polygons get modified
		// by translations and rotations
		if (org.aShape != null)
			this.aShape = (Polygon) org.aShape.clone();
		else
			this.aShape = null;
		svgShapeDrawing = org.svgShapeDrawing;
		/*
		 * @todo Copy the other attributes of the object, the actuator and sensor
		 * list
		 */
		Set<Entry<String, ObjectState>> theEntrySet = org.aStates.entrySet();

		for (Entry<String, ObjectState> thisEntry : theEntrySet) {
			this.addState((ObjectState) thisEntry.getValue().clone());
		}
	}

	/**
	 * This method clears this object's attributes, as well as all of its
	 * children's. It is meant to be used to reset a state to its default
	 * values.
	 */
	public void clearTransient() {
		// Postorder traversal, for no particular reason
		for (SimulatorObject c : aChildren)
			c.clearTransient();

		// For each of our ObjectState, clear its data
		for (Entry<String, ObjectState> thisEntry : aStates.entrySet()) {
			thisEntry.getValue().clearTransient();
		}
	}

	public ShapeDrawing setSVG(String pictureName) {
		svgShapeDrawing = new ShapeDrawing(pictureName);
		return svgShapeDrawing;
	}
}
