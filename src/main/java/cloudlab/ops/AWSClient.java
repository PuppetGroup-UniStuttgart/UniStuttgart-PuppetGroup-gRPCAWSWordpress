package cloudlab.ops;

import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import cloudlab.EC2OpsProto.DestroyReply;
import cloudlab.EC2OpsProto.DestroyRequest;
import cloudlab.EC2OpsProto.EC2OpsGrpc;
import cloudlab.EC2OpsProto.Reply;
import cloudlab.EC2OpsProto.Request;
import cloudlab.WordPressOpsProto.ConnectReply;
import cloudlab.WordPressOpsProto.ConnectRequest;
import cloudlab.WordPressOpsProto.DeployAppReply;
import cloudlab.WordPressOpsProto.DeployAppRequest;
import cloudlab.WordPressOpsProto.DeployDBReply;
import cloudlab.WordPressOpsProto.DeployDBRequest;
import cloudlab.WordPressOpsProto.WordPressOpsGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

/**
 * AWSClient: Provides an user interface to access the EC2Ops & WordPressOps
 * service by communicating with AWSServer A menu that populates the request
 * message with required details to create & destroy a AWS EC2 instance. Another
 * menu which populates the request message to deploy WordPress in the provided
 * EC2 instance
 */

public class AWSClient {
	private static final Logger logger = Logger.getLogger(AWSClient.class.getName());

	private final ManagedChannel channel;
	private final EC2OpsGrpc.EC2OpsBlockingStub blockingStub;
	private final WordPressOpsGrpc.WordPressOpsBlockingStub wblockingStub;
	Scanner in = new Scanner(System.in);
	boolean quit, appdone, dbdone = quit = appdone = false;

	public AWSClient(String host, int port) {
		channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext(true).build();
		blockingStub = EC2OpsGrpc.newBlockingStub(channel);
		wblockingStub = WordPressOpsGrpc.newBlockingStub(channel);
	}

	public void shutdown() throws InterruptedException {
		channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
	}

	private void serviceMenu() {
		int serviceItem;

		do {
			System.out.print("Choose service: ");
			System.out.println("1. EC2 Ops");
			System.out.println("2. Wordpress Ops");
			System.out.println("3. Exit");
			System.out.println("Enter Choice: ");
			serviceItem = in.nextInt();

			switch (serviceItem) {
			case 1:
				oneMenu();
				break;
			case 2:
				twoMenu();
				break;
			case 3:
				quit = true;
				in.close();
				break;
			default:
				System.out.println("Invalid choice.");
			}
		} while (!quit);
		System.out.println("Bye-bye!");
	}

	private void oneMenu() {
		int menuItem;
		boolean quit1 = false;
		String region, os, machineSize, keyPair, bucketName,
				instanceID = region = os = machineSize = keyPair = bucketName = null;

		do {
			System.out.print("Choose menu item: ");
			System.out.println("1. Create VM");
			System.out.println("2. Destroy VM");
			System.out.println("3. Go Back");
			System.out.println("4. Quit");
			System.out.println("Enter Choice: ");
			menuItem = in.nextInt();
			switch (menuItem) {
			case 1:
				System.out.println("Enter the region: ");
				region = in.next();
				System.out.println("Enter the OS (machine ID): ");
				os = in.next();
				System.out.println("Enter the machine size: ");
				machineSize = in.next();
				System.out.println("Enter the name of the key pair file: ");
				keyPair = in.next();
				System.out.println("Enter the name of S3 bucket (holds the pem file): ");
				bucketName = in.next();
				create(region, os, machineSize, keyPair, bucketName);
				break;
			case 2:
				System.out.println("Enter the instance ID: ");
				instanceID = in.next();
				System.out.println("Enter the region: ");
				region = in.next();
				destroy(instanceID, region);
				break;
			case 3:
				serviceMenu();
				quit1 = true;
				break;
			case 4:
				in.close();
				quit = true;
				quit1 = true;
				break;
			default:
				System.out.println("Invalid choice.");
			}
		} while (!quit1);

	}

