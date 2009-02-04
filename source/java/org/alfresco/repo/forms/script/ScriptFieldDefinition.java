/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.forms.script;

import java.util.List;

import org.alfresco.repo.forms.FieldDefinition;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

/**
 * FieldDefinition JavaScript Object. This object acts as a wrapper for the Java object
 * {@link org.alfresco.repo.forms.FieldDefinition} and all of its subclasses also.
 * 
 * @author Neil McErlean
 */
public class ScriptFieldDefinition extends ScriptableObject
{
    private static final long serialVersionUID = 8013009739132852748L;
    /**
     * This is the Java FieldDefinition object that is being wrapped.
     */
    private FieldDefinition fieldDefinition;
    private JSPropertyExtractor propertyExtractor = new JSPropertyExtractor();
    
    /* default */ ScriptFieldDefinition(FieldDefinition fieldDefinition)
    {
        this.fieldDefinition = fieldDefinition;
    }

    /**
     * This method retrieves a named property value in the normal (Mozilla JS) way.
     * If the named property is not found, an attempt is made to discover a Java
     * accessor method appropriate to the named property e.g. getFoo() or isFoo() for
     * a property named 'foo'. If such an accessor method is found, it is invoked and
     * the value is returned. (If there are both a getFoo() and an isFoo() method, then
     * the getFoo() method is invoked.)
     * 
     * @param name the named property
     * @param start the object in which the lookup began
     * @return the property value if found, else NOT_FOUND.
     * 
     * @see org.mozilla.javascript.Scriptable#get(String, Scriptable)
     */
    @Override
    public Object get(String name, Scriptable start)
    {
        Object initialResult = super.get(name, start);

        if (initialResult != null && !initialResult.equals(NOT_FOUND))
        {
            return initialResult;
        }

        Object result = propertyExtractor.extractProperty(name, fieldDefinition);
        
        if (result == null)
        {
            return NOT_FOUND;
        }

        //TODO Value conversion.
        if (result instanceof List)
        {
            return ((List)result).toArray();
        }
        return result;
    }

    /**
     * @see org.mozilla.javascript.Scriptable#getClassName()
     */
    public String getClassName()
    {
        return this.getClass().getSimpleName();
    }

    /**
     * @see org.mozilla.javascript.Scriptable#has(String, Scriptable)
     */
    public boolean has(String name, Scriptable start)
    {
        if (super.has(name, start))
        {
            return true;
        }
        else
        {
            return propertyExtractor.propertyExists(name, this.fieldDefinition);
        }
    }
}
