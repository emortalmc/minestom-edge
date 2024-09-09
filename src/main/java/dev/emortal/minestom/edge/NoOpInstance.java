package dev.emortal.minestom.edge;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.instance.generator.Generator;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.utils.chunk.ChunkSupplier;
import net.minestom.server.world.DimensionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class NoOpInstance extends Instance {

    public NoOpInstance(@NotNull UUID uniqueId, DynamicRegistry.@NotNull Key<DimensionType> dimensionType) {
        super(uniqueId, dimensionType);
    }

    @Override
    public void setBlock(int i, int i1, int i2, @NotNull Block block, boolean b) {

    }

    @Override
    public boolean placeBlock(BlockHandler.@NotNull Placement placement, boolean b) {
        return false;
    }

    @Override
    public boolean breakBlock(@NotNull Player player, @NotNull Point point, @NotNull BlockFace blockFace, boolean b) {
        return false;
    }

    @Override
    public @NotNull CompletableFuture<@NotNull Chunk> loadChunk(int i, int i1) {
        return null;
    }

    @Override
    public @NotNull CompletableFuture<@Nullable Chunk> loadOptionalChunk(int i, int i1) {
        return null;
    }

    @Override
    public void unloadChunk(@NotNull Chunk chunk) {

    }

    @Override
    public @Nullable Chunk getChunk(int i, int i1) {
        return null;
    }

    @Override
    public @NotNull CompletableFuture<Void> saveInstance() {
        return null;
    }

    @Override
    public @NotNull CompletableFuture<Void> saveChunkToStorage(@NotNull Chunk chunk) {
        return null;
    }

    @Override
    public @NotNull CompletableFuture<Void> saveChunksToStorage() {
        return null;
    }

    @Override
    public void setChunkSupplier(@NotNull ChunkSupplier chunkSupplier) {

    }

    @Override
    public ChunkSupplier getChunkSupplier() {
        return null;
    }

    @Override
    public @Nullable Generator generator() {
        return null;
    }

    @Override
    public void setGenerator(@Nullable Generator generator) {

    }

    @Override
    public @NotNull Collection<@NotNull Chunk> getChunks() {
        return List.of();
    }

    @Override
    public void enableAutoChunkLoad(boolean b) {

    }

    @Override
    public boolean hasEnabledAutoChunkLoad() {
        return false;
    }

    @Override
    public boolean isInVoid(@NotNull Point point) {
        return false;
    }
}
