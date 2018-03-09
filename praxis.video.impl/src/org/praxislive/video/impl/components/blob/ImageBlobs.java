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

import java.util.ArrayList;
import java.util.Collections;
import org.praxislive.video.render.PixelData;

/**
 * 
 * core internal class which handles all tracking code
 * 
 */
class ImageBlobs {

    public int idnumbers = 0;
    public int numblobs, prevnumblobs;
    public int trackednumblobs, prevtrackednumblobs;
    public int lifetime = 100;// 1000;
    public int ninpix = 100;
    public int maxpix = Integer.MAX_VALUE;// 250*250;//10000;
    public boolean[] imagemap = null;
    public boolean imagemaplit = false;
    public int w, h;// , w2, h2;
    public float wr, hr;
    public float wcoordsx, wcoordsy, w2, h2;
    public int worldw = 700, worldh = 700;
    // public boolean coordsmode;
    public int numpix;
    public float lp1 = 0.05f;
    public float lp2 = 1.0f - lp1;
    public float trackingmindist = 500; // ~22pix (squared)
    public ArrayList<ABlob> theblobs = null;
    public ArrayList<ABlob> prevblobs = null;
    public ArrayList<TrackedBlob> trackedblobs = null;
    public ArrayList<TrackedBlob> prevtrackedblobs = null;
    public ArrayList<TrackedBlob> tbsimplelist = null;
    public ArrayList<quadBlob> quadblobslist = null;
    public ArrayList<Pt2> thecoords = null;
//	private Flob tflob;

    ImageBlobs() { // /constructor
        trackedblobs = new ArrayList<TrackedBlob>();
        prevtrackedblobs = new ArrayList<TrackedBlob>();
        theblobs = new ArrayList<ABlob>();
        prevblobs = new ArrayList<ABlob>();
        thecoords = new ArrayList<Pt2>();
        tbsimplelist = new ArrayList<TrackedBlob>();
        quadblobslist = new ArrayList<quadBlob>();
        numblobs = prevnumblobs = 0;
        trackednumblobs = prevtrackednumblobs = 0;
    }

//	ImageBlobs(int w, int h, int ww, int wh) { // /constructor
//		trackedblobs = new ArrayList<trackedBlob>();
//		prevtrackedblobs = new ArrayList<trackedBlob>();
//		theblobs = new ArrayList<ABlob>();
//		prevblobs = new ArrayList<ABlob>();
//		thecoords = new ArrayList<pt2>();
//		tbsimplelist = new ArrayList<trackedBlob>();
//		quadblobslist = new ArrayList<quadBlob>();
//		numblobs = prevnumblobs = 0;
//		trackednumblobs = prevtrackednumblobs = 0;
//		calcdims(w, h, ww, wh);
//		worldw = ww;
//		worldh = wh;
//	}
    /**
     * default constructor takes a flob instance<br>
     * to access main flob class<br>
     * 
     * @param flob
     */
//	ImageBlobs(Flob flob) { // /constructor
//		tflob = flob;
//		trackedblobs = new ArrayList<trackedBlob>();
//		prevtrackedblobs = new ArrayList<trackedBlob>();
//		theblobs = new ArrayList<ABlob>();
//		prevblobs = new ArrayList<ABlob>();
//		thecoords = new ArrayList<pt2>();
//		tbsimplelist = new ArrayList<trackedBlob>();
//		quadblobslist = new ArrayList<quadBlob>();
//		numblobs = prevnumblobs = 0;
//		trackednumblobs = prevtrackednumblobs = 0;
//		calcdims(tflob.videoresw, tflob.videoresh, tflob.worldwidth,
//				tflob.worldheight);
//		// coordsmode = tflob.coordsmode;
//	}
    void calcdims(int w, int h, int ww, int wh) {
        this.w = w;
        this.h = h;
        // w2 = w / 2;
        // h2 = h / 2;

        wr = 1.0f / w;
        hr = 1.0f / h;
        numpix = w * h;
        worldw = ww;
        worldh = wh;
        wcoordsx = worldw * wr;
        wcoordsy = worldh * hr;
        w2 = worldw / 2;
        h2 = worldh / 2;
    }

    void setninpix(int nin) {
        ninpix = nin;
    }

    void setmaxpix(int max) {
        maxpix = max;
    }

    void setSmoothib(float f) {
        lp1 = f;
        lp2 = 1.0f - lp1;
    }

    ArrayList<ABlob> getblobsAL() {
        return theblobs;
    }

    void query() {
        System.out.print("query blobdata\n");
        System.out.print("numblobs " + numblobs + "\n");
        for (int i = 0; i < theblobs.size(); i++) {
            ABlob b = theblobs.get(i);
            System.out.print("blob" + b.id + " pix" + b.pixelcount + " coords "
                    + b.boxminx + " " + b.boxminy + " " + b.boxmaxx + " "
                    + b.boxmaxy + "  center " + b.boxcenterx + " "
                    + b.boxcentery + "\n");
        }
    }

