package com.nameless.indestructible.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nameless.indestructible.world.capability.Utils.IAdvancedCapability;
import com.nameless.indestructible.world.capability.Utils.IAnimationEventCapability;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.world.entity.Entity;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.network.server.SPPlayAnimation;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class AHPatchPlayAnimationCommand implements Command<CommandSourceStack> {
    private static final AHPatchPlayAnimationCommand COMMAND = new AHPatchPlayAnimationCommand();

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("play")
                .then(Commands.argument("animation", StringArgumentType.string())
                        .then(Commands.argument("convert_time", FloatArgumentType.floatArg())
                                .then(Commands.argument("speed", FloatArgumentType.floatArg())
                                        .executes(COMMAND))));
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Entity living = EntityArgument.getEntity(context, "living_entity");
        StaticAnimation animation = AnimationManager.getInstance().byKeyOrThrow(StringArgumentType.getString(context, "animation"));
        float convert_time = FloatArgumentType.getFloat(context, "convert_time");
        float speed = FloatArgumentType.getFloat(context, "speed");
        LivingEntityPatch<?> livingEntityPatch = EpicFightCapabilities.getEntityPatch(living, LivingEntityPatch.class);
        if(livingEntityPatch != null && animation != null){
            if(livingEntityPatch instanceof IAdvancedCapability iac){
                iac.setBlocking(false);
                iac.setAttackSpeed(speed);
                if(livingEntityPatch instanceof IAnimationEventCapability iec){
                    iec.getEventManager().initAnimationEvent();
                }
                livingEntityPatch.playAnimationSynchronized(animation, convert_time, SPPlayAnimation::new);
            } else livingEntityPatch.playAnimationSynchronized(animation, convert_time, SPPlayAnimation::new);
        }
        return 1;
    }
}
