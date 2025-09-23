/*
 * Second Life Scene Node - Java implementation of scene graph node
 *
 * Based on Second Life viewer implementation
 * Copyright (C) 2010, Linden Research, Inc.
 * Java conversion Copyright (C) 2024
 */

package lindenlab.llsd.viewer.secondlife.engine;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Basic scene graph node for Second Life 3D engine.
 * 
 * <p>This class provides the foundation for a scene graph structure
 * used in Second Life's 3D rendering system.</p>
 * 
 * @since 1.0
 */
public class SceneNode {
    
    private static final Map<UUID, SceneNode> nodeRegistry = new ConcurrentHashMap<>();
    
    private final UUID nodeId;
    private final String name;
    private SceneNode parent;
    private final List<SceneNode> children;
    private final Map<String, Object> properties;
    
    // Transform properties
    private Vector3 position;
    private Quaternion rotation;
    private Vector3 scale;
    private boolean visible;
    private boolean enabled;
    
    // Cached transformation matrices
    private double[][] localTransform;
    private double[][] worldTransform;
    private boolean transformDirty;
    
    /**
     * Create a new scene node.
     */
    public SceneNode(String name) {
        this.nodeId = UUID.randomUUID();
        this.name = name != null ? name : "Node_" + nodeId.toString().substring(0, 8);
        this.parent = null;
        this.children = new ArrayList<>();
        this.properties = new HashMap<>();
        
        this.position = Vector3.ZERO;
        this.rotation = Quaternion.IDENTITY;
        this.scale = Vector3.ONE;
        this.visible = true;
        this.enabled = true;
        
        this.transformDirty = true;
        
        // Register the node
        nodeRegistry.put(nodeId, this);
    }
    
    /**
     * Create a scene node with specific ID (for asset loading).
     */
    public SceneNode(UUID nodeId, String name) {
        this.nodeId = nodeId;
        this.name = name != null ? name : "Node_" + nodeId.toString().substring(0, 8);
        this.parent = null;
        this.children = new ArrayList<>();
        this.properties = new HashMap<>();
        
        this.position = Vector3.ZERO;
        this.rotation = Quaternion.IDENTITY;
        this.scale = Vector3.ONE;
        this.visible = true;
        this.enabled = true;
        
        this.transformDirty = true;
        
        // Register the node
        nodeRegistry.put(nodeId, this);
    }
    
    // Getters
    public UUID getNodeId() { return nodeId; }
    public String getName() { return name; }
    public SceneNode getParent() { return parent; }
    public List<SceneNode> getChildren() { return new ArrayList<>(children); }
    public Vector3 getPosition() { return position; }
    public Quaternion getRotation() { return rotation; }
    public Vector3 getScale() { return scale; }
    public boolean isVisible() { return visible; }
    public boolean isEnabled() { return enabled; }
    
    /**
     * Set position and mark transform dirty.
     */
    public void setPosition(Vector3 position) {
        this.position = position;
        markTransformDirty();
    }
    
    /**
     * Set rotation and mark transform dirty.
     */
    public void setRotation(Quaternion rotation) {
        this.rotation = rotation;
        markTransformDirty();
    }
    
    /**
     * Set scale and mark transform dirty.
     */
    public void setScale(Vector3 scale) {
        this.scale = scale;
        markTransformDirty();
    }
    
    /**
     * Set visibility.
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
    
    /**
     * Set enabled state.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    /**
     * Add a child node.
     */
    public void addChild(SceneNode child) {
        if (child == null || child == this) {
            throw new IllegalArgumentException("Invalid child node");
        }
        
        if (child.parent != null) {
            child.parent.removeChild(child);
        }
        
        children.add(child);
        child.parent = this;
        child.markTransformDirty();
    }
    
    /**
     * Remove a child node.
     */
    public void removeChild(SceneNode child) {
        if (children.remove(child)) {
            child.parent = null;
            child.markTransformDirty();
        }
    }
    
    /**
     * Remove this node from its parent.
     */
    public void removeFromParent() {
        if (parent != null) {
            parent.removeChild(this);
        }
    }
    
    /**
     * Find child by name.
     */
    public SceneNode findChild(String name) {
        for (SceneNode child : children) {
            if (child.name.equals(name)) {
                return child;
            }
        }
        return null;
    }
    
    /**
     * Find child by ID.
     */
    public SceneNode findChild(UUID nodeId) {
        for (SceneNode child : children) {
            if (child.nodeId.equals(nodeId)) {
                return child;
            }
        }
        return null;
    }
    