    void calc(PixelData pd) {
        int width = pd.getWidth();
        int height = pd.getHeight();
        int offset = pd.getOffset();
        int scanline = pd.getScanline();
        int threshold = 127;
        if (width != this.w || height != this.h) {
            calcdims(width, height, width, height);
        }
        int min0 = 10000;
        int max0 = -100;
        Pt2 p = new Pt2();
        Pt2 p2 = new Pt2();
        int pixelcount = 0;
        ABlob b = new ABlob();
        copy_blobs_to_previousblobs();
        thecoords.clear();

        imagemap = new boolean[numpix];
        int[] pixels = pd.getData();
        for (int j = 0; j < height; j++) {
            int rowLoc = (j * scanline) + offset;
            for (int i = 0; i < width; i++) {
                if (((pixels[rowLoc + i]) & 0xFF) > threshold) {
                    if (i < min0) {
                        min0 = i;
                    } else if (i > max0) {
                        max0 = i;
                    }
                    if (imagemap[(j * width) + i] == false) {
                        p.x = i;
                        p.y = j;
                        thecoords.add(p);
                        pixelcount = 0;
                        b.boxminx = i;
                        b.boxmaxx = i;
                        b.boxminy = j;
                        b.boxmaxy = j;
                        while (!thecoords.isEmpty()) {
                            p2 = thecoords.remove(0);
                            if ((p2.x >= 0) && (p2.x < width)
                                    && (p2.y >= 0) && (p2.y < height)) {
                                if (imagemap[(p2.y * width) + p2.x] == false) {
                                    int pixval2 = (pixels[(p2.y
                                            * scanline) + p2.x + offset]) & 0xFF;
                                    if (pixval2 > threshold) {
                                        imagemap[p2.y * width + p2.x] = true;
                                        pixelcount++;
                                        p = new Pt2(p2.x, p2.y + 1);
                                        thecoords.add(p);
                                        p = new Pt2(p2.x, p2.y - 1);
                                        thecoords.add(p);
                                        p = new Pt2(p2.x + 1, p2.y);
                                        thecoords.add(p);
                                        p = new Pt2(p2.x - 1, p2.y);
                                        thecoords.add(p);
                                        if (p2.x < b.boxminx) {
                                            b.boxminx = p2.x;
                                        }
                                        if (p2.x > b.boxmaxx) {
                                            b.boxmaxx = p2.x;
                                        }
                                        if (p2.y < b.boxminy) {
                                            b.boxminy = p2.y;
                                        }
                                        if (p2.y > b.boxmaxy) {
                                            b.boxmaxy = p2.y;
                                        }
                                    }
                                }
                            }
                        }

                        if (pixelcount >= ninpix && pixelcount <= maxpix) {
                            b.id = numblobs;
                            b.pixelcount = pixelcount;
                            b.boxcenterx = (int) ((b.boxminx + b.boxmaxx) * 0.5);
                            b.boxcentery = (int) ((b.boxminy + b.boxmaxy) * 0.5);
                            b.boxdimx = b.boxmaxx - b.boxminx;
                            b.boxdimy = b.boxmaxy - b.boxminy;
                            b.cx = b.boxcenterx * wcoordsx;
                            b.cy = b.boxcentery * wcoordsy;
                            b.dimx = ((b.boxmaxx - b.boxminx) * wcoordsx);
                            b.dimy = ((b.boxmaxy - b.boxminy) * wcoordsy);
//							if (tflob.getAnyFeatureActive()) {
//								if (tflob.trackfeatures[0])
//									b = calc_feature_head(b);
//								if (tflob.trackfeatures[1]) {
//									b = calc_feature_arms(b);
//								}
//								if (tflob.trackfeatures[2])
//									b = calc_feature_feet(b);
//								if (tflob.trackfeatures[3])
//									b = calc_feature_bottom(b);
//							}

                            ABlob blob = new ABlob(b);
                            theblobs.add(blob);
                            numblobs++;
                        }
                    }
                }
            }
        }
    }

//	void calcQuad(PImage pimage) {
//		int min0 = 10000;
//		int max0 = -100;
//		Pt2 p = new Pt2();
//		Pt2 p2 = new Pt2();
//		int pixelcount = 0;
//		quadBlob b = new quadBlob();
//
//		quadblobslist.clear();
//		numblobs = 0;
//		imagemap = new boolean[numpix];
//
//		pimage.loadPixels();
//		for (int j = 0; j < pimage.height; j++) {
//			int rowLoc = j * pimage.width;
//			for (int i = 0; i < pimage.width; i++) {
//				if (((pimage.pixels[rowLoc + i]) & 0xFF) > 0) {
//					if (i < min0)
//						min0 = i;
//					else if (i > max0)
//						max0 = i;
//					if (imagemap[rowLoc + i] == false) {
//						p.x = i;
//						p.y = j;
//						thecoords.add(p);
//						pixelcount = 0;
//						b.boxminx = i;
//						b.boxmaxx = i;
//						b.boxminy = j;
//						b.boxmaxy = j;
//						while (!thecoords.isEmpty()) {
//							p2 = thecoords.remove(0);
//							if ((p2.x >= 0) && (p2.x < pimage.width)
//									&& (p2.y >= 0) && (p2.y < pimage.height)) {
//								if (imagemap[p2.y * pimage.width + p2.x] == false) {
//									int pixval2 = (pimage.pixels[p2.y
//											* pimage.width + p2.x]) & 0xFF;
//									if (pixval2 > 0) {
//										imagemap[p2.y * pimage.width + p2.x] = true;
//										pixelcount++;
//										p = new Pt2(p2.x, p2.y + 1);
//										thecoords.add(p);
//										p = new Pt2(p2.x, p2.y - 1);
//										thecoords.add(p);
//										p = new Pt2(p2.x + 1, p2.y);
//										thecoords.add(p);
//										p = new Pt2(p2.x - 1, p2.y);
//										thecoords.add(p);
//										if (p2.x < b.boxminx)
//											b.boxminx = p2.x;
//										if (p2.x > b.boxmaxx)
//											b.boxmaxx = p2.x;
//										if (p2.y < b.boxminy)
//											b.boxminy = p2.y;
//										if (p2.y > b.boxmaxy)
//											b.boxmaxy = p2.y;
//									}
//								}
//							}
//						}
//						if (pixelcount >= ninpix && pixelcount <= maxpix) {
//							b.id = numblobs;
//							b.pixelcount = pixelcount;
//							b.boxcenterx = (int) ((b.boxminx + b.boxmaxx) * 0.5);
//							b.boxcentery = (int) ((b.boxminy + b.boxmaxy) * 0.5);
//							b.cx = b.boxcenterx * wcoordsx;
//							b.cy = b.boxcentery * wcoordsy;
//							b = calc_quad(b);
//							quadBlob blob = new quadBlob(b);
//							quadblobslist.add(blob);
//							numblobs++;
//						}
//					}
//				}
//			}
//		}
//	}
    boolean testimagemap(int x, int y) {

        // boolean px = false;
        // try {
        // px = imagemap[y * w + x];
        // } catch (Exception e) {
        // System.out.print("error testimagemap " + x + " " + y + "\n" + e
        // + "\n");
        // }
        return imagemap[y * w + x];

    }

