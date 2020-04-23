package icu.azim.wagrapple.entity;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.util.math.Vec3d;

public class GrappleLineHandler {
	private List<Vec3d> toDraw;
	private List<Vec3d> pieces;
	private double maxLen;
	private double piecesLen;
	private GrappleLineEntity line;
	
	public GrappleLineHandler(GrappleLineEntity line, double maxLen) {
		toDraw = new ArrayList<Vec3d>();
		pieces = new ArrayList<Vec3d>();
		this.maxLen = maxLen;
		this.piecesLen = 0;
		this.line = line;
	}
	
	public void add(Vec3d piece, Vec3d drawPiece) {
		if(pieces.size()>0) {
			piecesLen+= piece.distanceTo(getLastPiece());
			//System.out.println("new length: "+piecesLen+" ("+pieces.size()+" items)");
		}
		if(piecesLen>maxLen) {
			line.detachLine();
		}
		pieces.add(piece);
		toDraw.add(drawPiece);
	}
	
	public void add(int index, Vec3d piece, Vec3d drawPiece) {
		pieces.add(index, piece);
		toDraw.add(index, drawPiece);
		recalcLen();
		if(piecesLen>maxLen) {
			line.detachLine();
		}
	}
	
	private void recalcLen() {
		piecesLen = 0;
		for(int i = 0; i < pieces.size()-1;i++) {
			piecesLen += pieces.get(i).distanceTo(pieces.get(i+1));
		}
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
	
	public Vec3d getLastPiece() {
		return pieces.get(pieces.size()-1);
	}
	
	public Vec3d getLastDrawPiece() {
		return toDraw.get(toDraw.size()-1);
	}

	public double getPiecesLen() {
		return piecesLen;
	}

	public double getMaxLen() {
		return maxLen;
	}
}
