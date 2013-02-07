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

package net.neilcsmith.praxis.video.components.analysis.blobs;

public class quadBlob extends BaseBlob {

	public float cx, cy;
	public float quad[] = new float[8]; // 4*2

	quadBlob() {
	}

	quadBlob(quadBlob b) {
		quad[0] = b.quad[0];
		quad[1] = b.quad[1];
		quad[2] = b.quad[2];
		quad[3] = b.quad[3];
		quad[4] = b.quad[4];
		quad[5] = b.quad[5];
		quad[6] = b.quad[6];
		quad[7] = b.quad[7];
		cx = b.cx;
		cy = b.cy;
		id = b.id;
		pixelcount = b.pixelcount;
		boxminx = b.boxminx;
		boxminy = b.boxminy;
		boxmaxx = b.boxmaxx;
		boxmaxy = b.boxmaxy;
		boxcenterx = b.boxcenterx;
		boxcentery = b.boxcentery;
	}

}