    /**
     * ABlob calc_feature_arms(ABlob b)<br>
     * <br>
     * calculates where the left and right arm are in a blob and store the
     * values in the blob to be accessed after tracking
     * 
     * @Param ABlob b
     * @return ABlob b
     * 
     */
    ABlob calc_feature_arms(ABlob b) {
        //
        int bx = b.boxminx;
        int by = b.boxminy;
        int ex = b.boxmaxx;
        int ey = b.boxmaxy;
        // int ey = b.boxcentery;

        int cx = b.boxcenterx;

        int i = 0, j = 0;

        boolean found = false;
        // armleft
        i = bx;
        for (j = by; j < ey; j++) {
            if (testimagemap(i, j)) {
                b.armleftx = i * wcoordsx;
                b.armlefty = j * wcoordsy;
                found = true;
                break;
            }
        }
        if (!found) {
            j = by;
            for (i = bx; i < cx; i++) {
                if (testimagemap(i, j)) {
                    b.armleftx = i * wcoordsx;
                    b.armlefty = j * wcoordsy;
                    found = true;
                    break;
                }
            }
            if (!found) {
                b.armleftx = b.boxcenterx * wcoordsx;
                b.armlefty = b.boxcentery * wcoordsy;
            }
        }

        found = false;
        // armright
        i = ex;
        for (j = by; j < ey; j++) {
            if (testimagemap(i, j)) {
                b.armrightx = i * wcoordsx;
                b.armrighty = j * wcoordsy;
                found = true;
                break;
            }
        }
        // armright try upper quad
        if (!found) {
            j = by;
            for (i = ex - 1; i > cx; i++) {
                if (testimagemap(i, j)) {
                    b.armrightx = i * wcoordsx;
                    b.armrighty = j * wcoordsy;
                    found = true;
                    break;
                }
            }
            if (!found) {
                b.armrightx = b.boxcenterx * wcoordsx;
                b.armrighty = b.boxcentery * wcoordsy;
            }
        }
        return b;
    }

    /**
     * ABlob calc_feature_head(ABlob b)<br>
     * <br>
     * calculates where the top center point is in a blob.<br>
     * 
     * @Param ABlob b
     * @return ABlob b
     * 
     */
    ABlob calc_feature_head(ABlob b) {
        // int bx = b.boxminx;
        int by = b.boxminy;
        int ex = b.boxmaxx;
        // int ey = b.boxmaxy;
        int cx = b.boxcenterx;
        int i = 0, j = 0;
        int k = cx - 1;
        // head
        j = by;
        for (i = cx; i < ex; i++) {

            if (testimagemap(i, j)) {
                b.headx = i * wcoordsx;
                b.heady = j * wcoordsy;
                break;
            }
            if (testimagemap(k--, j)) {
                b.headx = i * wcoordsx;
                b.heady = j * wcoordsy;
                break;
            }
        }

        // //head
        // j = by;
        // for( i=bx; i< ex; i++){
        // if (testimagemap(i,j)){
        // b.headx = (float)i*wcoordsx;
        // b.heady = (float)j*wcoordsy;
        // break;
        // }
        // }

        return b;
    }

