/*
 * Code ported from flob - http://s373.net/code/flob/
 *
 * flob // flood-fill multi-blob detector
 *
 * (c) copyright 2008-2010 andré sier
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA  02111-1307  USA
 */


package org.praxislive.video.impl.components.blob;

public abstract class BaseBlob {

    public int id;
    public int pixelcount;
    public int boxminx, boxminy, boxmaxx, boxmaxy;
    public int boxcenterx, boxcentery;

    
}
