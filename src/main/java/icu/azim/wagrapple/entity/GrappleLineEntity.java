package icu.azim.wagrapple.entity;

import java.util.stream.Stream;

import icu.azim.wagrapple.WAGrappleMod;
import icu.azim.wagrapple.render.GrappleLineRenderer;
import icu.azim.wagrapple.util.Util;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.PacketContext;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.fabricmc.fabric.api.server.PlayerStream;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Arm;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RayTraceContext;
import net.minecraft.world.World;
import net.minecraft.world.RayTraceContext.FluidHandling;
import net.minecraft.world.RayTraceContext.ShapeType;

public class GrappleLineEntity extends Entity {
	

	private PlayerEntity player;
	private Vec3d motion;
	
	private GrappleLineHandler lineHandler;
	private KeyBinding ascend;
	private KeyBinding descend;
	private KeyBinding boost;
	private KeyBinding debug;
	private Vec3d direction;
	private float lpitch;
	private float lyaw;
	
	private double boostSpeed;
	private int boostCooldown;
	private int debugc;
	
	private boolean checked = false;
	private boolean ticked = false;

	public GrappleLineEntity(EntityType<?> type, World world) {
		super(type, world);
		lineHandler = new GrappleLineHandler(this, 16);
		motion = new Vec3d(0,0,0);
		boostSpeed = 1;
		direction = new Vec3d(0,0,0);
		boostCooldown = 15;
		debugc = 0;
		boostSpeed = 1;
		this.ignoreCameraFrustum = true;
	}
	
	public GrappleLineEntity(World world, PlayerEntity player, double length, BlockHitResult res) {
		this(WAGrappleMod.GRAPPLE_LINE, world);
		this.updatePosition(res.getPos().x, res.getPos().y, res.getPos().z);
		this.player = player;
		lineHandler= new GrappleLineHandler(this, length);
		lineHandler.add(res);
		if(world.isClient) {
			ascend = MinecraftClient.getInstance().options.keySneak;
			descend = MinecraftClient.getInstance().options.keySprint;
			boost = MinecraftClient.getInstance().options.keyJump;
			debug = MinecraftClient.getInstance().options.keySwapHands;
		}
	}
	
	public GrappleLineEntity(World world, PlayerEntity player, double length, Vec3d pos) {
		this(WAGrappleMod.GRAPPLE_LINE, world);
		this.updatePosition(pos.x, pos.y, pos.z);
		this.player = player;
		lineHandler = new GrappleLineHandler(this, length);
		lineHandler.addFirst(pos);
		if(world.isClient) {
			ascend = MinecraftClient.getInstance().options.keySneak;
			descend = MinecraftClient.getInstance().options.keySprint;
			boost = MinecraftClient.getInstance().options.keyJump;
			debug = MinecraftClient.getInstance().options.keySwapHands;
			world.playSound(player, pos.x, pos.y, pos.z, SoundEvents.BLOCK_ANVIL_PLACE, SoundCategory.PLAYERS, 0.3F, 1.0F);
		}
	}

	@Override
	protected void initDataTracker() {
		//FishingBobberEntityRenderer
	}
	
	public static void handleSyncPacket(PacketContext context, PacketByteBuf data) {
//
		CompoundTag tag = data.readCompoundTag();
		int id = tag.getInt("eid");
		if(context.getPacketEnvironment()==EnvType.CLIENT) {
			context.getTaskQueue().execute(()->{
				PlayerEntity player = context.getPlayer();
				Entity e = player.world.getEntityById(id);
				if(!(e instanceof GrappleLineEntity)) {
					return;
				}
				GrappleLineEntity line = (GrappleLineEntity)e;
				line.readCustomDataFromTag(tag);
			});
		}else {
			context.getTaskQueue().execute(()->{
				PlayerEntity player = context.getPlayer();
				Entity e = player.world.getEntityById(id);
				if(!(e instanceof GrappleLineEntity)) {
					return;
				}
				GrappleLineEntity line = (GrappleLineEntity)e;
				line.echoEntityDataToClients(tag);
			});
		}
	}

