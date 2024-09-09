package dev.emortal.minestom.edge;

import dev.agones.sdk.SDKGrpc;
import dev.emortal.api.agonessdk.AgonesUtils;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.concurrent.TimeUnit;

public class AgonesHandler {
    private static final String AGONES_ADDRESS = "127.0.0.1"; // SDK runs as a sidecar in production so address is always localhost
    private static final int AGONES_GRPC_PORT = Integer.parseInt(System.getenv("AGONES_SDK_GRPC_PORT"));

    private final SDKGrpc.SDKStub sdkStub;

    public AgonesHandler() {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(AGONES_ADDRESS, AGONES_GRPC_PORT).usePlaintext().build();
        this.sdkStub = SDKGrpc.newStub(channel);

        AgonesUtils.startHealthTask(this.sdkStub, 5, TimeUnit.SECONDS);
    }

    public void shutdown() {
        AgonesUtils.shutdownHealthTask();
    }
}
