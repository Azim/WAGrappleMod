package icu.azim.wagrapple.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import icu.azim.wagrapple.util.PacketUnwrapper;
import icu.azim.wagrapple.util.PacketUnwrapperPiece;
import icu.azim.wagrapple.util.Util;
import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
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
	
	public void updateFromServer(PacketUnwrapper unwrapped) {
		this.maxLen = unwrapped.getMaxLength();
		pieces.clear();
		List<PacketUnwrapperPiece> ps = unwrapped.getPieces();
		for(int i = 0; i<unwrapped.getSize(); i++) {
			PacketUnwrapperPiece p = ps.get(i);
			pieces.add(new GrappleLinePiece(p.getLocation(), p.getBpos(), p.getDirection(), line.world));
		}
		recalcLen();
	}
	
	public void updateFromCompound(CompoundTag tag) {
		this.setMaxLen(tag.getDouble("maxLen"));
		int pieces = tag.getInt("pieces");
		this.pieces.clear();
		for(int i = 0; i<pieces;i++) {
			this.pieces.add(new GrappleLinePiece(
					Util.readVec3d(tag, "location"+i), 
					Util.readBlockPos(tag, "bpos"+i),
					Util.readVec3d(tag, "direction"+i),
					line.world));
		}
		recalcLen();
	}
	
	public void add(BlockHitResult result) {
		Vec3d piece = getSnap(result.getPos(), line.world.getBlockState(result.getBlockPos()).getCollisionShape(line.world, result.getBlockPos()).getBoundingBox());
		
		if(this.size()>0) {
			if(getLastPiecePos()==piece) {
				return;
			}
			piecesLen += piece.distanceTo(getLastPiecePos());
		}
		Direction dir = result.getSide();
		Vec3d vdir = new Vec3d(dir.getOffsetX(), dir.getOffsetY(), dir.getOffsetZ());
		
		pieces.add(new GrappleLinePiece(piece, result.getBlockPos(), this.size()>0?calcDirection(getLastPiecePos(),piece,dir, result.getPos()):vdir, line.world));
		
		if(piecesLen>maxLen) {
			line.destroyLine();
		}
		if(line.world.isClient) {
			line.syncFromClient();
		}
	}
	
	public Vec3d getDirection(int index) {
		return pieces.get(index).getDirection();
	}
	
	private Vec3d calcDirection(Vec3d prev, Vec3d curr, Direction dir, Vec3d nonsnap) {
		Vec3d result = null;
		Vec3d vdir = new Vec3d(dir.getOffsetX(), dir.getOffsetY(), dir.getOffsetZ());
		Vec3d diff = prev.subtract(curr).normalize();
		
		
		if(diff.x==0) {
			double dx = prev.subtract(nonsnap).x;
			result = new Vec3d(dx,0,0).normalize();
		}else if(diff.y==0) {
			double dy = prev.subtract(nonsnap).y;
			result = new Vec3d(0,dy,0).normalize();
		}else if(diff.z==0) {
			double dz = prev.subtract(nonsnap).z;
			result = new Vec3d(0,0,dz).normalize();
		}else {
			result = diff.crossProduct(vdir).crossProduct(diff).normalize();
		}
		System.out.println(dir.toString()+" "+dir.getVector().toString()+" "+result);
		return result;
	}
	
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
		
		double cx = Util.getClosest(shape.x1,shape.x2,x)==1?shape.x1:shape.x2; //get the closest corner
		double cy = Util.getClosest(shape.y1,shape.y2,y)==1?shape.y1:shape.y2;
		double cz = Util.getClosest(shape.z1,shape.z2,z)==1?shape.z1:shape.z2;
		
		double dx = Math.abs(cx-x); //get the distance between the point and closest corner
		double dy = Math.abs(cy-y);
		double dz = Math.abs(cz-z);
		
		double nx = ix + ((ix<0)?-1:1)*(cx); //calculate the new coordinate
		double ny = iy + ((iy<0)?-1:1)*(cy);
		double nz = iz + ((iz<0)?-1:1)*(cz);
		
		Vec3d result; //find the one that is further away from the corners - and leave it as was
		if((dx<=(1/5)) && (dy<=(1/5)) && (dz<=(1/5))) { 		//corner
			result = new Vec3d(nx, ny, nz);
		}else if(dx>=dy && dx>=dz) { 				//leave x as was
			result = new Vec3d(point.x, ny, nz);
		}else if(dy>=dx && dy>=dz) { 				//leave y as was
			result = new Vec3d(nx, point.y, nz);
		}else { 									//leave z as was
			result = new Vec3d(nx, ny, point.z);
		}
		return result;
	}

	public Vec3d getPiecePos(int index) {
		return pieces.get(index).getLocation();
	}
	public BlockPos getPieceBlock(int index) {
		return pieces.get(index).getBlockPos();
	}
	public int size() {
		return pieces.size();
	}
	public Vec3d getLastPiecePos() {
		return pieces.get(pieces.size()-1).getLocation();
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
		if(pieces.size()>1) {
			double angle = pieces.get(pieces.size()-1).compare(getLastPiecePos().subtract(line.getPlayer().getCameraPosVec(0)));
			
			if(angle>90) {
				pieces.remove(pieces.size()-1);
				recalcLen();
				line.syncFromClient();
			}
		}
		for(GrappleLinePiece piece:pieces) {
			if(!piece.blockTick()) {
				System.out.println("block changed!");
				line.destroyLine();
				line.syncFromClient();
			}
		}
	}
	
	public boolean performCheck() {
		BlockPos bpos = getPieceBlock(0);
		Vec3d pos = getPiecePos(0);
		if(line.world.isClient) {
			Collection<Identifier> tags = MinecraftClient.getInstance().getNetworkHandler().getTagManager().blocks().getTagsFor(line.world.getBlockState(bpos).getBlock());
			if(tags.contains(new Identifier("wagrapple","ungrappable"))) {
				line.world.playSound(line.getPlayer(), pos.x, pos.y, pos.z, SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND, SoundCategory.PLAYERS, 0.7F, 1.0F);
				return false;
			}
			return true;
		}else {
			Collection<Identifier> tags = Util.getTagsFor(line.world.getBlockState(bpos).getBlock(), line.getServer().getTagManager().blocks().getEntries());
			if(tags.contains(new Identifier("wagrapple","ungrappable"))) {
				line.world.playSound(line.getPlayer(), pos.x, pos.y, pos.z, SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND, SoundCategory.PLAYERS, 0.7F, 1.0F);
				return false;
			}
			return true;
		}
	}

}
