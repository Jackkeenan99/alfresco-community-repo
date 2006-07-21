/*
 * Copyright (C) 2006 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */

package org.alfresco.repo.avm;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.service.namespace.QName;

/**
 * Base class for all repository file system like objects.
 * @author britt
 */
public abstract class AVMNodeImpl implements AVMNode, Serializable
{
    /**
     * The Object ID.
     */
    private long fID;
    
    /**
     * The Version ID.
     */
    private int fVersionID;
    
    /**
     * The basic attributes of this.  Owner, creator, mod time, etc.
     */
    private BasicAttributes fBasicAttributes;
    
    /**
     * The version number (for concurrency control).
     */
    private long fVers;
    
    /**
     * The rootness of this node.
     */
    private boolean fIsRoot;
    
    /**
     * Default constructor.
     */
    protected AVMNodeImpl()
    {
    }

    /**
     * Constructor used when creating a new concrete subclass instance.
     * @param id The object id.
     * @param store The AVMStore that owns this.
     */
    protected AVMNodeImpl(long id,
                          AVMStore store)
    {
        fID = id;
        fVersionID = -1;
        fIsRoot = false;
        long time = System.currentTimeMillis();
        fBasicAttributes = new BasicAttributesImpl("britt",
                                                   "britt",
                                                   "britt",
                                                   time,
                                                   time,
                                                   time);
    }
    
    /**
     * Set the ancestor of this node.
     * @param ancestor The ancestor to set.
     */
    public void setAncestor(AVMNode ancestor)
    {
        if (ancestor == null)
        {
            return;
        }
        HistoryLinkImpl link = new HistoryLinkImpl();
        link.setAncestor(ancestor);
        link.setDescendent(this);
        AVMContext.fgInstance.fHistoryLinkDAO.save(link);
    }

    /**
     * Get the ancestor of this node.
     * @return The ancestor of this node.
     */
    public AVMNode getAncestor()
    {
        return AVMContext.fgInstance.fAVMNodeDAO.getAncestor(this);
    }
    
    /**
     * Set the node that was merged into this.
     * @param mergedFrom The node that was merged into this.
     */
    public void setMergedFrom(AVMNode mergedFrom)
    {
        if (mergedFrom == null)
        {
            return;
        }
        MergeLinkImpl link = new MergeLinkImpl();
        link.setMfrom(mergedFrom);
        link.setMto(this);
        AVMContext.fgInstance.fMergeLinkDAO.save(link);
    }
    
    /**
     * Get the node that was merged into this.
     * @return The node that was merged into this.
     */
    public AVMNode getMergedFrom()
    {
        return AVMContext.fgInstance.fAVMNodeDAO.getMergedFrom(this);
    }
    
    /**
     * Equality based on object ids.
     * @param obj The thing to compare against.
     * @return Equality.
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!(obj instanceof AVMNode))
        {
            return false;
        }
        return fID == ((AVMNode)obj).getId();
    }

    /**
     * Get a reasonable hash value.
     * @return The hash code.
     */
    @Override
    public int hashCode()
    {
        return (int)fID;
    }
    
    /**
     * Set the object id.  For Hibernate.
     * @param id The id to set.
     */
    protected void setId(long id)
    {
        fID = id;
    }
    
    /**
     * Get the id of this node.
     * @return The object id.
     */
    public long getId()
    {
        return fID;
    }
 
    /**
     * Set the versionID for this node.  
     * @param versionID The id to set.
     */
    public void setVersionID(int versionID)
    {
        fVersionID = versionID;
    }
    
    /**
     * Get the version id of this node.
     * @return The version id.
     */
    public int getVersionID()
    {
        return fVersionID;
    }
    
    /**
     * Set the basic attributes. For Hibernate.
     * @param attrs
     */
    protected void setBasicAttributes(BasicAttributes attrs)
    {
        fBasicAttributes = attrs;
    }
    
    /**
     * Get the basic attributes.
     * @return The basic attributes.
     */
    public BasicAttributes getBasicAttributes()
    {
        return fBasicAttributes;
    }
    
    /**
     * Get whether this is a new node.
     * @return Whether this is new.
     */
    public boolean getIsNew()
    {
        return AVMContext.fgInstance.fNewInAVMStoreDAO.getByNode(this) != null;
    }
 
    /**
     * Set the version for concurrency control
     * @param vers
     */
    protected void setVers(long vers)
    {
        fVers = vers;
    }
    
    /**
     * Get the version for concurrency control.
     * @return The version for optimistic locks.
     */
    protected long getVers()
    {
        return fVers;
    }

    /**
     * Get whether this is a root node.
     * @return Whether this is a root node.
     */
    public boolean getIsRoot()
    {
        return fIsRoot;
    }

    /**
     * @param isRoot
     */
    public void setIsRoot(boolean isRoot)
    {
        fIsRoot = isRoot;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMNode#updateModTime()
     */
    public void updateModTime()
    {
        fBasicAttributes.setModDate(System.currentTimeMillis());
    }
    
    /**
     * Copy all properties from another node.
     * @param other The other node.
     */
    protected void copyProperties(AVMNode other)
    {
        Map<QName, PropertyValue> properties = other.getProperties();
        for (QName name : properties.keySet())
        {
            AVMNodeProperty newProp = new AVMNodePropertyImpl();
            newProp.setNode(this);
            newProp.setName(name);
            newProp.setValue(properties.get(name));
            AVMContext.fgInstance.fAVMNodePropertyDAO.save(newProp);
        }
    }
    
    /**
     * Set a property on a node. Overwrite it if it exists.
     * @param name The name of the property.
     * @param value The value to set.
     */
    public void setProperty(QName name, PropertyValue value)
    {
        AVMNodeProperty prop = AVMContext.fgInstance.fAVMNodePropertyDAO.get(this, name);
        if (prop != null)
        {
            prop.setValue(value);
            AVMContext.fgInstance.fAVMNodePropertyDAO.update(prop);
            return;
        }
        prop = new AVMNodePropertyImpl();
        prop.setNode(this);
        prop.setName(name);
        prop.setValue(value);
        AVMContext.fgInstance.fAVMNodePropertyDAO.save(prop);
    }
    
    /**
     * Set a collection of properties on this node.
     * @param properties The Map of QNames to PropertyValues.
     */
    public void setProperties(Map<QName, PropertyValue> properties)
    {
        for (QName name : properties.keySet())
        {
            setProperty(name, properties.get(name));
        }
    }
    
    /**
     * Get a property by name.
     * @param name The name of the property.
     * @return The PropertyValue or null if non-existent.
     */
    public PropertyValue getProperty(QName name)
    {
        AVMNodeProperty prop = AVMContext.fgInstance.fAVMNodePropertyDAO.get(this, name);
        if (prop == null)
        {
            return null;
        }
        return prop.getValue();
    }
    
    /**
     * Get all the properties associated with this node.
     * @return A Map of QNames to PropertyValues.
     */
    public Map<QName, PropertyValue> getProperties()
    {
        Map<QName, PropertyValue> retVal = new HashMap<QName, PropertyValue>();
        List<AVMNodeProperty> props = AVMContext.fgInstance.fAVMNodePropertyDAO.get(this);
        for (AVMNodeProperty prop : props)
        {
            retVal.put(prop.getName(), prop.getValue());
        }
        return retVal;
    }
}
