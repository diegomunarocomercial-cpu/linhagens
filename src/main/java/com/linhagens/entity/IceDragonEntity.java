package com.linhagens.entity;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.control.FlightMoveControl;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.FlyGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.pathing.BirdNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
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
import net.minecraft.particle.BlockStateParticleEffect;
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
 * Dragão de Gelo. O mesmo código serve para o chefe (ICE_DRAGON, "Dragão Ancião de Gelo") e para os filhotes.
 * VOA. Arsenal do chefe: Sopro Gélido, Estacas de Gelo, Rugido, Mergulho, Nevasca (50% de vida) e invocação de filhotes.
 */
public class IceDragonEntity extends HostileEntity {
    private static final TrackedData<Boolean> BREATHING = DataTracker.registerData(IceDragonEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    private final ServerBossBar bossBar = new ServerBossBar(Text.literal("Dragão Ancião de Gelo"),
            BossBar.Color.WHITE, BossBar.Style.NOTCHED_10);
    private int breathCooldown = 60;
    private int breathTicks = 0;
    private int skillCooldown = 80;
    private int blizzardTicks = 0;
    private int blizzardCooldown = 0;
    private boolean summoned66 = false, summoned33 = false;
    private final List<double[]> strikes = new ArrayList<>();   // {x, y, z, ticksRestantes}

    public IceDragonEntity(EntityType<? extends IceDragonEntity> type, World world) {
        super(type, world);
        this.moveControl = new FlightMoveControl(this, 20, true);
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
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 400.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25)
                .add(EntityAttributes.GENERIC_FLYING_SPEED, 0.6)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 16.0)
                .add(EntityAttributes.GENERIC_ARMOR, 10.0)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1.0)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 56.0);
    }

    public static DefaultAttributeContainer.Builder createBabyAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 50.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3)
                .add(EntityAttributes.GENERIC_FLYING_SPEED, 0.5)
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
    protected EntityNavigation createNavigation(World world) {
        BirdNavigation nav = new BirdNavigation(this, world);
        nav.setCanPathThroughDoors(false);
        nav.setCanSwim(true);
        nav.setCanEnterOpenDoors(true);
        return nav;
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(2, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.add(3, new FlyGoal(this, 0.8));
        this.goalSelector.add(4, new LookAtEntityGoal(this, PlayerEntity.class, 12.0f));
        this.goalSelector.add(5, new LookAroundGoal(this));
        this.targetSelector.add(1, new RevengeGoal(this));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
    }

    @Override
    public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
        return false;
    }

    // ------------------------------------------------------------------ IA / habilidades
    @Override
    protected void mobTick() {
        super.mobTick();
        if (!(this.getWorld() instanceof ServerWorld w)) return;
        if (isBoss()) bossBar.setPercent(this.getHealth() / this.getMaxHealth());
        if (breathTicks > 0 && --breathTicks == 0) this.dataTracker.set(BREATHING, false);
        if (breathCooldown > 0) breathCooldown--;
        if (skillCooldown > 0) skillCooldown--;
        if (blizzardCooldown > 0) blizzardCooldown--;

        tickStrikes(w);
        tickBlizzard(w);

        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) return;
        double dist = this.distanceTo(target);
        boolean sees = this.canSee(target);
        float hp = this.getHealth() / this.getMaxHealth();

        if (!isBoss()) {
            if (sees && breathCooldown <= 0 && dist >= 3.0 && dist <= 9.0) {
                breathe(target, 9.0, 3.5f);
                breathCooldown = 140;
            }
            return;
        }

        // invocação de filhotes em 66% e 33% da vida
        if (!summoned66 && hp <= 0.66f) { summoned66 = true; summonBabies(w); }
        if (!summoned33 && hp <= 0.33f) { summoned33 = true; summonBabies(w); }

        int cdScale = hp <= 0.30f ? 2 : 3;          // enfurecido: mais rápido
        if (skillCooldown > 0 || !sees) return;
        int r = this.random.nextInt(100);

        if (hp <= 0.5f && blizzardCooldown <= 0 && r < 25) {
            blizzardTicks = 100;
            blizzardCooldown = 500;
            skillCooldown = 40 * cdScale;
            w.playSound(null, getX(), getY(), getZ(), SoundEvents.ENTITY_ENDER_DRAGON_GROWL, SoundCategory.HOSTILE, 3f, 0.6f);
            return;
        }
        if (dist <= 7) {
            if (r < 38) roar(w);
            else if (r < 68) { breathe(target, 15.0, 9f); breathCooldown = 40; }
            else castStakes(w, target);
        } else if (dist <= 15) {
            if (r < 40) { breathe(target, 15.0, 9f); breathCooldown = 40; }
            else if (r < 75) castStakes(w, target);
            else dive(target);
        } else {
            if (r < 55) dive(target);
            else castStakes(w, target);
        }
        skillCooldown = 22 * cdScale;
    }

    private void breathe(LivingEntity target, double range, float damage) {
        if (!(this.getWorld() instanceof ServerWorld w)) return;
        this.dataTracker.set(BREATHING, true);
        breathTicks = 25;
        Vec3d origin = this.getEyePos();
        Vec3d dir = target.getEyePos().subtract(origin).normalize();
        Box box = this.getBoundingBox().expand(range);
        for (LivingEntity e : w.getEntitiesByClass(LivingEntity.class, box, x -> x != this && !(x instanceof IceDragonEntity) && x.isAlive())) {
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
        w.playSound(null, getX(), getY(), getZ(), SoundEvents.ENTITY_PLAYER_HURT_FREEZE, SoundCategory.HOSTILE, 1.5f, isBoss() ? 0.5f : 1.2f);
    }

    /** Estacas de Gelo: marcam o chão em volta do alvo e explodem 1,2s depois. */
    private void castStakes(ServerWorld w, LivingEntity target) {
        for (int i = 0; i < 6; i++) {
            double a = this.random.nextDouble() * Math.PI * 2;
            double r = i == 0 ? 0 : 1.5 + this.random.nextDouble() * 3.5;
            strikes.add(new double[]{target.getX() + Math.cos(a) * r, target.getY(), target.getZ() + Math.sin(a) * r, 24});
        }
        w.playSound(null, getX(), getY(), getZ(), SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.HOSTILE, 2f, 0.5f);
    }

    private void tickStrikes(ServerWorld w) {
        for (int i = strikes.size() - 1; i >= 0; i--) {
            double[] s = strikes.get(i);
            if (s[3] > 0) {
                s[3]--;
                if (((int) s[3]) % 4 == 0) w.spawnParticles(ParticleTypes.SNOWFLAKE, s[0], s[1] + 0.2, s[2], 6, 0.5, 0.1, 0.5, 0.01);
                continue;
            }
            Box box = new Box(s[0] - 1.6, s[1] - 1, s[2] - 1.6, s[0] + 1.6, s[1] + 3, s[2] + 1.6);
            for (LivingEntity e : w.getEntitiesByClass(LivingEntity.class, box, x -> x != this && !(x instanceof IceDragonEntity) && x.isAlive())) {
                e.damage(this.getDamageSources().mobAttack(this), isBoss() ? 11f : 4f);
                e.setFrozenTicks(200);
            }
            w.spawnParticles(new BlockStateParticleEffect(ParticleTypes.BLOCK, Blocks.PACKED_ICE.getDefaultState()), s[0], s[1] + 0.5, s[2], 30, 0.6, 1.2, 0.6, 0.2);
            w.spawnParticles(ParticleTypes.END_ROD, s[0], s[1] + 1, s[2], 8, 0.3, 1.0, 0.3, 0.05);
            w.playSound(null, s[0], s[1], s[2], SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.HOSTILE, 1.2f, 1.4f);
            strikes.remove(i);
        }
    }

    /** Rugido: onda de choque em volta do dragão. */
    private void roar(ServerWorld w) {
        for (LivingEntity e : w.getEntitiesByClass(LivingEntity.class, this.getBoundingBox().expand(13),
                x -> x != this && !(x instanceof IceDragonEntity) && x.isAlive())) {
            e.damage(this.getDamageSources().mobAttack(this), 7f);
            e.takeKnockback(1.8, this.getX() - e.getX(), this.getZ() - e.getZ());
            e.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 100, 1));
            e.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 100, 1));
        }
        for (int i = 0; i < 40; i++) {
            double a = i * Math.PI * 2 / 40;
            w.spawnParticles(ParticleTypes.CLOUD, getX() + Math.cos(a) * 6, getY() + 1, getZ() + Math.sin(a) * 6, 2, 0.2, 0.3, 0.2, 0.15);
        }
        w.playSound(null, getX(), getY(), getZ(), SoundEvents.ENTITY_ENDER_DRAGON_GROWL, SoundCategory.HOSTILE, 4f, 0.7f);
    }

    /** Mergulho: avança com força contra o alvo. */
    private void dive(LivingEntity target) {
        Vec3d dir = target.getPos().add(0, target.getHeight() / 2, 0).subtract(this.getPos()).normalize();
        this.setVelocity(dir.multiply(1.7));
        this.velocityModified = true;
        this.getWorld().playSound(null, getX(), getY(), getZ(), SoundEvents.ENTITY_ENDER_DRAGON_FLAP, SoundCategory.HOSTILE, 3f, 0.8f);
    }

    /** Nevasca: por 5 segundos, congela e fere quem estiver por perto. */
    private void tickBlizzard(ServerWorld w) {
        if (blizzardTicks <= 0) return;
        blizzardTicks--;
        w.spawnParticles(ParticleTypes.SNOWFLAKE, getX(), getY() + 1, getZ(), 30, 8, 3, 8, 0.2);
        if (blizzardTicks % 10 == 0) {
            for (LivingEntity e : w.getEntitiesByClass(LivingEntity.class, this.getBoundingBox().expand(14),
                    x -> x != this && !(x instanceof IceDragonEntity) && x.isAlive())) {
                e.damage(this.getDamageSources().mobAttack(this), 3f);
                e.setFrozenTicks(Math.max(e.getFrozenTicks(), 200));
                e.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 40, 0));
            }
        }
    }

    private void summonBabies(ServerWorld w) {
        for (int i = 0; i < 2; i++) {
            IceDragonEntity baby = new IceDragonEntity(ModEntities.ICE_DRAGON_BABY, w);
            baby.refreshPositionAndAngles(getX() + (i == 0 ? 4 : -4), getY() + 1, getZ(), getYaw(), 0);
            baby.setPersistent();
            w.spawnEntity(baby);
            w.spawnParticles(ParticleTypes.CLOUD, baby.getX(), baby.getY() + 1, baby.getZ(), 20, 0.5, 0.5, 0.5, 0.05);
        }
        w.playSound(null, getX(), getY(), getZ(), SoundEvents.ENTITY_ENDER_DRAGON_GROWL, SoundCategory.HOSTILE, 3f, 1.2f);
    }

    // ------------------------------------------------------------------ resto
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
