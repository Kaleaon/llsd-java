/*
 * Particle System - Java implementation of particle effects for Second Life
 *
 * Based on Second Life and Firestorm particle systems
 * Copyright (C) 2010, Linden Research, Inc.
 * Java conversion Copyright (C) 2024
 */

package lindenlab.llsd.viewer.secondlife.engine.rendering;

import lindenlab.llsd.viewer.secondlife.engine.Vector3;
import lindenlab.llsd.viewer.secondlife.engine.Quaternion;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.UUID;

/**
 * Advanced particle effects system for Second Life.
 * 
 * <p>This class provides a comprehensive particle system supporting various
 * effect types, emitters, and modern GPU-accelerated particle rendering
 * compatible with Second Life and enhanced for modern standards.</p>
 * 
 * @since 1.0
 */
public class ParticleSystem {
    
    /** A unique identifier for this particle system. */
    private final UUID systemId;
    /** The name of the particle system. */
    private String name;
    /** If false, the system will not be updated or rendered. */
    private boolean enabled;
    /** The current age of the particle system in seconds. */
    private double systemAge;
    /** The maximum age of the system in seconds. After this time, it will stop emitting. -1 for infinite. */
    private double maxAge;
    
    /** The emitter responsible for creating new particles. */
    private ParticleEmitter emitter;
    /** The position of the emitter in world or local space. */
    private Vector3 emitterPosition;
    /** The rotation of the emitter. */
    private Quaternion emitterRotation;
    /** The scale of the emitter volume. */
    private Vector3 emitterScale;
    
    /** A global acceleration vector applied to all particles (e.g., gravity). */
    private Vector3 globalAcceleration;
    /** A damping factor applied to all particle velocities each frame. */
    private double globalDamping;
    /** The bounding box that contains the particle system, used for culling. */
    private BoundingBox boundingBox;
    /** If true, particle positions are relative to the emitter's transform. */
    private boolean useLocalCoordinates;
    
    /** The rendering mode for the particles (e.g., BILLBOARD). */
    private RenderMode renderMode;
    /** The blend mode used for rendering particles (e.g., ALPHA, ADDITIVE). */
    private BlendMode blendMode;
    /** The UUID of the texture to apply to the particles. */
    private UUID texture;
    /** If true, particles will write to the depth buffer. */
    private boolean depthWrite;
    /** If true, particles will be depth-tested against the scene. */
    private boolean depthTest;
    /** The sorting mode for transparent particles. */
    private SortMode sortMode;
    
    /** The maximum number of active particles allowed in the system. */
    private int maxParticles;
    /** If true, the renderer can batch particle drawing for better performance. */
    private boolean useBatching;
    /** If true, particle simulation is offloaded to the GPU. */
    private boolean useGPUSimulation;
    /** The quality level of the simulation (0=low, 3=high). */
    private int simulationQuality;
    
    /** A queue of currently active (living) particles. */
    private final Queue<Particle> activeParticles;
    /** A pool of dead particles that can be recycled to create new ones. */
    private final Queue<Particle> deadParticles;
    
    /**
     * Defines the shape, rate, and properties of particle emission.
     * <p>
     * The emitter is responsible for generating new particles, defining their
     * initial position, velocity, and other properties based on its configuration.
     */
    public static class ParticleEmitter {
        /** The type of the emitter (e.g., POINT, BOX, SPHERE). */
        private EmitterType type;
        /** The shape of the emission area (e.g., VOLUME, SURFACE). */
        private EmitterShape shape;
        /** The dimensions of the emitter shape (e.g., box size, sphere radius). */
        private Vector3 size;
        
        /** The number of particles to emit per second in continuous mode. */
        private double emissionRate;
        /** The number of particles to emit in a single burst. */
        private double burstCount;
        /** The time interval in seconds between bursts. */
        private double burstInterval;
        /** If true, emission is continuous; if false, it occurs in bursts. */
        private boolean continuous;
        
        /** A template defining the initial properties of newly created particles. */
        private ParticleProperties particleTemplate;
        /** A set of variations to apply to the initial particle properties. */
        private RandomVariation variation;
        
        /** An enumeration of the types of emitter shapes. */
        public enum EmitterType {
            /** Emits from a single point. */
            POINT,
            /** Emits from a box shape. */
            BOX,
            /** Emits from a sphere shape. */
            SPHERE,
            /** Emits from a cone shape. */
            CONE,
            /** Emits from a cylinder shape. */
            CYLINDER,
            /** Emits from the vertices of a mesh. */
            MESH,
            /** Emits from a texture, based on pixel brightness. */
            TEXTURE
        }
        
        /** An enumeration of where on the emitter's shape particles are generated. */
        public enum EmitterShape {
            /** Emit from anywhere within the emitter's volume. */
            VOLUME,
            /** Emit from the surface of the emitter's shape. */
            SURFACE,
            /** Emit from the edges of the emitter's shape. */
            EDGE
        }
        
        /**
         * Constructs a new {@code ParticleEmitter} of a specified type.
         *
         * @param type The type of emitter to create.
         */
        public ParticleEmitter(EmitterType type) {
            this.type = type;
            this.shape = EmitterShape.VOLUME;
            this.size = new Vector3(1.0, 1.0, 1.0);
            this.emissionRate = 10.0;
            this.burstCount = 1.0;
            this.burstInterval = 1.0;
            this.continuous = true;
            this.particleTemplate = new ParticleProperties();
            this.variation = new RandomVariation();
        }
        
