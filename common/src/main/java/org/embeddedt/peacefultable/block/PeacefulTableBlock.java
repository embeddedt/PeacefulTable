package org.embeddedt.peacefultable.block;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.embeddedt.peacefultable.PeacefulTable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.Random;

public class PeacefulTableBlock extends Block {
    private final boolean isAdvanced;
    public PeacefulTableBlock(Properties properties, boolean isAdvanced) {
        super(properties);
        this.isAdvanced = isAdvanced;
    }

    private Optional<MobSpawnSettings.SpawnerData> findMob(ServerLevel worldServer, BlockPos pos, Random rand) {
        return NaturalSpawner.getRandomSpawnMobAt(worldServer, worldServer.structureFeatureManager(), worldServer.getChunkSource().getGenerator(), MobCategory.MONSTER, rand, pos);
    }

    private boolean trySpawnMob(ServerLevel worldServer, Random rand, BlockPos pos) {
        Container inventory = null;
        int swordSlot = 0;
        ItemStack sword = null;
        boolean result = true;
        searchSword:
        for(Direction facing : Direction.values()) {
            BlockPos tilePos = pos.offset(facing.getNormal());
            if(!worldServer.isLoaded(tilePos))
                continue;
            BlockEntity be = worldServer.getBlockEntity(tilePos);
            if(be instanceof Container container) {
                inventory = container;
                int numSlots = container.getContainerSize();
                for(swordSlot = 0; swordSlot < numSlots; swordSlot++) {
                    ItemStack theItem = container.getItem(swordSlot);
                    if(theItem.getItem() instanceof SwordItem) {
                        sword = theItem.copy();
                        break searchSword;
                    }
                }
            }
        }
        if(sword == null) {
            return false;
        }
        Optional<MobSpawnSettings.SpawnerData> spawnMobData = findMob(worldServer, pos, rand);
        if(spawnMobData.isEmpty()) {
            PeacefulTable.LOGGER.error("Unable to create a mob");
            return false;
        }
        var spawnData = spawnMobData.get();
        var entity = spawnData.type.create(worldServer);
        if(!(entity instanceof Mob mob)) {
            PeacefulTable.LOGGER.error("Unable to create entity: " + spawnData.type);
            return false;
        }
        mob.moveTo(pos.above(), 0, 0);
        mob.finalizeSpawn(worldServer, worldServer.getCurrentDifficultyAt(pos), MobSpawnType.NATURAL, null, null);
        worldServer.addFreshEntity(mob);
        mob.tickCount = 1;
        mob.setLastHurtByPlayer(null);
        float theHealth = mob.getHealth();
        float attackDamage = ((SwordItem)sword.getItem()).getDamage() + EnchantmentHelper.getDamageBonus(sword, mob.getMobType());
        if(attackDamage > 0) {
            boolean willDropLoot = true;
            int durabilityLost = Mth.ceil(theHealth/attackDamage);
            if(durabilityLost > (sword.getMaxDamage()-sword.getDamageValue()))
                willDropLoot = false;
            if(sword.hurt(durabilityLost, rand, null)) {
                sword = ItemStack.EMPTY;
            }
            NonNullList<ItemStack> loot = NonNullList.create();
            if(willDropLoot) {
                Collection<Entity> currentEntities = Sets.newHashSet(worldServer.getAllEntities());
                mob.dropAllDeathLoot(DamageSource.GENERIC);
                ArrayList<Entity> afterEntities = Lists.newArrayList(worldServer.getAllEntities());
                afterEntities.removeIf(currentEntities::contains);
                for(Entity e : afterEntities) {
                    if(e instanceof ItemEntity)
                        loot.add(((ItemEntity)e).getItem());
                    e.remove(Entity.RemovalReason.DISCARDED);
                }
            }
            mob.remove(Entity.RemovalReason.DISCARDED);
            for(ItemStack drop : loot) {
                boolean consumedWholeStack = false;
                for(Direction facing : Direction.values()) {
                    BlockPos tilePos = pos.offset(facing.getNormal());
                    if(!worldServer.isLoaded(pos))
                        continue;
                    BlockEntity tileEntity = worldServer.getBlockEntity(tilePos);
                    if(tileEntity instanceof Container container) {
                        drop = HopperBlockEntity.addItem(null, container, drop, null);
                        if(drop.isEmpty()) {
                            consumedWholeStack = true;
                            break;
                        }
                    }
                }
                if(!consumedWholeStack)
                    result = false;
            }
        }

        inventory.setItem(swordSlot, sword);
        inventory.setChanged();

        return result;
    }

    @Override
    public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        if(serverLevel.getDifficulty() == Difficulty.PEACEFUL) {
            trySpawnMob(serverLevel, random, blockPos);
        }
    }
}
