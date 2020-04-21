package icu.azim.wagrapple.entity;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.util.math.Vec3d;

public class GrappleLineHandler {
	private List<Vec3d> toDraw;
	private List<Vec3d> pieces;
	public GrappleLineHandler() {
		toDraw = new ArrayList<Vec3d>();
		pieces = new ArrayList<Vec3d>();
	}
	
	public void add(Vec3d piece, Vec3d drawPiece) {
		pieces.add(piece);
		toDraw.add(drawPiece);
	}
	
	public void add(int index, Vec3d piece, Vec3d drawPiece) {
		pieces.add(index, piece);
		toDraw.add(index, drawPiece);
	}
	
	public Vec3d getPiece(int index) {
		return pieces.get(index);
	}
	public Vec3d getDrawPieces(int index) {
		return toDraw.get(index);
	}

	public int size() {
		return pieces.size();
	}
}
