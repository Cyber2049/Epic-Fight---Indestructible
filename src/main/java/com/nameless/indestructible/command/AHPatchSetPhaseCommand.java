package com.nameless.indestructible.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nameless.indestructible.world.capability.Utils.IAdvancedCapability;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.world.entity.Entity;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.MobPatch;

public class AHPatchSetPhaseCommand implements Command<CommandSourceStack> {
    private static final AHPatchSetPhaseCommand COMMAND = new AHPatchSetPhaseCommand();

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("set_phase")
                .then(Commands.argument("custom_phase", IntegerArgumentType.integer(0, 20))
                                        .executes(COMMAND));
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Entity living = EntityArgument.getEntity(context, "living_entity");
        int phase = IntegerArgumentType.getInteger(context, "custom_phase");
        MobPatch<?> AHPatch = EpicFightCapabilities.getEntityPatch(living, MobPatch.class);
        if(AHPatch instanceof IAdvancedCapability iac){
           iac.setPhase(phase);
        }
        return 1;
    }
}
