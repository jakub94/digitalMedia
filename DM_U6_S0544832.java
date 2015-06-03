package digitalmedia;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.gui.NewImage;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import gdmvalidation.ScaleValidate;

public class DM_U6_S0544832 implements PlugInFilter 
{
	protected ImagePlus imp;

	public static void main(String args[]) 
	{
		ImageJ ij = new ImageJ(); // neue ImageJ Instanz starten und anzeigen
		ij.exitWhenQuitting(true);

		//XXX open your image here
		//IJ.open("/home/...../component.jpg");
		//IJ.open("/home/david.obermann/Arbeitsfläche/privat/ImageJ/plugins/digitalmedia/resources/component.jpg");
		IJ.open(System.getProperty("user.dir") + "\\digitalmedia\\component.jpg");

		DM_U6_S0544832 sd = new DM_U6_S0544832();
		sd.imp = IJ.getImage();
		ImageProcessor ip = sd.imp.getProcessor();
		sd.run(ip);
	}

	public int setup(String arg, ImagePlus imp) {
		if (arg.equals("about"))
		{showAbout(); return DONE;}
		return DOES_RGB+NO_CHANGES;
		// kann RGB-Bilder und veraendert das Original nicht
	}


	@Deprecated
	void makeTestIm( int[] pixels, int testImWidth, int testImHeight) {
		int startXCol = 2;
		int startYCol = 2;
		int col = 0;

		int [] testIm = new int[testImHeight* testImWidth];

		for ( int yi = 0; yi< testImHeight; yi++ ) {
			startXCol = startYCol;
			if (yi == 1)
				startYCol = 0;

			for (int xk = 0; xk< testImWidth; xk++) {
				col = 0xFF000000 | startXCol <<16 | startXCol << 8 | startXCol;
				//System.out.print("Col at ( " + xk +"," + yi + ")  ==" + startXCol + " ");
				startXCol +=10;
				testIm[ yi* testImWidth + xk ] = col;
			}
			System.out.println();
			startYCol +=15;
		}
	}

	void test() 
	{
		//pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) | b;
		int testImWidth = 4;
		int testImHeight= 4;
		int[] testIm = 
			{  
				0,   0,  0, 0,
				0, 100,  3, 0,
				0,   0,  0, 0,
				30,  30, 30, 0
			};

		Utils.makeRGB(testIm);

		int outTestImWidth = 7;
		int outTestImHeight= 9;
		int [] outTestIm = new int[outTestImWidth*outTestImHeight];
		Utils.reset(outTestIm);

		// blank new image      
		System.out.println();
		System.out.println("orig image:");
		Utils.printGreyValues(testIm, testImWidth, testImHeight);

		System.out.println();
		System.out.println("new image:");
		Utils.printGreyValues(outTestIm, outTestImWidth, outTestImHeight);
		// copy image
		System.out.println();
		copyImage(testIm, testImWidth, testImHeight, outTestIm, outTestImWidth, outTestImHeight);
		ScaleValidate.validateCopy(testIm, testImWidth, testImHeight, outTestIm, outTestImWidth, outTestImHeight);
		System.out.println("Copy Image:");
		Utils.printGreyValues(outTestIm, outTestImWidth, outTestImHeight);
		Utils.reset(outTestIm);
		// nearest neighbor
		System.out.println();
		nearestNeighbor(testIm, testImWidth, testImHeight, outTestIm, outTestImWidth, outTestImHeight);
		ScaleValidate.validateNearestNeighbor(testIm, testImWidth, testImHeight, outTestIm, outTestImWidth, outTestImHeight);
		System.out.println("nearest neighbour:");
		Utils.printGreyValues(outTestIm, outTestImWidth, outTestImHeight);
		Utils.reset(outTestIm);
		// bilinear    
		System.out.println();
		bilinearInterpolation(testIm, testImWidth, testImHeight, outTestIm, outTestImWidth, outTestImHeight);
		ScaleValidate.validateBilinear(testIm,testImWidth,testImHeight,outTestIm, outTestImWidth, outTestImHeight);

		System.out.println("bilinear image:");
		Utils.printGreyValues(outTestIm, outTestImWidth, outTestImHeight);
		System.out.println("test finished\n");
		Utils.reset(outTestIm);

	}


