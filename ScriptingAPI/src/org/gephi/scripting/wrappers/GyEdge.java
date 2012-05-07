/*
Copyright 2008-2012 Gephi
Authors : Luiz Ribeiro <luizribeiro@gmail.com>
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2012 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):

Portions Copyrighted 2011 Gephi Consortium.
 */
package org.gephi.scripting.wrappers;

import java.awt.Color;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.graph.api.Edge;
import org.gephi.scripting.util.GyNamespace;
import org.python.core.Py;
import org.python.core.PyFloat;
import org.python.core.PyInteger;
import org.python.core.PyObject;
import org.python.core.PyString;

/**
 * This class wraps an edge from the graph in a way that it is easier to be
 * handled from the scripting language.
 * 
 * <code>GyEdge</code> objects are only instantiated by the
 * <code>GyNamespace.getGyEdge</code> method, which is called every time the
 * user tries to access a variable whose name is reserved for edges on the
 * Gython's namespace.
 * 
 * This class overrides the default implementation of the
 * <code>__findattr_ex__</code> and <code>__setattr__</code> methods from
 * <code>PyObject</code> so that the user can access (read and write) the edges'
 * attributes in a seamless way.
 * 
 * @author Luiz Ribeiro
 */
public class GyEdge extends PyObject {

    /** The namespace in which this object is inserted */
    private GyNamespace namespace;
    /** The edge underlying on this wrapper */
    private Edge underlyingEdge;
    // Hack to get a few attributes into jythonconsole's auto-completion
    // TODO: get rid of this ugly hack (:
    public int color;
    public float weight;
    public String label;
    public boolean directed;
    public GyNode source;
    public GyNode target;

    /**
     * Constructor for the edge wrapper.
     * @param namespace     the namespace in which this object is inserted
     * @param edge          the edge object that will be wrapped
     */
    public GyEdge(GyNamespace namespace, Edge edge) {
        this.namespace = namespace;
        this.underlyingEdge = edge;
    }

    @Override
    public String toString() {
        return GyNamespace.EDGE_PREFIX + underlyingEdge.getEdgeData().getId();
    }

    /**
     * Retrieves the underlying edge object.
     * @return              the underlying edge object
     */
    public Edge getEdge() {
        return underlyingEdge;
    }

    @Override
    public void __setattr__(String name, PyObject value) {
        if (name.equals("color")) {
            Color color = (Color) value.__tojava__(Color.class);
            float red = color.getRed() / 255.0f;
            float green = color.getGreen() / 255.0f;
            float blue = color.getBlue() / 255.0f;
            underlyingEdge.getEdgeData().setColor(red, green, blue);
        } else if (name.equals("weight")) {
            float size = (Float) value.__tojava__(Float.class);
            underlyingEdge.getEdgeData().getAttributes().setValue("Weight", size);
        } else if (name.equals("label")) {
            underlyingEdge.getEdgeData().setLabel(value.toString());
        } else if (name.equals("directed")) {
            readonlyAttributeError(name);
        } else if (name.equals("source")) {
            readonlyAttributeError(name);
        } else if (name.equals("target")) {
            readonlyAttributeError(name);
        } else if (!name.startsWith("__")) {
            Object obj = null;

            // TODO: support conversions for other object types
            if (value instanceof PyString) {
                obj = (String) value.__tojava__(String.class);
            } else if (value instanceof PyInteger) {
                obj = (Integer) value.__tojava__(Integer.class);
            } else if (value instanceof PyFloat) {
                obj = (Float) value.__tojava__(Float.class);
            }

            if (obj == null) {
                throw Py.AttributeError("Unsupported edge attribute type '" + value.getType().getName() + "'");
            }

            underlyingEdge.getEdgeData().getAttributes().setValue(name, obj);
        } else {
            super.__setattr__(name, value);
        }
    }

    @Override
    public PyObject __findattr_ex__(String name) {
        if (name.equals("color")) {
            int red = (int) Math.round(underlyingEdge.getEdgeData().r() * 255.0f);
            int green = (int) Math.round(underlyingEdge.getEdgeData().g() * 255.0f);
            int blue = (int) Math.round(underlyingEdge.getEdgeData().b() * 255.0f);
            return Py.java2py(new Color(red, green, blue));
        } else if (name.equals("weight")) {
            float weight = (Float) underlyingEdge.getEdgeData().getAttributes().getValue("Weight");
            return Py.java2py(weight);
        } else if (name.equals("label")) {
            return Py.java2py(underlyingEdge.getEdgeData().getLabel());
        } else if (name.equals("directed")) {
            return Py.java2py(underlyingEdge.isDirected());
        } else if (name.equals("source")) {
            return namespace.getGyNode(underlyingEdge.getSource().getId());
        } else if (name.equals("target")) {
            return namespace.getGyNode(underlyingEdge.getTarget().getId());
        } else {
            AttributeModel attributeModel = namespace.getWorkspace().getLookup().lookup(AttributeModel.class);
            if (attributeModel.getEdgeTable().hasColumn(name)) {
                Object obj = underlyingEdge.getEdgeData().getAttributes().getValue(name);
                if (obj == null) {
                    return Py.None;
                }
                return Py.java2py(obj);
            }

            return super.__findattr_ex__(name);
        }
    }
}
