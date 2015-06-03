package digitalmedia;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Panel;

import javax.swing.BorderFactory;
import javax.swing.JSlider;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
     Opens an image window and adds a panel below the image
 */
public class DM_U2_S0544832 implements PlugIn {

	ImagePlus imp; // ImagePlus object
	private int[] origPixels;
	private int width;
	private int height;


	public static void main(String args[]) {
		//new ImageJ();
		conversionTest();

		//IJ.open("C:\\Users\\Jakub\\Dropbox\\Studium\\Semester 2\\Software\\ImageJ\\plugins\\GDM\\digitalmedia\\orchid.jpg");
		//IJ.open("C:\\Users\\David\\Desktop\\David\\Dropbox\\ImageJ\\plugins\\GDM\\digitalmedia\\orchid.jpg");
		IJ.open("C:\\Users\\D. Hackfleischhasser\\Dropbox\\Internationale Medieninformatik @ HTW\\2. Semester\\Grdl. DiMe\\Übungen\\Share mit Jakub und David - ImageJ\\plugins\\GDM\\digitalmedia\\orchid.jpg");

		DM_U2_S0544832 pw = new DM_U2_S0544832();
		pw.imp = IJ.getImage();
		pw.run("");
	}

	private static void conversionTest() {
		System.out.println("Color CONVERSIONTEST");
		for (int r =0; r< 256; r++) 
		{
			for (int g = 0; g<256; g++) 
			{
				for  (int b = 0; b<256; b++) 
				{
					double[] YCbCrResult = RGB2YCbCr( r, g, b);

					int[] backConverted = YCbCr2RGB(YCbCrResult[0], YCbCrResult[1], YCbCrResult[2]);

					if ( r != backConverted[0] || g != backConverted[1] ||
							b !=  backConverted[2])
					{
						System.out.println( "Your Conversion failed: r: "+ r + " g: " +g + " b: "+b + 
								" != " + backConverted[0] + ", " + backConverted[1]+", "+ backConverted[2]);
					}
				}
			}
		}
		System.out.println("done");
	}

	private static double[] RGB2YCbCr(int r, int g, int b)
	{
		// RGB --> YCbCr 
		double lum = 0.299*r + 0.587*g + 0.114*b; 
		double cb = -0.168736*r - 0.331264*g + 0.5*b;
		double cr = 0.5*r -0.418688*g -0.081312*b; 

		return new double[] {lum, cb, cr}; 
	}

