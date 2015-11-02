package net.eci_usa.hilbertvis;

public class ComplexData
{
	public interface ComplexDataSink
	{
		public boolean handleComplexData(ComplexData data);
	}
	
	public interface ComplexDataPipe
	{
		public ComplexDataPipe handleComplexData(ComplexData data);
	}
	
	
	
	public double[] data;
	
	public ComplexData(int pairCount)
	{
		this.data = new double[ pairCount * 2 ];
	}
	
	public ComplexData(double[] _data)
	{
		this.data = _data;
	}
	
	public ComplexData clone()
	{
		double[] tmp = data.clone();
		
		ComplexData result = new ComplexData(tmp);
		
		return result;
	}
	
	public int pairCount()
	{
		return data.length / 2;
	}
	
	public double[][] split()
	{
		int w = pairCount();
		double[][] result = new double[][] { new double[w], new double[w] };
		
		for(int i = 0; i < data.length/2; i++)
		{
			result[0][i] = data[i*2];
			result[1][i] = data[i*2 + 1];
		}
		
		return result;
	}
	
	public void joinInPlace(float[] i, float[] q)
	{
		for(int j = 0; j < i.length; j++)
		{
			data[j*2] = i[j];
			data[j*2+1] = q[j];
		}
		for(int j = i.length*2; j < data.length; j++ )
		{
			data[j] = 0;
		}
	}

	public void joinInPlace(float[] i, float[] q, int offset, int len)
	{
		for(int j = 0; j < len; j++)
		{
			data[j*2] = i[j+offset];
			data[j*2+1] = q[j+offset];
		}
		for(int j = len*2; j < data.length; j++ )
		{
			data[j] = 0;
		}
	}
	
	public static ComplexData join(double[] i, double[] q)
	{
		ComplexData result = new ComplexData( i.length );
		double[] data = result.data;
		for(int j = 0; j < i.length; j++)
		{
			data[j*2] = i[j];
			data[j*2+1] = q[j];
		}
		
		return result;
	}

	public static ComplexData join(float[] i, float[] q)
	{
		ComplexData result = new ComplexData( i.length );
		double[] data = result.data;
		for(int j = 0; j < i.length; j++)
		{
			data[j*2] = i[j];
			data[j*2+1] = q[j];
		}
		
		return result;
	}
}
