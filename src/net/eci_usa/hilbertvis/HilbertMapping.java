package net.eci_usa.hilbertvis;

public class HilbertMapping
{
	/* code derived from https://github.com/rjoberon/hilbert-curve/blob/master/src/main/java/de/unikassel/cs/kde/statistics/hilbert/HilbertCurve.java */
	/*
	 * current positions
	 */
	private int cpX = 0;
	private int cpY = 0;
	private int cpIndex = 0;

	private int maxLevel;

	int[] xVals;
	int[] yVals;
	
	int[][] toIndexMap;
		
	public static double logOfBase(int base, int num)
	{
		return Math.log(num) / Math.log(base);
	}
	
	public HilbertMapping(int hilbertLevel)
	{
		this.cpX = 0;
		this.cpY = 0;
		this.maxLevel = hilbertLevel;
		int realSequenceLength = (int)Math.pow(4, maxLevel);
		int width = (int)Math.pow(2, maxLevel);
		
		System.out.println("maxlevel: " + maxLevel + ", real seq len: " + realSequenceLength + ", width: " + width);
		
		this.xVals = new int[ realSequenceLength ];
		this.yVals = new int[ realSequenceLength ];
		this.toIndexMap = new int[width][width];
		
		add(0, 0);
		compute_worker(maxLevel, Direction.UP);
	}
	
	public int getWidth()
	{
		return toIndexMap.length;
	}
	
	public int getLength()
	{
		return xVals.length;
	}

	public int getIndex(int x, int y)
	{
		return toIndexMap[x][y];
	}
	
	public int[] getCoords(int index)
	{
		return new int[] { xVals[index], yVals[index] };
	}
	
	/**
	 * Code from
	 * <a href="http://www.compuphase.com/hilbert.htm">http://www.compuphase.com/
	 * hilbert.htm</a>.
	 * 
	 * @param level
	 * @param direction
	 */
	private void compute_worker(int level, Direction direction)
	{
		if (level == 1)
		{
			switch (direction)
			{
			case LEFT:
				move(Direction.RIGHT); /* move() could draw a line in... */
				move(Direction.DOWN); /* ...the indicated direction */
				move(Direction.LEFT);
				break;
			case RIGHT:
				move(Direction.LEFT);
				move(Direction.UP);
				move(Direction.RIGHT);
				break;
			case UP:
				move(Direction.DOWN);
				move(Direction.RIGHT);
				move(Direction.UP);
				break;
			case DOWN:
				move(Direction.UP);
				move(Direction.LEFT);
				move(Direction.DOWN);
				break;
			} /* switch */
		}
		else
		{
			switch (direction)
			{
			case LEFT:
				compute_worker(level - 1, Direction.UP);
				move(Direction.RIGHT);
				compute_worker(level - 1, Direction.LEFT);
				move(Direction.DOWN);
				compute_worker(level - 1, Direction.LEFT);
				move(Direction.LEFT);
				compute_worker(level - 1, Direction.DOWN);
				break;
			case RIGHT:
				compute_worker(level - 1, Direction.DOWN);
				move(Direction.LEFT);
				compute_worker(level - 1, Direction.RIGHT);
				move(Direction.UP);
				compute_worker(level - 1, Direction.RIGHT);
				move(Direction.RIGHT);
				compute_worker(level - 1, Direction.UP);
				break;
			case UP:
				compute_worker(level - 1, Direction.LEFT);
				move(Direction.DOWN);
				compute_worker(level - 1, Direction.UP);
				move(Direction.RIGHT);
				compute_worker(level - 1, Direction.UP);
				move(Direction.UP);
				compute_worker(level - 1, Direction.RIGHT);
				break;
			case DOWN:
				compute_worker(level - 1, Direction.RIGHT);
				move(Direction.UP);
				compute_worker(level - 1, Direction.DOWN);
				move(Direction.LEFT);
				compute_worker(level - 1, Direction.DOWN);
				move(Direction.DOWN);
				compute_worker(level - 1, Direction.LEFT);
				break;
			} /* switch */
		} /* if */
	}

	private void move(final Direction direction)
	{
		switch (direction)
		{
		case LEFT:
			cpX = cpX - 1;
			break;
		case RIGHT:
			cpX = cpX + 1;
			break;
		case UP:
			cpY = cpY - 1;
			break;
		case DOWN:
			cpY = cpY + 1;
			break;
		}
		add(cpX, cpY);
	}
	
	private void add(int x1, int y1)
	{
		System.out.printf("add[%d]: %d, %d\n", cpIndex, x1, y1);
		xVals[cpIndex] = x1;
		yVals[cpIndex] = y1;
		
		toIndexMap[x1][y1] = cpIndex++;
	}

	private static enum Direction
	{
		UP, LEFT, DOWN, RIGHT;
	}

}