    /**
     * ABlob calc_feature_feet(ABlob b)<br>
     * <br>
     * calculates where the left and right bottom points are in a blob.<br>
     * 
     * @Param ABlob b
     * @return ABlob b
     * 
     */
//	ABlob calc_feature_feet(ABlob b) {
//		// /passed to 2 feet instead of one bottom
//		int bx = PApplet.constrain(b.boxminx, 0, w - 1);
//		// int by = PApplet.constrain(b.boxminy,0,h-1);
//		int ex = PApplet.constrain(b.boxmaxx, 0, w - 1);
//		int ey = PApplet.constrain(b.boxmaxy, 0, h - 1);
//
//		int cx = b.boxcenterx;// (bx+ex)/2;///b.boxdimx/2 + bx;
//		int cy = b.boxcentery;// (by+ey)/2;//b.boxdimy/2 + by;
//
//		cx = PApplet.constrain(cx, 0, w - 1);
//		cy = PApplet.constrain(cy, 0, h - 1);
//
//		int i = 0, j = 0;
//
//		// footleft
//		j = ey;
//		for (i = bx; i < cx; i++) {
//			if (testimagemap(i, j)) {
//				b.footleftx = i * wcoordsx;
//				b.footlefty = j * wcoordsx;
//				// System.out.print("found armleft at "+b.armleftx+" "+b.armlefty
//				// );
//				break;
//
//			}
//		}
//		// footright
//		j = ey;
//		for (i = ex - 1; i > cx; i--) {
//			if (testimagemap(i, j)) {
//				b.footrightx = i * wcoordsx;
//				b.footrighty = j * wcoordsy;
//				break;
//			}
//		}
//
//		return b;
//	}
    /**
     * ABlob calc_feature_bottom(ABlob b)<br>
     * <br>
     * calculates where the bottom center point is in a blob.<br>
     * 
     * @Param ABlob b
     * @return ABlob b
     * 
     */
    ABlob calc_feature_bottom(ABlob b) {
        int ex = b.boxmaxx;
        int ey = b.boxmaxy;

        int cx = b.boxcenterx;// (bx+ex)/2;///b.boxdimx/2 + bx;
        // int cy = b.boxcentery;//(by+ey)/2;//b.boxdimy/2 + by;

        // cx = PApplet.constrain(cx ,0,w-1);
        // cy = PApplet.constrain(cy ,0,h-1);

        int i = 0, j = 0;
        // int dir=1;

        // bottom
        boolean found = false;
        j = ey;
        for (i = cx; i < ex; i++) {
            if (testimagemap(i, j)) {
                b.bottomx = i * wcoordsx;
                b.bottomy = j * wcoordsy;
                found = true;
                break;

            }
        }

        if (!found) {
            for (i = 0; i <= cx; i++) {
                if (testimagemap(i, j)) {
                    b.bottomx = i * wcoordsx;
                    b.bottomy = j * wcoordsy;
                    found = true;
                    break;

                }
            }
        }

        if (!found) {
            b.bottomx = b.boxcenterx * wcoordsx;
            b.bottomy = ey * wcoordsy;

        }

        return b;
    }

//	quadBlob calc_quad(quadBlob b) {
//		int bx = b.boxminx;
//		int by = b.boxminy;
//		int ex = b.boxmaxx;
//		int ey = b.boxmaxy;
//		int cx = b.boxcenterx;
//		int i = 0, j = 0;
//		boolean found = false;
//		i = bx;
//		for (j = by; j < ey; j++) {
//			if (testimagemap(i, j)) {
//				b.quad[0] = i * wcoordsx;
//				b.quad[1] = j * wcoordsy;
//				found = true;
//				break;
//			}
//		}
//		// armleft try upper quad
//		if (!found) {
//			j = by;
//			for (i = bx; i < cx; i++) {
//				if (testimagemap(i, j)) {
//					b.quad[0] = i * wcoordsx;
//					b.quad[1] = j * wcoordsy;
//					found = true;
//					break;
//				}
//			}
//			if (!found) {
//				b.quad[0] = b.boxcenterx * wcoordsx;
//				b.quad[1] = b.boxcentery * wcoordsy;
//			}
//		}
//
//		found = false;
//		// armright
//		i = ex;
//		for (j = by; j < ey; j++) {
//			if (testimagemap(i, j)) {
//				b.quad[2] = i * wcoordsx;
//				b.quad[3] = j * wcoordsy;
//				found = true;
//				break;
//			}
//		}
//
//		// armright try upper quad
//		if (!found) {
//			j = by;
//			for (i = ex - 1; i > cx; i++) {
//				if (testimagemap(i, j)) {
//					b.quad[2] = i * wcoordsx;
//					b.quad[3] = j * wcoordsy;
//					found = true;
//					break;
//				}
//			}
//
//			if (!found) {
//				b.quad[2] = b.boxcenterx * wcoordsx;
//				b.quad[3] = b.boxcentery * wcoordsy;
//			}
//
//		}
//
//		// feet
//
//		bx = PApplet.constrain(b.boxminx, 0, w - 1);
//		ex = PApplet.constrain(b.boxmaxx, 0, w - 1);
//		ey = PApplet.constrain(b.boxmaxy, 0, h - 1);
//
//		cx = b.boxcenterx;
//
//		cx = PApplet.constrain(cx, 0, w - 1);
//
//		// footleft
//		j = ey;
//		for (i = bx; i < cx; i++) {
//			if (testimagemap(i, j)) {
//				b.quad[4] = i * wcoordsx;
//				b.quad[5] = j * wcoordsx;
//				break;
//
//			}
//		}
//		// footright
//		j = ey;
//		for (i = ex - 1; i > cx; i--) {
//			if (testimagemap(i, j)) {
//				b.quad[6] = i * wcoordsx;
//				b.quad[7] = j * wcoordsy;
//				break;
//			}
//		}
//		return b;
//	}
    void copy_blobs_to_previousblobs() {
        prevnumblobs = numblobs;
        numblobs = 0; // reset count per frame at begin
        prevblobs.clear();// = new ArrayList<ABlob>();
        for (int i = 0; i < theblobs.size(); i++) {
            prevblobs.add(theblobs.get(i));
        }
        theblobs.clear();// = new ArrayList<ABlob>();
    }

