package digitalmedia;

//import gdmvalidation.Ueb5Validation;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;



import java.awt.Panel;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;


//import javax.swing.BorderFactory;
import javax.swing.JComboBox;

/**
     Opens an image window and adds a panel below the image
 */
public class DM_U5_S0544832 implements PlugIn 
{

	ImagePlus imp; // ImagePlus object
	private int[] origPixels1;
	//	private int width;
	//	private int height;

	String[] items = {"Original", "Filter 1", "Weichzeichnen", "Hochpass", "Unsharp Mask","test"};

	public static void main(String args[]) 
	{
		ImageJ ij = new ImageJ();
		ij.exitWhenQuitting(true);
		
		//IJ.open("/home/david.obermann/Arbeitsfläche/privat/ImageJ-146/plugins/digitalmedia/resources/sail.jpg");
		IJ.open(System.getProperty("user.dir") + "\\digitalmedia\\sail.jpg");

		DM_U5_S0544832 pw = new DM_U5_S0544832();
		pw.imp = IJ.getImage();
		pw.run("");
	}

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
		//		width = ip.getWidth();
		//		height = ip.getHeight();
		origPixels1 = ((int []) ip.getPixels()).clone();
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

		public void itemStateChanged(ItemEvent evt) {
			// Get the affected item
			Object item = evt.getItem();

			if (evt.getStateChange() == ItemEvent.SELECTED) 
			{
				System.out.println("Selected: " + item.toString());
				method = item.toString();
				changePixelValues(imp.getProcessor());
				imp.updateAndDraw();
			} 
		}


		private void changePixelValues(ImageProcessor ip) {

			// Array zum Zurückschreiben der Pixelwerte
			int[] changedPixels = (int[])ip.getPixels();
			int width = ip.getWidth();
			int height = ip.getHeight();
			if (method.equals("Original")) {
				copyImage(origPixels1, changedPixels, width, height);
			}

			if (method.equals("Filter 1")) {
				filter1(origPixels1, changedPixels, width, height );
			}
			if (method.equals("Weichzeichnen")) {
				lowPass(origPixels1, changedPixels, width, height);
			}
			if (method.equals("Hochpass")) {
				highPass(origPixels1, changedPixels, width, height);

			}
			if (method.equals("Unsharp Mask")) {
				unsharpMask(origPixels1, changedPixels, width, height);
				//unsharpMask2(origPixels1, changedPixels, width, height);
			}
			if (method.equals("test")) {
				test();
			}

		}
	}

	private void copyImage(int[] origPixels, int[] newpixels, int imwidth, int imheight) 
	{
		for (int y=0; y<imheight; y++) 
		{
			for (int x=0; x<imwidth; x++) 
			{
				int pos = y*imwidth + x;
				newpixels[pos] = origPixels[pos];
			}
		}
	}



	private void filter1(int[] origPixels, int[] newpixels, int imwidth, int imheight) 
	{
		for (int y=0; y<imheight; y++) 
		{
			for (int x=0; x<imwidth; x++) 
			{
				int pos = y*imwidth + x;
				int argb = origPixels[pos];  // Lesen der Originalwerte 

				int r = (argb >> 16) & 0xff;
				int g = (argb >>  8) & 0xff;
				int b =  argb        & 0xff;

				int rn = r/2;
				int gn = g/2;
				int bn = b/2;

				newpixels[pos] = (0xFF<<24) | (rn<<16) | (gn << 8) | bn;
			}
		}
	}

	private void lowPass(int[] testIm, int[] outTestIm, int testImWidth, int testImHeight) 
	{
		neighborhoodOperation(0, testImWidth, testImHeight, testIm, outTestIm);
	}

	private void highPass(int[] testIm, int[] outTestIm, int testImWidth, int testImHeight) 
	{
		neighborhoodOperation(1, testImWidth, testImHeight, testIm, outTestIm);
	}

	private void neighborhoodOperation(int operation, int testImWidth, int testImHeight, int[] origPixels, int[] outPixel)
	{
		double[][] matrix = {{0,0,0}, {0,1,0}, {0,0,0}};
		int offset = 0;
		int scale = 0;
		switch(operation)
		{
			case 0: // Lowpass
			{
				scale = 9;
				matrix = new double[][] {{1,1,1}, {1,1,1}, {1,1,1}};
				offset = 0;
				break;
			}
			case 1: // highpass
			{
				scale = 9;
				matrix = new double[][] {{-1,-1,-1}, {-1,8,-1}, {-1,-1,-1}};
				offset = 128;
				break;
			}
			case 2: // unsharped mask
			{
				scale = 9;
				matrix = new double[][] {{-1,-1,-1}, {-1,17,-1}, {-1,-1,-1}};
				offset = 0;
				break;
			}
		}
		
		for (int y=0; y<testImHeight; y++) 
		{
			for (int x=0; x<testImWidth; x++) 
			{
				applyMatrix(matrix, x, y, origPixels, outPixel, testImWidth, testImHeight, offset, scale);
			}
		}
			
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
	
	private void unsharpMask(int[] testIm, int[] outTestIm, int testImWidth, int testImHeight) 
	{
		neighborhoodOperation(2, testImWidth, testImHeight, testIm, outTestIm);
	}

	void test() {
		//pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) | b;
		int testImWidth = 5;
		int testImHeight= 5;
		int[] testIm = {0,   0,  0, 0,  0, 
						0, 100,  0, 0,  0,
						0, 100,  0, 0,  0,
						0,   0,  0, 0,  0,
						0,   0,  0, 0,  0,};

		Utils.makeRGB(testIm);

		int outTestImWidth = testImWidth;
		int outTestImHeight= testImHeight;
		int [] outTestIm = new int[outTestImWidth*outTestImHeight];
		Utils.reset(outTestIm);

		// blank new image      
		System.out.println();
		System.out.println("orig image:");
		Utils.printGreyValues(testIm, testImWidth, testImHeight);
		System.out.println();
		System.out.println("new image:");
		Utils.printGreyValues(outTestIm, outTestImWidth, outTestImHeight);

		// low pass image
		System.out.println();
		lowPass(testIm,outTestIm, testImWidth, testImHeight);
		System.out.println("low pass image:");
		Utils.printGreyValues(outTestIm, outTestImWidth, outTestImHeight);
		Utils.reset(outTestIm);
		// high pass
		System.out.println();
		highPass(testIm, outTestIm,testImWidth, testImHeight);
		System.out.println("high pass");
		Utils.printGreyValues(outTestIm, outTestImWidth, outTestImHeight);
		Utils.reset(outTestIm);
		// unsharp mask   
		System.out.println();
		unsharpMask(testIm, outTestIm,testImWidth, testImHeight);
		System.out.println("unsharp mask");
		Utils.printGreyValues(outTestIm, outTestImWidth, outTestImHeight);
		System.out.println("test finished\n");
		Utils.reset(outTestIm);
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
// CustomWindow inner class