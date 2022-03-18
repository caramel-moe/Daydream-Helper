package moe.caramel.daydream.helper;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Player class wrapper
 */
public final class PlayerUtil {

    private PlayerUtil() { throw new UnsupportedOperationException(); }

    /**
     * Returns the protocol version of the client.
     *
     * @return The client's protocol version, or {@code -1} if unknown
     * @see <a href="http://wiki.vg/Protocol_version_numbers">List of protocol
     *     version numbers</a>
     */
    public static int getProtocolVersion(@NotNull Player player) {
        return player.getProtocolVersion();
    }
}
