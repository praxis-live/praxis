package net.neilcsmith.praxis.video.components.analysis.blobs;

/**
 * 
 * base data struct holds info and normalized info for a simple blob
 * 
 * 
 */

class ABlob extends BaseBlob {
	// public int id;
	// public int pixelcount;
	// public int boxminx,boxminy,boxmaxx,boxmaxy;
	// public int boxcenterx,boxcentery;
	public int boxdimx, boxdimy;
	public int pboxcenterx, pboxcentery;
	// normalized values
	public float cx, cy;
	// float pcx,pcy;
	public float dimx, dimy;

	// /features:
	public float armleftx, armlefty, armrightx, armrighty, headx, heady,
			bottomx, bottomy, footleftx, footlefty, footrightx, footrighty;

	// public float quad[] = new float[8]; //4*2

	// / constructor
	public ABlob() {
	}

	public ABlob(ABlob b) {
		id = b.id;
		pixelcount = b.pixelcount;
		boxminx = b.boxminx;
		boxminy = b.boxminy;
		boxmaxx = b.boxmaxx;
		boxmaxy = b.boxmaxy;
		boxcenterx = b.boxcenterx;
		boxcentery = b.boxcentery;
		boxdimx = b.boxdimx;
		boxdimy = b.boxdimy;
		pboxcenterx = b.pboxcenterx;
		pboxcentery = b.pboxcentery;
		cx = b.cx;
		cy = b.cy;
		// pcx = b.pcx; pcy = b.pcy;
		dimx = b.dimx;
		dimy = b.dimy;

		armleftx = b.armleftx;
		armlefty = b.armlefty;
		armrightx = b.armrightx;
		armrighty = b.armrighty;
		headx = b.headx;
		heady = b.heady;
		bottomx = b.bottomx;
		bottomy = b.bottomy;
		heady = b.heady;
		footleftx = b.footleftx;
		footlefty = b.footlefty;
		footrightx = b.footrightx;
		footrighty = b.footrighty;

	}

    
}
