package net.eci_usa.hilbertvis;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.AudioFormat.Encoding;

public class WAV
{
	protected byte[] buffer;

	private File file;

	int samplesPerSec;

	int bps;

	boolean signed;

	boolean be;

	AudioInputStream ais;

	int frameSize;
	
	int frameCount;

	public WAV( File f ) throws IOException
	{
		this.file = f;
		getParams(f);
	}
	
	protected void getParams( File f ) throws IOException
	{
		try
		{
			AudioInputStream tmp = AudioSystem.getAudioInputStream( f );
			AudioFormat af = tmp.getFormat();
			samplesPerSec = (int) af.getFrameRate();
			bps = af.getSampleSizeInBits();
			signed = af.getEncoding() == Encoding.PCM_SIGNED;
			be = af.isBigEndian();
			frameSize = af.getFrameSize();
			if ( bps != 16 ) throw new IOException( "Bad bps in wave file" );
			if ( !signed ) throw new IOException( "Unsupported signed in wave file" );
			frameCount = tmp.available() / frameSize;
			
			tmp.close();
		}
		catch ( UnsupportedAudioFileException e )
		{
			throw new IOException( e );
		}
	}
	
	public int processSamples( int chunkSize, ComplexData.ComplexDataSink complexDataSink )
	{
		int skip = chunkSize;
		FileInputStream fis = null;
		initBuffer( chunkSize * 2 );

		try
		{
			fis = openFile( file );

			ComplexData data = new ComplexData( chunkSize );
			int savecount = (chunkSize - skip) * 2;
			double[] saved = null;
			while ( true )
			{
				int elementsRead;
				if ( saved == null )
				{
					elementsRead = read( fis, data.data, 0, chunkSize * 2 );
					saved = new double[savecount];
				}
				else
				{
					System.arraycopy( saved, 0, data.data, 0, savecount );
					elementsRead = read( fis, data.data, savecount, skip * 2 );
					if ( elementsRead > 0 ) elementsRead += savecount;
				}

				if ( elementsRead == 0 ) break;

				Arrays.fill( data.data, elementsRead, data.data.length, 0 );

				System.arraycopy( data.data, skip * 2, saved, 0, savecount );

				if ( !complexDataSink.handleComplexData(data) ) break;
			}

		}
		catch ( IOException ioe )
		{
			throw new RuntimeException( ioe );
		}
		finally
		{
			if ( fis != null )
			{
				closeFile( fis );
			}
		}

		return 0;
	}

	protected FileInputStream openFile( File f ) throws IOException
	{
		FileInputStream fis = null;
		try
		{
			fis = new FileInputStream( f );
			ais = AudioSystem.getAudioInputStream( new BufferedInputStream(fis) );

			return fis;
		}
		catch ( UnsupportedAudioFileException e )
		{
			if ( fis != null ) fis.close();

			throw new IOException( e );
		}
	}

	protected void closeFile( FileInputStream fis )
	{
		try
		{
			ais.close();
		}
		catch ( IOException e )
		{
		}
		try
		{
			fis.close();
		}
		catch ( IOException e )
		{
		}
	}

	public int availableElements()
	{
		return frameCount;
	}

	protected int elementSize()
	{
		return frameSize;
	}

	protected void initBuffer( int elementCount )
	{
		buffer = new byte[elementCount * frameSize];
	}

	protected int read( InputStream is, double[] dest, int offset, int len ) throws IOException
	{
		int bytesread = ais.read( buffer, 0, frameSize * (len / 2) );
		if ( bytesread <= 0 ) return 0;
		Arrays.fill( buffer, bytesread, buffer.length, (byte) 0 );

		int shortsread = 0;
		for ( int i = 0; i < bytesread - 1; i += 2, shortsread++ )
		{
			short tmp = (short) ((buffer[i] & 0xff) | ((buffer[i + 1] & 0xff)) << 8);
			dest[offset + shortsread] = tmp;
		}
		return shortsread;
	}

	protected int read( InputStream is, float[] dest, int offset, int len ) throws IOException
	{
		int bytesread = ais.read( buffer, 0, frameSize * (len / 2) );
		if ( bytesread <= 0 ) return 0;
		Arrays.fill( buffer, bytesread, buffer.length, (byte) 0 );

		int shortsread = 0;
		for ( int i = 0; i < bytesread - 1; i += 2, shortsread++ )
		{
			short tmp = (short) ((buffer[i] & 0xff) | ((buffer[i + 1] & 0xff)) << 8);
			dest[offset + shortsread] = tmp;
		}
		return shortsread;
	}
	
	public int getSamplesPerSec()
	{
		return samplesPerSec;
	}

	public int getBitsPerSample()
	{
		return bps;
	}

	public boolean isBigEndian()
	{
		return be;
	}

}
