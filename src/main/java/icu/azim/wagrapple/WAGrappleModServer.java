package icu.azim.wagrapple;

import icu.azim.wagrapple.entity.GrappleLineEntity;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public class WAGrappleModServer implements net.fabricmc.api.DedicatedServerModInitializer{

	@Override
	public void onInitializeServer() {
		ServerSidePacketRegistry.INSTANCE.register(WAGrappleMod.DETACH_LINE_PACKET_ID, (packetContext, attachedData) -> {
            boolean detach = attachedData.readBoolean();
            PlayerEntity player = packetContext.getPlayer();
            packetContext.getTaskQueue().execute(() -> {
            	if(WAGrappleMod.GRAPPLE_COMPONENT.get(player).isGrappled() && detach) {
            		int id = WAGrappleMod.GRAPPLE_COMPONENT.get(player).getLineId();
            		if(id>0) {
        				Entity e = player.world.getEntityById(id);
        				if(e!=null) {
        					e.remove();
        				}
            		}
            		WAGrappleMod.GRAPPLE_COMPONENT.get(player).setLineId(-1);
        			WAGrappleMod.GRAPPLE_COMPONENT.get(player).setGrappled(!detach);
        			WAGrappleMod.GRAPPLE_COMPONENT.get(player).sync();
            	}
 
            });
        });
		
		ServerSidePacketRegistry.INSTANCE.register(WAGrappleMod.UPDATE_LINE_PACKET_ID, (context,data)-> GrappleLineEntity.handleSyncPacket(context, data));
		System.out.println("server init done");
	}

}
