package com.nameless.indestructible.api.animation.types;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.damagesource.StunType;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class CommandEvent {
	private final Consumer<LivingEntityPatch<?>> event;
	private CommandEvent(Consumer<LivingEntityPatch<?>> event) {
		this.event = event;
	}

	public void testAndExecute(LivingEntityPatch<?> entitypatch) {
		if(!entitypatch.isLogicalClient()) {
			this.event.accept(entitypatch);
		}
	}

	public static class TimeStampedEvent extends CommandEvent implements Comparable<TimeStampedEvent> {
		private final float time;

		private TimeStampedEvent(float time, Consumer<LivingEntityPatch<?>> event) {
			super(event);
			this.time = time;
		}

		public void testAndExecute(LivingEntityPatch<?> entitypatch, float prevElapsed, float elapsed) {
				if (this.time >= prevElapsed && this.time < elapsed) {
					super.testAndExecute(entitypatch);
				}
		}

		@Override
		public int compareTo(TimeStampedEvent event) {
			if(this.time == event.time) {
				return 0;
			} else {
				return this.time > event.time ? 1 : -1;
			}
		}
		public static TimeStampedEvent CreateTimeCommandEvent(float time, String command, boolean isTarget) {
			Consumer<LivingEntityPatch<?>> event = (entitypatch) -> {
				Level server = entitypatch.getOriginal().level;
				CommandSourceStack css = entitypatch.getOriginal().createCommandSourceStack().withPermission(2).withSuppressedOutput();
				if (isTarget && entitypatch.getTarget() != null) {
					css = css.withEntity(entitypatch.getTarget());
				}
				if(server.getServer() != null && entitypatch.getOriginal() != null){
					server.getServer().getCommands().performCommand(css,command);
				}
			};
			return new TimeStampedEvent(time, event);
		}
	}

	public static class HitEvent {
		protected final BiConsumer<LivingEntityPatch<?>, Entity> event;
		private HitEvent(BiConsumer<LivingEntityPatch<?>, Entity> event){
			this.event = event;
		}
		public static HitEvent CreateHitCommandEvent(String command, boolean isTarget) {
			BiConsumer<LivingEntityPatch<?>, Entity> event = (entitypatch, target) -> {
				Level server = entitypatch.getOriginal().level;
				CommandSourceStack css = entitypatch.getOriginal().createCommandSourceStack().withPermission(2).withSuppressedOutput();
				if (isTarget && target instanceof LivingEntity) {
					css = css.withEntity(target);
				}
				if(server.getServer() != null && entitypatch.getOriginal() != null){
					server.getServer().getCommands().performCommand(css,command);
				}
			};
			return new HitEvent(event);
		}

		public void testAndExecute(LivingEntityPatch<?> entitypatch, Entity target) {
			if(!entitypatch.isLogicalClient()) {
				this.event.accept(entitypatch,target);
			}
		}
	}

	public static class StunEvent extends HitEvent{
		private final int condition;
		private StunEvent(BiConsumer<LivingEntityPatch<?>, Entity> event, int condition){
			super(event);
			this.condition = condition;
		}
		public static StunEvent CreateStunCommandEvent(String command, boolean isTarget, StunType stunType) {
			BiConsumer<LivingEntityPatch<?>, Entity> event = (entitypatch, target) -> {
				Level server = entitypatch.getOriginal().level;
				CommandSourceStack css = entitypatch.getOriginal().createCommandSourceStack().withPermission(2).withSuppressedOutput();
				if (isTarget && target instanceof LivingEntity) {
					css = css.withEntity(target);
				}
				if(server.getServer() != null && entitypatch.getOriginal() != null){
					server.getServer().getCommands().performCommand(css,command);
				}
			};

			return  new StunEvent(event, stunType.ordinal());
		}

		public void testAndExecute(LivingEntityPatch<?> entitypatch, Entity target, int condition) {
			if(!entitypatch.isLogicalClient() && this.condition == condition) {
				this.event.accept(entitypatch, target);
			}
		}
	}
}