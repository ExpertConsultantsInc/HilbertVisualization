package net.eci_usa.hilbertvis;

public class ZigzagMapping2 implements CoordMapper
{
	int width;
	
	public ZigzagMapping2(int width)
	{
		this.width = width;
	}

	public int getWidth()
	{
		return width;
	}

	public int getHeight()
	{
		return width;
	}

	public int getLength()
	{
		return width*width;
	}

	public int getIndex(int col, int row)
	{
		return row*width + (row % 2 == 0 ? col : width-col-1);
	}

	public int[] getCoords(int index)
	{
		int row = index / width;
		int[] result = new int[] { index % width, row };
		if ( row % 2 != 0 ) result[0] = width - result[0] - 1;
		if ( result[0] > width )
		{
			System.out.println("warning");
		}
		return result;
	}

	public String getMapperName()
	{
		return "zigzag2";
	}

}