	public void run(ImageProcessor ip) 
	{

		String[] dropdownmenue = {"Kopie", "Pixelwiederholung", "Bilinear", "Test"};
		GenericDialog gd = new GenericDialog("scale");
		gd.addChoice("Methode",dropdownmenue,dropdownmenue[0]);
		gd.addNumericField("Breite:",500,0);
		gd.addNumericField("Hoehe:",460,0);

		gd.showDialog();

		int width_n =  (int)gd.getNextNumber();
		int height_n = (int)gd.getNextNumber(); // _n fuer das neue skalierte Bild
		String choice = gd.getNextChoice();

		int width  = ip.getWidth();  // Breite bestimmen
		int height = ip.getHeight(); // Hoehe bestimmen


		ImagePlus neu = NewImage.createRGBImage("Skaliertes Bild",
				width_n, height_n, 1, NewImage.FILL_WHITE);

		ImageProcessor ip_n = neu.getProcessor();

		int[] pix = (int[])ip.getPixels();
		int[] pix_n = (int[])ip_n.getPixels();
		boolean test = false;
		if (choice == "Test") 
		{
			test();
			test = true;
		} 
		else if (choice == "Kopie") 
		{
			copyImage(pix, width, height, pix_n, width_n, height_n);
			ScaleValidate.validateCopy(pix, width, height, pix_n, width_n, height_n);
		} 
		else if (choice == "Pixelwiederholung") {
			nearestNeighbor(pix, width, height, pix_n, width_n, height_n);
			ScaleValidate.validateNearestNeighbor(pix, width, height, pix_n, width_n, height_n);
		} 
		else if (choice == "Bilinear") {
			bilinearInterpolation(pix, width, height, pix_n, width_n, height_n);
			ScaleValidate.validateBilinear(pix,width,height,pix_n, width_n, height_n);
		}
		// neues Bild anzeigen
		if (!test) {
			neu.show();
			neu.updateAndDraw();
		}
	}

	/*
	private void pixelWiederholungFilterSchteil(int[] origPix, int origWidth, int origHeight, int[] newPix, int newWidth, int newHeight)
	{
		double[][] matrix = {{0, 0, 0}, {0 , 1, 0}, {0, 0, 0}};	
	}
	*/
	
	private void bilinearInterpolation(int[] origPix, int origWidth, int origHeight, int[] newPix, int newWidth, int newHeight) 
	{
		double ratioY =  (origHeight - 1) / (double)(newHeight - 1) ;
		double ratioX =  (origWidth  - 1) / (double)(newWidth  - 1);
		double h, v;
		h = v = 0;
		int posNew;
		double t, k; 

		for (int y_n=0; y_n<newHeight; y_n++) 
		{
			for (int x_n=0; x_n<newWidth; x_n++) 
			{
				k = (x_n * ratioX);
				t = (y_n * ratioY);

				h = (k - Math.floor(k)); 
				v = (t - Math.floor(t));

			
				int x = (int)Math.floor(k);
				int y = (int)Math.floor(t);

				posNew = y_n * newWidth + x_n;

				int[] a = getNewPixelValue(origPix, origWidth, origHeight, x, y, h, v);

				newPix[posNew] = (0xFF<<24) | (a[0]<<16) | (a[1] << 8) | a[2];
			}
		}
	}

	private int[] getNewPixelValue(int[] origPix, int origWidth, int origHeight, int x, int y, double h, double v)
	{
		double[] newPix = {0,0,0};
		int pos = 0;
		double factor = 0;

		//Randbehandlung = original pixel
		//TODO Evtl. ordentliche Randbehandlung
		if((y == (origHeight-1))  || (x == (origWidth-1)))
		{
			int argb = origPix[(y*origWidth) + x];
			int r = (argb >> 16) & 0xff;
			int g = (argb >>  8) & 0xff;
			int b =  argb        & 0xff;
			return new int[]{r,g,b};
		}
		
		for(int i = y; i < y+2; i++)
		{
			for(int j = x; j<x+2; j++)
			{
				pos = (i*origWidth) + j;
				
				if(i == y && j == x)  					//A
					factor = (1-h)*(1-v);

				else if((i == (y)) && (j == (x+1))) 	//B
					factor = (h)*(1-v);				

				else if((i == (y+1)) && (j == x)) 		//C
					factor =  (1-h)*(v);

				else if((i == (y+1)) && (j == (x+1))) 	//D
					factor = (h*v);
				
				newPix = calculateValue(origPix[pos], newPix, factor);
			}
		}
		
		// newPix as int array
		int[] intNewPix = new int[3];
		
		// put double values from newPix as int values into intNewPix
		for (int i = 0; i < newPix.length; ++i) {
			intNewPix[i] = (int)Math.round(newPix[i]);
		}
		
		return constrain(intNewPix);
	}

