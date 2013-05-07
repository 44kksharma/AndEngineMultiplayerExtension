package org.andengine.extension.multiplayer.protocol.client.connector;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.andengine.extension.multiplayer.protocol.adt.message.client.IClientMessage;
import org.andengine.extension.multiplayer.protocol.adt.message.server.IServerMessage;
import org.andengine.extension.multiplayer.protocol.client.IServerMessageHandler;
import org.andengine.extension.multiplayer.protocol.client.IServerMessageReader;
import org.andengine.extension.multiplayer.protocol.client.IServerMessageReader.ServerMessageReader;
import org.andengine.extension.multiplayer.protocol.server.ClientMessagePool;
import org.andengine.extension.multiplayer.protocol.shared.Connection;
import org.andengine.extension.multiplayer.protocol.shared.Connector;
import org.andengine.extension.multiplayer.protocol.util.MessagePool;
import org.andengine.util.adt.list.SmartList;
import org.andengine.util.call.ParameterCallable;
import org.andengine.util.debug.Debug;

/**
 * (c) 2010 Nicolas Gramlich
 * (c) 2011 Zynga Inc.
 *
 * @author Nicolas Gramlich
 * @since 21:40:51 - 18.09.2009
 */
public class ServerConnector<C extends Connection> extends Connector<C> {
	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	private final IServerMessageReader<C> mServerMessageReader;

	private final MessagePool<IClientMessage> mClientMessagePool;

	private final BlockingQueue<IClientMessage> mClientMessageQueue = new ArrayBlockingQueue<IClientMessage>(100, true); // TODO See if LinkedBlockingQueue works better

	private final ParameterCallable<IServerConnectorListener<C>> mOnStartedParameterCallable = new ParameterCallable<ServerConnector.IServerConnectorListener<C>>() {
		@Override
		public void call(final IServerConnectorListener<C> pServerConnectorListener) {
			pServerConnectorListener.onStarted(ServerConnector.this);
		}
	};

	private final ParameterCallable<IServerConnectorListener<C>> mOnTerminatedParameterCallable = new ParameterCallable<ServerConnector.IServerConnectorListener<C>>() {
		@Override
		public void call(final IServerConnectorListener<C> pServerConnectorListener) {
			pServerConnectorListener.onTerminated(ServerConnector.this);
		}
	};

	// ===========================================================
	// Constructors
	// ===========================================================

	public ServerConnector(final C pConnection, final IServerConnectorListener<C> pServerConnectorListener) throws IOException {
		this(pConnection, new ServerMessageReader<C>(), new ClientMessagePool(), pServerConnectorListener);
	}

	public ServerConnector(final C pConnection, final IServerMessageReader<C> pServerMessageReader, final IServerConnectorListener<C> pServerConnectorListener) throws IOException {
		this(pConnection, pServerMessageReader, new ClientMessagePool(), pServerConnectorListener);
	}

	public ServerConnector(final C pConnection, final MessagePool<IClientMessage> pClientMessagePool, final IServerConnectorListener<C> pServerConnectorListener) throws IOException {
		this(pConnection, new ServerMessageReader<C>(), pClientMessagePool, pServerConnectorListener);
	}

	public ServerConnector(final C pConnection, final IServerMessageReader<C> pServerMessageReader, final MessagePool<IClientMessage> pClientMessagePool, final IServerConnectorListener<C> pServerConnectorListener) throws IOException {
		super(pConnection);

		this.mServerMessageReader = pServerMessageReader;
		this.mClientMessagePool = pClientMessagePool;

		this.addServerConnectorListener(pServerConnectorListener);
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public IServerMessageReader<C> getServerMessageReader() {
		return this.mServerMessageReader;
	}

	public MessagePool<IClientMessage> getClientMessagePool() {
		return this.mClientMessagePool;
	}

	public IClientMessage obtainClientMessage(final short pFlag) {
		return this.mClientMessagePool.obtainMessage(pFlag);
	}

	@SuppressWarnings("unchecked")
	@Override
	public SmartList<IServerConnectorListener<C>> getConnectorListeners() {
		return (SmartList<IServerConnectorListener<C>>) super.getConnectorListeners();
	}

	public void addServerConnectorListener(final IServerConnectorListener<C> pServerConnectorListener) {
		super.addConnectorListener(pServerConnectorListener);
	}

	public boolean removeServerConnectorListener(final IServerConnectorListener<C> pServerConnectorListener) {
		return super.removeConnectorListener(pServerConnectorListener);
	}

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	@Override
	public void onStarted(final Connection pConnection) {
		this.getConnectorListeners().call(this.mOnStartedParameterCallable);
	}

	@Override
	public void onTerminated(final Connection pConnection) {
		this.getConnectorListeners().call(this.mOnTerminatedParameterCallable);
	}

	@Override
	public void read(final DataInputStream pDataInputStream) throws IOException {
		final IServerMessage serverMessage = this.mServerMessageReader.readMessage(pDataInputStream);
		this.mServerMessageReader.handleMessage(this, serverMessage);
		this.mServerMessageReader.recycleMessage(serverMessage);
	}

	@Override
	public void write(final DataOutputStream pDataOutputStream) throws IOException, InterruptedException {
		final IClientMessage clientMessage = this.mClientMessageQueue.take();
		clientMessage.write(pDataOutputStream);
		pDataOutputStream.flush();
		this.mClientMessagePool.recycleMessage(clientMessage);
	}

	// ===========================================================
	// Methods
	// ===========================================================

	public void registerServerMessage(final short pFlag, final Class<? extends IServerMessage> pServerMessageClass) {
		this.mServerMessageReader.registerMessage(pFlag, pServerMessageClass);
	}

	public void registerServerMessage(final short pFlag, final Class<? extends IServerMessage> pServerMessageClass, final IServerMessageHandler<C> pServerMessageHandler) {
		this.mServerMessageReader.registerMessage(pFlag, pServerMessageClass, pServerMessageHandler);
	}

	public void registerServerMessageHandler(final short pFlag, final IServerMessageHandler<C> pServerMessageHandler) {
		this.mServerMessageReader.registerMessageHandler(pFlag, pServerMessageHandler);
	}

	public synchronized void sendClientMessage(final IClientMessage pClientMessage) {
		this.mClientMessageQueue.add(pClientMessage);
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

	public static interface IServerConnectorListener<T extends Connection> extends IConnectorListener<ServerConnector<T>> {
		// ===========================================================
		// Final Fields
		// ===========================================================

		// ===========================================================
		// Methods
		// ===========================================================

		@Override
		public void onStarted(final ServerConnector<T> pServerConnector);

		@Override
		public void onTerminated(final ServerConnector<T> pServerConnector);
	}
}
