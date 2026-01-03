package at.pst.jme;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Test {
	public static void main(String[] args) {
		System.out.println("s: " + IntStream.range(0, 10).mapToObj(i -> "" + i).collect(Collectors.joining(", ")));
	}
}