	private double[] calculateValue(int origPix, double[] newPixel, double factor)
	{
		int r = (origPix >> 16) & 0xff;
		int g = (origPix >>  8) & 0xff;
		int b =  origPix        & 0xff;

		newPixel[0] += (r * factor);
		newPixel[1] += (g * factor);
		newPixel[2] += (b * factor);

		return newPixel;
	}

	private int[] constrain(int[] rgb)
	{
		for(int i = 0; i < rgb.length; i++)
		{
			if(rgb[i] > 255)
				rgb[i] =  255;

			else if(rgb[i] < 0)
				rgb[i] =  0;
		}
		return rgb;
	}
	
	private void nearestNeighbor(int[] origPix, int origWidth, int origHeight,int[] newPix, int newWidth, int newHeight) 
	{
		double ratioY = (origHeight - 1) / (double)(newHeight - 1) ;
		double ratioX = (origWidth  - 1) / (double)(newWidth  - 1);
		double k,t,h,v;
		int x,y, pos, posNew;

		for (int y_n=0; y_n < newHeight; y_n++) 
		{
			for (int x_n=0; x_n < newWidth; x_n++) 
			{
				k = (x_n * ratioX);
				t = (y_n * ratioY);
			
				h = Math.round((k - Math.floor(k)));
				v = Math.round((t - Math.floor(t)));
				
				x = (int)Math.floor(k + h);
				y = (int)Math.floor(t + v);
				
				pos = (int)((y * origWidth) + x);
				posNew = y_n * newWidth + x_n;
				newPix[posNew] = origPix[pos];
			}
		}
	}

	private void copyImage(int[] origPix, int origWidth, int origHeight, int[] newPix, int newWidth, int newHeight) 
	{
		for (int yN=0; yN<newHeight; yN++) 
		{
			for (int xN=0; xN<newWidth; xN++) 
			{
				int y = yN;
				int x = xN;

				if (y < origHeight && x < origWidth) 
				{
					int posNew = yN*newWidth + xN;
					int pos  =  y  *origWidth   + x;

					newPix[posNew] = origPix[pos];
				}
			}
		}
	}

	void showAbout() {
		IJ.showMessage("");
	}
	private void applyMatrix(double[][] matrix, int x, int y, int[] origPixel, int[] outPixel, int width, int height, int offset, int scale)
	{		
		double rn, gn, bn;
		rn = gn = bn = 0;
		int pos_tmp, i, j;
		int matrixSize = matrix.length / 2;
		
		if(y - matrixSize > 0)
			i = y - matrixSize;
		else if(y == height-1)
			i = y - matrixSize - 1;
		else
			i = 0;
			
		
		for(int t = 0; i <= y + matrixSize; i++, t++) 
		{
			if(x - matrixSize > 0)
				j = x - matrixSize;
			else
				j = 0;
			
			for(int k = 0; j <= x + matrixSize; j++, k++) 
			{
				if(i > height -1 || j > width -1)
					break;
				
				pos_tmp = i*width + j;
				
				int argb = origPixel[pos_tmp];  // Lesen der Originalwerte 
				int r = (argb >> 16) & 0xff;
				int g = (argb >>  8) & 0xff;
				int b =  argb        & 0xff;
				
				rn += (r * matrix[t][k]);
				gn += (g * matrix[t][k]);  
				bn += (b * matrix[t][k]);								
			}
		}
		
		rn = constrain((int)(Math.round((rn / scale) + offset))); 
		gn = constrain((int)(Math.round((rn / scale) + offset)));
		bn = constrain((int)(Math.round((rn / scale) + offset)));
		
		int pos = y*width + x;
		outPixel[pos] = (0xFF<<24) | ((int)rn<<16) | ((int)gn << 8) | (int)bn;
	}
	public int constrain(int RGBValue)
	{
		if(RGBValue > 255)
			RGBValue = 255;
		else if(RGBValue < 0)
			RGBValue = 0; 
		return RGBValue; 
	}
}

