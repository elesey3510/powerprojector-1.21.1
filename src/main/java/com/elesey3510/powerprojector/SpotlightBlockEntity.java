package com.elesey3510.powerprojector;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.patryk3211.powergrid.electricity.light.fixture.LightFixtureBlock;
import org.patryk3211.powergrid.electricity.light.fixture.LightFixtureBlockEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaterniondc;
import dev.ryanhcode.sable.companion.math.Pose3d;
import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.light.data.AreaLightData;
import foundry.veil.api.client.render.light.renderer.LightRenderHandle;

public class SpotlightBlockEntity extends LightFixtureBlockEntity {
    private LightRenderHandle<AreaLightData> lightHandle = null;
    private AreaLightData light = null;

    private final Pose3d interpolatedPose = new Pose3d();
    // Термодинамика на клиенте (плавный нагрев и остывание нити)
    private float currentKelvin = 0.0f;
    private float currentBrightness = 0.0f;

    public SpotlightBlockEntity(BlockPos pos, BlockState blockState) {
        super(PowerProjector.SPOTLIGHT_BE.get(), pos, blockState);
    }

    @Override
    public void tick() {
        super.tick();
        if (level != null && level.isClientSide) {
            clientTick();
        }
    }
    @Override
    public net.minecraft.world.phys.AABB getRenderBoundingBox() {
        // Расширяем коробку видимости на 80 блоков (длину луча),
        // чтобы игра не выключала рендер прожектора, когда сам блок находится сзади игрока, а луч светит вперед
        return new net.minecraft.world.phys.AABB(worldPosition).inflate(80.0);
    }
    private void clientTick() {
        if (isRemoved()
                || (level != null && level.getBlockState(worldPosition).getBlock() != getBlockState().getBlock())) {
            removeLight();
            return;
        }

        int powerLevel = getPowerLevel();
        boolean hasWorkingBulb = bulbState != null && !bulbState.isBurned();

        // 1. Определяем целевую температуру и яркость от состояния Power Grid
        float targetKelvin = 0.0f;
        float targetBrightness = 0.0f;

        if (hasWorkingBulb && powerLevel > 0) {
            if (powerLevel == 1) {
                // Просадка напряжения: недогретая нить (густой оранжевый свет)
                targetKelvin = 1900.0f;
                targetBrightness = 0.6f;
            } else {
                // Номинал: штатный режим мощной галогенки (теплый белый)
                targetKelvin = 3200.0f;
                targetBrightness = 2.0f;
            }
        }

        // 2. Тепловая инерция (нить плавно нагревается за ~0.2 сек и плавно остывает)
        currentKelvin = Mth.lerp(0.15f, currentKelvin, targetKelvin);
        currentBrightness = Mth.lerp(0.15f, currentBrightness, targetBrightness);

        // 3. Управление источником света Veil
        if (currentBrightness > 0.05f && currentKelvin >= 1000.0f) {
            if (lightHandle == null) {
                light = new AreaLightData();
                // Инициализируем координаты один раз при создании:
                updateLightParameters(1.0f);
                // Добавляем готовый источник в Veil
                lightHandle = VeilRenderSystem.renderer().getLightRenderer().addLight(light);
            }
        } else {
            removeLight();
        }
    }

    public void frameUpdate(float partialTick) {
        if (light != null && currentBrightness > 0.05f) {
            updateLightParameters(partialTick);
        }
    }

