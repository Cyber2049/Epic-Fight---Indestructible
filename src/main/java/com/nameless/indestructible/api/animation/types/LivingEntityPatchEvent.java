package com.nameless.indestructible.api.animation.types;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.MobPatch;
import yesman.epicfight.world.damagesource.StunType;

import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class LivingEntityPatchEvent {
	public static TimeStampedEvent createTimeStampedEvent(float time, Consumer<LivingEntityPatch<?>> event){
		return new TimeStampedEvent(time, event);
	}
	public static BiEvent createBiEvent(BiConsumer<LivingEntityPatch<?>, Entity> event){
		return new BiEvent(event);
	}
	public static StunEvent createStunEvent(BiConsumer<LivingEntityPatch<?>, Entity> event, Object object){
		StunType stunType = StunType.NONE;
		if(object instanceof String s){
			stunType = StunType.valueOf(s.toUpperCase(Locale.ROOT));
		} else if (object instanceof StunType t){
			stunType = t;
		}
		return new StunEvent(event, stunType.ordinal());
	}
	public static BlockedEvent createBlockedEvent(BiConsumer<LivingEntityPatch<?>, Entity> event, boolean isParry){
		return new BlockedEvent(event, isParry);
	}
	public static class TimeStampedEvent extends LivingEntityPatchEvent implements Comparable<TimeStampedEvent> {
		private final float time;
		private final Consumer<LivingEntityPatch<?>> event;

		public TimeStampedEvent(float time, Consumer<LivingEntityPatch<?>> event) {
			this.event = event;
			this.time = time;
		}

		public void testAndExecute(LivingEntityPatch<?> entitypatch, float prevElapsed, float elapsed) {
			if(!entitypatch.isLogicalClient() && !entitypatch.isLogicalClient()) {
				this.event.accept(entitypatch);
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
				Level server = entitypatch.getOriginal().level();
				CommandSourceStack css = entitypatch.getOriginal().createCommandSourceStack().withPermission(2).withSuppressedOutput();
				if (isTarget && entitypatch.getTarget() != null) {
					LivingEntity target;
					if(entitypatch instanceof MobPatch<?> mobPatch){
						target = mobPatch.getTarget();
					} else target = entitypatch.getTarget();
					css = css.withEntity(target);
				}
				if(server.getServer() != null && entitypatch.getOriginal() != null){
					server.getServer().getCommands().performPrefixedCommand(css,command);
				}
			};
			return new TimeStampedEvent(time, event);
		}
	}

	public static class BiEvent {
		protected final BiConsumer<LivingEntityPatch<?>, Entity> event;
		public BiEvent(BiConsumer<LivingEntityPatch<?>, Entity> event){
			this.event = event;
		}
		public static BiEvent CreateBiCommandEvent(String command, boolean isTarget) {
			BiConsumer<LivingEntityPatch<?>, Entity> event = (entitypatch, target) -> {
				Level server = entitypatch.getOriginal().level();
				CommandSourceStack css = entitypatch.getOriginal().createCommandSourceStack().withPermission(2).withSuppressedOutput();
				if (isTarget && target instanceof LivingEntity) {
					css = css.withEntity(target);
				}
				if(server.getServer() != null && entitypatch.getOriginal() != null){
					server.getServer().getCommands().performPrefixedCommand(css,command);
				}
			};
			return new BiEvent(event);
		}

		public void testAndExecute(LivingEntityPatch<?> entitypatch, Entity target) {
			if(!entitypatch.isLogicalClient()) {
				this.event.accept(entitypatch,target);
			}
		}
	}

	public static class StunEvent extends BiEvent {
		private final int condition;
		public StunEvent(BiConsumer<LivingEntityPatch<?>, Entity> event, int condition){
			super(event);
			this.condition = condition;
		}
		public static StunEvent CreateStunCommandEvent(String command, boolean isTarget, StunType stunType) {
			BiConsumer<LivingEntityPatch<?>, Entity> event = (entitypatch, target) -> {
				Level server = entitypatch.getOriginal().level();
				CommandSourceStack css = entitypatch.getOriginal().createCommandSourceStack().withPermission(2).withSuppressedOutput();
				if (isTarget && target instanceof LivingEntity) {
					css = css.withEntity(target);
				}
				if(server.getServer() != null && entitypatch.getOriginal() != null){
					server.getServer().getCommands().performPrefixedCommand(css,command);
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

	public static class BlockedEvent {
		protected final BiConsumer<LivingEntityPatch<?>, Entity> event;
		boolean isParry;
		public BlockedEvent(BiConsumer<LivingEntityPatch<?>, Entity> event, boolean isParry){
			this.event = event;
			this.isParry = isParry;
		}
		public static BlockedEvent CreateBlockCommandEvent(String command, boolean isTarget, boolean isParry) {
			BiConsumer<LivingEntityPatch<?>, Entity> event = (entitypatch, target) -> {
				Level server = entitypatch.getOriginal().level();
				CommandSourceStack css = entitypatch.getOriginal().createCommandSourceStack().withPermission(2).withSuppressedOutput();
				if (isTarget && target instanceof LivingEntity) {
					css = css.withEntity(target);
				}
				if(server.getServer() != null && entitypatch.getOriginal() != null){
					server.getServer().getCommands().performPrefixedCommand(css,command);
				}
			};
			return new BlockedEvent(event, isParry);
		}

		public void testAndExecute(LivingEntityPatch<?> entitypatch, Entity target, boolean isParry) {
			if(!entitypatch.isLogicalClient() && this.isParry == isParry) {
				this.event.accept(entitypatch,target);
			}
		}
	}
}