    /**
     * public ArrayList<trackedBlob> calcsimpleAL()<br>
     * <br>
     * calc simple tries to calc blob velocities in simple ways<br>
     * 
     * @Param void
     * @return ArrayList<trackedBlob>
     * 
     */
    public ArrayList<TrackedBlob> calcsimpleAL() {

        trackedblobs.clear();// = new ArrayList<trackedBlob>();
        TrackedBlob b1, b2;
        ABlob ab;

        for (int i = 0; i < theblobs.size(); i++) {
            ab = theblobs.get(i);
            b1 = new TrackedBlob(ab);
            b2 = (i >= prevblobs.size()) ? null : new TrackedBlob(
                    prevblobs.get(i));
            if (b2 != null) {

                b1.id = b2.id; // b2maintains id!
                b1.presencetime = b2.presencetime + 1;
                b1.prevelx = b2.velx;
                b1.prevely = b2.vely;
                b1.pboxcenterx = b2.boxcenterx;
                b1.pboxcentery = b2.boxcentery;

                b1.armleftx = ab.armleftx;
                b1.armlefty = ab.armlefty;
                b1.armrightx = ab.armrightx;
                b1.armrighty = ab.armrighty;
                b1.headx = ab.headx;
                b1.heady = ab.heady;
                b1.bottomx = ab.bottomx;
                b1.bottomy = ab.bottomy;
                b1.footleftx = ab.footleftx;
                b1.footlefty = ab.footlefty;
                b1.footrightx = ab.footrightx;
                b1.footrighty = ab.footrighty;

            } else {
                b1.id = idnumbers++;
                b1.pboxcenterx = ab.boxcenterx;
                b1.pboxcentery = ab.boxcentery;
                b1.prevelx = 0.f;
                b1.prevely = 0.f;

                b1.armleftx = ab.armleftx;
                b1.armlefty = ab.armlefty;
                b1.armrightx = ab.armrightx;
                b1.armrighty = ab.armrighty;
                b1.headx = ab.headx;
                b1.heady = ab.heady;
                b1.bottomx = ab.bottomx;
                b1.bottomy = ab.bottomy;
                b1.footleftx = ab.footleftx;
                b1.footlefty = ab.footlefty;
                b1.footrightx = ab.footrightx;
                b1.footrighty = ab.footrighty;

            }

            b1.cx = ab.cx;// already *worldcoords
            b1.cy = ab.cy;
            b1.boxcenterx = ab.boxcenterx;
            b1.boxcentery = ab.boxcentery;

            b1.velx += lp2 * b1.velx + lp1 * (b1.boxcenterx - b1.pboxcenterx)
                    * wr;// vx;//b.cx - b.pcx;
            b1.vely = lp2 * b1.vely + lp1 * (b1.boxcentery - b1.pboxcentery)
                    * hr;// vy;//b.cy - b.pcy;

            // // old smooth
            // b1.velx = lp2 * b1.velx + lp1 * (b1.boxcenterx - b1.pboxcenterx)
            // * wr;// vx;//b.cx - b.pcx;
            // b1.vely = lp2 * b1.vely + lp1 * (b1.boxcentery - b1.pboxcentery)
            // * hr;// vy;//b.cy - b.pcy;

            // b1.velx = lp2*b1.prevelx + lp1*(b1.cx - b1.pcx);//vx;//b.cx -
            // b.pcx;
            // b1.vely = lp2*b1.prevely + lp1*(b1.cy - b1.pcy);//vy;//b.cy -
            // b.pcy;
            b1.boxminx = ab.boxminx;
            b1.boxmaxx = ab.boxmaxx;
            b1.boxminy = ab.boxminy;
            b1.boxmaxy = ab.boxmaxy;
            b1.boxdimx = ab.boxdimx;
            b1.boxdimy = ab.boxdimy;

            b1.dimx = ab.dimx;
            b1.dimy = ab.dimy;
            b1.rad = (ab.boxdimx < ab.boxdimy) ? ab.boxdimx / 2f
                    : ab.boxdimy / 2f;
            b1.rad2 = b1.rad * b1.rad;

            trackedblobs.add(b1);

        }

        return trackedblobs;

    }

