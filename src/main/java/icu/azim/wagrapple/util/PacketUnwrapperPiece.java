package icu.azim.wagrapple.util;

import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class PacketUnwrapperPiece {
	private final Vec3d location;
	private final Vec3d direction;
	private final BlockPos bpos;
	
	public PacketUnwrapperPiece(PacketByteBuf buffer) {
		bpos = buffer.readBlockPos();
		location = Util.readVec3d(buffer);
		direction = Util.readVec3d(buffer);
	}

	public Vec3d getLocation() {
		return location;
	}

	public Vec3d getDirection() {
		return direction;
	}

	public BlockPos getBpos() {
		return bpos;
	}
}