	private static int[] YCbCr2RGB(double lum, double cb, double cr)
	{
		int rn = (int)Math.round((lum + 1.402*cr)); 
		int gn = (int)Math.round((lum - 0.3441*cb - 0.7141*cr));
		int bn = (int)Math.round((lum + 1.772*cb));

		return new int[] {rn, gn, bn}; 
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

	class CustomWindow extends ImageWindow implements ChangeListener {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private JSlider jSliderBrightness;
		private JSlider jSliderContrast;
		private JSlider jSliderSaturation;
		private JSlider jSliderHue;
		private double brightness;
		private double contrast;
		private double saturation; 
		private double hueDegree; 


		CustomWindow(ImagePlus imp, ImageCanvas ic) 
		{
			super(imp, ic);
			setInitialValues();
			addPanel();
		}

		void addPanel() 
		{
			//JPanel panel = new JPanel();
			Panel panel = new Panel();

			panel.setLayout(new GridLayout(4, 1));
			jSliderBrightness = makeTitledSlider("Helligkeit", -128, 128, (int)(brightness));
			jSliderContrast = makeTitledSlider("Kontrast", 0, 100, (int)(contrast));
			jSliderSaturation = makeTitledSlider("Sättigung", 0, 50, (int)(saturation));
			jSliderHue = makeTitledSlider("Farbton", 0, 360, (int)hueDegree);
			panel.add(jSliderBrightness);
			panel.add(jSliderContrast);
			panel.add(jSliderSaturation);
			panel.add(jSliderHue);

			add(panel);
			pack();
		}

		private JSlider makeTitledSlider(String string, int minVal, int maxVal, int val) {

			JSlider slider = new JSlider(JSlider.HORIZONTAL, minVal, maxVal, val );
			Dimension preferredSize = new Dimension(width, 50);
			slider.setPreferredSize(preferredSize);
			TitledBorder tb = new TitledBorder(BorderFactory.createEtchedBorder(), 
					string, TitledBorder.LEFT, TitledBorder.ABOVE_BOTTOM,
					new Font("Sans", Font.PLAIN, 11));
			slider.setBorder(tb);
			slider.setMajorTickSpacing((maxVal - minVal)/10 );
			slider.setPaintTicks(true);
			slider.addChangeListener(this);

			return slider;
		}

		private void setSliderTitle(JSlider slider, String str) {
			TitledBorder tb = new TitledBorder(BorderFactory.createEtchedBorder(),
					str, TitledBorder.LEFT, TitledBorder.ABOVE_BOTTOM,
					new Font("Sans", Font.PLAIN, 11));
			slider.setBorder(tb);
		}

		public void stateChanged( ChangeEvent e ){
			JSlider slider = (JSlider)e.getSource();
			
			if (slider == jSliderBrightness) 
			{
				brightness = slider.getValue(); // Überlauf korrigiert! (-100 weg)
				String str = "Helligkeit " + brightness; 
				setSliderTitle(jSliderBrightness, str); 
			}
			else if (slider == jSliderContrast) 
			{
				contrast = slider.getValue();
				String str = "Kontrast " + (contrast/10); 
				setSliderTitle(jSliderContrast, str); 
			}
			else if (slider == jSliderSaturation) 
			{
				saturation = slider.getValue();
				String str = "Sättigung " + (saturation / 10); 
				setSliderTitle(jSliderSaturation, str); 
			}
			else if (slider == jSliderHue) 
			{
				hueDegree = slider.getValue();
				String str = "Farbton " + hueDegree; 
				setSliderTitle(jSliderHue, str); 
			}
			changePixelValues(imp.getProcessor());
			imp.updateAndDraw();
		}

		private void changePixelValues(ImageProcessor ip) 
		{

			// Array fuer den Zugriff auf die Pixelwerte
			int[] pixels = (int[])ip.getPixels();
			
			
			double hueRadian = Math.toRadians(hueDegree);
			double cosHue = Math.cos(hueRadian) ;
			double sinHue = Math.sin(hueRadian);
			

			for (int y=0; y<height; y++) 
			{
				for (int x=0; x<width; x++) 
				{
					int pos = y*width + x;
					int argb = origPixels[pos];  // Lesen der Originalwerte 

					int r = (argb >> 16) & 0xff;
					int g = (argb >>  8) & 0xff;
					int b =  argb        & 0xff;


					double[] YCbCr = RGB2YCbCr(r, g, b);

					YCbCr = adjustBrightness(YCbCr[0], YCbCr[1], YCbCr[2]);
					YCbCr = adjustHue(YCbCr[0], YCbCr[1], YCbCr[2], cosHue, sinHue);
					YCbCr = adjustSaturation(YCbCr[0], YCbCr[1], YCbCr[2]);
					YCbCr = adjustContrast(YCbCr[0], YCbCr[1], YCbCr[2]);


					int[] newRGB = YCbCr2RGB(YCbCr[0], YCbCr[1], YCbCr[2]);

					// Hier muessen die neuen RGB-Werte wieder auf den Bereich von 0 bis 255 begrenzt werden
					newRGB[0] = constrainRGB(newRGB[0]);
					newRGB[1] = constrainRGB(newRGB[1]);
					newRGB[2] = constrainRGB(newRGB[2]);


					pixels[pos] = (0xFF<<24) | (newRGB[0]<<16) | (newRGB[1]<<8) | newRGB[2];
				}
			}
		}

		private double[] adjustBrightness(double lum, double cb, double cr)
		{
			lum = lum + brightness; 
			
			return new double[] {lum, cb, cr}; 
		}

		private double[] adjustHue(double lum, double cb, double cr, double cosHue, double sinHue)
		{
			double nCb = (cosHue * cb) + (sinHue * cr);
			double nCr = ((-sinHue) * cb) + (cosHue * cr);
			
			return new double[] {lum, nCb, nCr};
		}	

		private double[] adjustSaturation(double lum, double cb, double cr)
		{
			double nCb = ((saturation/10) * cb);
			double nCr = ((saturation/10) * cr);
			
			return new double[] {lum, nCb, nCr};
		}

		private double[] adjustContrast(double lum, double cb, double cr)
		{
			double nLuma = ((lum - 128) * (contrast/10) + 128);
			
			return new double[] {nLuma, cb, cr};
		}

		private int constrainRGB(int value)
		{
			if(value > 255) {
				return 255; }
			else if (value < 0) {
				return 0; }

			return value;
		}

		private void setInitialValues()
		{
			saturation = 10;
			brightness = 0;
			hueDegree = 0;
			contrast = 10;
		}
	}

} // CustomWindow inner class

