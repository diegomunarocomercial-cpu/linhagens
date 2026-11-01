package com.linhagens.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Dragão de Gelo. O mesmo código serve para o chefe (ICE_DRAGON, enorme) e para os filhotes (ICE_DRAGON_BABY).
 * Ataca de perto e solta um Sopro Gélido em cone (dano + congelamento + lentidão).
 */
public class IceDragonEntity extends HostileEntity {
    private static final TrackedData<Boolean> BREATHING = DataTracker.registerData(IceDragonEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    private final ServerBossBar bossBar = new ServerBossBar(Text.literal("Dragão de Gelo Ancestral"),
            BossBar.Color.WHITE, BossBar.Style.NOTCHED_10);
    private int breathCooldown = 60;
    private int breathTicks = 0;

    public IceDragonEntity(EntityType<? extends IceDragonEntity> type, World world) {
        super(type, world);
        this.experiencePoints = isBoss() ? 500 : 60;
    }

    public boolean isBoss() {
        return this.getType() == ModEntities.ICE_DRAGON;
    }

    public boolean isBreathing() {
        return this.dataTracker.get(BREATHING);
    }

    public static DefaultAttributeContainer.Builder createBossAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 300.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.27)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 14.0)
                .add(EntityAttributes.GENERIC_ARMOR, 10.0)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1.0)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 48.0);
    }

    public static DefaultAttributeContainer.Builder createBabyAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 45.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.34)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 6.0)
                .add(EntityAttributes.GENERIC_ARMOR, 3.0)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(BREATHING, false);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(2, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.add(3, new WanderAroundFarGoal(this, 0.7));
        this.goalSelector.add(4, new LookAtEntityGoal(this, PlayerEntity.class, 12.0f));
        this.goalSelector.add(5, new LookAroundGoal(this));
        this.targetSelector.add(1, new RevengeGoal(this));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
    }

    @Override
    protected void mobTick() {
        super.mobTick();
        if (isBoss()) {
            bossBar.setPercent(this.getHealth() / this.getMaxHealth());
        }
        if (breathTicks > 0 && --breathTicks == 0) {
            this.dataTracker.set(BREATHING, false);
        }
        if (breathCooldown > 0) breathCooldown--;

        LivingEntity target = this.getTarget();
        double range = isBoss() ? 15.0 : 9.0;
        if (target != null && breathCooldown <= 0 && this.canSee(target)) {
            double dist = this.distanceTo(target);
            if (dist >= 3.0 && dist <= range) {
                breathe(target, range);
                breathCooldown = isBoss() ? 90 : 140;
            }
        }
    }

    private void breathe(LivingEntity target, double range) {
        if (!(this.getWorld() instanceof ServerWorld w)) return;
        this.dataTracker.set(BREATHING, true);
        breathTicks = 25;

        Vec3d origin = this.getEyePos();
        Vec3d dir = target.getEyePos().subtract(origin).normalize();
        float damage = isBoss() ? 9f : 3.5f;
        Box box = this.getBoundingBox().expand(range);
        for (LivingEntity e : w.getEntitiesByClass(LivingEntity.class, box,
                x -> x != this && !(x instanceof IceDragonEntity) && x.isAlive())) {
            Vec3d to = e.getPos().add(0, e.getHeight() / 2, 0).subtract(origin);
            double dist = to.length();
            if (dist > range || dist < 0.5) continue;
            if (to.normalize().dotProduct(dir) < 0.72) continue;
            e.damage(this.getDamageSources().mobAttack(this), damage);
            e.setFrozenTicks(Math.max(e.getFrozenTicks(), 240));
            e.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 80, isBoss() ? 2 : 1));
        }
        for (int i = 1; i <= (int) (range * 2); i++) {
            double d = i * 0.5;
            Vec3d p = origin.add(dir.multiply(d));
            w.spawnParticles(ParticleTypes.SNOWFLAKE, p.x, p.y, p.z, 5, d * 0.06, d * 0.06, d * 0.06, 0.02);
            if (i % 3 == 0) w.spawnParticles(ParticleTypes.CLOUD, p.x, p.y, p.z, 1, d * 0.05, d * 0.05, d * 0.05, 0.01);
        }
        w.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_PLAYER_HURT_FREEZE,
                SoundCategory.HOSTILE, 1.5f, isBoss() ? 0.5f : 1.2f);
    }

    @Override
    public boolean tryAttack(Entity target) {
        boolean hit = super.tryAttack(target);
        if (hit && target instanceof LivingEntity living) {
            living.setFrozenTicks(Math.max(living.getFrozenTicks(), 160));
        }
        return hit;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return source.isOf(DamageTypes.FREEZE) || super.isInvulnerableTo(source);
    }

    @Override
    public boolean canFreeze() {
        return false;
    }

    @Override
    public boolean canImmediatelyDespawn(double distanceSquared) {
        return false;
    }

    @Override
    public void onStartedTrackingBy(ServerPlayerEntity player) {
        super.onStartedTrackingBy(player);
        if (isBoss()) bossBar.addPlayer(player);
    }

    @Override
    public void onStoppedTrackingBy(ServerPlayerEntity player) {
        super.onStoppedTrackingBy(player);
        bossBar.removePlayer(player);
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        super.remove(reason);
        bossBar.clearPlayers();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_ENDER_DRAGON_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_ENDER_DRAGON_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_ENDER_DRAGON_DEATH;
    }
}