        /** @return The type of the emitter. */
        public EmitterType getType() { return type; }
        /** @param type The new type for the emitter. */
        public void setType(EmitterType type) { this.type = type; }
        
        /** @return The shape of the emission area. */
        public EmitterShape getShape() { return shape; }
        /** @param shape The new shape for the emission area. */
        public void setShape(EmitterShape shape) { this.shape = shape; }
        
        /** @return The dimensions of the emitter shape. */
        public Vector3 getSize() { return size; }
        /** @param size The new dimensions for the emitter shape. */
        public void setSize(Vector3 size) { this.size = size; }
        
        /** @return The number of particles to emit per second. */
        public double getEmissionRate() { return emissionRate; }
        /** @param emissionRate The new emission rate, clamped to be non-negative. */
        public void setEmissionRate(double emissionRate) { this.emissionRate = Math.max(0.0, emissionRate); }
        
        /** @return The number of particles to emit in a single burst. */
        public double getBurstCount() { return burstCount; }
        /** @param burstCount The new burst count, must be at least 1.0. */
        public void setBurstCount(double burstCount) { this.burstCount = Math.max(1.0, burstCount); }
        
        /** @return The time interval in seconds between bursts. */
        public double getBurstInterval() { return burstInterval; }
        /** @param burstInterval The new burst interval, must be positive. */
        public void setBurstInterval(double burstInterval) { this.burstInterval = Math.max(0.01, burstInterval); }
        
        /** @return True if emission is continuous, false if it occurs in bursts. */
        public boolean isContinuous() { return continuous; }
        /** @param continuous The new emission mode. */
        public void setContinuous(boolean continuous) { this.continuous = continuous; }
        
        /** @return The template for initial particle properties. */
        public ParticleProperties getParticleTemplate() { return particleTemplate; }
        /** @param particleTemplate The new template for particle properties. */
        public void setParticleTemplate(ParticleProperties particleTemplate) { this.particleTemplate = particleTemplate; }
        
        /** @return The set of random variations for particle properties. */
        public RandomVariation getVariation() { return variation; }
        /** @param variation The new set of random variations. */
        public void setVariation(RandomVariation variation) { this.variation = variation; }
        
        /**
         * Generates a random position for a new particle within the emitter's volume or on its surface.
         *
         * @param random A {@link Random} instance to use for generation.
         * @return A {@link Vector3} representing the new particle's initial position relative to the emitter.
         */
        public Vector3 generatePosition(Random random) {
            switch (type) {
                case POINT:
                    return Vector3.ZERO;
                    
                case BOX:
                    if (shape == EmitterShape.VOLUME) {
                        return new Vector3(
                            (random.nextDouble() - 0.5) * size.x,
                            (random.nextDouble() - 0.5) * size.y,
                            (random.nextDouble() - 0.5) * size.z
                        );
                    } else if (shape == EmitterShape.SURFACE) {
                        // Generate on box surface
                        int face = random.nextInt(6);
                        double u = random.nextDouble() - 0.5;
                        double v = random.nextDouble() - 0.5;
                        
                        switch (face) {
                            case 0: return new Vector3(-size.x/2, u * size.y, v * size.z); // -X face
                            case 1: return new Vector3( size.x/2, u * size.y, v * size.z); // +X face
                            case 2: return new Vector3(u * size.x, -size.y/2, v * size.z); // -Y face
                            case 3: return new Vector3(u * size.x,  size.y/2, v * size.z); // +Y face
                            case 4: return new Vector3(u * size.x, v * size.y, -size.z/2); // -Z face
                            case 5: return new Vector3(u * size.x, v * size.y,  size.z/2); // +Z face
                        }
                    }
                    break;
                    
                case SPHERE:
                    double radius = size.x; // Use X component as radius
                    if (shape == EmitterShape.VOLUME) {
                        // Generate within sphere volume
                        double r = radius * Math.cbrt(random.nextDouble());
                        double theta = random.nextDouble() * 2 * Math.PI;
                        double phi = Math.acos(2 * random.nextDouble() - 1);
                        
                        return new Vector3(
                            r * Math.sin(phi) * Math.cos(theta),
                            r * Math.sin(phi) * Math.sin(theta),
                            r * Math.cos(phi)
                        );
                    } else if (shape == EmitterShape.SURFACE) {
                        // Generate on sphere surface
                        double theta = random.nextDouble() * 2 * Math.PI;
                        double phi = Math.acos(2 * random.nextDouble() - 1);
                        
                        return new Vector3(
                            radius * Math.sin(phi) * Math.cos(theta),
                            radius * Math.sin(phi) * Math.sin(theta),
                            radius * Math.cos(phi)
                        );
                    }
                    break;
                    
                case CONE:
                    // Generate within cone
                    double height = size.z;
                    double baseRadius = size.x;
                    double h = random.nextDouble() * height;
                    double r = baseRadius * (height - h) / height; // Cone taper
                    
                    if (shape == EmitterShape.VOLUME) {
                        r *= Math.sqrt(random.nextDouble()); // Uniform distribution in disk
                    }
                    
                    double angle = random.nextDouble() * 2 * Math.PI;
                    
                    return new Vector3(
                        r * Math.cos(angle),
                        r * Math.sin(angle),
                        h - height / 2 // Center at origin
                    );
                    
                default:
                    return Vector3.ZERO;
            }
            return Vector3.ZERO;
        }
        
