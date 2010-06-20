package org.anddev.andengine.extension.multiplayer.protocol.util.constants;

/**
 * @author Nicolas Gramlich
 * @since 15:37:07 - 18.09.2009
 */
public interface ClientMessageFlags {
	// ===========================================================
	// Final Fields
	// ===========================================================

	public static final short COUNT_FLAG_CLIENTMESSAGE_CONNECTION = 64;

	public static final short INDEX_FLAG_CLIENTMESSAGE_CONNECTION_FIRST = 0;
	public static final short INDEX_FLAG_CLIENTMESSAGE_CONNECTION_LAST = INDEX_FLAG_CLIENTMESSAGE_CONNECTION_FIRST + COUNT_FLAG_CLIENTMESSAGE_CONNECTION - 1;

	/* Connection ClientMessages */
	public static final short FLAG_CLIENTMESSAGE_CONNECTION_ESTABLISH = INDEX_FLAG_CLIENTMESSAGE_CONNECTION_FIRST;
	public static final short FLAG_CLIENTMESSAGE_CONNECTION_CLOSE = FLAG_CLIENTMESSAGE_CONNECTION_ESTABLISH + 1;
	public static final short FLAG_CLIENTMESSAGE_CONNECTION_PING = FLAG_CLIENTMESSAGE_CONNECTION_CLOSE + 1;
	public static final short FLAG_CLIENTMESSAGE_CONNECTION_PONG = FLAG_CLIENTMESSAGE_CONNECTION_PING + 1;

	// ===========================================================
	// Methods
	// ===========================================================
}