	public void sendEntityDataToServer() {
		if(!world.isClient) {
			System.out.println("attempted to sync from wrong side (expected client, got server)");
			return;
		}
		PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer());
		CompoundTag data = new CompoundTag();
		this.writeCustomDataToTag(data);
		passedData.writeCompoundTag(data);
		ClientSidePacketRegistry.INSTANCE.sendToServer(WAGrappleMod.UPDATE_LINE_PACKED_ID, passedData);
	}
	
	private void echoEntityDataToClients(CompoundTag tag) {
		if(world.isClient) {
			System.out.println("attempted to sync from wrong side (expected server, got client)");
			return;
		}
		Stream<PlayerEntity> watchingPlayers = Stream.concat(PlayerStream.watching(this),PlayerStream.watching(this.getPlayer())).distinct().filter(player->player!=this.getPlayer());
		
		PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer());
		passedData.writeCompoundTag(tag);
		watchingPlayers.forEach(player->ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, WAGrappleMod.UPDATE_LINE_PACKED_ID, passedData));
	}

	@Override
	public Packet<?> createSpawnPacket() {
	    return new EntitySpawnS2CPacket(this, player==null?this.getEntityId():player.getEntityId());
	}
	
	public GrappleLineHandler getHandler(){
		return lineHandler;
	}
	
	@Override
	public void tick() {
		if(player==null||!player.isAlive()) {
			this.remove();
			return;
		}
		if(!ticked) {
			ticked = true;
		}
		if(!checked&&age>10) {
			if(lineHandler.performCheck()) {
				checked = true;
			}else {
				this.destroyLine();
				return;
			}
		}
		if(world.isClient) {
			if(boostCooldown>0) boostCooldown--;
			if(debugc>0) debugc--;

			lineHandler.tick();
			if(this.removed) {
				return;
			}

			int hand = player.getMainArm() == Arm.RIGHT ? 1 : -1;
			ItemStack main = player.getMainHandStack();
			if(main.getItem()!=WAGrappleMod.GRAPPLE_ITEM) {
				hand = -hand;
				main = player.getOffHandStack();
				if(main.getItem()!=WAGrappleMod.GRAPPLE_ITEM) { //neither of hands have the hook item, removing
					destroyLine();
					return;
				}
			}
			handlePlayerInput(hand);
			if(this.removed) {
				return;
			}
			grapplePhysicsTick(hand);
			if(this.removed) {
				return;
			}
			movementPhysicsTick(hand);
			if(this.removed) {
				return;
			}
			
		}else {
			if(!WAGrappleMod.GRAPPLE_COMPONENT.get(player).isGrappled()) {
				this.remove();
			}
		}
		
		
		super.tick();
	}
	
	public void handlePlayerInput(int hand) {
		
		if(player.teleporting) {
			destroyLine();
			return;
		}
		
		
		if(ascend.isPressed()&&descend.isPressed()) {
			return; //not moving anywhere
		}
		if(player.abilities.flying||player.onGround) {
			boostCooldown = 5;
		}
		
		if(boost.isPressed() && !player.abilities.flying && (boostCooldown==0)) {
			Vec3d origin = lineHandler.getLastPiecePos();
			Vec3d direction = player.getCameraPosVec(0).subtract(origin).normalize().multiply(-boostSpeed);
			player.addVelocity(direction.x,direction.y,direction.z);
			
			detachLine();
		}
		
		if(ascend.isPressed()) {
			if(lineHandler.getMaxLen()-lineHandler.getPiecesLen()>1) {
				lineHandler.setMaxLen(lineHandler.getMaxLen()-0.1);
			}
		}
		
		if(descend.isPressed()) {
			lineHandler.setMaxLen(lineHandler.getMaxLen()+0.1);
		}
		
		if(debug.isPressed()&&debugc==0) {
			System.out.println("debug pressed");
			GrappleLineRenderer.debug = !GrappleLineRenderer.debug;
			debugc = 60;
		}
	}
	
	public void grapplePhysicsTick(int hand) {
		
		BlockHitResult res = this.world.rayTrace(new RayTraceContext(Util.getPlayerShoulder(player, hand, 1),lineHandler.getPiecePos(lineHandler.size()-1), ShapeType.COLLIDER, FluidHandling.NONE, player));
		
		if(res.getType()==Type.BLOCK) {
			lineHandler.add(res);
		}else {
			
		}
	}
	
	public void movementPhysicsTick(int hand) {
		/*
		if(true) {
			return;
		}//*/
		Vec3d origin = lineHandler.getLastPiecePos();
		double distanceToOrigin = player.getPos().distanceTo(origin);
		
		this.direction = Util.getPlayerShoulder(player, hand, 1).subtract(origin).normalize();
		calcAxis();
		double totalLen = distanceToOrigin+lineHandler.getPiecesLen();
		if(distanceToOrigin>lineHandler.getMaxLen()*2) {
			destroyLine();
		}
		
		if(totalLen>lineHandler.getMaxLen()) {
			
			Vec3d originToPlayer = origin.subtract(player.getPos());
			Vec3d direction = originToPlayer.normalize().multiply(totalLen-lineHandler.getMaxLen());
			Vec3d projection = project(player.getVelocity(),originToPlayer);

			Vec3d newSpeed = player.getVelocity().subtract(projection);
			
			//double angle = getAngle(new Vec3d(0, 1,0), direction.normalize())*180/Math.PI;
			newSpeed = newSpeed.multiply((player.getVelocity().length()-0.001)/newSpeed.length());
			
			
			
			if(newSpeed.lengthSquared()<direction.lengthSquared()) { //outside of the radius, but not swinging
				newSpeed = newSpeed.add(direction);
			}
			motion = newSpeed;//.add(direction);
			
			if(MinecraftClient.getInstance().options.keyForward.isPressed() && player.getPos().y<origin.y) {
				motion = motion.add(player.getRotationVector().normalize().multiply(0.05));
			}
			if(motion.lengthSquared()>6.25) {
				motion = motion.normalize().multiply(2.5);
			}
			player.setVelocity(motion.x, motion.y, motion.z);
		}else {
			if(player.onGround && totalLen<lineHandler.getMaxLen()) { //player moves towards the pivot point on land
				//do nothing actually, it's not this way in WA
			}
		}
	}

	public void destroyLine() {
		if(world.isClient) {
			player.playSound(SoundEvents.ENTITY_ITEM_BREAK, 1, 1);
			
			PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer());
			passedData.writeBoolean(true);
			ClientSidePacketRegistry.INSTANCE.sendToServer(WAGrappleMod.DETACH_LINE_PACKET_ID, passedData);
		}
		this.remove();
	}
	
	public void detachLine() {
		if(world.isClient) {
			player.playSound(SoundEvents.ENTITY_ENDER_DRAGON_SHOOT, 0.5f, 1.5f);
			player.playSound(SoundEvents.ITEM_ARMOR_EQUIP_CHAIN, 1.0F, 0.6F);
			
			PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer());
			passedData.writeBoolean(true);
			ClientSidePacketRegistry.INSTANCE.sendToServer(WAGrappleMod.DETACH_LINE_PACKET_ID, passedData);
		}
		this.remove();
	}
	
	private Vec3d project(Vec3d a, Vec3d b) {
		return b.multiply(a.dotProduct(b)/b.dotProduct(b));
	}
	
	@SuppressWarnings("unused")
	private static double getAngle(Vec3d a, Vec3d b) {
		double part = (a.x*b.x+a.y*b.y+a.z*b.z)/(a.length()*b.length());
		return Math.acos(part);
	}
	
	public float getLinePitch() {
		return this.lpitch; 
	}

	public float getLineYaw() {
		return this.lyaw;
	}
	
	private void calcAxis() {
		this.lpitch = (float) Math.asin(-this.direction.y);
		this.lyaw = (float) Math.atan2(this.direction.x, this.direction.z);
	}
	
	public PlayerEntity getPlayer() {
		return player;
	}
	
	@Override
	public PistonBehavior getPistonBehavior() {
		return PistonBehavior.IGNORE;
	}
	@Override //draw the line no matter distance
	public boolean shouldRender(double distance) {
		return true;
	}
	@Override //draw the line no matter distance
	public boolean shouldRender(double cameraX, double cameraY, double cameraZ) {
		return true;
	}

	@Override //unused methods
	protected void readCustomDataFromTag(CompoundTag tag) {
		if(ticked) {
			Vec3d pos = Util.readVec3d(tag, "pos");
			this.setPos(pos.x, pos.y, pos.z);
			lineHandler.updateFromCompound(tag);
		}
	}
	@Override
	protected void writeCustomDataToTag(CompoundTag tag) {
		tag.putInt("eid", this.getEntityId());
		tag.putDouble("maxLen", lineHandler.getMaxLen());
		tag.putInt("pieces", lineHandler.size());
		Util.writeVec3d(tag, "pos", this.getPos());
		
		for(int i = 0; i < lineHandler.size(); i++) {
			Util.writeBlockPos(tag, "bpos"+i, lineHandler.getPieceBlock(i));
			Util.writeVec3d(tag,"location"+i, lineHandler.getPiecePos(i));
			Util.writeVec3d(tag,"direction"+i, lineHandler.getDirection(i));
		}
	}

	public Vec3d getDirection() {
		return direction;
	}

	public double getMaxLength() {
		return lineHandler.getMaxLen();
	}

}
