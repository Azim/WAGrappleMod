package icu.azim.wagrapple.entity;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class GrappleLineHandler {
	private List<GrappleLinePiece> pieces;
	private double maxLen;
	private double piecesLen;
	private GrappleLineEntity line;
	
	public GrappleLineHandler(GrappleLineEntity line, double maxLen) {
		pieces = new ArrayList<GrappleLinePiece>();
		this.maxLen = maxLen;
		this.piecesLen = 0;
		this.line = line;
	}
	
	public void add(Vec3d piece, BlockPos blockPos) {
		if(pieces.size()>0) {
			piecesLen+= piece.distanceTo(getLastPiece());
			//System.out.println("new length: "+piecesLen+" ("+pieces.size()+" items)");
		}
		if(piecesLen>maxLen) {
			line.destroyLine();
		}
		pieces.add(new GrappleLinePiece(piece, blockPos, line.world));
	}
	
	public void add(int index, Vec3d piece, BlockPos blockPos) {
		pieces.add(new GrappleLinePiece(piece, blockPos, line.world));
		recalcLen();
		if(piecesLen>maxLen) {
			line.destroyLine();
		}
	}
	
	private void recalcLen() {
		piecesLen = 0;
		for(int i = 0; i < pieces.size()-1;i++) {
			piecesLen += pieces.get(i).getLocation().distanceTo(pieces.get(i+1).getLocation());
		}
	}

	public Vec3d getPiece(int index) {
		return pieces.get(index).getLocation();
	}
	public Vec3d getDrawPieces(int index) {
		return pieces.get(index).getDrawLocation();
	}

	public int size() {
		return pieces.size();
	}
	
	public Vec3d getLastPiece() {
		return pieces.get(pieces.size()-1).getLocation();
	}
	
	public Vec3d getLastDrawPiece() {
		return pieces.get(pieces.size()-1).getDrawLocation();
	}

	public double getPiecesLen() {
		return piecesLen;
	}

	public double getMaxLen() {
		return maxLen;
	}
	public void setMaxLen(double maxLen) {
		this.maxLen = maxLen;
	}
	
	public void tick() {
		for(GrappleLinePiece piece:pieces) {
			if(!piece.blockTick()) {
				System.out.println("block changed!");
				line.destroyLine();
			}
		}
	}

}
