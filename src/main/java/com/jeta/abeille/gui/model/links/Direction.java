package com.jeta.abeille.gui.model.links;

/**
 * The direction for a link Terminal
 * 
 * @author Jeff Tassin
 */
public class Direction {
	private final String m_name;
	private final int m_sgn;

	public static final Direction EAST = new Direction("EAST", 1);
	public static final Direction WEST = new Direction("WEST", -1);

	private Direction(String name, int sgn) {
		m_name = name;
		m_sgn = sgn;
	}

	public int getSign() {
		return m_sgn;
	}

	public boolean equals(Object obj) {
		if (obj instanceof Direction) {
			Direction d = (Direction) obj;
			return d.m_sgn == m_sgn;
		} else {
			return false;
		}
	}

	public String toString() {
		return m_name;
	}
}
