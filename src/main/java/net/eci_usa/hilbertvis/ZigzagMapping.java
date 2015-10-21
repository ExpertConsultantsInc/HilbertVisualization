package net.eci_usa.hilbertvis;

public class ZigzagMapping implements CoordMapper
{
	int width;
	
	public ZigzagMapping(int width)
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
		return row*width + col;
	}

	public int[] getCoords(int index)
	{
		return new int[] { index % width, index / width };
	}

	public String getMapperName()
	{
		return "zigzag";
	}

}
