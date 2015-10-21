package net.eci_usa.hilbertvis;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

public class SpiralMappingTest
{

	@Test
	public void test()
	{
		for (int i = 0; i < 100; i++)
		{
			int[] coords1 = getPosition(i);
			int[] coords2 = getTileCoordinates(i);
			
			//System.out.println("Method 1: " + Arrays.toString(coords1));
			System.out.println("Method 2: " + i + ": " +  Arrays.toString(coords2));
		}
	}

	public int[] getPosition(int n)
	{
		// given n an index in the squared spiral
		// p the sum of point in inner square
		// a the position on the current square
		// n = p + a

		int r = (int) Math.floor((Math.sqrt(n + 1) - 1) / 2) + 1;

		// compute radius : inverse arithmetic sum of 8+16+24+...=
		int p = 4 * r * (r - 1);
		// compute total point on radius -1 : arithmetic sum of 8+16+24+...

		int en = r * 2;
		// points by face

		int a = (1 + n - p) % (r * 8);
		// compute de position and shift it so the first is (-r,-r) but (-r+1,-r)
		// so square can connect

		int pos[] = new int[] { 0, 0, r };
		int xx = (int) Math.floor(a / (r * 2));
		switch (xx)
		{
		// find the face : 0 top, 1 right, 2, bottom, 3 left
		case 0:
		{
			pos[0] = (int) (a - r);
			pos[1] = -r;
		}
			break;
		case 1:
		{
			pos[0] = r;
			pos[1] = (int) ((a % en) - r);

		}
			break;
		case 2:
		{
			pos[0] = (int) (r - (a % en));
			pos[1] = r;
		}
			break;
		case 3:
		{
			pos[0] = -r;
			pos[1] = (int) (r - (a % en));
		}
			break;
		}
		//System.out.println("n : " + n + " r : " + r + " p : " + p + " a : " + a + "  -->  " + Arrays.toString(pos));
		return pos;
	}

	public int[] getTileCoordinates(int tileNum)
	{
		int intRoot = (int) Math.floor(Math.sqrt(tileNum));

		int x = (int) ((Math.round(intRoot / 2f) * Math.pow(-1, intRoot + 1)) + (Math.pow(-1, intRoot + 1)
				* (((intRoot * (intRoot + 1)) - tileNum) - Math.abs((intRoot * (intRoot + 1)) - tileNum)) / 2f));

		int y = (int) ((Math.round(intRoot / 2f) * Math.pow(-1, intRoot)) + (Math.pow(-1, intRoot + 1)
				* (((intRoot * (intRoot + 1)) - tileNum) + Math.abs((intRoot * (intRoot + 1)) - tileNum)) / 2f));

		return new int[] { x, y };
	}
}