    /**
     * Find descendant by name (recursive).
     */
    public SceneNode findDescendant(String name) {
        SceneNode child = findChild(name);
        if (child != null) {
            return child;
        }
        
        for (SceneNode childNode : children) {
            SceneNode descendant = childNode.findDescendant(name);
            if (descendant != null) {
                return descendant;
            }
        }
        
        return null;
    }
    
    /**
     * Get property value.
     */
    public Object getProperty(String key) {
        return properties.get(key);
    }
    
    /**
     * Set property value.
     */
    public void setProperty(String key, Object value) {
        properties.put(key, value);
    }
    
    /**
     * Remove property.
     */
    public void removeProperty(String key) {
        properties.remove(key);
    }
    
    /**
     * Check if property exists.
     */
    public boolean hasProperty(String key) {
        return properties.containsKey(key);
    }
    
    /**
     * Get all properties.
     */
    public Map<String, Object> getProperties() {
        return new HashMap<>(properties);
    }
    
    /**
     * Get local transformation matrix.
     */
    public double[][] getLocalTransform() {
        if (transformDirty || localTransform == null) {
            updateLocalTransform();
        }
        return cloneMatrix(localTransform);
    }
    
    /**
     * Get world transformation matrix.
     */
    public double[][] getWorldTransform() {
        if (transformDirty || worldTransform == null) {
            updateWorldTransform();
        }
        return cloneMatrix(worldTransform);
    }
    
    /**
     * Get world position.
     */
    public Vector3 getWorldPosition() {
        double[][] world = getWorldTransform();
        return new Vector3(world[0][3], world[1][3], world[2][3]);
    }
    
    /**
     * Get world rotation.
     */
    public Quaternion getWorldRotation() {
        if (parent == null) {
            return rotation;
        }
        
        return parent.getWorldRotation().multiply(rotation);
    }
    
    /**
     * Transform a point from local to world space.
     */
    public Vector3 transformToWorld(Vector3 localPoint) {
        return multiplyMatrixVector(getWorldTransform(), localPoint);
    }
    
    /**
     * Transform a point from world to local space.
     */
    public Vector3 transformToLocal(Vector3 worldPoint) {
        return multiplyMatrixVector(invertMatrix(getWorldTransform()), worldPoint);
    }
    
    /**
     * Update local transformation matrix.
     */
    private void updateLocalTransform() {
        // Create transformation matrix: T * R * S
        double[][] translation = createTranslationMatrix(position);
        double[][] rotationMatrix = create4x4Matrix(rotation.toMatrix3());
        double[][] scaleMatrix = createScaleMatrix(scale);
        
        localTransform = multiplyMatrices(translation, multiplyMatrices(rotationMatrix, scaleMatrix));
        transformDirty = false;
    }
    
    /**
     * Update world transformation matrix.
     */
    private void updateWorldTransform() {
        if (transformDirty || localTransform == null) {
            updateLocalTransform();
        }
        
        if (parent == null) {
            worldTransform = cloneMatrix(localTransform);
        } else {
            worldTransform = multiplyMatrices(parent.getWorldTransform(), localTransform);
        }
    }
    
    /**
     * Mark this node and all children as having dirty transforms.
     */
    private void markTransformDirty() {
        transformDirty = true;
        for (SceneNode child : children) {
            child.markTransformDirty();
        }
    }
    
    /**
     * Get depth in scene hierarchy.
     */
    public int getDepth() {
        int depth = 0;
        SceneNode current = parent;
        while (current != null) {
            depth++;
            current = current.parent;
        }
        return depth;
    }
    
    /**
     * Check if this node is an ancestor of another node.
     */
    public boolean isAncestorOf(SceneNode node) {
        SceneNode current = node.parent;
        while (current != null) {
            if (current == this) {
                return true;
            }
            current = current.parent;
        }
        return false;
    }
    
    /**
     * Visit all nodes in subtree (depth-first).
     */
    public void visitSubtree(NodeVisitor visitor) {
        visitor.visit(this);
        for (SceneNode child : children) {
            child.visitSubtree(visitor);
        }
    }
    
    /**
     * Visit all nodes in subtree (breadth-first).
     */
    public void visitSubtreeBreadthFirst(NodeVisitor visitor) {
        Queue<SceneNode> queue = new LinkedList<>();
        queue.add(this);
        
        while (!queue.isEmpty()) {
            SceneNode current = queue.poll();
            visitor.visit(current);
            queue.addAll(current.children);
        }
    }
    
