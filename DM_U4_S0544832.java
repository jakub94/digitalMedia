package digitalmedia;

import ij.*;
import ij.io.*;
import ij.process.*;
import ij.gui.*;
import ij.plugin.filter.*;


public class DM_U4_S0544832 implements PlugInFilter {

	protected ImagePlus imp;
	final static String[] choices = {"Wischen", "Weiche Blende", "Overlay A", "Overlay B", "Schiebe-Blende", "Chroma Key", "Extra"};

	public int setup(String arg, ImagePlus imp) {
		this.imp = imp;
		return DOES_RGB+STACK_REQUIRED;
	}

	public static void main(String args[]) {
		ImageJ ij = new ImageJ(); // neue ImageJ Instanz starten und anzeigen 
		ij.exitWhenQuitting(true);

		IJ.open("C:\\Users\\Jakub\\Dropbox\\Studium\\Semester 2\\Software\\ImageJ\\img\\StackB.zip");

		DM_U4_S0544832 sd = new DM_U4_S0544832();
		sd.imp = IJ.getImage();
		ImageProcessor B_ip = sd.imp.getProcessor();
		sd.run(B_ip);
	}

	public void run(ImageProcessor B_ip) 
	{
		// Film B wird uebergeben
		ImageStack stack_B = imp.getStack();

		int length = stack_B.getSize();
		int width  = B_ip.getWidth();
		int height = B_ip.getHeight();

		// ermoeglicht das Laden eines Bildes / Films
		Opener o = new Opener();
		OpenDialog od_A = new OpenDialog("Auswählen des 2. Filmes ...",  "");

		// Film A wird dazugeladen
		String dateiA = od_A.getFileName();
		if (dateiA == null) return; // Abbruch
		String pfadA = od_A.getDirectory();
		ImagePlus A = o.openImage(pfadA,dateiA);
		if (A == null) return; // Abbruch

		ImageProcessor A_ip = A.getProcessor();
		ImageStack stack_A  = A.getStack();

		if (A_ip.getWidth() != width || A_ip.getHeight() != height)
		{
			IJ.showMessage("Fehler", "Bildgroessen passen nicht zusammen");
			return;
		}

		// Neuen Film (Stack) "Erg" mit der kleineren Laenge von beiden erzeugen
		length = Math.min(length,stack_A.getSize());

		ImagePlus Erg = NewImage.createRGBImage("Ergebnis", width, height, length, NewImage.FILL_BLACK);
		ImageStack stack_Erg  = Erg.getStack();

		// Dialog fuer Auswahl des Ueberlagerungsmodus
		GenericDialog gd = new GenericDialog("Ueberlagerung");
		gd.addChoice("Methode",choices,"");
		gd.showDialog();

		int methode = 0;		
		String s = gd.getNextChoice();
		if (s.equals("Wischen")) methode = 1;
		if (s.equals("Weiche Blende")) methode = 2;
		if (s.equals("Overlay A")) methode = 3;
		if (s.equals("Overlay B")) methode = 4;
		if (s.equals("Schiebe-Blende")) methode = 5;
		if (s.equals("Extra")) methode = 7;
		if (s.equals("Chroma Key")) methode = 6;
		// Arrays fuer die einzelnen Bilder
		int[] pixels_B; //Moon
		int[] pixels_A; //Orange
		int[] pixels_Erg;

		double[] chromaKey = RGB2YCbCr(234, 168, 72);
		int k = 20;


		// Schleife ueber alle Bilder
		for (int z=1; z<=length; z++)
		{

			pixels_B   = (int[]) stack_B.getPixels(z);
			pixels_A   = (int[]) stack_A.getPixels(z);
			pixels_Erg = (int[]) stack_Erg.getPixels(z);


			if(z % (int)(length / 20) == 0)
			{
				if(k > 1)
					k--;
			}


			int pos = 0;
			for (int y=0; y<height; y++)
				for (int x=0; x<width; x++, pos++)
				{
					int cA = pixels_A[pos];
					int rA = (cA & 0xff0000) >> 16;
				int gA = (cA & 0x00ff00) >> 8;
				int bA = (cA & 0x0000ff);

				int cB = pixels_B[pos];
				int rB = (cB & 0xff0000) >> 16;
				int gB = (cB & 0x00ff00) >> 8;
				int bB = (cB & 0x0000ff);

				if (methode == 1) {
					if (y+1 > (z-1)*(double)width/(length-1)) {
						pixels_Erg[pos] = pixels_B[pos];
					}
					else {
						pixels_Erg[pos] = pixels_A[pos];
					}
				}

				/* copy this!
					if (methode == 2) {
						// ...

						int r = ...
						int g = ...
						int b = ...

						pixels_Erg[pos] = 0xFF000000 + ((r & 0xff) << 16) + ((g & 0xff) << 8) + ( b & 0xff);
					}
				 */

				if (methode == 2) 
				{
					// ...
					double alpha = (z*255./length);
					int r = (int)(alpha*rA+(255-alpha)*rB)/255; 
					int g = (int)(alpha*gA+(255-alpha)*gB)/255; 
					int b = (int)(alpha*bA+(255-alpha)*bB)/255; 

					pixels_Erg[pos] = 0xFF000000 + ((r & 0xff) << 16) + ((g & 0xff) << 8) + ( b & 0xff);	
				}

				if (methode == 3) 
				{
					// ...

					int r = overlayValue(rB, rA);
					int g = overlayValue(gB, gA);
					int b = overlayValue(bB, bA);

					pixels_Erg[pos] = 0xFF000000 + ((r & 0xff) << 16) + ((g & 0xff) << 8) + ( b & 0xff);

				}

				if (methode == 4) 
				{
					// ...

					int r = overlayValue(rA, rB);
					int g = overlayValue(gA, gB);
					int b = overlayValue(bA, bB);

					pixels_Erg[pos] = 0xFF000000 + ((r & 0xff) << 16) + ((g & 0xff) << 8) + ( b & 0xff);

				}


				if (methode == 5) 
				{


					int r = 0;
					int g = 0;
					int b = 0;


					double factor = width*1.0/(length-1)*1.0; 
					int a = (int)(factor * (z-1)); 

					if(x < a)
					{
						int cD = pixels_B[pos];
						r = (cD & 0xff0000) >> 16;
					g = (cD & 0x00ff00) >> 8;
					b = (cD & 0x0000ff);

					}
					else
					{
						int cC = pixels_A[pos-a];
						r = (cC & 0xff0000) >> 16;
					g = (cC & 0x00ff00) >> 8;
					b = (cC & 0x0000ff);
					}
					pixels_Erg[pos] = 0xFF000000 + ((r & 0xff) << 16) + ((g & 0xff) << 8) + ( b & 0xff);
				}

				if (methode == 6) 
				{
					double[] YCbCr = RGB2YCbCr(rA, gA, bA);

					double distance = colorDistance(chromaKey, YCbCr);

					int r, g, b = 0;
					if(distance > 55)
					{
						r = rA;
						g = gA;
						b = bA;
					}
					else 
					{
						r = rB;
						g = gB;
						b = bB;
					}


					pixels_Erg[pos] = 0xFF000000 + ((r & 0xff) << 16) + ((g & 0xff) << 8) + ( b & 0xff);
				}


				if (methode == 7) {
					// ...


					int r = 0; 
					int g = 0;
					int b = 0; 		

					if(x % k == 0)
					{
						r = rA; 
						g = gA;
						b = bA; 
					}
					else
					{
						r = rB; 
						g = gB;
						b = bB; 
					}


					pixels_Erg[pos] = 0xFF000000 + ((r & 0xff) << 16) + ((g & 0xff) << 8) + ( b & 0xff);
				}


				}

			// neues Bild anzeigen
			Erg.show();
			Erg.updateAndDraw();

		}

	}

	private double colorDistance(double[] p1, double[] p2)
	{
		double x=p2[1]-p1[1];
		double y=p2[2]-p1[2];

		double xpow = Math.pow(x, 2);
		double ypow = Math.pow(y, 2);

		return Math.sqrt(xpow + ypow);
	}

	private int overlayValue(int front, int back)
	{
		int overlayValue = 0; 

		if(back <=128)
		{
			overlayValue = (int)((front*back)/128.);
		}
		else
		{
			overlayValue = (int)(255-((255-front)*(255-back)/128.));
		}

		return overlayValue; 
	}

	private static double[] RGB2YCbCr(int r, int g, int b)
	{
		// RGB --> YCbCr 
		double lum = 0.299*r + 0.587*g + 0.114*b; 
		double cb = -0.168736*r - 0.331264*g + 0.5*b;
		double cr = 0.5*r -0.418688*g -0.081312*b; 

		return new double[] {lum, cb, cr}; 
	}


}