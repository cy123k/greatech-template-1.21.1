package com.create.gregtech.greatech.content.steam;

import com.create.gregtech.greatech.content.kinetics.failure.KineticBreakable;
import com.create.gregtech.greatech.content.kinetics.GreatechKineticMaterial;
import com.create.gregtech.greatech.content.kinetics.MaterialKineticBlock;
import com.create.gregtech.greatech.content.kinetics.SteamPoweredKineticBlock;
import com.create.gregtech.greatech.content.shaft.GreatechShaftBlock;
import com.create.gregtech.greatech.registry.GreatechBlockEntityTypes;
import com.create.gregtech.greatech.registry.GreatechBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.AbstractShaftBlock;
import com.simibubi.create.content.kinetics.simpleRelays.ShaftBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class GreatechPoweredShaftBlock extends AbstractShaftBlock
        implements KineticBreakable, MaterialKineticBlock, SteamPoweredKineticBlock {
    private final GreatechKineticMaterial material;
    private final float breakStressLimit;

    public GreatechPoweredShaftBlock(GreatechKineticMaterial material, Properties properties, float breakStressLimit) {
        super(properties);
        this.material = material;
        this.breakStressLimit = breakStressLimit;
    }

    @Override
    public GreatechKineticMaterial getMaterial() {
        return material;
    }

    @Override
    public float getKineticBreakStressLimit() {
        return breakStressLimit;
    }

    @Override
    public BlockEntityType<? extends KineticBlockEntity> getBlockEntityType() {
        return GreatechBlockEntityTypes.getFamily(material).poweredShaft().get();
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return AllShapes.EIGHT_VOXEL_POLE.get(state.getValue(AXIS));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return 0;
    }

    @Override
    public boolean supportsExternalFaceHiding(BlockState state) {
        return false;
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState state) {
        return false;
    }

    @Override
    protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // Disabled compatibility block: no generated rotation while the steam output is redesigned.
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, net.minecraft.world.phys.HitResult target,
            LevelReader level, BlockPos pos, net.minecraft.world.entity.player.Player player) {
        return new ItemStack(asItem());
    }

    @Override
    public BlockState getUnpoweredEquivalent(BlockState stateForPlacement) {
        return GreatechBlocks.getShaft(material).defaultBlockState()
                .setValue(ShaftBlock.AXIS, stateForPlacement.getValue(ShaftBlock.AXIS))
                .setValue(GreatechShaftBlock.PLACEMENT_GHOST, false);
    }

    public static BlockState getEquivalent(BlockState stateForPlacement) {
        GreatechKineticMaterial material = stateForPlacement.getBlock() instanceof MaterialKineticBlock materialBlock
                ? materialBlock.getMaterial()
                : GreatechKineticMaterial.STEEL;
        return GreatechBlocks.getPoweredShaft(material).defaultBlockState()
                .setValue(ShaftBlock.AXIS, stateForPlacement.getValue(ShaftBlock.AXIS));
    }
}
