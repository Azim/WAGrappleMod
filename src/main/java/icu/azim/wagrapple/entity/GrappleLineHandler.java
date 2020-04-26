package icu.azim.wagrapple.entity;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
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
	
	public void add(BlockHitResult result) {
		Vec3d piece = getSnap(result.getPos(), line.world.getBlockState(result.getBlockPos()).getCollisionShape(line.world, result.getBlockPos()).getBoundingBox());
		
		if(this.size()>0) {
			if(getLastPiece()==piece) {
				return;
			}
			piecesLen += piece.distanceTo(getLastPiece());
		}
		Direction dir = result.getSide();
		Vec3d vdir = new Vec3d(dir.getOffsetX(), dir.getOffsetY(), dir.getOffsetZ());
		
		pieces.add(new GrappleLinePiece(piece, result.getBlockPos(), this.size()>0?getDirection(getLastPiece(),piece,dir):vdir, line.world));
		
		if(piecesLen>maxLen) {
			line.destroyLine();
		}
	}
	
	private Vec3d getDirection(Vec3d prev, Vec3d curr, Direction dir) {
		System.out.print(dir.toString()+" ");
		Vec3d vdir = new Vec3d(dir.getOffsetX(), dir.getOffsetY(), dir.getOffsetZ());
		Vec3d diff = prev.subtract(curr).normalize();
		
		return diff.crossProduct(vdir).crossProduct(diff);
	}
	
	/*
	public void add(int index, BlockHitResult result) {		
		Vec3d piece = getSnap(result.getPos(), line.world.getBlockState(result.getBlockPos()).getCollisionShape(line.world, result.getBlockPos()).getBoundingBox());
		if(this.size()>0) {
			piecesLen += piece.distanceTo(getLastPiece());
		}
		pieces.add(index, new GrappleLinePiece(piece, result.getBlockPos(), result.getSide(), line.world));
		recalcLen();
		if(piecesLen>maxLen) {
			line.destroyLine();
		}
	}*/
	
	@SuppressWarnings("unused")
	private void recalcLen() {
		piecesLen = 0;
		for(int i = 0; i < pieces.size()-1;i++) {
			piecesLen += pieces.get(i).getLocation().distanceTo(pieces.get(i+1).getLocation());
		}
	}
	
	private Vec3d getSnap(Vec3d point, Box shape) {
		int ix = (int)point.x;
		int iy = (int)point.y;
		int iz = (int)point.z;
		
		double x = Math.abs(point.x-(int)point.x); //get the decimal part
		double y = Math.abs(point.y-(int)point.y);
		double z = Math.abs(point.z-(int)point.z);
		
		double cx = getClosest(shape.x1,shape.x2,x)==1?shape.x1:shape.x2; //get the closest corner
		double cy = getClosest(shape.y1,shape.y2,y)==1?shape.y1:shape.y2;
		double cz = getClosest(shape.z1,shape.z2,z)==1?shape.z1:shape.z2;
		
		double dx = Math.abs(cx-x); //get the distance between the point and closest corner
		double dy = Math.abs(cy-y);
		double dz = Math.abs(cz-z);
		
		double nx = ix + ((ix<0)?-1:1)*(cx); //calculate the new coordinate
		double ny = iy + ((iy<0)?-1:1)*(cy);
		double nz = iz + ((iz<0)?-1:1)*(cz);
		
		
		
		
		Vec3d result; //find the one that is further away from the corners - and leave it as was
		if(dx>=dy && dx>=dz) { 						//leave x as was
			result = new Vec3d(point.x, ny, nz);
		}else if(dy>=dx && dy>=dz) { 				//leave y as was
			result = new Vec3d(nx, point.y, nz);
		}else { 									//leave z as was
			result = new Vec3d(nx, ny, point.z);
		}
		return result;
	}
	
	private int getClosest(double a, double b, double x) {
		return (Math.abs(a-x)<Math.abs(b-x)?1:2);
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
		pieces.get(pieces.size()-1).compare(getLastPiece().subtract(line.getPlayer().getPos()));
		
		for(GrappleLinePiece piece:pieces) {
			if(!piece.blockTick()) {
				System.out.println("block changed!");
				line.destroyLine();
			}
		}
	}

}