    /**
     * public ArrayList<trackedBlob> tracksimpleAL()<br>
     * <br>
     * tracksimpleAL() is a simpler tracking mechanism,<br>
     * a bit faster than track, but doesn't maintain everything<br>
     * 
     * @Param void
     * @return ArrayList<trackedBlob>
     * 
     */
    // /// simple tracking code in flob
    public ArrayList<TrackedBlob> tracksimpleAL() {
        // tbsimplelist = new ArrayList<trackedBlob>();
        prevtrackedblobs = new ArrayList<TrackedBlob>();
        for (int i = 0; i < trackedblobs.size(); i++) {
            prevtrackedblobs.add(trackedblobs.get(i));
        }

        trackedblobs.clear();
        TrackedBlob b1, b2;
        ABlob ab;

        for (int i = 0; i < theblobs.size(); i++) {
            ab = theblobs.get(i);
            b1 = new TrackedBlob(ab);
            b2 = (i >= prevnumblobs) ? null : new TrackedBlob(
                    prevtrackedblobs.get(i));
            if (b2 != null) {
                b1.id = b2.id;
                b1.prevelx = b2.velx;
                b1.prevely = b2.vely;
                b1.pcx = b2.cx;
                b1.pcy = b2.cy;
            } else {
                b1.id = idnumbers++;
                b1.pcx = ab.cx;
                b1.pcy = ab.cy;
                b1.prevelx = 0.f;
                b1.prevely = 0.f;
            }
            b1.cx = ab.cx;
            b1.cy = ab.cy;
            b1.velx = lp2 * b1.prevelx + lp1 * (b1.cx - b1.pcx);// vx;//b.cx -
            // b.pcx;
            b1.vely = lp2 * b1.prevely + lp1 * (b1.cy - b1.pcy);// vy;//b.cy -
            // b.pcy;
            b1.boxminx = ab.boxminx;
            b1.boxmaxx = ab.boxmaxx;
            b1.boxminy = ab.boxminy;
            b1.boxmaxy = ab.boxmaxy;
            b1.boxdimx = ab.boxdimx;
            b1.boxdimy = ab.boxdimy;

            b1.dimx = ab.dimx;
            b1.dimy = ab.dimy;
            b1.rad = (ab.boxdimx < ab.boxdimy) ? ab.boxdimx / 2f
                    : ab.boxdimy / 2f;
            b1.rad2 = b1.rad * b1.rad;

            // cp feats
            b1.armleftx = ab.armleftx;
            b1.armlefty = ab.armlefty;
            b1.armrightx = ab.armrightx;
            b1.armrighty = ab.armrighty;
            b1.headx = ab.headx;
            b1.heady = ab.heady;
            b1.bottomx = ab.bottomx;
            b1.bottomy = ab.bottomy;
            b1.footleftx = ab.footleftx;
            b1.footlefty = ab.footlefty;
            b1.footrightx = ab.footrightx;
            b1.footrighty = ab.footrighty;

            trackedblobs.add(b1);

        }

        return trackedblobs;

    }

    // void
    // /// tracking code
    void addTrackedBlob(TrackedBlob b) {
        // b.id = b.id;//idnumbers++;
        // b.birthtime=System.currentTimeMillis();
        // b.presencetime=0;
        b.presencetime++;
        trackednumblobs++;
        trackedblobs.add(b);
    }

    void addNewBlob(TrackedBlob b) {
        b.id = idnumbers++;
        b.birthtime = System.currentTimeMillis();
        b.presencetime = 0;
        // add new box to it
        trackednumblobs++;
        trackedblobs.add(b);
    }

    /**
     * void dotracking()<br>
     * <br>
     * main internal tracking algorithm, copies prevtracked blobs<br>
     * places new blobs, estimates id's based on distance from current<br>
     * blob to previous blob, maintains a list of trackedblobs with <br>
     * id persistence in case the blobs enter and exit scene <br>
     * in a more or less stable way<br>
     * 
     * @Param void
     * @return ArrayList<trackedBlob>
     * 
     */
    void dotracking() {
        // / copy current tracked blob to prev tracked blob and increment life

        prevtrackednumblobs = trackednumblobs;
        trackednumblobs = 0;
        prevtrackedblobs.clear();// = new ArrayList<trackedBlob>();
        for (int i = 0; i < trackedblobs.size(); i++) {
            TrackedBlob tb = trackedblobs.get(i);
            // tb.presencetime++;
            prevtrackedblobs.add(tb);
        }

        // new arraylist of trackedblobs
        trackedblobs.clear();// = new ArrayList<trackedBlob>();

        // always init tracking, unlink all blobs, do this every frame
        for (int i = 0; i < prevtrackedblobs.size(); i++) {
            prevtrackedblobs.get(i).linked = false;
        }

        if (numblobs > 0) {
            compareblobsprevblobs();
        }
        // always
        doremoveprevblobs();

        sorttrackedblobs();

        if (trackedblobs.size() < 1 && idnumbers != 0) { // reset id count
            idnumbers = 0;
        }
    }

