/**
 * SNE-XACML: A high performance XACML evaluation engine.
 *
 * Copyright (C) 2013-2014 Canh Ngo <canhnt@gmail.com>
 * System and Network Engineering Group, University of Amsterdam.
 * All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301 USA
 */
/**
 * System and Network Engineering Group
 * University of Amsterdam
 *
 */
package nl.uva.sne.midd.nodes;

import java.io.OutputStream;

import nl.uva.sne.midd.MIDDException;

public abstract class AbstractNode {

    private int id;

    public AbstractNode(int id) {
        this.id = id;
    }

    /**
     * Copy constructor.
     *
     * @param n
     */
    public AbstractNode(AbstractNode n){
        id = n.id;
    }

    public int getID() {
        return id;
    }

    public abstract Class<?> getType();

    public abstract void print(OutputStream os) throws MIDDException;

}
