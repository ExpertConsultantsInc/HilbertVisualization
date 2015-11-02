package net.eci_usa.hilbertvis;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import net.eci_usa.hilbertvis.FFT.FFTOutput;

public class FFTTest
{

	@Test
	public void test() throws IOException
	{
		WAV wav = new WAV( new File("./Capture_090508_153408 CF8700 Span 190KHz.wav") );
		FFT fft = new FFT( 256 );
		
		wav.processSamples( 256, (ComplexData data) -> 
		{
			FFTOutput fftoutput = fft.process(data);
			return true;
		} );
		
	}

}