    void sorttrackedblobs() {

        Collections.sort(trackedblobs);
//        ArrayList<TrackedBlob> temp = new ArrayList<TrackedBlob>();
//
//        if (trackedblobs.size() > 0) {
//
//            for (int i = trackedblobs.size() - 1; i >= 0; i--) {
//                int minid2 = (int) 2e63 - 1;// Math.MA100000000;
//                int who = -1;
//                for (int j = 0; j < trackedblobs.size(); j++) {
//                    TrackedBlob tb = trackedblobs.get(i);
//                    if (tb.id < minid2) {
//                        minid2 = tb.id;
//                        who = j;
//                    }
//                }
//                //
//                if (who > -1) {
//                    temp.add(trackedblobs.remove(who));// minid2));
//                }
//            }
//
//            for (int i = 0; i < temp.size(); i++) {
//                trackedblobs.add(temp.remove(i));
//            }
//
//        }

    }

    boolean matchblobprevtrackedblobs(ABlob ab) {

        boolean matched = false;
        float mintrackeddist = 10000;
        int who = -1;
        float mindist = trackingmindist;// 1000;///2500;//1000; //
        // float vx0=0f;
        // float vy=0f;

        for (int i = prevtrackedblobs.size() - 1; i >= 0; i--) {

            TrackedBlob prev = prevtrackedblobs.get(i);
            if (prev.linked) {
                continue;
            }

            float dx = ab.cx - prev.cx;
            float dy = ab.cy - prev.cy;
            float d2 = dx * dx + dy * dy;
            if (d2 < mindist && d2 < mintrackeddist) {
                mintrackeddist = d2;
                who = i;
                matched = true;
                // vx0=dx;
                // vy=dy;
            }
        }

        if (matched) {
            // System.out.print("matched blob "+who+ "\n");
            TrackedBlob b = prevtrackedblobs.remove(who);
            b.linked = true;
            b.newblob = false;
            b.presencetime++;
            b.prevelx = b.velx;
            b.prevely = b.vely;
            b.pcx = b.cx;
            b.pcy = b.cy;
            b.cx = ab.cx;
            b.cy = ab.cy;
            b.velx = lp2 * b.prevelx + lp1 * (b.cx - b.pcx);// vx;//b.cx -
            // b.pcx;
            b.vely = lp2 * b.prevely + lp1 * (b.cy - b.pcy);// vy;//b.cy -
            // b.pcy;
            // box
            b.boxminx = ab.boxminx;
            b.boxmaxx = ab.boxmaxx;
            b.boxminy = ab.boxminy;
            b.boxmaxy = ab.boxmaxy;
            b.boxdimx = ab.boxdimx;
            b.boxdimy = ab.boxdimy;
            b.dimx = ab.dimx;
            b.dimy = ab.dimy;
            b.boxcenterx = ab.boxcenterx;
            b.boxcentery = ab.boxcentery;

            b.rad = (ab.boxdimx < ab.boxdimy) ? ab.boxdimx / 2f
                    : ab.boxdimy / 2f;
            b.rad2 = b.rad * b.rad;

            // cp feats
//            b.armleftx = ab.armleftx;
//            b.armlefty = ab.armlefty;
//            b.armrightx = ab.armrightx;
//            b.armrighty = ab.armrighty;
//            b.headx = ab.headx;
//            b.heady = ab.heady;
//            b.bottomx = ab.bottomx;
//            b.bottomy = ab.bottomy;
//            b.footleftx = ab.footleftx;
//            b.footlefty = ab.footlefty;
//            b.footrightx = ab.footrightx;
//            b.footrighty = ab.footrighty;

//            trackedblobs.add(b);
            addTrackedBlob(b);

        }

        return matched;
    }

    void compareblobsprevblobs() {

        for (int i = 0; i < theblobs.size(); i++) {

            ABlob ab = (theblobs.get(i));
            boolean matched = matchblobprevtrackedblobs(ab);

            if (!matched) {
                addNewBlob(new TrackedBlob(ab));
            }

        }

    }

    void doremoveprevblobs() {

        for (int i = prevtrackedblobs.size() - 1; i >= 0; i--) {

            TrackedBlob tb = prevtrackedblobs.get(i);

            if (tb.linked) {
                System.out.print("flob: a linked blob in doremove error." + i
                        + " \n");
                throw new IllegalStateException();
            } else {
                // check life
                if (tb.lifetime-- < 0) {
                    prevtrackedblobs.remove(i);
                } else {
                    // addNewBlob(prevtrackedblobs.remove(i));
                    TrackedBlob b = prevtrackedblobs.remove(i);
                    b.velx = 0.f;
                    b.vely = 0.f;
                    addTrackedBlob(b);
                }
            }
        }

    }

    void doaddnewtrackedblobs() {
        for (int i = 0; i < prevtrackedblobs.size(); i++) {
            TrackedBlob newtb = prevtrackedblobs.get(i);// new
            // TrackedBlob((prevblobs.get(i)));
            newtb.birthtime = System.currentTimeMillis();
            newtb.presencetime = 0;
            trackednumblobs++;
            trackedblobs.add(newtb);

        }
    }

    void add_tracker_match(ABlob b, TrackedBlob prev) {

        //
        TrackedBlob tb = new TrackedBlob(b, prev);
        tb.prevelx = prev.velx;
        tb.prevely = prev.vely;
        tb.pcx = prev.cx;
        tb.pcy = prev.cy;
        tb.velx = tb.cx - prev.cx;
        tb.vely = tb.cy - prev.cy;
        tb.presencetime++;

        /*
         * b.id = prev.id; b.pcx = prev.cx; b.pcy = prev.cy; b.cx = b.cx; b.cy =
         * b.cy; b.prevelx = prev.velx; b.prevely = prev.vely; b.velx = b.cx -
         * prev.cx; b.vely = b.cy - prev.cy; b.presencetime++; // add
         */

        trackedblobs.add(tb);
    }

