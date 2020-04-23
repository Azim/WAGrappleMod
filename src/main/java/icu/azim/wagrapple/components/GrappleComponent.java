package icu.azim.wagrapple.components;

import net.minecraft.nbt.CompoundTag;

public class GrappleComponent implements IGrappleComponent{
	private boolean grappled = false;
	private int lineId = -1;
	
	
	@Override
	public void fromTag(CompoundTag tag) {
		grappled = tag.getBoolean("grappled");
		lineId = tag.getInt("lineid");
	}

	@Override
	public CompoundTag toTag(CompoundTag tag) {
		tag.putBoolean("grappled", grappled);
		tag.putInt("lineid", lineId);
		return tag;
	}

	@Override
	public boolean isGrappled() {
		return grappled;
	}

	@Override
	public void setGrappled(boolean b) {
		grappled = b;
	}

	@Override
	public int getLineId() {
		return lineId;
	}
	@Override
	public void setLineId(int id) {
		lineId = id;
	}

}
