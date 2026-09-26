package com.elesey3510.powerprojector;

import net.createmod.catnip.math.VoxelShaper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.patryk3211.powergrid.electricity.base.terminals.BlockStateTerminalCollection;
import org.patryk3211.powergrid.electricity.light.fixture.LightFixtureBlock;
import org.patryk3211.powergrid.electricity.light.fixture.LightFixtureBlockEntity;
import org.patryk3211.powergrid.electricity.wire.powercord.AutoCordEndpoint;
import org.patryk3211.powergrid.electricity.wire.powercord.IAcceptCord;
import org.patryk3211.powergrid.electricity.base.TerminalBoundingBox;
public class SpotlightBlock extends LightFixtureBlock implements IAcceptCord {

    // Пустой список терминалов для обычных проводов:
    private static final TerminalBoundingBox[] NO_TERMINALS = new TerminalBoundingBox[0];
    private static final VoxelShape SHAPE_UP = box(3.0, 0.0, 3.0, 13.0, 9.0, 13.0);
    private static final VoxelShaper SHAPER = VoxelShaper.forDirectional(SHAPE_UP, Direction.UP);
    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return 0;
    }
    public SpotlightBlock(Properties settings) {
        super(settings);

        // Передаем пустой массив через .forAllStates, чтобы массив не был null:
        setTerminalCollection(BlockStateTerminalCollection.builder(this)
                .forAllStates(state -> NO_TERMINALS)
                .withShapeMapper(state -> SHAPER.get(state.getValue(FACING)))
                .build());
    }
    // Задаем хитбокс выделения и коллизии блока под текущее направление
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPER.get(state.getValue(FACING));
    }

    // Логических контактов 2 (для шнура питания: L и N)
    @Override
    public int terminalCount() {
        return 2;
    }

    // 3. ТОЧКА ПОДКЛЮЧЕНИЯ ШНУРА (CORD)
    @Override
    public @Nullable AutoCordEndpoint getEndpoint(UseOnContext context) {
        var level = context.getLevel();
        var pos = context.getClickedPos();
        var state = level.getBlockState(pos);
        var facing = state.getValue(FACING);

        var center = Vec3.atCenterOf(pos);
        var normal = facing.getNormal();

        // Смещаем точку подключения штекера на ЗАДНЮЮ стенку прожектора:
        // normal смотрит вперед (по лучу), значит с минусом идем назад
        // 0.4375 блока = 7 пикселей от центра назад к задней грани
        // Ровно в центр задней крышки прожектора (на Z = 16):
        var plugPoint = center.add(
                normal.getX() * -0.5,
                normal.getY() * -0.5,
                normal.getZ() * -0.54);

        // Направление штекера — противоположное направлению прожектора (втыкается
        // сзади
        Direction plugFace = facing.getOpposite();

        return new AutoCordEndpoint(pos, 0, 1, plugPoint, plugFace);
    }

    @Override
    public BlockEntityType<? extends LightFixtureBlockEntity> getBlockEntityType() {
        return PowerProjector.SPOTLIGHT_BE.get();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<LightFixtureBlockEntity> getBlockEntityClass() {
        return (Class<LightFixtureBlockEntity>) (Class<?>) SpotlightBlockEntity.class;
    }
}