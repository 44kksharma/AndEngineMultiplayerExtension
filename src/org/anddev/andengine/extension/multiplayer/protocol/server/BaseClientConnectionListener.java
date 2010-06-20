package org.anddev.andengine.extension.multiplayer.protocol.server;

import org.anddev.andengine.extension.multiplayer.protocol.adt.message.client.BaseClientMessage;
import org.anddev.andengine.extension.multiplayer.protocol.shared.BaseConnectionListener;
import org.anddev.andengine.extension.multiplayer.protocol.shared.BaseConnector;
import org.anddev.andengine.util.Debug;

/**
 * @author Nicolas Gramlich
 * @since 01:00:11 - 20.09.2009
 */
public abstract class BaseClientConnectionListener extends BaseConnectionListener<BaseClientMessage, BaseConnector<BaseClientMessage>> {
	// ===========================================================
	// Final Fields
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================

	public static class DefaultClientConnectionListener extends BaseClientConnectionListener {
		@Override
		protected void onConnectInner(final BaseConnector<BaseClientMessage> pConnector) {
			Debug.d("Accepted Client-Connection from: '" + pConnector.getSocket().getRemoteSocketAddress() + "'");
		}

		@Override
		protected void onDisconnectInner(final BaseConnector<BaseClientMessage> pConnector) {
			Debug.d("Closed Client-Connection from: '" + pConnector.getSocket().getRemoteSocketAddress() + "'");
		}
	}
}
