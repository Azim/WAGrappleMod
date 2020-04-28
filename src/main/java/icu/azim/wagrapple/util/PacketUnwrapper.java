package icu.azim.wagrapple.util;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.util.PacketByteBuf;

public class PacketUnwrapper {
	private int eid;
	private double maxLength;
	private int size;
	private List<PacketUnwrapperPiece> pieces;
	public PacketUnwrapper(PacketByteBuf data) {
		eid = data.readInt();
		maxLength = data.readDouble();
		size = data.readInt();
		pieces = new ArrayList<>();
		for(int i = 0; i < size; i++) {
			pieces.add(new PacketUnwrapperPiece(data));
		}
	}
	public int getEid() {
		return eid;
	}
	public double getMaxLength() {
		return maxLength;
	}
	public int getSize() {
		return size;
	}
	public List<PacketUnwrapperPiece> getPieces() {
		return pieces;
	}
	
}
