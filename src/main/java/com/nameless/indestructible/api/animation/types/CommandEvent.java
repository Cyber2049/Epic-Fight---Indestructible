package com.nameless.indestructible.api.animation.types;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import yesman.epicfight.world.capabilities.entitypatch.MobPatch;
import yesman.epicfight.world.damagesource.StunType;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class CommandEvent {
	private final Consumer<MobPatch<?>> event;
	private CommandEvent(Consumer<MobPatch<?>> event) {
		this.event = event;
	}

	public void testAndExecute(MobPatch<?> entitypatch) {
		if(!entitypatch.isLogicalClient()) {
			this.event.accept(entitypatch);
		}
	}

	public static class TimeStampedEvent extends CommandEvent implements Comparable<TimeStampedEvent> {
		private final float time;

		private TimeStampedEvent(float time, Consumer<MobPatch<?>> event) {
			super(event);
			this.time = time;
		}

		public void testAndExecute(MobPatch<?> entitypatch, float prevElapsed, float elapsed) {
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
			Consumer<MobPatch<?>> event = (entitypatch) -> {
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

	public static class BiEvent {
		protected final BiConsumer<MobPatch<?>, Entity> event;
		private BiEvent(BiConsumer<MobPatch<?>, Entity> event){
			this.event = event;
		}
		public static BiEvent CreateBiCommandEvent(String command, boolean isTarget) {
			BiConsumer<MobPatch<?>, Entity> event = (entitypatch, target) -> {
				Level server = entitypatch.getOriginal().level;
				CommandSourceStack css = entitypatch.getOriginal().createCommandSourceStack().withPermission(2).withSuppressedOutput();
				if (isTarget && target instanceof LivingEntity) {
					css = css.withEntity(target);
				}
				if(server.getServer() != null && entitypatch.getOriginal() != null){
					server.getServer().getCommands().performCommand(css,command);
				}
			};
			return new BiEvent(event);
		}

		public void testAndExecute(MobPatch<?> entitypatch, Entity target) {
			if(!entitypatch.isLogicalClient()) {
				this.event.accept(entitypatch,target);
			}
		}
	}

	public static class StunEvent extends BiEvent {
		private final int condition;
		private StunEvent(BiConsumer<MobPatch<?>, Entity> event, int condition){
			super(event);
			this.condition = condition;
		}
		public static StunEvent CreateStunCommandEvent(String command, boolean isTarget, StunType stunType) {
			BiConsumer<MobPatch<?>, Entity> event = (entitypatch, target) -> {
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

		public void testAndExecute(MobPatch<?> entitypatch, Entity target, int condition) {
			if(!entitypatch.isLogicalClient() && this.condition == condition) {
				this.event.accept(entitypatch, target);
			}
		}
	}

	public static class BlockedEvent {
		protected final BiConsumer<MobPatch<?>, Entity> event;
		boolean isParry;
		private BlockedEvent(BiConsumer<MobPatch<?>, Entity> event, boolean isParry){
			this.event = event;
			this.isParry = isParry;
		}
		public static BlockedEvent CreateBlockCommandEvent(String command, boolean isTarget, boolean isParry) {
			BiConsumer<MobPatch<?>, Entity> event = (entitypatch, target) -> {
				Level server = entitypatch.getOriginal().level;
				CommandSourceStack css = entitypatch.getOriginal().createCommandSourceStack().withPermission(2).withSuppressedOutput();
				if (isTarget && target instanceof LivingEntity) {
					css = css.withEntity(target);
				}
				if(server.getServer() != null && entitypatch.getOriginal() != null){
					server.getServer().getCommands().performCommand(css,command);
				}
			};
			return new BlockedEvent(event, isParry);
		}

		public void testAndExecute(MobPatch<?> entitypatch, Entity target, boolean isParry) {
			if(!entitypatch.isLogicalClient() && this.isParry == isParry) {
				this.event.accept(entitypatch,target);
			}
		}
	}
}