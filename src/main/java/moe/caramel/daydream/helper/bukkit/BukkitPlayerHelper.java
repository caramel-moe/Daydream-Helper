package moe.caramel.daydream.helper.bukkit;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

/**
 * Player class helper
 */
@ApiStatus.NonExtendable
@SuppressWarnings("unused")
public interface BukkitPlayerHelper {

    /**
     * Returns the protocol version of the client.
     *
     * @param player the Player
     * @return The client's protocol version, or {@code -1} if unknown
     * @see <a href="http://wiki.vg/Protocol_version_numbers">List of protocol
     *     version numbers</a>
     */
    static int getProtocolVersion(final Player player) {
        return player.getProtocolVersion();
    }

    /**
     * Returns player's client brand name. If the client didn't send this information, the brand name will be null.<br>
     * For the Notchian client this name defaults to <code>vanilla</code>. Some modified clients report other names such as <code>forge</code>.<br>
     *
     * @param player the Player
     * @return client brand name
     */
    @Nullable
    static String getClientBrandName(final Player player) {
        return player.getClientBrandName();
    }
}
