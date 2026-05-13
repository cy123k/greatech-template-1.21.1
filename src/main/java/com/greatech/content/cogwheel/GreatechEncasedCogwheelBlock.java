package com.greatech.content.cogwheel;

import com.greatech.content.kinetics.GreatechEncasingType;
import com.greatech.content.kinetics.GreatechKineticMaterial;
import com.greatech.content.kinetics.MaterialKineticBlock;
import com.greatech.content.kinetics.failure.KineticBreakable;
import com.greatech.registry.GreatechBlockEntityTypes;
import com.greatech.registry.GreatechBlocks;
import com.simibubi.create.api.contraption.transformable.TransformableBlock;
import com.simibubi.create.api.schematic.requirement.SpecialBlockItemRequirement;
import com.simibubi.create.content.contraptions.StructureTransform;
import com.simibubi.create.content.decoration.encasing.EncasedBlock;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.simibubi.create.content.kinetics.simpleRelays.BracketedKineticBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.CogWheelBlock;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import com.simibubi.create.content.schematics.requirement.ItemRequirement;
import com.simibubi.create.foundation.block.IBE;

import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.math.VoxelShaper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class GreatechEncasedCogwheelBlock extends RotatedPillarKineticBlock
        implements ICogWheel, IBE<BracketedKineticBlockEntity>, SpecialBlockItemRequirement, TransformableBlock,
        EncasedBlock, KineticBreakable, MaterialKineticBlock {
    public static final BooleanProperty TOP_SHAFT = BooleanProperty.create("top_shaft");
    public static final BooleanProperty BOTTOM_SHAFT = BooleanProperty.create("bottom_shaft");

    private final GreatechKineticMaterial material;
    private final GreatechEncasingType encasingType;
    private final boolean large;

    public GreatechEncasedCogwheelBlock(GreatechKineticMaterial material, GreatechEncasingType encasingType,
            boolean large, Properties properties) {
        super(properties);
        this.material = material;
        this.encasingType = encasingType;
        this.large = large;
        registerDefaultState(defaultBlockState()
                .setValue(TOP_SHAFT, false)
                .setValue(BOTTOM_SHAFT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(TOP_SHAFT, BOTTOM_SHAFT));
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
        return large ? material.largeCogwheelBreakStressLimit() : material.smallCogwheelBreakStressLimit();
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos,
            Player player) {
        if (target instanceof BlockHitResult blockHit && blockHit.getDirection().getAxis() != getRotationAxis(state)) {
            return GreatechBlocks.getCogwheel(material, large).asItem().getDefaultInstance();
        }
        return getCasing().asItem().getDefaultInstance();
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState placedOn = context.getLevel().getBlockState(context.getClickedPos()
                .relative(context.getClickedFace().getOpposite()));
        BlockState stateForPlacement = super.getStateForPlacement(context);
        if (ICogWheel.isSmallCog(placedOn)) {
            stateForPlacement = stateForPlacement.setValue(AXIS, ((IRotate) placedOn.getBlock()).getRotationAxis(placedOn));
        }
        return stateForPlacement;
    }

    @Override
    public boolean skipRendering(BlockState state, BlockState adjacentState, Direction direction) {
        return state.getBlock() == adjacentState.getBlock() && state.getValue(AXIS) == adjacentState.getValue(AXIS);
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        if (context.getClickedFace().getAxis() != state.getValue(AXIS)) {
            return super.onWrenched(state, context);
        }

        Level level = context.getLevel();
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        BlockPos pos = context.getClickedPos();
        KineticBlockEntity.switchToBlockState(level, pos, state.cycle(
                context.getClickedFace().getAxisDirection() == AxisDirection.POSITIVE ? TOP_SHAFT : BOTTOM_SHAFT));
        IWrenchable.playRotateSound(level, pos);
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult onSneakWrenched(BlockState state, UseOnContext context) {
        if (context.getLevel().isClientSide) {
            return InteractionResult.SUCCESS;
        }

        context.getLevel().levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, context.getClickedPos(), Block.getId(state));
        KineticBlockEntity.switchToBlockState(context.getLevel(), context.getClickedPos(),
                GreatechBlocks.getCogwheel(material, large).defaultBlockState().setValue(AXIS, state.getValue(AXIS)));
        return InteractionResult.SUCCESS;
    }

    @Override
    public BlockState getRotatedBlockState(BlockState originalState, Direction targetedFace) {
        originalState = swapShaftsForRotation(originalState, Rotation.CLOCKWISE_90, targetedFace.getAxis());
        return originalState.setValue(AXIS,
                VoxelShaper.axisAsFace(originalState.getValue(AXIS)).getClockWise(targetedFace.getAxis()).getAxis());
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis() == state.getValue(AXIS)
                && state.getValue(face.getAxisDirection() == AxisDirection.POSITIVE ? TOP_SHAFT : BOTTOM_SHAFT);
    }

    @Override
    protected boolean areStatesKineticallyEquivalent(BlockState oldState, BlockState newState) {
        if (newState.getBlock() instanceof GreatechEncasedCogwheelBlock
                && oldState.getBlock() instanceof GreatechEncasedCogwheelBlock) {
            if (newState.getValue(TOP_SHAFT) != oldState.getValue(TOP_SHAFT)) {
                return false;
            }
            if (newState.getValue(BOTTOM_SHAFT) != oldState.getValue(BOTTOM_SHAFT)) {
                return false;
            }
        }
        return super.areStatesKineticallyEquivalent(oldState, newState);
    }

    @Override
    public boolean isSmallCog() {
        return !large;
    }

    @Override
    public boolean isLargeCog() {
        return large;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return CogWheelBlock.isValidCogwheelPosition(large, level, pos, state.getValue(AXIS));
    }

    @Override
    public Axis getRotationAxis(BlockState state) {
        return state.getValue(AXIS);
    }

    public BlockState swapShafts(BlockState state) {
        boolean bottom = state.getValue(BOTTOM_SHAFT);
        boolean top = state.getValue(TOP_SHAFT);
        return state.setValue(BOTTOM_SHAFT, top).setValue(TOP_SHAFT, bottom);
    }

    public BlockState swapShaftsForRotation(BlockState state, Rotation rotation, Axis rotationAxis) {
        if (rotation == Rotation.NONE) {
            return state;
        }

        Axis axis = state.getValue(AXIS);
        if (axis == rotationAxis) {
            return state;
        }

        if (rotation == Rotation.CLOCKWISE_180) {
            return swapShafts(state);
        }

        boolean clockwise = rotation == Rotation.CLOCKWISE_90;
        if (rotationAxis == Axis.X) {
            if (axis == Axis.Z && !clockwise || axis == Axis.Y && clockwise) {
                return swapShafts(state);
            }
        } else if (rotationAxis == Axis.Y) {
            if (axis == Axis.X && !clockwise || axis == Axis.Z && clockwise) {
                return swapShafts(state);
            }
        } else if (rotationAxis == Axis.Z
                && (axis == Axis.Y && !clockwise || axis == Axis.X && clockwise)) {
            return swapShafts(state);
        }

        return state;
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        Axis axis = state.getValue(AXIS);
        if (axis == Axis.X && mirror == Mirror.FRONT_BACK || axis == Axis.Z && mirror == Mirror.LEFT_RIGHT) {
            return swapShafts(state);
        }
        return state;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        state = swapShaftsForRotation(state, rotation, Axis.Y);
        return super.rotate(state, rotation);
    }

    @Override
    public BlockState transform(BlockState state, StructureTransform transform) {
        if (transform.mirror != null) {
            state = mirror(state, transform.mirror);
        }

        if (transform.rotationAxis == Axis.Y) {
            return rotate(state, transform.rotation);
        }

        state = swapShaftsForRotation(state, transform.rotation, transform.rotationAxis);
        return state.setValue(AXIS, transform.rotateAxis(state.getValue(AXIS)));
    }

    @Override
    public ItemRequirement getRequiredItems(BlockState state, BlockEntity blockEntity) {
        return ItemRequirement.of(GreatechBlocks.getCogwheel(material, large).defaultBlockState(), blockEntity);
    }

    @Override
    public Class<BracketedKineticBlockEntity> getBlockEntityClass() {
        return BracketedKineticBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends BracketedKineticBlockEntity> getBlockEntityType() {
        return large ? GreatechBlockEntityTypes.getFamily(material).largeCogwheel().get()
                : GreatechBlockEntityTypes.getFamily(material).cogwheel().get();
    }

    @Override
    public Block getCasing() {
        return encasingType.casing();
    }

    @Override
    public void handleEncasing(BlockState state, Level level, BlockPos pos, ItemStack heldItem, Player player,
            InteractionHand hand, BlockHitResult ray) {
        BlockState encasedState = defaultBlockState().setValue(AXIS, state.getValue(AXIS));

        for (Direction direction : Iterate.directionsInAxis(state.getValue(AXIS))) {
            BlockState adjacentState = level.getBlockState(pos.relative(direction));
            if (!(adjacentState.getBlock() instanceof IRotate rotate)) {
                continue;
            }
            if (!rotate.hasShaftTowards(level, pos.relative(direction), adjacentState, direction.getOpposite())) {
                continue;
            }
            encasedState = encasedState.cycle(
                    direction.getAxisDirection() == AxisDirection.POSITIVE ? TOP_SHAFT : BOTTOM_SHAFT);
        }

        KineticBlockEntity.switchToBlockState(level, pos, encasedState);
    }
}