        /**
         * Generates an initial velocity for a new particle based on the emitter's type and properties.
         *
         * @param position The initial position of the particle, which can influence the velocity direction (e.g., for sphere emitters).
         * @param random A {@link Random} instance to use for generation.
         * @return A {@link Vector3} representing the new particle's initial velocity.
         */
        public Vector3 generateVelocity(Vector3 position, Random random) {
            Vector3 direction = Vector3.ZERO;
            
            switch (type) {
                case POINT:
                    // Random direction
                    double theta = random.nextDouble() * 2 * Math.PI;
                    double phi = Math.acos(2 * random.nextDouble() - 1);
                    direction = new Vector3(
                        Math.sin(phi) * Math.cos(theta),
                        Math.sin(phi) * Math.sin(theta),
                        Math.cos(phi)
                    );
                    break;
                    
                case SPHERE:
                    // Radial direction from center
                    direction = position.normalize();
                    break;
                    
                case CONE:
                    // Upward cone direction
                    direction = new Vector3(0, 0, 1);
                    // Add some spread based on cone angle
                    double spread = Math.atan2(size.x, size.z); // Cone half-angle
                    double spreadAngle = (random.nextDouble() - 0.5) * spread;
                    double spreadDir = random.nextDouble() * 2 * Math.PI;
                    
                    Vector3 spreadVec = new Vector3(
                        Math.sin(spreadAngle) * Math.cos(spreadDir),
                        Math.sin(spreadAngle) * Math.sin(spreadDir),
                        Math.cos(spreadAngle)
                    );
                    direction = direction.add(spreadVec).normalize();
                    break;
                    
                default:
                    direction = new Vector3(0, 0, 1); // Default upward
                    break;
            }
            
            double speed = particleTemplate.initialSpeed;
            if (variation.speedVariation > 0) {
                speed += (random.nextGaussian()) * variation.speedVariation;
                speed = Math.max(0, speed);
            }
            
            return direction.multiply(speed);
        }
    }
    
    /**
     * A container for all the physical and visual properties of a single particle.
     */
    public static class ParticleProperties {
        /** The total lifetime of the particle in seconds. */
        public double life;
        /** The current age of the particle in seconds. */
        public double age;
        
        /** The position of the particle. */
        public Vector3 position;
        /** The velocity of the particle. */
        public Vector3 velocity;
        /** The acceleration of the particle. */
        public Vector3 acceleration;
        
        /** The color of the particle. */
        public Vector3 color;
        /** The alpha (transparency) of the particle. */
        public double alpha;
        /** The size (scale) of the particle. */
        public double size;
        /** The rotation of the particle in radians. */
        public double rotation;
        /** The angular velocity of the particle in radians per second. */
        public double angularVelocity;
        
        /** The initial speed of the particle upon emission. */
        public double initialSpeed;
        /** The mass of the particle, used in physics calculations. */
        public double mass;
        /** The drag coefficient, representing air resistance. */
        public double drag;
        /** The coefficient of restitution, determining how much the particle bounces on collision. */
        public double bounce;
        
        /** The total number of frames in the particle's texture animation. */
        public int textureAnimFrames;
        /** The current frame of the texture animation. */
        public int currentFrame;
        /** The time elapsed on the current animation frame. */
        public double frameTime;
        /** The playback rate of the texture animation in frames per second. */
        public double frameRate;
        
        /**
         * Constructs a new {@code ParticleProperties} object with default values.
         */
        public ParticleProperties() {
            this.life = 5.0;
            this.age = 0.0;
            
            this.position = Vector3.ZERO;
            this.velocity = Vector3.ZERO;
            this.acceleration = Vector3.ZERO;
            
            this.color = new Vector3(1.0, 1.0, 1.0);
            this.alpha = 1.0;
            this.size = 1.0;
            this.rotation = 0.0;
            this.angularVelocity = 0.0;
            
            this.initialSpeed = 1.0;
            this.mass = 1.0;
            this.drag = 0.0;
            this.bounce = 0.0;
            
            this.textureAnimFrames = 1;
            this.currentFrame = 0;
            this.frameTime = 0.0;
            this.frameRate = 1.0;
        }
        
        /**
         * Checks if the particle is still alive (i.e., its age is less than its total lifetime).
         *
         * @return {@code true} if the particle is alive, {@code false} otherwise.
         */
        public boolean isAlive() {
            return age < life;
        }
        
        /**
         * Gets the normalized age of the particle, a value from 0.0 to 1.0.
         *
         * @return The particle's age as a fraction of its total lifetime.
         */
        public double getNormalizedAge() {
            return life > 0 ? age / life : 1.0;
        }
    }
    
