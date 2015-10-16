package net.eci_usa.hilbertvis;

public interface CoordMapper
{
	public int getWidth();
	public int getHeight();
	public int getLength();
	public int getIndex(int x, int y);
	public int[] getCoords(int index);
	public String getMapperName();

}
