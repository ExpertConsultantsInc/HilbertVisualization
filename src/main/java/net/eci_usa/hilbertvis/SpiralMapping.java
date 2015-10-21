package net.eci_usa.hilbertvis;

public class SpiralMapping implements CoordMapper
{
	int offset;
	int[][] coordsToIndexMapping;
	
	public SpiralMapping(int width)
	{
		this.offset = (width/2) -1;
		coordsToIndexMapping = new int[width][width];
		for(int i = 0; i < width*width; i++)
		{
			int[] xy = getCoords(i);
			coordsToIndexMapping[xy[0]][xy[1]] = i;
		}
	}

	public int getWidth()
	{
		return coordsToIndexMapping.length;
	}

	public int getHeight()
	{
		return coordsToIndexMapping.length;
	}

	public int getLength()
	{
		return coordsToIndexMapping.length * coordsToIndexMapping.length;
	}

	public int getIndex(int x, int y)
	{
		return coordsToIndexMapping[x][y];
	}

	public int[] getCoords(int index)
	{
		int intRoot = (int) Math.floor(Math.sqrt(index));

		int x = (int) ((Math.round(intRoot / 2f) * Math.pow(-1, intRoot + 1)) + (Math.pow(-1, intRoot + 1)
				* (((intRoot * (intRoot + 1)) - index) - Math.abs((intRoot * (intRoot + 1)) - index)) / 2f));

		int y = (int) ((Math.round(intRoot / 2f) * Math.pow(-1, intRoot)) + (Math.pow(-1, intRoot + 1)
				* (((intRoot * (intRoot + 1)) - index) + Math.abs((intRoot * (intRoot + 1)) - index)) / 2f));

		return new int[] { x+offset, y+offset+1 };
	}

	public String getMapperName()
	{
		return "spiral";
	}

}
