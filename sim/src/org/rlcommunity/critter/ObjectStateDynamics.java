package org.rlcommunity.critter;

/**
 * ObjectStateDynamics
 *
 * Defines properties of an object necessary for movement and collision. That
 * is, anything that should move or can be run into needs this state component.
 *
 * @author Marc G. Bellemare
 * @author Anna Koop
 */
import java.util.LinkedList;
import java.util.List;

import java.awt.Graphics;

public class ObjectStateDynamics implements ObjectState {

    public static final double MIN_MASS = 0.000001; // 1 mg
    public static final double MIN_MOMENT_INERTIA = MIN_MASS * 1; // 1 mg m^2
    public static final double MAX_MASS = Double.MAX_VALUE;
    public static final double MAX_MOMENT_INERTIA = Double.MAX_VALUE;
        public static final double GRAVITY = 9.81; //@todo check units
        

    /** Dynamics state */
    /** Object velocity, in m/s */
    private Vector2D aVel;
    /** Forces that will be applied to the object this time step (in N) */
    protected LinkedList<Force> aForces;
    /** Collisions that the object has been involved in on this timestep */
    protected LinkedList<Collision> collisions;
    /** Angular velocity, in rad/s */
    private double aAngVel;
    /** Torque, in ? */
    //@todo make this part of the forces list
    protected double aTorque;
    /** Object mass, in kg */
    private double aMass;
    /** Object moment of inertia, in kg m^2 */
    private double aMomI;
    /** @todo Object coefficient of friction against floor */
    private double coefficientFrictionStatic = .2;
    private double coefficientFrictionDyn = .1;
    /** Object coefficient of restitution with some imaginary median obj  */
    private double coefficientRestitution = 1;
    /** Min and max speed the object can move at, mostly useful for stationary objects */
    private double minSpeed, maxSpeed;
    /** @todo Object center of mass (used as axis of rotation as well */
    
    /** Creates a new dynamics state component with a particular mass and 
     *  moment of inertia.
     *
     * @param pMass The mass of the object to which this state component belongs,
     *   in kilograms
     * @param pMomentI The moment of inertia of the object to which this state
     *   component belongs, in kg m^2
     */
    public ObjectStateDynamics(double pMass, double pMomentInertia) {
        aMass = pMass;
        aMomI = pMomentInertia;

        aVel = new Vector2D(0, 0);
        aAngVel = aTorque = 0;
        minSpeed = 0;
        maxSpeed = 1;
        collisions = new LinkedList<Collision>();
    }

    public void clear() {
        aVel.x = 0;
        aVel.y = 0;
        aAngVel = 0;
    }
    /** Creates a nearly massless Dynamics state. Because a minimum mass 
     *  is recommended by classical physics, we use it here as well.
     *  Massless objects (e.g. invisible light, magnetic, etc, sources)
     *  should not be given an ObjectStateDynamics.
     */
    public ObjectStateDynamics() {
        this(MIN_MASS, MIN_MOMENT_INERTIA);
    }

    /** Return the sum of forces acting on the object. Because it is meaningless
     *  to talk about a point of contact when many forces are in play,
     *  this new force has no 'source'.
     *
     * @return The sum of the forces
     */
    public Force getForceSum() {
        Force sum = new Force(0, 0);

        if (aForces == null) {
            return sum;
        }
        for (Force f : aForces) {
            sum.vec.plusEquals(f.vec);
        }
        return sum;
    }

    public List<Force> getForces() {
        return aForces;
    }

    public void addForce(Force f) {
        if (aForces == null) {
            aForces = new LinkedList<Force>();
        }
        aForces.add(f);
    }

    public void clearForces() {
        if (aForces != null) {
            aForces.clear();
        }
    }

    public List<Collision> getCollisions() {
        return collisions;
    }

    public void addCollision(Collision c) {
        collisions.add(c);
    }

    public void clearCollisions() {
        collisions.clear();
    }

    public Vector2D getVelocity() {
        return aVel;
    }
    
  

    public void setVelocity(Vector2D v) {
        aVel = v;
    }

    public double getAngVelocity() {
        return aAngVel;
    }

    public void setAngVelocity(double v) {
        aAngVel = v;
    }

    public double getTorque() {
        return aTorque;
    }

    public void addTorque(double t) {
        aTorque += t;
    }

    public void setTorque(double t) {
        aTorque = t;
    }

    public void clearTorque() {
        aTorque = 0;
    }

    public void setMass(double m) {
        aMass = m;
    }

    public double getMass() {
        return aMass;
    }

    public void setMomentInertia(double m) {
        aMomI = m;
    }

    public double getMomentInertia() {
        return aMomI;
    }

    /** ObjectState interface */
    public String getName() {
        return SimulatorComponentDynamics.NAME;
    }

    public Object clone() {
        ObjectStateDynamics newDyn = new ObjectStateDynamics();
        newDyn.copyFrom(this);

        return newDyn;
    }

    protected void copyFrom(ObjectState os) {
        ObjectStateDynamics dyn = (ObjectStateDynamics) os;

        this.aVel = (Vector2D) dyn.getVelocity().clone();
        this.aAngVel = dyn.getAngVelocity();

        this.setMass(dyn.getMass());
        this.setMomentInertia(dyn.getMomentInertia());
        this.setMinSpeed(dyn.getMinSpeed());
        this.setMaxSpeed(dyn.getMaxSpeed());

    // Should we copy the forces over? by definition we shouldn't carry
    //  them from state to state, but...
    }

    public void draw(Graphics g, SimulatorObject o) {
        // @@@ Draw forces here? Collisions!
    }

    public double getCoefficientRestitution() {
        return coefficientRestitution;
    }

    public void setCoefficientRestitution(double coefficientRestitution) {
        this.coefficientRestitution = coefficientRestitution;
    }

    /**
     * Clear all the dynamics data
     */
    public void clearAll() {
        clearTorque();
        clearForces();
        clearCollisions();
        setVelocity(new Vector2D(0,0));
        setAngVelocity(0);
    }

    public Vector2D subtractFriction(Force f) {
        return subtractFriction(f.vec);
    }
    
    public Vector2D subtractFriction(Vector2D applied) {
        double mu = 0;
        // this is not complete, because if the force is sufficient to start
        // it moving then we should use the dynamic mu, but whatevs
        if(aVel.equals(new Vector2D(0,0)))
            mu = coefficientFrictionStatic;
        else
            mu = coefficientFrictionDyn;
        Vector2D f = aVel.reverse();
        f.normalize();
        // now we have the component ratio
        f.times(mu*this.getMass()*ObjectStateDynamics.GRAVITY);
        return applied.minus(f);
    }

    double getCoefficientRestitution(ObjectStateDynamics o2) {
        double e = getCoefficientRestitution() + o2.getCoefficientRestitution();
        e = Math.max(0, e);
        e = Math.min(e, 1);
        return e;
    }

    /**
     * @return the minSpeed
     */
    public double getMinSpeed() {
        return minSpeed;
    }

    /**
     * @param minSpeed the minSpeed to set
     */
    public void setMinSpeed(double minSpeed) {
        this.minSpeed = minSpeed;
    }

    /**
     * @return the maxSpeed
     */
    public double getMaxSpeed() {
        return maxSpeed;
    }

    /**
     * @param maxSpeed the maxSpeed to set
     */
    public void setMaxSpeed(double maxSpeed) {
        this.maxSpeed = maxSpeed;
    }


}

