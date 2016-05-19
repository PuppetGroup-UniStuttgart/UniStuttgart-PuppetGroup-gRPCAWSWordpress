package cloudlab.ops;

import java.util.logging.Logger;

import cloudlab.EC2OpsProto.EC2OpsGrpc;
import cloudlab.WordPressOpsProto.WordPressOpsGrpc;
import io.grpc.Server;
import io.grpc.ServerBuilder;
/**
 * AWSServer:
 * The service EC2Ops & WordPressOps is bound to this server's port
 */

public class AWSServer {
	private static final Logger logger = Logger.getLogger(AWSServer.class.getName());

	private int port = 50051;
	private Server server;

	private void start() throws Exception {
		server = ServerBuilder.forPort(port).addService(EC2OpsGrpc.bindService(new EC2OpsImpl())).addService(WordPressOpsGrpc.bindService(new WordPressOpsImpl())).build().start();
		logger.info("Server started, listening on " + port);
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				System.err.println("*** shutting down gRPC server since JVM is shutting down");
				AWSServer.this.stop();
				System.err.println("*** server shut down");
			}
		});
	}

	private void stop() {
		if (server != null) {
			server.shutdown();
		}
	}

/*	 Await termination on the main thread since the grpc library uses daemon
	 threads.*/

	private void blockUntilShutdown() throws InterruptedException {
		if (server != null) {
			server.awaitTermination();
		}
	}

	public static void main(String[] args) throws Exception {
		final AWSServer server = new AWSServer();
		server.start();
		server.blockUntilShutdown();
	}
}
