package net.eci_usa.hilbertvis;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;

import net.eci_usa.hilbertvis.FFT.FFTOutput;

public class App2
{
	static Map<String, CoordMapper> mappers = new HashMap<>();
	
	public static CoordMapper addMapper(CoordMapper cm)
	{
		mappers.put(cm.getMapperName(), cm);
		return cm;
	}
	
	public static List<File> getInputFiles()
	{
		File srcDir = new File("./samples_2");
		File[] files = srcDir.listFiles( (File f) -> f.getName().endsWith(".wav") );
		return Arrays.asList(files);
	}
	
	public static void main(String args[]) throws IOException
	{
		HilbertMapping hm = new HilbertMapping(8);
		doMapper(hm, hm.getLength() / 30);
		
		SpiralMapping sm = new SpiralMapping(256);
		doMapper(sm, sm.getLength() / 53);

		ZigzagMapping zm = new ZigzagMapping(256);
		doMapper(zm, zm.getLength() / 23);
		
		ZigzagMapping2 zm2 = new ZigzagMapping2(256);
		doMapper(zm2, zm2.getLength() / 23);
		
		List<File> inputFiles = getInputFiles();

		for(File f : inputFiles)
		{
			String outname = f.getName().replaceAll("\\.[Ww][Aa][Vv]", "") + "_waterfall_fft.gif"; 
			writeFFTWaterfallGIF(f, 1024, new File(f.getParentFile(), outname), 1000 );
		}
		
		for(CoordMapper cm : mappers.values())
		{
			for(File f : inputFiles)
			{
				String outname = f.getName().replaceAll("\\.[Ww][Aa][Vv]", "") + "_" + cm.getMapperName() + "_fft.gif"; 
				File outputFile = new File( f.getParentFile(), outname);
				
				writeAnimatedFFTGIF(cm, f, cm.getLength(), outputFile, 100);
			}
		}
	}
	
	public static void doMapper(CoordMapper cm, int sweepWidth) throws FileNotFoundException, IOException
	{
		addMapper(cm);
		
//		writeAnimiatedNonLocalNeighborMeasurementGIF(cm, 20, new File("./nlnm_" + cm.getMapperName() + ".gif"));
//		writeAnimiatedNonLocalNeighborMeasurement2GIF(cm, 20, new File("./nlnm2_" + cm.getMapperName() + ".gif"));
//		writeAnimatedBlobSweepGIF(cm, sweepWidth, new File("./sweep_" + cm.getMapperName() + ".gif"));
	}

	static class RowInfo
	{
		int rownum;
	}
	public static void writeFFTWaterfallGIF(File inputFile, int fftWidth, File destGif, int maxHt) throws FileNotFoundException, IOException
	{
		destGif.delete();
		ImageOutputStream output = new FileImageOutputStream(destGif);
		int frameLenMS = 100;

		WAV wav = new WAV( inputFile );
		FFT fft = new FFT( fftWidth );
		float squelch = 0.8f;
		BufferedImage bi = new BufferedImage(fftWidth, maxHt, BufferedImage.TYPE_INT_RGB );
		RowInfo ri = new RowInfo();
		
		wav.processSamples( fftWidth, (ComplexData cd) -> 
		{
			FFTOutput fftoutput = fft.process(cd);
			
			double data[] = fftoutput.mags;
			double minmaxavg[] = getMinMaxAvg(data);
			double range = (minmaxavg[1] - minmaxavg[0]) * squelch;
			
			float[] hsb = Color.RGBtoHSB(Color.GREEN.getRed(), Color.GREEN.getGreen(), Color.GREEN.getBlue(), null);

			for(int i = 0; i < data.length; i++)
			{
				double d = data[i];
				float adj = (float)((d-minmaxavg[0]) / range);	// adj = [0..1]
				
				int rgb = Color.HSBtoRGB(hsb[0], hsb[1], adj );
				
				bi.setRGB(i, ri.rownum, rgb);
			}
			ri.rownum++;
			
			return ri.rownum < maxHt;
		} );

		GifSequenceWriter writer = new GifSequenceWriter(output, BufferedImage.TYPE_INT_RGB, frameLenMS, true);
		try
		{
			writer.writeToSequence(bi);
		}
		catch (Exception e)
		{
		}
		
		System.out.println("Wrote frame " + writer.getFrameCount() + " to " + destGif);
		
		writer.close();
		output.close();
		
		System.out.println("Finished writing FFT waterfall gif " + destGif + " from " + inputFile);
	}
	
	

