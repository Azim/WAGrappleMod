package icu.azim.wagrapple.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
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
}
