package digitalmedia;


import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;

import java.awt.Panel;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JComboBox;

import java.util.Random;

/**
     Opens an image window and adds a panel below the image
 */
public class DM_U3_S0544832 implements PlugIn {

	ImagePlus imp; // ImagePlus object
	private int[] origPixels;
	private int width;
	private int height;

	String[] items = { "Original", "Rot-Kanal", "Negativ", "Graustufen",
			"BinÃ¤rbild", "5 Graustufen", "10 Graustufen", "32 Graustufen",
			"Random Dithering", "Fehlerdiffusion","Sepia", "6 Farben",
	"Floyd-Steinberg" };

	public static void main(String args[]) {

		//TODO 
		IJ.open("C:\\Users\\Jakub\\Dropbox\\Studium\\Semester 2\\Software\\ImageJ\\plugins\\GDM\\digitalmedia\\bear.jpg");
		//IJ.open("C:\\Users\\David\\Desktop\\David\\Dropbox\\ImageJ\\plugins\\GDM\\digitalmedia\\bear.jpg");
		//IJ.open("C:\\Users\\D. Hackfleischhasser\\Dropbox\\Internationale Medieninformatik @ HTW\\2. Semester\\Grdl. DiMe\\Übungen\\Share mit Jakub und David - ImageJ\\plugins\\GDM\\digitalmedia\\bear.jpg");

		DM_U3_S0544832 pw = new DM_U3_S0544832();
		pw.imp = IJ.getImage();
		pw.run("");
	}

	@Override
	public void run(String arg) {
		if (imp==null) 
			imp = WindowManager.getCurrentImage();
		if (imp==null) {
			return;
		}
		CustomCanvas cc = new CustomCanvas(imp);
		storePixelValues(imp.getProcessor());
		new CustomWindow(imp, cc);
	}


	private void storePixelValues(ImageProcessor ip) {
		width = ip.getWidth();
		height = ip.getHeight();

		origPixels = ((int []) ip.getPixels()).clone();
	}
	class CustomCanvas extends ImageCanvas {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		CustomCanvas(ImagePlus imp) {
			super(imp);
		}
	} // CustomCanvas inner class

	class CustomWindow extends ImageWindow implements ItemListener {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private String method;

		CustomWindow(ImagePlus imp, ImageCanvas ic) {
			super(imp, ic);
			addPanel();
		}

		void addPanel() {
			//JPanel panel = new JPanel();
			Panel panel = new Panel();
			JComboBox cb = new JComboBox(items);
			panel.add(cb);
			cb.addItemListener(this);
			add(panel);
			pack();
		}


		@Override
		public void itemStateChanged(ItemEvent evt) {
			// Get the affected item
			Object item = evt.getItem();

			if (evt.getStateChange() == ItemEvent.SELECTED) {
				System.out.println("Selected: " + item.toString());
				method = item.toString();
				changePixelValues(imp.getProcessor());
				imp.updateAndDraw();
			} 
		}

		private void changePixelValues(ImageProcessor ip) {

			// Array zum ZurÃ¼ckschreiben der Pixelwerte
			int[] pixels = (int[])ip.getPixels();

			if (method.equals("Original")) {
				for (int y=0; y<height; y++) {
					for (int x=0; x<width; x++) {
						int pos = y*width + x;
						pixels[pos] = origPixels[pos];
					}
				}
			}

			if (method.equals("Rot-Kanal")) {
				redChanel(origPixels, pixels, width, height);
			}
			if (method.equals("Negativ")) {
				negativeImage(origPixels, pixels, width, height);
				//gdmvalidation.Ueb3Validation.validateNegativeImage(origPixels, pixels, width, height);
			}
			if (method.equals("Graustufen")) {
				greyValueImage(origPixels, pixels, width, height);
			}
			if (method.equals("BinÃ¤rbild")) {
				binaryImage(origPixels, pixels, width, height);
			}
			if (method.equals("5 Graustufen")) {
				greyValueImage5Values(origPixels, pixels, width, height);
			}
			if (method.equals("10 Graustufen")) {
				greyValueImage10Values(origPixels, pixels, width, height);
			}
			if (method.equals("32 Graustufen")) {
				greyValueImage32Values(origPixels, pixels, width, height);
			}
			if (method.equals("Random Dithering")) {
				randomDithering(origPixels, pixels, width, height);
			}
			if (method.equals("Fehlerdiffusion")) {
				errorDiffusion(origPixels, pixels, width, height);
			}
			if (method.equals("Sepia")) {
				sepiaImage(origPixels, pixels, width, height);
			}
			if (method.equals("6 Farben")) {
				mapImageTo6Colors(origPixels, pixels, width, height);
			}
			if (method.equals("Floyd-Steinberg")) {
				floydSteinberg(origPixels, pixels, width, height);
			}

		}