    private void updateLightParameters(float partialTick) {
        if (light == null || level == null)
            return;

        Direction direction = getBlockState().getValue(LightFixtureBlock.FACING);

        // 1. Позиция
        Vec3 localPos = new Vec3(
                worldPosition.getX() + 0.5 + direction.getStepX() * 0.08,
                worldPosition.getY() + 0.5 + direction.getStepY() * 0.08,
                worldPosition.getZ() + 0.5 + direction.getStepZ() * 0.08);

        double finalX = localPos.x;
        double finalY = localPos.y;
        double finalZ = localPos.z;

        // Локальный вектор луча
        Vector3d worldDir = new Vector3d(direction.getStepX(), direction.getStepY(), direction.getStepZ());
        // Вектор «верха» (для исключения переворотов при тангаже)
        Vector3d worldUp = new Vector3d(0.0, 1.0, 0.0);

        // 2. Если на корабле — используем плавную интерполяцию Sable:
        if (level != null) {
            SubLevelAccess subLevel = SableCompanion.INSTANCE.getContaining(level, worldPosition);
            if (subLevel != null) {
                // Плавно смешиваем прошлый и текущий тик под частоту монитора:
                Pose3dc pose = subLevel.lastPose().lerp(subLevel.logicalPose(), (double) partialTick,
                        this.interpolatedPose);

                // Позиция через Sable
                Vec3 globalPos = pose.transformPosition(localPos);
                finalX = globalPos.x;
                finalY = globalPos.y;
                finalZ = globalPos.z;

                // Направление и вектор «верха» через официальный transformNormal:
                pose.transformNormal(worldDir, worldDir);
                pose.transformNormal(worldUp, worldUp);
            }
        }

        // 3. Строем ориентацию (lookAlong с mul(-1)):
        Quaternionf finalRot = new Quaternionf();
        Vector3f dirF = new Vector3f((float) worldDir.x, (float) worldDir.y, (float) worldDir.z).mul(-1.0f);
        Vector3f upF = new Vector3f((float) worldUp.x, (float) worldUp.y, (float) worldUp.z);

        // Если луч направлен строго параллельно UP-вектору (строго вверх или вниз),
        // меняем вспомогательную ось
        if (Math.abs(dirF.dot(upF)) > 0.99f) {
            upF.set(0.0f, 0.0f, 1.0f);
        }

        finalRot.lookAlong(dirF, upF);

        // Передаем координаты и поворот в Veil
        light.getPositionMutable().set(finalX, finalY, finalZ);
        light.getOrientationMutable().set(finalRot);

        // Размер линзы прожектора (под раструб вашей 3D-модели)
        light.setSize(0.5, 0.5);

        // Дистанция и угол рассеивания луча
        light.setDistance(80.0f);
        light.setAngle((float) Math.toRadians(35));

        // 4. Цвет и яркость (с формулой абсолютно черного тела)
        Vector3f color = Formulas.kelvinToRGB(currentKelvin);
        if (bulbState != null && bulbState.getColor() != null) {
            DyeColor dye = bulbState.getColor();
            int rgbInt = dye.getTextureDiffuseColor();
            float r = ((rgbInt >> 16) & 0xFF) / 255.0f;
            float g = ((rgbInt >> 8) & 0xFF) / 255.0f;
            float b = (rgbInt & 0xFF) / 255.0f;
            color.mul(r, g, b);
        }

        light.setColor(color);
        light.setBrightness(currentBrightness);

        // 5. Обязательно уведомляем GPU об обновлении кадра
        light.markDirty();
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if (level != null && level.isClientSide) {
            removeLight();
        }
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        if (level != null && level.isClientSide) {
            removeLight();
        }
    }

    private Quaternionf getLightOrientation(Direction direction) {
        return switch (direction) {
            case SOUTH -> new Quaternionf();
            case NORTH -> new Quaternionf().rotationY((float) Math.PI);
            case UP -> new Quaternionf().rotationX((float) Math.PI / 2F);
            case DOWN -> new Quaternionf().rotationX((float) -Math.PI / 2F);
            case EAST -> new Quaternionf().rotationY((float) -Math.PI / 2F);
            case WEST -> new Quaternionf().rotationY((float) Math.PI / 2F);
        };
    }

    private void removeLight() {
        if (lightHandle != null) {
            lightHandle.close();
            lightHandle = null;
            light = null;
        }
    }

    @Override
    public void destroy() {
        // В Create вместо setRemoved() для SmartBlockEntity переопределяется destroy()
        if (level != null && level.isClientSide) {
            removeLight();
        }
        super.destroy();
    }
}