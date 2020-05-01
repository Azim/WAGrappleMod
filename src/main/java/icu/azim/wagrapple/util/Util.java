package icu.azim.wagrapple.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class Util {

	public static void writeVec3d(PacketByteBuf buffer, Vec3d vec) {
		buffer.writeDouble(vec.x);
		buffer.writeDouble(vec.y);
		buffer.writeDouble(vec.z);
	}
	
	public static Vec3d readVec3d(PacketByteBuf buffer) {
		double x = buffer.readDouble();
		double y = buffer.readDouble();
		double z = buffer.readDouble();
		return new Vec3d(x,y,z);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Collection<Identifier> getTagsFor(Block block, Map<Identifier, Tag<Block>> entries) {
		List<Identifier> list = Lists.newArrayList();
		Iterator var3 = entries.entrySet().iterator();
		while(var3.hasNext()) {
			Entry<Identifier, Tag<Block>> entry = (Entry)var3.next();
			if (((Tag)entry.getValue()).contains(block)) {
				list.add(entry.getKey());
			}
		}
		return list;
	}
	
	public static int getClosest(double a, double b, double x) {
		return (Math.abs(a-x)<Math.abs(b-x)?1:2);
	}
	
	public static Vec3d getPlayerShoulder(PlayerEntity player, int hand, float tickDelta) {
		double x = 0;
		double y = 1.3;
		double z = 0;
		double yaw = MathHelper.lerp(tickDelta, player.prevBodyYaw, player.bodyYaw)%360;
		yaw = yaw*Math.PI/180;
		yaw += hand==-1?-Math.PI*3/4:+Math.PI/4;
		double ycos = Math.cos(yaw);
		double ysin = Math.sin(yaw);
		x = x + MathHelper.lerp(tickDelta, player.prevX, player.getX()) - (ycos + ysin)*0.2;// * 0.8D;
		y = y + MathHelper.lerp(tickDelta, player.prevY, player.getY()) - ((player.isSneaking()&&!player.abilities.flying)?0.3:0);
		z = z + MathHelper.lerp(tickDelta, player.prevZ, player.getZ()) - (ysin - ycos)*0.2;// * 0.8D;
		//System.out.println(yaw+" | "+(x-player.prevX)+" "+(y-player.prevY)+" "+(z-player.prevZ));
		return new Vec3d(x,y,z);
	}
}