    /**
     * Defines the amount of random variation to apply to the initial properties of new particles.
     */
    public static class RandomVariation {
        /** The random variation to apply to particle lifetime. */
        public double lifeVariation;
        /** The random variation to apply to initial particle speed. */
        public double speedVariation;
        /** The random variation to apply to initial particle color. */
        public Vector3 colorVariation;
        /** The random variation to apply to initial particle alpha. */
        public double alphaVariation;
        /** The random variation to apply to initial particle size. */
        public double sizeVariation;
        /** The random variation to apply to initial particle rotation. */
        public double rotationVariation;
        /** The random variation to apply to initial particle angular velocity. */
        public double angularVelocityVariation;
        
        /**
         * Constructs a new {@code RandomVariation} object with all variations set to zero.
         */
        public RandomVariation() {
            this.lifeVariation = 0.0;
            this.speedVariation = 0.0;
            this.colorVariation = Vector3.ZERO;
            this.alphaVariation = 0.0;
            this.sizeVariation = 0.0;
            this.rotationVariation = 0.0;
            this.angularVelocityVariation = 0.0;
        }
    }
    
    /**
     * Represents a single particle instance in the system.
     * <p>
     * This class holds the particle's current properties and references to
     * animation curves that can modify its properties over its lifetime.
     */
    public static class Particle {
        /** The current properties of the particle. */
        public ParticleProperties properties;
        
        /** An animation curve for the particle's size over its lifetime. */
        public AnimationCurve sizeCurve;
        /** An animation curve for the particle's color over its lifetime. */
        public AnimationCurve colorCurve;
        /** An animation curve for the particle's alpha over its lifetime. */
        public AnimationCurve alphaCurve;
        
        /**
         * Constructs a new {@code Particle} with default properties.
         */
        public Particle() {
            this.properties = new ParticleProperties();
        }
        
        public Particle(ParticleProperties template) {
            this.properties = new ParticleProperties();
            copyProperties(template, this.properties);
        }
        
        private void copyProperties(ParticleProperties from, ParticleProperties to) {
            to.life = from.life;
            to.position = from.position;
            to.velocity = from.velocity;
            to.acceleration = from.acceleration;
            to.color = from.color;
            to.alpha = from.alpha;
            to.size = from.size;
            to.rotation = from.rotation;
            to.angularVelocity = from.angularVelocity;
            to.initialSpeed = from.initialSpeed;
            to.mass = from.mass;
            to.drag = from.drag;
            to.bounce = from.bounce;
            to.textureAnimFrames = from.textureAnimFrames;
            to.frameRate = from.frameRate;
        }
        
        /**
         * Updates the particle's state over a given time step.
         *
         * @param deltaTime The time elapsed since the last update, in seconds.
         * @param globalAcceleration A global acceleration vector to apply to the particle.
         * @param globalDamping A global damping factor to apply to the particle's velocity.
         */
        public void update(double deltaTime, Vector3 globalAcceleration, double globalDamping) {
            if (!properties.isAlive()) return;
            
            properties.age += deltaTime;
            
            // Physics update
            Vector3 totalAcceleration = properties.acceleration.add(globalAcceleration);
            
            // Apply drag
            if (properties.drag > 0) {
                double dragForce = properties.velocity.magnitude() * properties.drag;
                Vector3 dragAccel = properties.velocity.normalize().multiply(-dragForce / properties.mass);
                totalAcceleration = totalAcceleration.add(dragAccel);
            }
            
            // Update velocity and position (Verlet integration)
            properties.velocity = properties.velocity.add(totalAcceleration.multiply(deltaTime));
            properties.velocity = properties.velocity.multiply(Math.pow(globalDamping, deltaTime));
            properties.position = properties.position.add(properties.velocity.multiply(deltaTime));
            
            // Update rotation
            properties.rotation += properties.angularVelocity * deltaTime;
            
            // Update animation
            if (properties.textureAnimFrames > 1) {
                properties.frameTime += deltaTime;
                if (properties.frameTime >= 1.0 / properties.frameRate) {
                    properties.currentFrame = (properties.currentFrame + 1) % properties.textureAnimFrames;
                    properties.frameTime = 0.0;
                }
            }
            
            // Apply animation curves
            double t = properties.getNormalizedAge();
            if (sizeCurve != null) {
                properties.size *= sizeCurve.evaluate(t);
            }
            if (alphaCurve != null) {
                properties.alpha *= alphaCurve.evaluate(t);
            }
            if (colorCurve != null) {
                // Modulate color - this is simplified, real implementation would handle RGB separately
                double colorMod = colorCurve.evaluate(t);
                properties.color = properties.color.multiply(colorMod);
            }
        }
    }
    
    /**
     * Defines a curve that interpolates a value over time, used for animating particle properties.
     * <p>
     * An {@code AnimationCurve} is defined by a series of {@link KeyFrame} objects.
     */
    public static class AnimationCurve {
        private final List<KeyFrame> keyFrames;
        
        /**
         * Represents a single point on an animation curve.
         */
        public static class KeyFrame {
            /** The time of the keyframe, normalized from 0.0 to 1.0. */
            public double time;
            /** The value of the curve at this keyframe. */
            public double value;
            /** The interpolation mode to use between this keyframe and the next. */
            public InterpolationMode interpolation;
            
