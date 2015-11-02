package net.eci_usa.hilbertvis;

import ca.uol.aig.fftpack.ComplexDoubleFFT;

public class FFT
{
	public static class FFTOutput
	{
		double[] mags;
		double[] phase;
		
		public FFTOutput(int bincount)
		{
			mags = new double[bincount];
			phase = new double[bincount];
		}
	}
	
	private ComplexDoubleFFT fft;
	
	public FFT(int width)
	{
		fft = new ComplexDoubleFFT(width);
	}
	
	public FFTOutput process(ComplexData data)
	{
		fft.ft( data.data );
		int bincount = data.pairCount();
		
		FFTOutput result = new FFTOutput(bincount);

		for ( int i = 0; i < data.data.length; i += 2 )
		{
			double r = data.data[i];
			double im = data.data[i + 1];

			double mag = Math.sqrt( im * im + r * r );
		
			result.mags[i / 2] = mag;
			result.phase[i / 2] = Math.atan2( im, r );
		}
		
		return result;
	}

}
