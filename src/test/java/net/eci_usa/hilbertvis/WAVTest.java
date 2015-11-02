package net.eci_usa.hilbertvis;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

public class WAVTest
{

	@Test
	public void test() throws IOException
	{
		WAV wav = new WAV( new File("./Capture_090508_153408 CF8700 Span 190KHz.wav") );
		
		wav.processSamples( 10, (ComplexData data) -> { System.out.println(data.data.length); return true; } );
	}

}
