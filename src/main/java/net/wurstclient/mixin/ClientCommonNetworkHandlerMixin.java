/*
 * Copyright (c) 2014-2023 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.network.listener.ClientCommonPacketListener;
import net.minecraft.network.packet.Packet;
import net.wurstclient.event.EventManager;
import net.wurstclient.events.PacketOutputListener.PacketOutputEvent;

@Mixin(ClientCommonNetworkHandler.class)
public abstract class ClientCommonNetworkHandlerMixin
	implements ClientCommonPacketListener
{
	@Inject(at = @At("HEAD"),
		method = "sendPacket(Lnet/minecraft/network/packet/Packet;)V",
		cancellable = true)
	private void onSendPacket(Packet<?> packet, CallbackInfo ci)
	{
		PacketOutputEvent event = new PacketOutputEvent(packet);
		EventManager.fire(event);
		
		if(event.isCancelled())
			ci.cancel();
	}
}
