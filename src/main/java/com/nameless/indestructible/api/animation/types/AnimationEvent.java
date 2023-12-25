package com.nameless.indestructible.api.animation.types;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class AnimationEvent {
	final Consumer<LivingEntityPatch<?>> event;
	private AnimationEvent(Consumer<LivingEntityPatch<?>> event) {
		this.event = event;
	}

	public void testAndExecute(LivingEntityPatch<?> entitypatch) {
		if(!entitypatch.isLogicalClient()) {
			this.event.accept(entitypatch);
		}
	}

	public static AnimationEvent create(Consumer<LivingEntityPatch<?>> event) {
		return new AnimationEvent(event);
	}

	public static class TimeStampedEvent extends AnimationEvent implements Comparable<TimeStampedEvent> {
		final float time;

		private TimeStampedEvent(float time, Consumer<LivingEntityPatch<?>> event) {
			super(event);
			this.time = time;
		}

		public void testAndExecute(LivingEntityPatch<?> entitypatch, float prevElapsed, float elapsed) {
			if(!entitypatch.isLogicalClient()) {
				if (this.time >= prevElapsed && this.time < elapsed) {
					super.testAndExecute(entitypatch);
				}
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

		public static TimeStampedEvent create(float time, Consumer<LivingEntityPatch<?>> event) {
			return new TimeStampedEvent(time, event);
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
			return create(time, event);
		}
	}

	public static class HitEvent {
		BiConsumer<LivingEntityPatch<?>, Entity> event;
		private HitEvent (BiConsumer<LivingEntityPatch<?>, Entity> event){
			this.event = event;
		}

		public static HitEvent create(BiConsumer<LivingEntityPatch<?>, Entity> event) {
			return new HitEvent(event);
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
			return create(event);
		}

		public void testAndExecute(LivingEntityPatch<?> entitypatch, Entity target) {
			if(!entitypatch.isLogicalClient()) {
				this.event.accept(entitypatch,target);
			}
		}
	}
}