    public boolean isCollide(int x, int y) {

        // receives a pair, tests inside any box, if inside boxes tests inside
        // imagemap

        if (x >= 0 && x < w && y >= 0 && y < h) {
            for (int i = 0; i < theblobs.size(); i++) {
                ABlob b = theblobs.get(i);
                if (x > b.boxminx && x < b.boxmaxx && y > b.boxminy
                        && y < b.boxmaxy) {
                    // inside a box; if is true, return, else keep searching
                    // blobs
                    if (imagemap[y * w + x]) {
                        return true;
                    }
                }
            }
        }

        return false;

    }

    public float[] postcollidetrackedblobs(float x, float y, float rad) {

        float[] dcol = {0f, -1f, -1f, 0f, 0f}; // default return
        // x,y,rad are normed to scene size
        x *= w;
        y *= h;
        rad *= w;
        // receives a pair, tests inside any box, if inside boxes tests inside
        // imagemap

        if (x >= 0f && x < w - 1f && y >= 0f && y < h - 1f) {

            for (int i = 0; i < trackedblobs.size(); i++) {
                // ABlob b = (ABlob) theblobs.get(i);
                TrackedBlob b = trackedblobs.get(i);

                // 0. close point on blob
                float closex = (x < b.boxminx) ? b.boxminx
                        : ((x > b.boxmaxx) ? b.boxmaxx : x);
                float closey = (y < b.boxminy) ? b.boxminy
                        : ((y > b.boxmaxy) ? b.boxmaxy : y);

                // 1. dist blob close
                float dx0 = closex - x;
                float dy0 = closey - y;
                float d0 = dx0 * dx0 + dy0 * dy0;
                float minsdist = rad * rad + b.rad2;

                if (d0 < minsdist && imagemap[((int) y * w + (int) x)]) {
                    // compute normalized vector from close to center
                    float nvx = b.boxcenterx - closex;
                    float nvy = b.boxcentery - closey;
                    float d1 = Math.abs(nvx) + Math.abs(nvy);// (float)Math.sqrt(nvx*nvx+nvy*nvy);
                    float nvl = d1 > 0f ? 1.0f / d1 : 1.f;
                    nvx *= nvl;
                    nvy *= nvl;
                    // moving the circle along this normal by a distance equal
                    // to the circle radius
                    // minus the distance from the closest point to the circle
                    // center
                    float move = rad - d1 + 0.0001f;

                    nvx *= move;
                    nvy *= move;

                    dcol[0] = 1f;
                    dcol[1] = nvx * wr;
                    dcol[2] = nvy * hr;
                    dcol[3] = b.velx * wr;
                    dcol[4] = b.vely * hr;
                    return dcol;

                }

            }
        }

        return dcol;

    }

    public float[] postcollideblobs(float x, float y, float rad) {

        float[] dcol = {0f, -1f, -1f, 0f, 0f}; // default return
        // x,y,rad are normed to scene size
        x *= w;
        y *= h;
        rad *= w;
        // receives a pair, tests inside any box, if inside boxes tests inside
        // imagemap

        if (x >= 0f && x < w && y >= 0f && y < h) {

            for (int i = 0; i < trackedblobs.size(); i++) {
                // ABlob b = (ABlob) theblobs.get(i);
                TrackedBlob b = trackedblobs.get(i);

                // 0. close point on blob
                float closex = (x < b.boxminx) ? b.boxminx
                        : ((x > b.boxmaxx) ? b.boxmaxx : x);
                float closey = (y < b.boxminy) ? b.boxminy
                        : ((y > b.boxmaxy) ? b.boxmaxy : y);

                // 1. dist blob close
                float dx0 = closex - x;
                float dy0 = closey - y;
                float d0 = dx0 * dx0 + dy0 * dy0;
                float minsdist = rad * rad + b.rad2;
                if (d0 < minsdist && imagemap[(int) (y * w + x)]) {
                    // compute normalized vector from close to center
                    float nvx = b.boxcenterx - closex;
                    float nvy = b.boxcentery - closey;
                    float d1 = Math.abs(nvx) + Math.abs(nvy);// (float)Math.sqrt(nvx*nvx+nvy*nvy);
                    float nvl = d1 > 0f ? 1.0f / d1 : 1.f;
                    nvx *= nvl;
                    nvy *= nvl;
                    // moving the circle along this normal by a distance equal
                    // to the circle radius
                    // minus the distance from the closest point to the circle
                    // center
                    float move = rad - d1 + 0.0001f;

                    nvx *= move;
                    nvy *= move;

                    dcol[0] = 1f;
                    dcol[1] = nvx * wr;
                    dcol[2] = nvy * hr;
                    dcol[3] = b.velx * wr;
                    dcol[4] = b.vely * hr;
                    // break;
                    return dcol;

                }
            }
        }

        return dcol;
    }
}
