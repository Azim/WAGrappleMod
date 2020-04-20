package icu.azim.wagrapple.entity;

import java.util.UUID;

import icu.azim.wagrapple.WAGrappleMod;
import net.minecraft.client.render.entity.FishingBobberEntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class GrappleLine extends Entity {
	

	private PlayerEntity player;
	private UUID playerUUID;
	private Vec3d hitPos;
	private float length = 0;
	private int lastId;

	public GrappleLine(EntityType<?> type, World world) {
		super(type, world);
	}
	
	public GrappleLine(World world, PlayerEntity player, float length, Vec3d pos) {
		this(WAGrappleMod.GRAPPLE_LINE, world);
		this.updatePosition(pos.x, pos.y, pos.z);
		lastId = player.getEntityId();
		this.hitPos = pos;
		this.player = player;
		this.setLength(length);
		this.ignoreCameraFrustum = true;
		playerUUID = player.getUuid();
	}

	@Override
	protected void initDataTracker() {
		//FishingBobberEntityRenderer
	}

	@Override
	protected void readCustomDataFromTag(CompoundTag tag) {
	}

	@Override
	protected void writeCustomDataToTag(CompoundTag tag) {
	}

	@Override
	public Packet<?> createSpawnPacket() {
	    return new EntitySpawnS2CPacket(this, player==null?this.getEntityId():player.getEntityId());
	}
	
	@Override
	public void tick() {
		if(player==null) {
			this.remove();
		}
		super.tick();
	}
	
	
	public PlayerEntity getPlayer() {
		return player;
	}

	public Vec3d getHitPos() {
		return hitPos;
	}

	public void setHitPos(Vec3d hitPos) {
		this.hitPos = hitPos;
	}

	public float getLength() {
		return length;
	}

	public void setLength(float length) {
		this.length = length;
	}

}