            /** An enumeration of the interpolation modes between keyframes. */
            public enum InterpolationMode {
                /** Linear interpolation. */
                LINEAR,
                /** Cubic Hermite spline interpolation (simplified). */
                CUBIC,
                /** No interpolation; the value holds until the next keyframe. */
                STEP
            }
            
            /**
             * Constructs a new {@code KeyFrame} with linear interpolation.
             *
             * @param time The time of the keyframe (0.0 to 1.0).
             * @param value The value at this time.
             */
            public KeyFrame(double time, double value) {
                this(time, value, InterpolationMode.LINEAR);
            }
            
            /**
             * Constructs a new {@code KeyFrame} with a specified interpolation mode.
             *
             * @param time The time of the keyframe (0.0 to 1.0).
             * @param value The value at this time.
             * @param interpolation The interpolation mode.
             */
            public KeyFrame(double time, double value, InterpolationMode interpolation) {
                this.time = time;
                this.value = value;
                this.interpolation = interpolation;
            }
        }
        
        /**
         * Constructs a new, empty {@code AnimationCurve}.
         */
        public AnimationCurve() {
            this.keyFrames = new ArrayList<>();
        }
        
        /**
         * Adds a new keyframe to the curve.
         *
         * @param time The time of the keyframe (0.0 to 1.0).
         * @param value The value at this time.
         */
        public void addKeyFrame(double time, double value) {
            addKeyFrame(new KeyFrame(time, value));
        }
        
        /**
         * Adds a pre-constructed keyframe to the curve.
         *
         * @param keyFrame The {@link KeyFrame} to add.
         */
        public void addKeyFrame(KeyFrame keyFrame) {
            keyFrames.add(keyFrame);
            keyFrames.sort(Comparator.comparing(k -> k.time));
        }
        
        /**
         * Evaluates the curve at a specific time.
         *
         * @param time The time at which to evaluate the curve, normalized from 0.0 to 1.0.
         * @return The interpolated value of the curve at the given time.
         */
        public double evaluate(double time) {
            if (keyFrames.isEmpty()) return 1.0;
            if (keyFrames.size() == 1) return keyFrames.get(0).value;
            
            time = Math.max(0.0, Math.min(1.0, time));
            
            // Find surrounding keyframes
            KeyFrame before = keyFrames.get(0);
            KeyFrame after = keyFrames.get(keyFrames.size() - 1);
            
            for (int i = 0; i < keyFrames.size() - 1; i++) {
                if (time >= keyFrames.get(i).time && time <= keyFrames.get(i + 1).time) {
                    before = keyFrames.get(i);
                    after = keyFrames.get(i + 1);
                    break;
                }
            }
            
            if (before == after) return before.value;
            
            // Interpolate
            double t = (time - before.time) / (after.time - before.time);
            
            switch (before.interpolation) {
                case LINEAR:
                    return before.value + (after.value - before.value) * t;
                    
                case CUBIC:
                    // Simplified cubic interpolation
                    double t2 = t * t;
                    double t3 = t2 * t;
                    return before.value * (2*t3 - 3*t2 + 1) + after.value * (3*t2 - 2*t3);
                    
                case STEP:
                    return before.value;
                    
                default:
                    return before.value + (after.value - before.value) * t;
            }
        }
    }
    
    /** An enumeration of the rendering modes for particles. */
    public enum RenderMode {
        /** Particles are rendered as billboards that always face the camera. */
        BILLBOARD,
        /** Particles are stretched and aligned with their velocity vector. */
        VELOCITY_ALIGNED,
        /** Particles are rendered as billboards constrained to the horizontal plane. */
        HORIZONTAL,
        /** Particles are rendered as billboards constrained to a vertical axis. */
        VERTICAL,
        /** Particles are rendered as instances of a 3D mesh. */
        MESH,
        /** Particles are rendered as streaks based on their velocity. */
        STREAKS
    }
    
    /** An enumeration of the blend modes for transparent particles. */
    public enum BlendMode {
        /** Standard alpha blending. */
        ALPHA,
        /** Additive blending, useful for fire and light effects. */
        ADDITIVE,
        /** Multiplicative blending. */
        MULTIPLY,
        /** Subtractive blending. */
        SUBTRACT,
        /** Screen blending. */
        SCREEN
    }
    
    /** An enumeration of the sorting modes for transparent particles. */
    public enum SortMode {
        /** No sorting is performed. */
        NONE,
        /** Particles are sorted from back to front based on distance from the camera. */
        BACK_TO_FRONT,
        /** Particles are sorted from front to back. */
        FRONT_TO_BACK,
        /** Particles are sorted by their age. */
        BY_AGE,
        /** Particles are sorted by their size. */
        BY_SIZE
    }
    
    /**
     * An axis-aligned bounding box (AABB) for the particle system, used for culling.
     */
    public static class BoundingBox {
        /** The minimum corner of the box. */
        public Vector3 min;
        /** The maximum corner of the box. */
        public Vector3 max;
        
        /**
         * Constructs a new {@code BoundingBox} with default values.
         */
        public BoundingBox() {
            this.min = new Vector3(-10, -10, -10);
            this.max = new Vector3(10, 10, 10);
        }
        
        public BoundingBox(Vector3 min, Vector3 max) {
            this.min = min;
            this.max = max;
        }
        