	public static void writeAnimatedFFTGIF(CoordMapper cm, File inputFile, int fftWidth, File destGif, int maxFrameCount) throws FileNotFoundException, IOException
	{
		destGif.delete();
		ImageOutputStream output = new FileImageOutputStream(destGif);
		int frameLenMS = 100;
		GifSequenceWriter writer = new GifSequenceWriter(output, BufferedImage.TYPE_INT_RGB, frameLenMS, true);

		WAV wav = new WAV( inputFile );
		FFT fft = new FFT( fftWidth );
		
		wav.processSamples( fftWidth, (ComplexData data) -> 
		{
			FFTOutput fftoutput = fft.process(data);
			
			BufferedImage bi = createBufferedImageFromData(cm, fftoutput.mags, .2f);
			try
			{
				writer.writeToSequence(bi);
			}
			catch (Exception e)
			{
			}
			
			System.out.println("Wrote frame " + writer.getFrameCount() + " to " + destGif);
			
			return writer.getFrameCount() < maxFrameCount;
		} );

		BufferedImage finalBI = new BufferedImage(cm.getWidth(), cm.getHeight(), BufferedImage.TYPE_INT_RGB );
		writer.writeToSequence(finalBI);
		
		writer.close();
		output.close();
		
		System.out.println("Finished writing FFT gif " + destGif + " from " + inputFile);
	}
	
	
	static int sweepAnimationTotalLenMS = 5000;
	static float blobOverlap = 0.3f; // 30% overlap
	public static void writeAnimatedBlobSweepGIF(CoordMapper hm, int blobWidth, File f) throws FileNotFoundException, IOException
	{
		f.delete();
		ImageOutputStream output = new FileImageOutputStream(f);
		GifSequenceWriter writer = null;
		
		float stepSize = blobWidth * blobOverlap; //(hm.getLength() - blobWidth) / (float)stepCount;
		int frameCount = (int)((hm.getLength() - blobWidth) / stepSize);
		int frameLenMS = sweepAnimationTotalLenMS / frameCount;
		
		int blobCenterVal = 7;
		int blobEdgeVal = 3;
		int blobTraceVal = 1;
		float blobValStep = (blobCenterVal-blobEdgeVal)/(float)(blobWidth/2);
		
		System.out.println("Creating sweep for " + hm.getMapperName() + ", framecount " + frameCount + ", " + frameLenMS + "ms/frame");
		
		for(int step = 0; step < frameCount; step++)
		{
			int[][] data = new int[hm.getWidth()][hm.getWidth()];
			int leftOffset = (int)(step * stepSize);
			int blobCenter = leftOffset + blobWidth/2;
			for(int i = 0; i < leftOffset+blobWidth; i++)
			{
				int[] coords = hm.getCoords(i);
				int centerDist = Math.abs( blobCenter - i);
				int val = i < leftOffset ? blobTraceVal : (int)(blobCenterVal - centerDist*blobValStep);  
				data[coords[0]][coords[1]] = val;
			}
			BufferedImage bi = createBufferedImageFromData(data, 1 );
			if ( writer == null )
			{
				writer = new GifSequenceWriter(output, bi.getType(), frameLenMS, true);
			}
			writer.writeToSequence(bi);
		}
		writer.close();
		output.close();
	}
	
	
	public static void writeAnimiatedNonLocalNeighborMeasurementGIF(CoordMapper hm, int stepCount, File f) throws FileNotFoundException, IOException
	{
		int[][] data = computeNonLocalNeighborMeasurement(hm);
		
		f.delete();
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
	
	public static void writeAnimiatedNonLocalNeighborMeasurement2GIF(CoordMapper hm, int stepCount, File f) throws FileNotFoundException, IOException
	{
		int[][] data = computeNonLocalNeighborMeasurement2(hm);
		
		f.delete();
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
			if ( step == stepCount-1)
			{
				for(int x = 0; x < 10; x++) writer.writeToSequence(bi);
			}
		}
		for(int step = stepCount-1; step >= 0; step--)
		{
			BufferedImage bi = createBufferedImageFromData(data, (step+1) * stepSize );
			writer.writeToSequence(bi);
			if ( step == 0 )
			{
				for(int x = 0; x < 10; x++) writer.writeToSequence(bi);
			}
		}
		writer.close();
		output.close();
	}
	
	static int[][] computeNonLocalNeighborMeasurement(CoordMapper hm)
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
	
	interface PointHandler
	{
		public void handlePoint(int x, int y);
	}
	
	static void forEachPointInCircle(int cx, int cy, int radius, int max, PointHandler ph)
	{
		int x1 = cx - radius;
		int y1 = cy - radius;
		int x2 = cx + radius;
		int y2 = cy + radius;
		x1 = Math.max(0,  x1);
		y1 = Math.max(0,  y1);
		x2 = Math.min(x2, max);
		y2 = Math.min(y2,  max);
		int r2 = radius*radius;
		
		for(int x = x1; x < x2; x++)
		{
			for(int y = y1; y < y2; y++)
			{
				int dx = cx-x;
				int dy = cy-y;
				boolean incircle = (dx*dx + dy*dy) <= r2;
				if ( incircle ) ph.handlePoint(x, y);
			}
		}
	}
	