		private void redChanel(int[] origPixels, int[] pixels, int width, int height) {
			for (int y=0; y<height; y++) {
				for (int x=0; x<width; x++) {
					int pos = y*width + x;
					int argb = origPixels[pos];  // Lesen der Originalwerte 

					int r = (argb >> 16) & 0xff;
					//int g = (argb >>  8) & 0xff;
					//int b =  argb        & 0xff;

					int rn = r;
					int gn = 0;
					int bn = 0;

					// Hier muessen ggfs. die neuen RGB-Werte wieder auf den Bereich von 0 bis 255 begrenzt werden

					pixels[pos] = (0xFF<<24) | (rn<<16) | (gn<<8) | bn;
				}
			}
		}

		private void negativeImage(int[] origPixels, int[] pixels, int width, int height) 
		{
			for (int y=0; y<height; y++) {
				for (int x=0; x<width; x++) {
					int pos = y*width + x;
					int argb = origPixels[pos];  // Lesen der Originalwerte 

					int r = (argb >> 16) & 0xff;
					int g = (argb >>  8) & 0xff;
					int b =  argb        & 0xff;
					// TODO b)
					int rn = 255-r;
					int gn = 255-g;
					int bn = 255-b;

					pixels[pos] = (0xFF<<24) | (rn<<16) | (gn<<8) | bn;
				}
			}
		}

		private void greyValueImage(int[] origPixels, int[] pixels, int width,
				int height) 
		{

			for (int y=0; y<height; y++) {
				for (int x=0; x<width; x++) {
					int pos = y*width + x;
					int argb = origPixels[pos];  // Lesen der Originalwerte 

					int r = (argb >> 16) & 0xff;
					int g = (argb >>  8) & 0xff;
					int b =  argb        & 0xff;
					// TODO c)

					int greyValue = (r + g + b)/3 ; 


					int rn = greyValue;
					int gn = greyValue;
					int bn = greyValue;

					pixels[pos] = (0xFF<<24) | (rn<<16) | (gn<<8) | bn;
				}
			}
		}

		private void binaryImage(int[] origPixels, int[] pixels, int width, int height, int threshold) 
		{
			for (int y=0; y<height; y++)
			{
				for (int x=0; x<width; x++) {

					int pos = y*width + x;
					int argb = origPixels[pos];  // Lesen der Originalwerte 


					int r = (argb >> 16) & 0xff;
					int g = (argb >>  8) & 0xff;
					int b =  argb        & 0xff;

					int greyValue256 = (r + g + b)/3 ;
					int newGreyValue = 0;

					if(greyValue256 > threshold)
						newGreyValue = 255;
					if(greyValue256 < threshold)
						newGreyValue = 0; 


					int rn = newGreyValue;
					int gn = newGreyValue;
					int bn = newGreyValue;

					pixels[pos] = (0xFF<<24) | (rn<<16) | (gn<<8) | bn;
				}
			}	
		}
		private void binaryImage(int[] origPixels, int[] pixels, int width, int height)
		{
			binaryImage(origPixels, pixels, width, height, 100); // 
		}

		private void greyValueImage(int[] origPixels, int[] pixels, int width, int heigth, int greyValues)
		{
			for (int y=0; y<height; y++) {
				for (int x=0; x<width; x++) {
					int pos = y*width + x;
					int argb = origPixels[pos];  // Lesen der Originalwerte 

					int r = (argb >> 16) & 0xff;
					int g = (argb >>  8) & 0xff;
					int b =  argb        & 0xff;

					int newGreyValue = 0; 

					int greyValue256 = (r + g + b)/3 ;

					if(greyValue256 < 1)
					{
						newGreyValue = 0; 
					}
					else
					{
						double intervall = greyValues / 255. ;
						int intervall2 = 255 / (greyValues -1);
						newGreyValue = (int)(greyValue256 * intervall) * intervall2; // Integer Division rounds. No remainder left. 
					}

					int rn = newGreyValue;
					int gn = newGreyValue;
					int bn = newGreyValue;
					pixels[pos] = (0xFF<<24) | (rn<<16) | (gn<<8) | bn;
				}
			}		
		}

		private void greyValueImage5Values(int[] origPixels, int[] pixels, int width, int height) 
		{
			greyValueImage(origPixels, pixels, width, height, 5);
		}

		private void greyValueImage10Values(int[] origPixels, int[] pixels, int width, int height) 
		{
			greyValueImage(origPixels, pixels, width, height, 10);
		}

		private void greyValueImage32Values(int[] origPixels2, int[] pixels, int width2, int height2) 
		{
			greyValueImage(origPixels, pixels, width, height, 32);
		}

		private void randomDithering(int[] origPixels, int[] pixels, int width, int height) 
		{
			int rn, bn, gn = 0;
			int ditherAmount = 128;
			int threshold = 128;
			for (int y=0; y<height; y++) {
				for (int x=0; x<width; x++) {
					int pos = y*width + x;
					int argb = origPixels[pos];  // Lesen der Originalwerte 

					int r = (argb >> 16) & 0xff;
					int g = (argb >>  8) & 0xff;
					int b =  argb        & 0xff;
					// TODO e)

					double rand  = (Math.random()-0.5)*ditherAmount;
					int greyValue256 = (r + g + b)/3;

					if((rand+greyValue256) > threshold)
						rn = gn = bn = 255;
					else
						rn = gn = bn = 0;


					pixels[pos] = (0xFF<<24) | (rn<<16) | (gn<<8) | bn;
				}
			}		
		}

