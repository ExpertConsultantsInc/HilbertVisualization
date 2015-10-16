package net.eci_usa.hilbertvis;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

public class SpiralMappingTest
{

	@Test
	public void test()
	{
		for(int i =0; i < 100; i++)
		{
			getPosition(i);
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
		double p = (8 * r * (r - 1)) / 2;
		// compute total point on radius -1 : arithmetic sum of 8+16+24+...

		double en = r * 2;
		// points by face

		double a = (1 + n - p) % (r * 8);
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
		System.out.println("n : " + n + " r : " + r + " p : " + p + " a : " + a + "  -->  " + Arrays.toString(pos));
		return pos;
	}

}
