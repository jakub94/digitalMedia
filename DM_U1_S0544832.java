package digitalmedia;

import gdmvalidation.Ueb1Validation;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.gui.NewImage;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;

//erste Uebung (elementare Bilderzeugung)
public class DM_U1_S0544832 implements PlugIn {

	final static String[] choices = { "Schwarzes Bild", "Gelbes Bild",
		"Schwarz/Weiss Verlauf",
		"Horiz. Schwarz/Rot vert. Schwarz/Blau Verlauf",
		"Italienische Fahne", "Bahamische Fahne", "Japanische Fahne",
		"Japanische Fahne mit weichen Kanten","Sexy Streifen" };

	private String choice;

	public static void main(String args[]) {
		ImageJ ij = new ImageJ(); // neue ImageJ Instanz starten und anzeigen
		ij.exitWhenQuitting(true);

		DM_U1_S0544832 imageGeneration = new DM_U1_S0544832();
		imageGeneration.run("");
	}

	public void run(String arg) {

		int width = 566; // Breite
		int height = 400; // Hoehe

		// RGB-Bild erzeugen
		ImagePlus imagePlus = NewImage.createRGBImage("DM_U1", width, height,
				1, NewImage.FILL_BLACK);
		ImageProcessor ip = imagePlus.getProcessor();

		// Arrays fuer den Zugriff auf die Pixelwerte
		int[] pixels = (int[]) ip.getPixels();

		dialog();

		// //////////////////////////////////////////////////////////////
		if (choice.equals("Schwarzes Bild")) {
			int r,g,b;
			r = g = b = 0;
			setImageToColor(pixels, width, height,r, g, b);
		} else if (choice.equals("Gelbes Bild")) {
			int r,g,b;
			r = g = 255;
			b = 0;
			//TODO set the color to yellow
			setImageToColor(pixels, width, height,r, g, b);
		} else if (choice.equals("Schwarz/Weiss Verlauf")) {
			blackToWhite(pixels, width, height);
			Ueb1Validation.validateBlackToWhite(pixels, width, height);
		} else if (choice.equals("Horiz. Schwarz/Rot vert. Schwarz/Blau Verlauf")) {
			black2RedAndBlack2Blue(pixels, width, height);
			Ueb1Validation.validateBlack2RedAndBlack2Blue(pixels, width, height);
		} else if (choice.equals("Italienische Fahne")) {
			flagItalian(pixels, width, height);
			Ueb1Validation.validateFlagItalian(pixels, width, height);
		} else if (choice.equals("Bahamische Fahne")) {
			flagBahamian(pixels, width, height);
		} else if (choice.equals("Japanische Fahne")) {
			flagOfJapan(pixels, width, height);
		} else if (choice.equals("Japanische Fahne mit weichen Kanten")) {
			flagOfJapanSmooth(pixels, width, height);}
		else if (choice.equals("Sexy Streifen")) {
			drawStripes(pixels, width, height, 8);
		}

		// //////////////////////////////////////////////////////////////////

		// neues Bild anzeigen
		imagePlus.show();
		imagePlus.updateAndDraw();
	}

