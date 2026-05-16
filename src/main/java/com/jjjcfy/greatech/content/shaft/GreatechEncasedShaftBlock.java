/*
 * Create compatibility note: this encased shaft block integrates with Create's MIT-licensed
 * encased shaft and schematic requirement APIs. See THIRD_PARTY_NOTICES.md.
 */
package com.jjjcfy.greatech.content.shaft;

import com.jjjcfy.greatech.content.kinetics.GreatechEncasingType;
import com.jjjcfy.greatech.content.kinetics.GreatechKineticMaterial;
import com.jjjcfy.greatech.content.kinetics.MaterialKineticBlock;
import com.jjjcfy.greatech.content.kinetics.failure.KineticBreakable;
import com.jjjcfy.greatech.registry.GreatechBlockEntityTypes;
import com.jjjcfy.greatech.registry.GreatechBlocks;
import com.simibubi.create.api.schematic.requirement.SpecialBlockItemRequirement;
import com.simibubi.create.content.decoration.encasing.EncasedBlock;
import com.simibubi.create.content.kinetics.base.AbstractEncasedShaftBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.simibubi.create.content.schematics.requirement.ItemRequirement;
import com.simibubi.create.foundation.block.IBE;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class GreatechEncasedShaftBlock extends AbstractEncasedShaftBlock
        implements IBE<GreatechShaftBlockEntity>, SpecialBlockItemRequirement, EncasedBlock, KineticBreakable,
        MaterialKineticBlock {
    private final GreatechKineticMaterial material;
    private final GreatechEncasingType encasingType;

    public GreatechEncasedShaftBlock(GreatechKineticMaterial material, GreatechEncasingType encasingType,
            Properties properties) {
        super(properties);
        this.material = material;
        this.encasingType = encasingType;
    }

    @Override
    public GreatechKineticMaterial getMaterial() {
        return material;
    }

    public GreatechEncasingType getEncasingType() {
        return encasingType;
    }

    @Override
    public float getKineticBreakStressLimit() {
        return material.shaftBreakStressLimit();
    }

    @Override
    public Block getCasing() {
        return encasingType.casing();
    }

    @Override
    public void handleEncasing(BlockState state, Level level, BlockPos pos, ItemStack heldItem, Player player,
            InteractionHand hand, BlockHitResult ray) {
        KineticBlockEntity.switchToBlockState(level, pos, defaultBlockState()
                .setValue(RotatedPillarKineticBlock.AXIS, state.getValue(RotatedPillarKineticBlock.AXIS)));
    }

    @Override
    public InteractionResult onSneakWrenched(BlockState state, UseOnContext context) {
        if (context.getLevel().isClientSide) {
            return InteractionResult.SUCCESS;
        }

        context.getLevel().levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, context.getClickedPos(),
                Block.getId(state));
        KineticBlockEntity.switchToBlockState(context.getLevel(), context.getClickedPos(),
                GreatechBlocks.getShaft(material).defaultBlockState().setValue(AXIS, state.getValue(AXIS)));
        return InteractionResult.SUCCESS;
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos,
            Player player) {
        if (target instanceof BlockHitResult blockHit && blockHit.getDirection().getAxis() != getRotationAxis(state)) {
            return getCasing().asItem().getDefaultInstance();
        }
        return GreatechBlocks.getShaft(material).asItem().getDefaultInstance();
    }

    @Override
    public ItemRequirement getRequiredItems(BlockState state, BlockEntity blockEntity) {
        return ItemRequirement.of(GreatechBlocks.getShaft(material).defaultBlockState(), blockEntity);
    }

    @Override
    public Class<GreatechShaftBlockEntity> getBlockEntityClass() {
        return GreatechShaftBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends GreatechShaftBlockEntity> getBlockEntityType() {
        return GreatechBlockEntityTypes.getFamily(material).shaft().get();
    }
}