    /**
     * Create LLSD representation of this node.
     */
    public Map<String, Object> toLLSD() {
        Map<String, Object> nodeData = new HashMap<>();
        nodeData.put("NodeID", nodeId);
        nodeData.put("Name", name);
        nodeData.put("Position", Arrays.asList(position.x, position.y, position.z));
        nodeData.put("Rotation", Arrays.asList(rotation.x, rotation.y, rotation.z, rotation.w));
        nodeData.put("Scale", Arrays.asList(scale.x, scale.y, scale.z));
        nodeData.put("Visible", visible);
        nodeData.put("Enabled", enabled);
        nodeData.put("Properties", new HashMap<>(properties));
        
        List<Map<String, Object>> childrenData = new ArrayList<>();
        for (SceneNode child : children) {
            childrenData.add(child.toLLSD());
        }
        nodeData.put("Children", childrenData);
        
        return nodeData;
    }
    
    /**
     * Cleanup and unregister node.
     */
    public void dispose() {
        // Remove from parent
        removeFromParent();
        
        // Dispose all children
        for (SceneNode child : new ArrayList<>(children)) {
            child.dispose();
        }
        
        // Unregister
        nodeRegistry.remove(nodeId);
    }
    
    // Matrix utility methods
    private static double[][] createTranslationMatrix(Vector3 translation) {
        return new double[][]{
            {1, 0, 0, translation.x},
            {0, 1, 0, translation.y},
            {0, 0, 1, translation.z},
            {0, 0, 0, 1}
        };
    }
    
    private static double[][] createScaleMatrix(Vector3 scale) {
        return new double[][]{
            {scale.x, 0, 0, 0},
            {0, scale.y, 0, 0},
            {0, 0, scale.z, 0},
            {0, 0, 0, 1}
        };
    }
    
    private static double[][] create4x4Matrix(double[][] matrix3) {
        return new double[][]{
            {matrix3[0][0], matrix3[0][1], matrix3[0][2], 0},
            {matrix3[1][0], matrix3[1][1], matrix3[1][2], 0},
            {matrix3[2][0], matrix3[2][1], matrix3[2][2], 0},
            {0, 0, 0, 1}
        };
    }
    
    private static double[][] multiplyMatrices(double[][] a, double[][] b) {
        double[][] result = new double[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                result[i][j] = a[i][0] * b[0][j] + a[i][1] * b[1][j] + 
                               a[i][2] * b[2][j] + a[i][3] * b[3][j];
            }
        }
        return result;
    }
    
    private static Vector3 multiplyMatrixVector(double[][] matrix, Vector3 vector) {
        double x = matrix[0][0] * vector.x + matrix[0][1] * vector.y + 
                   matrix[0][2] * vector.z + matrix[0][3];
        double y = matrix[1][0] * vector.x + matrix[1][1] * vector.y + 
                   matrix[1][2] * vector.z + matrix[1][3];
        double z = matrix[2][0] * vector.x + matrix[2][1] * vector.y + 
                   matrix[2][2] * vector.z + matrix[2][3];
        return new Vector3(x, y, z);
    }
    
    private static double[][] cloneMatrix(double[][] matrix) {
        double[][] clone = new double[matrix.length][];
        for (int i = 0; i < matrix.length; i++) {
            clone[i] = matrix[i].clone();
        }
        return clone;
    }
    
    private static double[][] invertMatrix(double[][] matrix) {
        // Simple 4x4 matrix inversion (for transforms only)
        // This is a simplified version - full implementation would be more robust
        double[][] result = new double[4][4];
        
        // Extract translation
        Vector3 translation = new Vector3(-matrix[0][3], -matrix[1][3], -matrix[2][3]);
        
        // Transpose rotation part
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                result[i][j] = matrix[j][i];
            }
        }
        
        // Transform translation
        result[0][3] = result[0][0] * translation.x + result[0][1] * translation.y + result[0][2] * translation.z;
        result[1][3] = result[1][0] * translation.x + result[1][1] * translation.y + result[1][2] * translation.z;
        result[2][3] = result[2][0] * translation.x + result[2][1] * translation.y + result[2][2] * translation.z;
        result[3][3] = 1.0;
        
        return result;
    }
    
    /**
     * Get node from registry.
     */
    public static SceneNode getNode(UUID nodeId) {
        return nodeRegistry.get(nodeId);
    }
    
    /**
     * Get all registered nodes.
     */
    public static Collection<SceneNode> getAllNodes() {
        return new ArrayList<>(nodeRegistry.values());
    }
    
    /**
     * Clear node registry.
     */
    public static void clearRegistry() {
        nodeRegistry.clear();
    }
    
    /**
     * Node visitor interface.
     */
    public interface NodeVisitor {
        void visit(SceneNode node);
    }
    
    @Override
    public String toString() {
        return String.format("SceneNode[%s](%s) children=%d", 
                           name, nodeId.toString().substring(0, 8), children.size());
    }
}