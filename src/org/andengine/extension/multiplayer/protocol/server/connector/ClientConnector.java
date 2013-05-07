package org.andengine.extension.multiplayer.protocol.server.connector;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.andengine.extension.multiplayer.protocol.adt.message.client.IClientMessage;
import org.andengine.extension.multiplayer.protocol.adt.message.server.IServerMessage;
import org.andengine.extension.multiplayer.protocol.client.ServerMessagePool;
import org.andengine.extension.multiplayer.protocol.server.IClientMessageHandler;
import org.andengine.extension.multiplayer.protocol.server.IClientMessageReader;
import org.andengine.extension.multiplayer.protocol.server.IClientMessageReader.ClientMessageReader;
import org.andengine.extension.multiplayer.protocol.shared.Connection;
import org.andengine.extension.multiplayer.protocol.shared.Connector;
import org.andengine.extension.multiplayer.protocol.util.MessagePool;
import org.andengine.util.adt.list.SmartList;
import org.andengine.util.call.ParameterCallable;

/**
 * (c) 2010 Nicolas Gramlich
 * (c) 2011 Zynga Inc.
 *
 * @author Nicolas Gramlich
 * @since 21:40:51 - 18.09.2009
 */
public class ClientConnector<C extends Connection> extends Connector<C> {
	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	private final IClientMessageReader<C> mClientMessageReader;

	private final MessagePool<IServerMessage> mServerMessagePool;

	private final BlockingQueue<IServerMessage> mServerMessageQueue = new ArrayBlockingQueue<IServerMessage>(100, true); // TODO See if LinkedBlockingQueue works better

	private final ParameterCallable<IClientConnectorListener<C>> mOnStartedParameterCallable = new ParameterCallable<ClientConnector.IClientConnectorListener<C>>() {
		@Override
		public void call(final IClientConnectorListener<C> pClientConnectorListener) {
			pClientConnectorListener.onStarted(ClientConnector.this);
		}
	};

	private final ParameterCallable<IClientConnectorListener<C>> mOnTerminatedParameterCallable = new ParameterCallable<ClientConnector.IClientConnectorListener<C>>() {
		@Override
		public void call(final IClientConnectorListener<C> pClientConnectorListener) {
			pClientConnectorListener.onTerminated(ClientConnector.this);
		}
	};

	// ===========================================================
	// Constructors
	// ===========================================================

	public ClientConnector(final C pConnection) throws IOException {
		this(pConnection, new ClientMessageReader<C>(), new ServerMessagePool());
	}

	public ClientConnector(final C pConnection, final IClientMessageReader<C> pClientMessageReader) throws IOException {
		this(pConnection, pClientMessageReader, new ServerMessagePool());
	}

	public ClientConnector(final C pConnection, final MessagePool<IServerMessage> pServerMessagePool) throws IOException {
		this(pConnection, new ClientMessageReader<C>(), pServerMessagePool);
	}

	public ClientConnector(final C pConnection, final IClientMessageReader<C> pClientMessageReader, final MessagePool<IServerMessage> pServerMessagePool) throws IOException {
		super(pConnection);

		this.mClientMessageReader = pClientMessageReader;
		this.mServerMessagePool = pServerMessagePool;
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public IClientMessageReader<C> getClientMessageReader() {
		return this.mClientMessageReader;
	}

	public MessagePool<IServerMessage> getServerMessagePool() {
		return this.mServerMessagePool;
	}

	public IServerMessage obtainServerMessagePool(final short pFlag) {
		return this.mServerMessagePool.obtainMessage(pFlag);
	}

	@SuppressWarnings("unchecked")
	@Override
	public SmartList<IClientConnectorListener<C>> getConnectorListeners() {
		return (SmartList<IClientConnectorListener<C>>) super.getConnectorListeners();
	}

	public void addClientConnectorListener(final IClientConnectorListener<C> pClientConnectorListener) {
		super.addConnectorListener(pClientConnectorListener);
	}

	public void removeClientConnectorListener(final IClientConnectorListener<C> pClientConnectorListener) {
		super.removeConnectorListener(pClientConnectorListener);
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
		final IClientMessage clientMessage = this.mClientMessageReader.readMessage(pDataInputStream);
		this.mClientMessageReader.handleMessage(this, clientMessage);
		this.mClientMessageReader.recycleMessage(clientMessage);
	}

	@Override
	public void write(final DataOutputStream pDataOutputStream) throws IOException, InterruptedException {
		final IServerMessage serverMessage = this.mServerMessageQueue.take();
		serverMessage.write(pDataOutputStream);
		pDataOutputStream.flush();
		this.mServerMessagePool.recycleMessage(serverMessage);
	}

	// ===========================================================
	// Methods
	// ===========================================================

	public void registerClientMessage(final short pFlag, final Class<? extends IClientMessage> pClientMessageClass) {
		this.mClientMessageReader.registerMessage(pFlag, pClientMessageClass);
	}

	public void registerClientMessage(final short pFlag, final Class<? extends IClientMessage> pClientMessageClass, final IClientMessageHandler<C> pClientMessageHandler) {
		this.mClientMessageReader.registerMessage(pFlag, pClientMessageClass, pClientMessageHandler);
	}

	public void registerClientMessageHandler(final short pFlag, final IClientMessageHandler<C> pClientMessageHandler) {
		this.mClientMessageReader.registerMessageHandler(pFlag, pClientMessageHandler);
	}

	public synchronized void sendServerMessage(final IServerMessage pServerMessage) {
		this.mServerMessageQueue.add(pServerMessage);
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

	public static interface IClientConnectorListener<T extends Connection> extends IConnectorListener<ClientConnector<T>> {
		// ===========================================================
		// Final Fields
		// ===========================================================

		// ===========================================================
		// Methods
		// ===========================================================

		@Override
		public void onStarted(final ClientConnector<T> pClientConnector);

		@Override
		public void onTerminated(final ClientConnector<T> pClientConnector);
	}
}
