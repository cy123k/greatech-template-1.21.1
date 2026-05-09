package com.greatech.content.hydraulic;

import static com.simibubi.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour.ProcessingResult.HOLD;
import static com.simibubi.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour.ProcessingResult.PASS;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.simibubi.create.Create;
import com.simibubi.create.content.kinetics.belt.BeltHelper;
import com.simibubi.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour.TransportedResult;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.createmod.catnip.nbt.NBTHelper;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class HydraulicPressingBehaviour extends BeltProcessingBehaviour {
    public static final int CYCLE = 240;
    private static final int ENTITY_SCAN = 10;

    public final List<ItemStack> particleItems = new ArrayList<>();
    private final HydraulicPressBlockEntity press;

    public int prevRunningTicks;
    public int runningTicks;
    public boolean running;
    public Mode mode = Mode.WORLD;
    private int entityScanCooldown = ENTITY_SCAN;

    public HydraulicPressingBehaviour(HydraulicPressBlockEntity press) {
        super(press);
        this.press = press;
        whenItemEnters((stack, handler) -> onItemReceived(stack, handler));
        whileItemHeld((stack, handler) -> whenItemHeld(stack, handler));
    }

    @Override
    public void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        running = tag.getBoolean("Running");
        mode = Mode.values()[tag.getInt("Mode")];
        prevRunningTicks = runningTicks = tag.getInt("Ticks");
        super.read(tag, registries, clientPacket);

        if (clientPacket) {
            NBTHelper.iterateCompoundList(tag.getList("ParticleItems", Tag.TAG_COMPOUND),
                    compound -> particleItems.add(ItemStack.parseOptional(registries, compound)));
            spawnParticles();
        }
    }

    @Override
    public void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        tag.putBoolean("Running", running);
        tag.putInt("Mode", mode.ordinal());
        tag.putInt("Ticks", runningTicks);
        super.write(tag, registries, clientPacket);

        if (clientPacket) {
            tag.put("ParticleItems", NBTHelper.writeCompoundList(particleItems,
                    stack -> (CompoundTag) stack.saveOptional(registries)));
            particleItems.clear();
        }
    }

    @Override
    public void tick() {
        super.tick();

        Level level = getWorld();
        BlockPos pos = getPos();
        if (!running || level == null) {
            if (level != null && !level.isClientSide && press.getKineticSpeed() != 0) {
                if (entityScanCooldown > 0) {
                    entityScanCooldown--;
                }
                if (entityScanCooldown <= 0) {
                    entityScanCooldown = ENTITY_SCAN;
                    if (BlockEntityBehaviour.get(level, pos.below(2),
                            TransportedItemStackHandlerBehaviour.TYPE) != null) {
                        return;
                    }
                    for (ItemEntity itemEntity : level.getEntitiesOfClass(ItemEntity.class,
                            new AABB(pos.below()).deflate(.125f))) {
                        if (!itemEntity.isAlive() || !itemEntity.onGround()) {
                            continue;
                        }
                        if (!press.tryProcessInWorld(itemEntity, true)) {
                            continue;
                        }
                        start(Mode.WORLD);
                        return;
                    }
                }
            }
            return;
        }

        if (level.isClientSide && runningTicks == -CYCLE / 2) {
            prevRunningTicks = CYCLE / 2;
            return;
        }

        if (runningTicks == CYCLE / 2 && press.getKineticSpeed() != 0) {
            if (mode == Mode.WORLD) {
                applyInWorld();
            }
            if (!level.isClientSide) {
                blockEntity.sendData();
            }
        }

        if (!level.isClientSide && runningTicks > CYCLE) {
            running = false;
            particleItems.clear();
            press.onPressingCompleted();
            blockEntity.sendData();
            return;
        }

        prevRunningTicks = runningTicks;
        runningTicks += getRunningTickSpeed();
        if (prevRunningTicks < CYCLE / 2 && runningTicks >= CYCLE / 2) {
            runningTicks = CYCLE / 2;
            if (level.isClientSide && !blockEntity.isVirtual()) {
                runningTicks = -(CYCLE / 2);
            }
        }
    }

    public void start(Mode mode) {
        this.mode = mode;
        running = true;
        prevRunningTicks = 0;
        runningTicks = 0;
        particleItems.clear();
        blockEntity.sendData();
    }

    public float getRenderedHeadOffset(float partialTicks) {
        if (!running) {
            return 0;
        }
        int absoluteTicks = Math.abs(runningTicks);
        float ticks = Mth.lerp(partialTicks, prevRunningTicks, absoluteTicks);
        if (absoluteTicks < (CYCLE * 2) / 3) {
            return (float) Mth.clamp(Math.pow(ticks / CYCLE * 2, 3), 0, 1);
        }
        return Mth.clamp((CYCLE - ticks) / CYCLE * 3, 0, 1);
    }

    private BeltProcessingBehaviour.ProcessingResult onItemReceived(TransportedItemStack transported,
            TransportedItemStackHandlerBehaviour handler) {
        if (press.getKineticSpeed() == 0 || running) {
            return running ? HOLD : PASS;
        }
        if (!press.tryProcessOnBelt(transported, null, true)) {
            return PASS;
        }
        start(Mode.BELT);
        return HOLD;
    }

    private BeltProcessingBehaviour.ProcessingResult whenItemHeld(TransportedItemStack transported,
            TransportedItemStackHandlerBehaviour handler) {
        if (press.getKineticSpeed() == 0 || !running) {
            return PASS;
        }
        if (runningTicks != CYCLE / 2) {
            return HOLD;
        }

        particleItems.clear();
        ArrayList<ItemStack> results = new ArrayList<>();
        if (!press.tryProcessOnBelt(transported, results, false)) {
            return PASS;
        }

        int processed = Math.max(1, press.getLastProcessedCount());
        transported.clearFanProcessingData();
        List<TransportedItemStack> created = results.stream()
                .map(stack -> {
                    TransportedItemStack copy = transported.copy();
                    boolean centered = BeltHelper.isItemUpright(stack);
                    copy.stack = stack;
                    copy.locked = true;
                    copy.angle = centered ? 180 : Create.RANDOM.nextInt(360);
                    return copy;
                })
                .collect(Collectors.toList());

        if (processed >= transported.stack.getCount()) {
            handler.handleProcessingOnItem(transported, created.isEmpty()
                    ? TransportedResult.removeItem()
                    : TransportedResult.convertTo(created));
        } else {
            TransportedItemStack left = transported.copy();
            left.stack.shrink(processed);
            handler.handleProcessingOnItem(transported, created.isEmpty()
                    ? TransportedResult.convertTo(left)
                    : TransportedResult.convertToAndLeaveHeld(created, left));
        }

        blockEntity.sendData();
        return HOLD;
    }

    private void applyInWorld() {
        Level level = getWorld();
        if (level == null || level.isClientSide) {
            return;
        }
        particleItems.clear();
        for (ItemEntity itemEntity : level.getEntitiesOfClass(ItemEntity.class,
                new AABB(getPos().below()).deflate(.125f))) {
            if (!itemEntity.isAlive() || !itemEntity.onGround()) {
                continue;
            }
            entityScanCooldown = 0;
            if (press.tryProcessInWorld(itemEntity, false)) {
                blockEntity.sendData();
            }
            break;
        }
    }

    private int getRunningTickSpeed() {
        float speed = press.getKineticSpeed();
        if (speed == 0) {
            return 0;
        }
        return (int) Mth.lerp(Mth.clamp(Math.abs(speed) / 512f, 0, 1), 1, 60);
    }

    private void spawnParticles() {
        if (particleItems.isEmpty()) {
            return;
        }
        Vec3 center = VecHelper.getCenterOf(getPos().below()).add(0, -1 / 4f, 0);
        particleItems.forEach(stack -> makePressingParticleEffect(center, stack));
        particleItems.clear();
    }

    private void makePressingParticleEffect(Vec3 pos, ItemStack stack) {
        Level level = getWorld();
        if (level == null || !level.isClientSide) {
            return;
        }
        for (int i = 0; i < 15; i++) {
            Vec3 motion = VecHelper.offsetRandomly(Vec3.ZERO, level.random, .125f).multiply(1, 0, 1);
            motion = motion.add(0, 0.125f, 0);
            level.addParticle(new ItemParticleOption(ParticleTypes.ITEM, stack), pos.x, pos.y - .25f, pos.z,
                    motion.x, motion.y, motion.z);
        }
    }

    public enum Mode {
        WORLD(1),
        BELT(19f / 16f);

        public final float headOffset;

        Mode(float headOffset) {
            this.headOffset = headOffset;
        }
    }
}