		private void errorDiffusion(int[] origPixels, int[] pixels, int width, int height) 
		{
			int carry = 0;
			int threshold = 128;
			int rn, gn, bn = 0;

			for (int y=0; y<height; y++) {
				carry = 0; 
				for (int x=0; x<width; x++) {
					int pos = y*width + x;
					int argb = origPixels[pos];  // Lesen der Originalwerte 

					int r = (argb >> 16) & 0xff;
					int g = (argb >>  8) & 0xff;
					int b =  argb        & 0xff;

					int currentValue = (r + g + b)/3;

					currentValue += carry;

					if((currentValue) > threshold)
					{
						rn=gn=bn=255;
						carry = currentValue-255;
					}
					else
					{
						rn=gn=bn=0;
						carry = currentValue;
					}

					pixels[pos] = (0xFF<<24) | (rn<<16) | (gn<<8) | bn;
				}
			}
		}

		private void sepiaImage(int[] origPixels, int[] pixels, int width, int height)
		{
			for (int y=0; y<height; y++) {
				for (int x=0; x<width; x++) {
					int pos = y*width + x;
					int argb = origPixels[pos];  // Lesen der Originalwerte 

					int r = (argb >> 16) & 0xff;
					int g = (argb >>  8) & 0xff;
					int b =  argb        & 0xff;
					// TODO g)
					int greyValue256 = (r + g + b)/3;
					int rn = constrain((int) (greyValue256*1.8));
					int gn = constrain((int) (greyValue256*1.35));
					int bn = greyValue256;

					pixels[pos] = (0xFF<<24) | (rn<<16) | (gn<<8) | bn;
				}
			}		
		}

		public int constrain(int RGBValue)
		{
			if(RGBValue > 255)
				RGBValue = 255;
			else if(RGBValue < 0)
				RGBValue = 0; 
			return RGBValue; 
		}

		private void mapImageTo6Colors(int[] origPixels, int[] pixels, int width, int height) 
		{
			for (int y=0; y<height; y++) {
				for (int x=0; x<width; x++) {
					int pos = y*width + x;
					int argb = origPixels[pos];  // Lesen der Originalwerte 

					int r = (argb >> 16) & 0xff;
					int g = (argb >>  8) & 0xff;
					int b =  argb        & 0xff;
					// TODO h)

					
					int[] rgb = {r, g, b};
					
					int[] g0 = {20, 20, 20};
					int[] g1 = {80, 80, 80};
					int[] g2 = {130, 130, 130}; 
					int[] g3 = {200, 200, 200};
					int[] bl0 = {54,108,146}; 
					int[] br0 = {96,73,53};


					int[][] colors = {g0, g1, g2, g3, bl0, br0};
					
					
					
					int[] newRgb = getClosestColor(rgb, colors);


					


					pixels[pos] = (0xFF<<24) | (newRgb[0]<<16) | (newRgb[1]<<8) | newRgb[2];




				}
			}
		}

		private int[] getClosestColor(int[] rgb, int[][] colors)
		{


			int[] distances = new int[colors.length]; 


			for(int i = 0; i < colors.length; i++)
			{
				distances[i] = vectorDistance(colors[i], rgb);
			}

			int d = getSmallestInt(distances);

			int position = findIntegerPositionInArray(distances, d); 

			return colors[position]; 


		}

		private int findIntegerPositionInArray(int[] values, int d)
		{
			for (int i = 0; i < values.length ; i++)
			{
				if(values[i] == d)
					return i; 
			}
			return -1; 

		}

		private int vectorDistance(int[] v1, int[] v2)
		{
			int x=v2[0]-v1[0];
			int y=v2[1]-v1[1];
			int z=v2[2]-v1[2];
			double xpow = Math.pow(x, 2);
			double ypow = Math.pow(y, 2);
			double zpow = Math.pow(z, 2);
			
			double root = Math.sqrt(xpow+ ypow +zpow);
			
			int d = (int)root; 
			
			
			//int d = (int)(Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2))); 
			return d; 
		}

		private int getSmallestInt(int[] values)
		{
			int v = values[0]; 
			for(int i = 0; i < values.length; i++)
			{

				if(values[i] < v)
					v = values[i];

			}
			return v; 
		}

		private void floydSteinberg(int[] origPixels, int[] pixels, int width,
				int height) {
			for (int y=0; y<height; y++) {
				for (int x=0; x<width; x++) {
					int pos = y*width + x;
					int argb = origPixels[pos];  // Lesen der Originalwerte 

					int r = (argb >> 16) & 0xff;
					int g = (argb >>  8) & 0xff;
					int b =  argb        & 0xff;
					// TODO i)
					int rn = r;
					int gn = g;
					int bn = b;
					pixels[pos] = (0xFF<<24) | (rn<<16) | (gn<<8) | bn;
				}
			}
		}


	} // CustomWindow inner class
} 