	static int[] getPointsInCircle(int[] center, int radius, int max)
	{
		int x1 = center[0] - radius;
		int y1 = center[1] - radius;
		int x2 = center[0] + radius;
		int y2 = center[1] + radius;
		x1 = Math.max(0,  x1);
		y1 = Math.max(0,  y1);
		x2 = Math.min(x2, max);
		y2 = Math.min(y2,  max);
		int r2 = radius*radius;
		
		for(int x = x1; x < x2; x++)
		{
			for(int y = y1; y < y2; y++)
			{
				int dx = center[0]-x;
				int dy = center[1]-y;
				boolean incircle = (dx*dx + dy*dy) <= r2;
			}
		}
		return null;
	}
	
	static int[][] computeNonLocalNeighborMeasurement(CoordMapper hm, int radius)
	{
		int width = hm.getWidth();
		
		int nonlocalNeighborMeasurement[][] = new int[width][width];
		
		for(int x = 1; x < width-1; x++)
		{
			for(int y = 1; y < width-1; y++)
			{
				int linearIndexCenter = hm.getIndex(x, y);
				
				forEachPointInCircle(x, y, radius, width, (px,py) -> nonlocalNeighborMeasurement[px][py] += Math.abs(hm.getIndex(px, py)-linearIndexCenter) );
				
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
	
	static int[][] computeNonLocalNeighborMeasurement2(CoordMapper hm)
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
		int min = data[0][0];
		int max = data[0][0];
		for(int x = 0; x < width; x++)
		{
			for(int y = 0; y < ht; y++)
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

	static double[] getMinMaxAvg(double[] data)
	{
		double max = data[0];
		double min = data[0];
		double total = 0;
		for(double d : data)
		{
			if ( min > d ) min = d;
			if ( max < d ) max = d;
			total += d;
		}
		double avg = total / (double)data.length;
		
		return new double[] { min, max, avg };
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
		//System.out.println("CreateBufferedImageFromData, rawlimit: " + stats[1] + ", cutoff: " + vallimit + ", " + limitCutoff);
		int ht = data.length;
		int width = data[0].length;
		
		float[] hsbVals = Color.RGBtoHSB(Color.GREEN.getRed(), Color.GREEN.getGreen(), Color.GREEN.getBlue(), null);

		BufferedImage bi = new BufferedImage(ht, width, BufferedImage.TYPE_INT_RGB );
		for(int x = 0; x < width; x++)
		{
			for(int y = 0; y < ht; y++)
			{
				int val = data[x][y];
				float[] hsb = hsbVals;
				if ( val > vallimit ) val = (int) vallimit;
				float b = (float)val / (float)vallimit;
				
				int rgb = Color.HSBtoRGB(hsb[0], hsb[1], b );
				bi.setRGB(x, y, rgb);
			}
		}
		
		String msg = String.format("Cutoff: %d (%2.0f%%)", (int)vallimit, limitCutoff*100);
		writeString(bi.createGraphics(), msg, 10, 10, TEXT_ALIGN.LEFT, Color.WHITE);
		
		return bi;
	}
	
	public static BufferedImage createBufferedImageFromData(CoordMapper cm, double[] data, float squelch )
	{
		double minmaxavg[] = getMinMaxAvg(data);
		double range = (minmaxavg[1] - minmaxavg[0]) * squelch;
		
		float[] hsb = Color.RGBtoHSB(Color.GREEN.getRed(), Color.GREEN.getGreen(), Color.GREEN.getBlue(), null);

		BufferedImage bi = new BufferedImage(cm.getWidth(), cm.getHeight(), BufferedImage.TYPE_INT_RGB );
		for(int i = 0; i < data.length && i < cm.getLength(); i++)
		{
			int coords[] = cm.getCoords(i);
			double d = data[i];
			float adj = (float)((d-minmaxavg[0]) / range);	// adj = [0..1]
			
			int rgb = Color.HSBtoRGB(hsb[0], hsb[1], adj );
			
			bi.setRGB(coords[0], coords[1], rgb);
		}
		
		return bi;
	}

	public static void writeString(Graphics2D g, String multiline, int x, int y, TEXT_ALIGN ta, Color tc)
	{
		String lines[] = multiline.split("\n");
		for (String s : lines)
		{
			Rectangle2D rect = g.getFontMetrics().getStringBounds(s, g);
			switch (ta)
			{
			case LEFT:
				break;
			case CENTER:
				x = x - (int) (rect.getWidth() / 2);
				break;
			case RIGHT:
				x = x - (int) rect.getWidth();
				break;
			}
			g.setColor(tc);
			g.drawString(s, x, y);
			y += rect.getHeight();
		}
	}

	public enum TEXT_ALIGN
	{
		LEFT, CENTER, RIGHT
	}	
	
}