	private void flagOfJapanSmooth(int[] pixels, int width, int height) 
	{
		double innerRadius = 100;	
		double outerRadius = 170;   
		int midX = width / 2;
		int midY = height / 2;

		double difX; 	//delta x
		double difXsq;	//(delta x)² 

		double difY;	//delta y
		double difYsq; 	//(delta y)²
		double rsq;		//Fläche der Hypothenuse
		int r,g,b;

		// Schleife ueber die y-Werte		
		for (int y = 0; y < height; y++) {
			// Schleife ueber die x-Werte
			for (int x = 0; x < width; x++) 
			{
				r = g = b = 0;
				int pos = y * width + x; // Arrayposition bestimmen

				difX = Math.abs(x - midX);
				difY = Math.abs(y - midY);
				difXsq = Math.pow(difX, 2);
				difYsq = Math.pow(difY, 2);
				rsq = difYsq + difXsq;


				if((Math.pow(innerRadius, 2)) > rsq)
					r = 255;

				else if((Math.pow(outerRadius, 2)) < rsq)
					r = g = b = 255;

				else 
				{
					r = 255;
					double gbValue = (Math.sqrt(rsq) - innerRadius) / (outerRadius - innerRadius) * 255;
					g = b = (int)gbValue;
				}
				pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) | b;
			}
		}

	}

	private void flagOfJapan(int[] pixels, int width, int height) 
	{

		double radius = 100; 
		int midX = width / 2;
		int midY = height / 2;

		int difX; 	//delta x
		int difXsq;	//(delta x)² 

		int difY;	//delta y
		int difYsq; 	//(delta y)²
		int rsq;		//Fläche gebildet durch Quadrat der Hypothenuse
		int r,g,b;
		// Schleife ueber die y-Werte		
		for (int y = 0; y < height; y++) 
		{
			// Schleife ueber die x-Werte
			for (int x = 0; x < width; x++) 
			{
				r = g = b = 0;
				int pos = y * width + x; // Arrayposition bestimmen

				difX = (x - midX);
				difY = (y - midY);
				difXsq = (difX*difX);
				difYsq = (difY*difY);
				rsq = difYsq + difXsq;

				if((Math.pow(radius, 2)) > rsq)
					r = 255;
				else
					r = g = b = 255;

				pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) | b;
			}
		}
	}

	private void flagBahamian(int[] pixels, int width, int height) 
	{	
		double yMid = height / 2; // Mittelwert der Höhe. Hier muss das Dreieck wieder zurück laufen
		double third = width / 3;  // Drittel der Breite. Hier liegt die "Spitze" des Dreiecks
		double step = yMid / third; // Schrittweite. Wird mit y-Koordinate mulipliziert um die Reichweite der gezeichneten Pixel zu bestimmen
		int r,g,b;
		r = g = b = 0;
		int factor = 0; 

		drawBahamianBackground(pixels, width, height);  //Erstelle den Hintergrund der Flagge (Blau/Gelb/Blau)

		// Schleife ueber die y-Werte
		for (int y = 0; y < height; y++) 
		{
			// Schleife ueber die x-Werte

			if(y < yMid) 
				factor = y; // untere Hälfte 
			else
				factor--;// obere Hälfte				
			for (int x = 0; x < (int)(step * factor); x++) 
			{
				int pos = y * width + x; // Arrayposition bestimmen


				r = g = b = 0;
				pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) | b;
			}
		} 
	}

	/**
	 * 
	 * @param pixels
	 * @param width
	 * @param height
	 * @description Zeichnet den Hintergrund für die Flagge der Bahamas 
	 */
	private void drawBahamianBackground(int[] pixels, int width, int height) 
	{
		// Schleife ueber die y-Werte
		int yValue = height/3;
		int r,g,b;
		r = g = b = 0;
		for (int y = 0; y < height; y++) 
		{
			// Schleife ueber die x-Werte
			if((y < yValue) || y > 2*yValue) //Zeichne Blaue Abschnitte
			{
				r = g = 0;
				b = 255; 

			}
			else //Zeichne Gelben Abschnitt 
			{
				r = 255;
				g = 255;
				b = 0;
			}

			for (int x = 0; x < width; x++) 
			{
				int pos = y * width + x; // Arrayposition bestimmen

				pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) | b;
			}
		}
	}

	private void flagItalian(int[] pixels, int width, int height) {

		// Schleife ueber die y-Werte
		for (int y = 0; y < height; y++) {

			// Schleife ueber die x-Werte
			for (int x = 0; x < width; x++) {

				int pos = y * width + x; // Arrayposition bestimmen
				int r,g,b;
				r = g = b = 0;

				if(x < (width/3)) //Erstes Drittel Grün
				{
					r = b = 0;
					g = 255;
				}

				else if (  x < ((width / 3)*2)) //Zweites Drittel Weiß
				{
					r = g = b = 255;
				}
				else //Letztes Drittel Rot
				{				
					g = b = 0;
					r = 255;					
				}				
				pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) | b;
			}
		}
	}

	private void black2RedAndBlack2Blue(int[] pixels, int width, int height) 
	{	
		double max = 255; 				//Maxmimaler Wert für RGB
		double xFactor = max/(width-1); 	//Wird mit x multipliziert um die Veränderung von Blau zu bestimmen bestimmen 
		double yFactor = max/(height-1); 	//Wird mit y multipliziert um die Veränderung von Rot zu bestimmen 
		int r,g,b;
		r = g = b = 0; 

		// Schleife ueber die y-Werte
		for (int y = 0; y < height; y++) 
		{
			b = (int)(y * yFactor); 	//Weise neuen Wert für Blau zu 

			// Schleife ueber die x-Werte
			for (int x = 0; x < width; x++) 
			{
				int pos = y * width + x; // Arrayposition bestimmen

				r = (int)(x * xFactor); // Weise neuen Wert für Rot zu

				pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) | b;
			}
		}
	}

	private void blackToWhite(int[] pixels, int width, int height) 
	{

/*		// Schleife ueber die y-Werte
		for (int y = 0; y < height; y++) {
			// Schleife ueber die x-Werte
			int r,g,b;
			r = g = b = 0;

			for (int x = 0; x < width; x++) 
			{
				int pos = y * width + x; // Arrayposition bestimmen

				double max = 255; 					//Maximaler Wert für RGB
				double xFactor = max/(width-1); 		//Faktor mit dem X multipliziert wird um die Farbveränderung zwischen den Schritten zu bestimmen 
				int rgbValue = (int)(x * xFactor); 	//Weise neun Farbwert zu 
				r = g = b = rgbValue;

				pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) | b;
			}
		}*/
		
		double quarter = width / 4; 
		// Schleife ueber die y-Werte
		for (int y = 0; y < height; y++) {
			// Schleife ueber die x-Werte
			int r,g,b;
			r = g = b = 255;
			

			for (int x = 0; x < width; x++) 
			{
				int pos = y * width + x; // Arrayposition bestimmen

				if(x <= quarter )
				{
					r = 255;
					g = 0;
					b = 0; 					
				}
				else if ( x <= 2* quarter)
				{
					g = 255;
					b =  (int)(- 255/quarter * (x - quarter) + 255);
				}
				
				else if ( x <= 3* quarter)
				{
					g = 255;
					b =  (int)(- 255/quarter * (x - 2*quarter) + 255);
				}
				

				pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) | b;
			}
		}
	}

	private void drawStripes(int[] pixels, int width, int height, int numberOfStripes) 
	{

		// Schleife ueber die y-Werte
		int stripeWidth = width / numberOfStripes;
		int remainder = width % stripeWidth;
		if(remainder != 0)
			stripeWidth++;


		for (int y = 0; y < height; y++) {
			// Schleife ueber die x-Werte
			int r,g,b;
			r = g = b = 0;

			for (int x = 0; x < width; x++) 
			{
				if(((x / stripeWidth) % 2) == 0)
				{
					r=g=b=255;					
					if((remainder != 0) && (stripeWidth == (width/numberOfStripes)))
						remainder = remainder - 2;
					if((remainder <= 0) && (stripeWidth == (width/numberOfStripes)))
						stripeWidth--;
				}
				else 
					r = g = b = 0;

				int pos = y * width + x; // Arrayposition bestimmen
				pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) | b;
			}
		}
	}

	private void setImageToColor(int[] pixels, int width, int height,int r, int g, int b) {
		int color = 0xFF000000 | (r << 16) | (g << 8) | b;
		// Schleife ueber die y-Werte
		for (int y = 0; y < height; y++) {
			// Schleife ueber die x-Werte
			for (int x = 0; x < width; x++) {
				int pos = y * width + x; // Arrayposition bestimmen
				// Werte zurueckschreiben
				pixels[pos] = color;
			}
		}
	}
	private void dialog() {
		// Dialog fuer Auswahl der Bilderzeugung
		GenericDialog gd = new GenericDialog("Bildart");
		gd.addChoice("Bildtyp", choices, choices[0]);
		gd.showDialog(); // generiere Eingabefenster
		choice = gd.getNextChoice(); // Auswahl uebernehmen

		if (gd.wasCanceled())
			System.exit(0);
	}
}