        public boolean contains(Vector3 point) {
            return point.x >= min.x && point.x <= max.x &&
                   point.y >= min.y && point.y <= max.y &&
                   point.z >= min.z && point.z <= max.z;
        }
        
        public Vector3 getCenter() {
            return min.add(max).multiply(0.5);
        }
        
        public Vector3 getSize() {
            return max.subtract(min);
        }
    }
    
    /**
     * Constructs a new {@code ParticleSystem} with a given name and default settings.
     *
     * @param name The name of the particle system.
     */
    public ParticleSystem(String name) {
        this.systemId = UUID.randomUUID();
        this.name = name;
        this.enabled = true;
        this.systemAge = 0.0;
        this.maxAge = -1.0; // Infinite by default
        
        this.emitter = new ParticleEmitter(ParticleEmitter.EmitterType.POINT);
        this.emitterPosition = Vector3.ZERO;
        this.emitterRotation = Quaternion.IDENTITY;
        this.emitterScale = new Vector3(1.0, 1.0, 1.0);
        
        this.globalAcceleration = new Vector3(0.0, 0.0, -9.81); // Default gravity
        this.globalDamping = 0.99;
        this.boundingBox = new BoundingBox();
        this.useLocalCoordinates = true;
        
        this.renderMode = RenderMode.BILLBOARD;
        this.blendMode = BlendMode.ALPHA;
        this.depthWrite = false;
        this.depthTest = true;
        this.sortMode = SortMode.BACK_TO_FRONT;
        
        this.maxParticles = 1000;
        this.useBatching = true;
        this.useGPUSimulation = false;
        this.simulationQuality = 2;
        
        this.activeParticles = new ConcurrentLinkedQueue<>();
        this.deadParticles = new ConcurrentLinkedQueue<>();
    }
    
    /** @return The unique identifier for this particle system. */
    public UUID getSystemId() { return systemId; }
    /** @return The name of the particle system. */
    public String getName() { return name; }
    /** @param name The new name for the particle system. */
    public void setName(String name) { this.name = name; }
    
    /** @return True if the system is enabled. */
    public boolean isEnabled() { return enabled; }
    /** @param enabled The new enabled state for the system. */
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    
    /** @return The current age of the system in seconds. */
    public double getSystemAge() { return systemAge; }
    /** @return The maximum age of the system in seconds. */
    public double getMaxAge() { return maxAge; }
    /** @param maxAge The new maximum age for the system. */
    public void setMaxAge(double maxAge) { this.maxAge = maxAge; }
    
    /** @return The particle emitter for this system. */
    public ParticleEmitter getEmitter() { return emitter; }
    /** @param emitter The new particle emitter for this system. */
    public void setEmitter(ParticleEmitter emitter) { this.emitter = emitter; }
    
    /** @return The position of the emitter. */
    public Vector3 getEmitterPosition() { return emitterPosition; }
    /** @param emitterPosition The new position for the emitter. */
    public void setEmitterPosition(Vector3 emitterPosition) { this.emitterPosition = emitterPosition; }
    
    /** @return The rotation of the emitter. */
    public Quaternion getEmitterRotation() { return emitterRotation; }
    /** @param emitterRotation The new rotation for the emitter. */
    public void setEmitterRotation(Quaternion emitterRotation) { this.emitterRotation = emitterRotation; }
    
    /** @return The scale of the emitter. */
    public Vector3 getEmitterScale() { return emitterScale; }
    /** @param emitterScale The new scale for the emitter. */
    public void setEmitterScale(Vector3 emitterScale) { this.emitterScale = emitterScale; }
    
    /** @return The global acceleration applied to all particles. */
    public Vector3 getGlobalAcceleration() { return globalAcceleration; }
    /** @param globalAcceleration The new global acceleration vector. */
    public void setGlobalAcceleration(Vector3 globalAcceleration) { this.globalAcceleration = globalAcceleration; }
    
    /** @return The global damping factor. */
    public double getGlobalDamping() { return globalDamping; }
    /** @param globalDamping The new global damping factor, clamped to the range [0, 1]. */
    public void setGlobalDamping(double globalDamping) { this.globalDamping = Math.max(0.0, Math.min(1.0, globalDamping)); }
    
    /** @return The bounding box of the system. */
    public BoundingBox getBoundingBox() { return boundingBox; }
    /** @param boundingBox The new bounding box for the system. */
    public void setBoundingBox(BoundingBox boundingBox) { this.boundingBox = boundingBox; }
    
    /** @return True if particles use local coordinates relative to the emitter. */
    public boolean isUseLocalCoordinates() { return useLocalCoordinates; }
    /** @param useLocalCoordinates The new coordinate system mode. */
    public void setUseLocalCoordinates(boolean useLocalCoordinates) { this.useLocalCoordinates = useLocalCoordinates; }
    
    /** @return The rendering mode for the particles. */
    public RenderMode getRenderMode() { return renderMode; }
    /** @param renderMode The new rendering mode for the particles. */
    public void setRenderMode(RenderMode renderMode) { this.renderMode = renderMode; }
    
    /** @return The blend mode for the particles. */
    public BlendMode getBlendMode() { return blendMode; }
    /** @param blendMode The new blend mode for the particles. */
    public void setBlendMode(BlendMode blendMode) { this.blendMode = blendMode; }
    
