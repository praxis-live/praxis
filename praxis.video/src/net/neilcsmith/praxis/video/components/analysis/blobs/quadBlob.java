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