	private void twoMenu() {
		int menuItem;
		boolean quit2 = false;
		String credentials, username, bucketName, publicIP = username = credentials = bucketName = null;

		do {
			System.out.print("Choose menu item: ");
			System.out.println("1. Deploy App");
			System.out.println("2. Deploy DB");
			System.out.println("3. Connect App to DB");
			System.out.println("4. Backup DB");
			System.out.println("5. Restore DB");
			System.out.println("6. Go Back");
			System.out.println("7. Quit");
			System.out.println("Enter Choice: ");
			menuItem = in.nextInt();

			switch (menuItem) {
			case 1:
				System.out.println("Enter the name of the key pair file: ");
				credentials = in.next();
				System.out.println("Enter the name of S3 bucket (that contains the pem file): ");
				bucketName = in.next();
				System.out.println("Enter the username: ");
				username = in.next();
				System.out.println("Enter the public IP of instance: ");
				publicIP = in.next();
				deployApp(credentials, bucketName, username, publicIP);
				break;
			case 2:
				if (appdone) {
					System.out.println("Enter the name of the key pair file: ");
					credentials = in.next();
					System.out.println("Enter the name of S3 bucket (that contains the pem file): ");
					bucketName = in.next();
					System.out.println("Enter the username: ");
					username = in.next();
					System.out.println("Enter the public IP of instance: ");
					publicIP = in.next();
					deployDB(credentials, bucketName, username, publicIP);
				} else
					System.out.println("Deploy App first ");
				break;
			case 3:
				if (dbdone) {
					System.out.println("Enter the name of the key pair file: ");
					credentials = in.next();
					System.out.println("Enter the name of S3 bucket (that contains the pem file): ");
					bucketName = in.next();
					System.out.println("Enter the username: ");
					username = in.next();
					System.out.println("Enter the public IP of instance: ");
					publicIP = in.next();
					connect(credentials, bucketName, username, publicIP);
				} else
					System.out.println("Deploy DB first ");
				break;
			case 4:
				break;
			case 5:
				break;
			case 6:
				serviceMenu();
				quit2 = true;
				break;
			case 7:
				in.close();                                                                                                                                       
				quit = true;
				quit2 = true;
				break;
			default:
				System.out.println("Invalid choice.");
			}
		} while (!quit2);
	}

	private void create(String region, String OS, String machineSize, String keyPair, String bucketName) {
		Request request = Request.newBuilder().setRegion(region).setOS(OS).setMachineSize(machineSize).setKeyPair(keyPair).setBucketName(bucketName).build();
		Reply reply;
		try {
			reply = blockingStub.createVM(request);
		} catch (StatusRuntimeException e) {
			logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
			return;
		}
		logger.info("Instance ID: " + reply.getInstanceID());
		logger.info("Public IP: " + reply.getPublicIP());
	}

	private void destroy(String instanceID, String region) {
		DestroyRequest request = DestroyRequest.newBuilder().setInstanceID(instanceID).setRegion(region).build();
		DestroyReply reply;
		try {
			reply = blockingStub.destroyVM(request);
		} catch (StatusRuntimeException e) {
			logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
			return;
		}
		logger.info(instanceID + " status is: " + reply.getStatus());

	}

	private void deployApp(String credentials, String bucketName, String username, String publicIP) {
		DeployAppRequest request = DeployAppRequest.newBuilder().setCredentials(credentials).setBucketName(bucketName).setUsername(username)
				.setPublicIP(publicIP).build();
		DeployAppReply reply;
		try {
			reply = wblockingStub.deployApp(request);
		} catch (StatusRuntimeException e) {
			logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
			return;
		}
		logger.info("Output: " + reply.getOutput());
		appdone = true;
	}

	private void deployDB(String credentials, String bucketName, String username, String publicIP) {
		DeployDBRequest request = DeployDBRequest.newBuilder().setCredentials(credentials).setBucketName(bucketName).setUsername(username)
				.setPublicIP(publicIP).build();
		DeployDBReply reply;
		try {
			reply = wblockingStub.deployDB(request);
		} catch (StatusRuntimeException e) {
			logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
			return;
		}
		logger.info("Output: " + reply.getOutput());
		dbdone = true;
	}

	private void connect(String credentials, String bucketName, String username, String publicIP) {
		ConnectRequest request = ConnectRequest.newBuilder().setCredentials(credentials).setBucketName(bucketName).setUsername(username)
				.setPublicIP(publicIP).build();
		ConnectReply reply;
		try {
			reply = wblockingStub.connectAppToDB(request);
		} catch (StatusRuntimeException e) {
			logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
			return;
		}
		logger.info("Output: " + reply.getOutput());
	}

	public static void main(String[] args) throws Exception {
		AWSClient client = new AWSClient("localhost", 50051);

		try {
			System.out.println("MENU");
			client.serviceMenu();
		} finally {
			client.shutdown();
		}
	}
}