    /** @return The UUID of the texture used by the particles. */
    public UUID getTexture() { return texture; }
    /** @param texture The new texture UUID for the particles. */
    public void setTexture(UUID texture) { this.texture = texture; }
    
    /** @return True if particles write to the depth buffer. */
    public boolean isDepthWrite() { return depthWrite; }
    /** @param depthWrite The new depth write state. */
    public void setDepthWrite(boolean depthWrite) { this.depthWrite = depthWrite; }
    
    /** @return True if particles are depth-tested. */
    public boolean isDepthTest() { return depthTest; }
    /** @param depthTest The new depth test state. */
    public void setDepthTest(boolean depthTest) { this.depthTest = depthTest; }
    
    /** @return The sorting mode for transparent particles. */
    public SortMode getSortMode() { return sortMode; }
    /** @param sortMode The new sorting mode. */
    public void setSortMode(SortMode sortMode) { this.sortMode = sortMode; }
    
    /** @return The maximum number of active particles. */
    public int getMaxParticles() { return maxParticles; }
    /** @param maxParticles The new maximum number of particles, must be at least 1. */
    public void setMaxParticles(int maxParticles) { this.maxParticles = Math.max(1, maxParticles); }
    
    /** @return True if particle rendering is batched. */
    public boolean isUseBatching() { return useBatching; }
    /** @param useBatching The new batching state. */
    public void setUseBatching(boolean useBatching) { this.useBatching = useBatching; }
    
    /** @return True if particle simulation is performed on the GPU. */
    public boolean isUseGPUSimulation() { return useGPUSimulation; }
    /** @param useGPUSimulation The new GPU simulation state. */
    public void setUseGPUSimulation(boolean useGPUSimulation) { this.useGPUSimulation = useGPUSimulation; }
    
    /** @return The quality level of the simulation. */
    public int getSimulationQuality() { return simulationQuality; }
    /** @param simulationQuality The new simulation quality level, clamped to the range [0, 3]. */
    public void setSimulationQuality(int simulationQuality) { this.simulationQuality = Math.max(0, Math.min(3, simulationQuality)); }
    
    /**
     * Gets the current number of active particles in the system.
     *
     * @return The number of active particles.
     */
    public int getActiveParticleCount() {
        return activeParticles.size();
    }
    
    /**
     * Checks if the particle system has exceeded its maximum lifetime.
     *
     * @return {@code true} if the system has expired, {@code false} otherwise.
     */
    public boolean hasExpired() {
        return maxAge > 0 && systemAge >= maxAge;
    }
    
    /**
     * Updates the entire particle system, including emitting new particles and updating existing ones.
     *
     * @param deltaTime The time elapsed since the last update, in seconds.
     */
    public void update(double deltaTime) {
        if (!enabled || hasExpired()) return;
        
        systemAge += deltaTime;
        
        // Emit new particles
        emitParticles(deltaTime);
        
        // Update existing particles
        updateParticles(deltaTime);
        
        // Remove dead particles
        cleanupDeadParticles();
    }
    
    private void emitParticles(double deltaTime) {
        if (emitter == null) return;
        
        Random random = new Random();
        
        if (emitter.isContinuous()) {
            // Continuous emission
            double particlesToEmit = emitter.getEmissionRate() * deltaTime;
            int wholeParticles = (int) particlesToEmit;
            double fractionalPart = particlesToEmit - wholeParticles;
            
            // Emit whole particles
            for (int i = 0; i < wholeParticles && activeParticles.size() < maxParticles; i++) {
                emitParticle(random);
            }
            
            // Stochastic emission for fractional part
            if (random.nextDouble() < fractionalPart && activeParticles.size() < maxParticles) {
                emitParticle(random);
            }
        } else {
            // Burst emission (simplified - would need burst timing logic)
            if (systemAge > 0 && (systemAge % emitter.getBurstInterval()) < deltaTime) {
                int burstCount = (int) emitter.getBurstCount();
                for (int i = 0; i < burstCount && activeParticles.size() < maxParticles; i++) {
                    emitParticle(random);
                }
            }
        }
    }
    
    private void emitParticle(Random random) {
        Particle particle = getParticleFromPool();
        
        // Generate initial position and velocity
        Vector3 localPos = emitter.generatePosition(random);
        Vector3 localVel = emitter.generateVelocity(localPos, random);
        
        // Transform to world space if needed
        if (useLocalCoordinates) {
            // Apply emitter transform
            localPos = emitterRotation.rotate(localPos.multiply(emitterScale));
            localVel = emitterRotation.rotate(localVel);
            
            particle.properties.position = emitterPosition.add(localPos);
            particle.properties.velocity = localVel;
        } else {
            particle.properties.position = localPos.add(emitterPosition);
            particle.properties.velocity = localVel;
        }
        
        // Apply random variations
        applyRandomVariation(particle, random);
        
        activeParticles.offer(particle);
    }
    
