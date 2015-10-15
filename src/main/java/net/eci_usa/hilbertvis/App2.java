package net.eci_usa.hilbertvis;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;

public class App2
{
	public static void main(String args[]) throws IOException
	{
		HilbertMapping hm = new HilbertMapping(9);
		
		int[][] nlnm = computeNonLocalNeighborMeasurement(hm);
		dumpStats(nlnm);
		
//		BufferedImage bi = writeToImage(nlnm);
//		ImageIO.write(bi, "gif", new File("./nlnm.gif"));
		
		writeAnimiated(hm, nlnm, 20, new File("./nlnm.gif"));
		
//		nlnm = computeNonLocalNeighborMeasurement2(hm);
//		dumpStats(nlnm);
//		bi = writeToImage(nlnm);
//		ImageIO.write(bi, "jpg", new File("./nlnm2.jpg"));
	}
	
	public static void writeAnimiated(HilbertMapping hm, int[][] data, int stepCount, File f) throws FileNotFoundException, IOException
	{
		ImageOutputStream output = new FileImageOutputStream(f);
		GifSequenceWriter writer = null;
		
		float stepSize = (float) 1 / (float)stepCount;
		
		for(int step = 0; step < stepCount; step++)
		{
			BufferedImage bi = createBufferedImageFromData(data, (step+1) * stepSize );
			if ( writer == null )
			{
				writer = new GifSequenceWriter(output, bi.getType(), 500, true);
			}
			writer.writeToSequence(bi);
		}
		for(int step = stepCount-1; step >= 0; step--)
		{
			BufferedImage bi = createBufferedImageFromData(data, (step+1) * stepSize );
			writer.writeToSequence(bi);
		}
		writer.close();
		output.close();
	}
	
	static int[][] computeNonLocalNeighborMeasurement(HilbertMapping hm)
	{
		int width = hm.getWidth();
		
		int nonlocalNeighborMeasurement[][] = new int[width][width];
		
		for(int x = 1; x < width-1; x++)
		{
			for(int y = 1; y < width-1; y++)
			{
				int index = hm.getIndex(x, y);
				
				int indexUp = hm.getIndex(x, y-1);
				int indexDown = hm.getIndex(x, y+1);
				int indexLeft = hm.getIndex(x-1, y);
				int indexRight = hm.getIndex(x+1, y);
				
				int nlnm = Math.abs(index - indexUp) + Math.abs(index - indexDown) + Math.abs(index - indexLeft) + Math.abs(index - indexRight);
				
				nonlocalNeighborMeasurement[x][y] = nlnm;
			}
		}
		
		return nonlocalNeighborMeasurement;
	}
	
	static double distance(int xy1[], int xy2[])
	{
		int dx = xy1[0] - xy2[0];
		int dy = xy1[1] - xy2[1];
		return Math.sqrt( dx*dx + dy*dy );
	}
	
	static int[][] computeNonLocalNeighborMeasurement2(HilbertMapping hm)
	{
		int width = hm.getWidth();
		
		int nlnm[][] = new int[width][width];
		
		for(int index = 1; index < hm.getLength()-1; index++)
		{
			int xy[] = hm.getCoords(index);
			
			int l[] = hm.getCoords(index-1);
			int r[] = hm.getCoords(index+1);
			
			double dist = distance(xy, l) + distance(xy, r);
			nlnm[xy[0]][xy[1]] = (int)dist;
		}
		return nlnm;
	}

	static int[] stats(int[][] data)
	{
		int width = data.length;
		int ht = data[0].length;
		int count = 0;
		
		long total = 0;
		int min = data[1][1];
		int max = data[1][1];
		for(int x = 1; x < width-1; x++)
		{
			for(int y = 1; y < ht-1; y++)
			{
				int val = data[x][y];
				
				if ( min > val ) min = val;
				if ( max < val ) max = val;
				total += val;
				count++;
			}
		}
		int avg = (int)(total / count);
		
		return new int[] { min, max, avg };
	}
	
	static void dumpStats(int[][] data)
	{
		int width = data.length;
		int ht = data[0].length;
		int count = 0;
		
		long total = 0;
		int min = data[1][1];
		int max = data[1][1];
		for(int x = 1; x < width-1; x++)
		{
			for(int y = 1; y < ht-1; y++)
			{
				int val = data[x][y];
				
				if ( min > val ) min = val;
				if ( max < val ) max = val;
				total += val;
				count++;
			}
		}
		int avg = (int)(total / count);
		
		System.out.println("W/H: " + width + ", " + ht);
		System.out.println("Min/max: " + min + ", " + max);
		System.out.println("avg: " + avg);
	}
	
	public static BufferedImage writeToImage(int[][] data)
	{
		int[] stats = stats(data);
		float vallimit = stats[2];
		int width = data.length;
		int ht = data[0].length;
		
		float[] hsbVals = Color.RGBtoHSB(Color.GREEN.getRed(), Color.GREEN.getGreen(), Color.GREEN.getBlue(), null);
		float[] hsbVals2 = Color.RGBtoHSB(Color.RED.getRed(), Color.RED.getGreen(), Color.RED.getBlue(), null);

		BufferedImage bi = new BufferedImage(width, ht, BufferedImage.TYPE_INT_RGB );
		for(int x = 0; x < width; x++)
		{
			for(int y = 0; y < ht; y++)
			{
				int val = data[x][y];
				float[] hsb = hsbVals;
				if ( val > vallimit )
				{
					hsb = hsbVals2;
					val -= vallimit;
				}
				if ( val > vallimit ) val = (int)vallimit;
				float b = (float)val / (float)vallimit;
				
				int rgb = Color.HSBtoRGB(hsb[0], hsb[1], b /*hsbVals[2]*/);
				bi.setRGB(x, y,  rgb);
			}
		}
		
		return bi;
	}
	
	public static BufferedImage createBufferedImageFromData(int[][] data, float limitCutoff)
	{
		int[] stats = stats(data);
		float vallimit = stats[1] * limitCutoff;
		System.out.println("CreateBufferedImageFromData, rawlimit: " + stats[2] + ", cutoff: " + vallimit + ", " + limitCutoff);
		int width = data.length;
		int ht = data[0].length;
		
		float[] hsbVals = Color.RGBtoHSB(Color.GREEN.getRed(), Color.GREEN.getGreen(), Color.GREEN.getBlue(), null);

		BufferedImage bi = new BufferedImage(width, ht, BufferedImage.TYPE_INT_RGB );
		for(int x = 0; x < width; x++)
		{
			for(int y = 0; y < ht; y++)
			{
				int val = data[x][y];
				float[] hsb = hsbVals;
				if ( val > vallimit ) val = (int) vallimit;
				float b = (float)val / (float)vallimit;
				
				int rgb = Color.HSBtoRGB(hsb[0], hsb[1], b );
				bi.setRGB(x, y,  rgb);
			}
		}
		
		return bi;
	}
	
}
