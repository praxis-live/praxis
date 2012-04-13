package net.neilcsmith.praxis.video.components.analysis.blobs;


/**
* 
* TrackedBlob extends ABlob. has more internal variables like vel, id, birthtime,...
*
* 
*/

class TrackedBlob extends ABlob implements Comparable<TrackedBlob> {
	public boolean newblob=false;
	public long birthtime;
	public long lifetime=-1;//60;//5*60;//300frames
	public int presencetime;
	public boolean linked=false;

	public int pposx,pposy;
	public float pcx,pcy;

	public float velx,vely;
	public float prevelx,prevely;
	public float maxdist2=2555f;//1000;//~31px//100;
	public float rad,rad2;

	TrackedBlob(TrackedBlob b){

		newblob = b.newblob;
		birthtime = b.birthtime;
		presencetime = b.presencetime;

		pposx= b.pposx; pposy=b.pposy;
		pcx = b.pcx; pcy = b.pcy;

		velx = b.velx; vely = b.vely;
		prevelx = b.prevelx; prevely = b.prevely;
		maxdist2=b.maxdist2;
		rad=b.rad;
		rad2=b.rad2;
		linked=b.linked;
		
		armleftx = b.armleftx;
		armlefty = b.armlefty;
		armrightx = b.armrightx;
		armrighty = b.armrighty;
		headx = b.headx;
		heady = b.heady;
		bottomx = b.bottomx;
		bottomy = b.bottomy;
		footleftx = b.footleftx;
		footlefty = b.footlefty;
		footrightx = b.footrightx;
		footrighty = b.footrighty;

	}	
	TrackedBlob(ABlob a,TrackedBlob b){	

		//super(a);
		id = b.id;
		pixelcount = b.pixelcount;
		boxminx = b.boxminx;    boxminy = b.boxminy;    boxmaxx = b.boxmaxx;    boxmaxy = b.boxmaxy;
		boxcenterx = b.boxcenterx;    boxcentery = b.boxcentery;
		boxdimx = b.boxdimx;    boxdimy = b.boxdimy;
		pboxcenterx = b.pboxcenterx;    pboxcentery = b.pboxcentery;
		cx = b.cx; cy = b.cy;
		//   pcx = b.pcx; pcy = b.pcy;
		dimx = b.dimx; dimy = b.dimy;

		//this(b);

		newblob = b.newblob;
		birthtime = b.birthtime;
		presencetime = b.presencetime;

		pposx= b.pposx; pposy=b.pposy;
		pcx = b.pcx; pcy = b.pcy;

		velx = b.velx; vely = b.vely;
		prevelx = b.prevelx; prevely = b.prevely;
		maxdist2=b.maxdist2;
		rad=b.rad;
		rad2=b.rad2;
		linked=b.linked;
		
		
		armleftx = a.armleftx;
		armlefty = a.armlefty;
		armrightx = a.armrightx;
		armrighty = a.armrighty;
		headx = a.headx;
		heady = a.heady;
		bottomx = a.bottomx;
		bottomy = a.bottomy;
		footleftx = a.footleftx;
		footlefty = a.footlefty;
		footrightx = a.footrightx;
		footrighty = a.footrighty;
		

	}

	TrackedBlob(ABlob b){	
		super(b); 
		armleftx = b.armleftx;
		armlefty = b.armlefty;
		armrightx = b.armrightx;
		armrighty = b.armrighty;
		headx = b.headx;
		heady = b.heady;
		bottomx = b.bottomx;
		bottomy = b.bottomy;
		footleftx = b.footleftx;
		footlefty = b.footlefty;
		footrightx = b.footrightx;
		footrighty = b.footrighty;
		
		calcrad();
	}

	/*	TrackedBlob(TrackedBlob b, boolean _newblob){	
		super((ABlob) b); 
		newblob = _newblob;
		if(newblob){
			birthtime=System.currentTimeMillis();
			presencetime=0;
		}

	}
	 */
	void calcrad(){
		rad = (boxdimx<boxdimy)?boxdimx/2f:boxdimy/2f;
		rad2 = rad*rad;
	}

    public int compareTo(TrackedBlob t) {
        return t.pixelcount - pixelcount;
    }
}



