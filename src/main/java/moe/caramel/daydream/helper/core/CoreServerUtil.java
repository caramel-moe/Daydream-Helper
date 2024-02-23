package moe.caramel.daydream.helper.core;

import net.minecraft.server.MinecraftServer;

/**
 * 서버를 제어하는 유틸리티 클래스
 */
@SuppressWarnings("unused")
public final class CoreServerUtil {

    private CoreServerUtil() { throw new UnsupportedOperationException(); }

    // ================================

    /**
     * 일부 서버 리소스를 리로드 후, 클라이언트에게 전송합니다.
     */
    public static void reloadResources() {
        MinecraftServer.getServer().getPlayerList().reloadResources();
    }

    // ================================
}