    private void applyRandomVariation(Particle particle, Random random) {
        RandomVariation var = emitter.getVariation();
        
        if (var.lifeVariation > 0) {
            double lifeMod = 1.0 + (random.nextGaussian() * var.lifeVariation);
            particle.properties.life = Math.max(0.1, particle.properties.life * lifeMod);
        }
        
        if (!var.colorVariation.isZero()) {
            Vector3 colorMod = new Vector3(
                1.0 + (random.nextGaussian() * var.colorVariation.x),
                1.0 + (random.nextGaussian() * var.colorVariation.y),
                1.0 + (random.nextGaussian() * var.colorVariation.z)
            );
            particle.properties.color = particle.properties.color.multiply(colorMod);
        }
        
        if (var.sizeVariation > 0) {
            double sizeMod = 1.0 + (random.nextGaussian() * var.sizeVariation);
            particle.properties.size = Math.max(0.01, particle.properties.size * sizeMod);
        }
        
        if (var.rotationVariation > 0) {
            particle.properties.rotation = random.nextGaussian() * var.rotationVariation;
        }
        
        if (var.angularVelocityVariation > 0) {
            particle.properties.angularVelocity = random.nextGaussian() * var.angularVelocityVariation;
        }
    }
    
    private void updateParticles(double deltaTime) {
        for (Particle particle : activeParticles) {
            particle.update(deltaTime, globalAcceleration, globalDamping);
            
            // Check bounds
            if (!boundingBox.contains(particle.properties.position)) {
                // Handle boundary collision or remove particle
                particle.properties.age = particle.properties.life; // Kill particle
            }
        }
    }
    
    private void cleanupDeadParticles() {
        Iterator<Particle> iterator = activeParticles.iterator();
        while (iterator.hasNext()) {
            Particle particle = iterator.next();
            if (!particle.properties.isAlive()) {
                iterator.remove();
                returnParticleToPool(particle);
            }
        }
    }
    
    private Particle getParticleFromPool() {
        Particle particle = deadParticles.poll();
        if (particle == null) {
            particle = new Particle(emitter.getParticleTemplate());
        } else {
            // Reset particle properties
            particle.properties.age = 0.0;
        }
        return particle;
    }
    
    private void returnParticleToPool(Particle particle) {
        deadParticles.offer(particle);
    }
    
    /**
     * Gets a collection of all active particles, suitable for rendering.
     *
     * @return A new {@link Collection} containing the active particles.
     */
    public Collection<Particle> getActiveParticles() {
        return new ArrayList<>(activeParticles);
    }
    
    /**
     * Clears all active particles from the system, returning them to the pool.
     */
    public void clear() {
        while (!activeParticles.isEmpty()) {
            returnParticleToPool(activeParticles.poll());
        }
    }
    
    /**
     * Resets the particle system to its initial state, clearing all particles and resetting its age.
     */
    public void reset() {
        clear();
        systemAge = 0.0;
    }
    
    /**
     * Converts this particle system's configuration into an LLSD map representation.
     *
     * @return A {@link Map} suitable for serialization, representing the particle system's settings.
     */
    public Map<String, Object> toLLSD() {
        Map<String, Object> systemData = new HashMap<>();
        
        systemData.put("SystemID", systemId);
        systemData.put("Name", name);
        systemData.put("Enabled", enabled);
        systemData.put("MaxAge", maxAge);
        systemData.put("MaxParticles", maxParticles);
        
        // Emitter data
        Map<String, Object> emitterData = new HashMap<>();
        emitterData.put("Type", emitter.getType().name());
        emitterData.put("Shape", emitter.getShape().name());
        emitterData.put("Size", Arrays.asList(emitter.getSize().x, emitter.getSize().y, emitter.getSize().z));
        emitterData.put("EmissionRate", emitter.getEmissionRate());
        emitterData.put("BurstCount", emitter.getBurstCount());
        emitterData.put("BurstInterval", emitter.getBurstInterval());
        emitterData.put("Continuous", emitter.isContinuous());
        systemData.put("Emitter", emitterData);
        
        // Transform
        systemData.put("Position", Arrays.asList(emitterPosition.x, emitterPosition.y, emitterPosition.z));
        systemData.put("Rotation", Arrays.asList(emitterRotation.x, emitterRotation.y, emitterRotation.z, emitterRotation.w));
        systemData.put("Scale", Arrays.asList(emitterScale.x, emitterScale.y, emitterScale.z));
        
        // Global properties
        systemData.put("GlobalAcceleration", Arrays.asList(globalAcceleration.x, globalAcceleration.y, globalAcceleration.z));
        systemData.put("GlobalDamping", globalDamping);
        systemData.put("UseLocalCoordinates", useLocalCoordinates);
        
        // Rendering
        systemData.put("RenderMode", renderMode.name());
        systemData.put("BlendMode", blendMode.name());
        if (texture != null) systemData.put("Texture", texture);
        systemData.put("DepthWrite", depthWrite);
        systemData.put("DepthTest", depthTest);
        systemData.put("SortMode", sortMode.name());
        
        // Performance
        systemData.put("UseBatching", useBatching);
        systemData.put("UseGPUSimulation", useGPUSimulation);
        systemData.put("SimulationQuality", simulationQuality);
        
        return systemData;
    }
    
    @Override
    public String toString() {
        return String.format("ParticleSystem[%s, particles=%d/%d, age=%.2f]",
                           name, activeParticles.size(), maxParticles, systemAge);
    }
}