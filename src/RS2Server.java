import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

public class RS2Server {

	public void start(int port) {

		RS2ISOMessage rs2Message = new RS2ISOMessage();
		try (ServerSocket serverSocket = new ServerSocket(port);

				) {

			while (true) {
				Socket socket = serverSocket.accept();
				// PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
				DataOutputStream out = new DataOutputStream(socket.getOutputStream());
				// BufferedReader in = new BufferedReader(new
				// InputStreamReader(socket.getInputStream()));
				// String clientMsg = in.readLine();

				// Reading data received from clients
				DataInputStream in = new DataInputStream(socket.getInputStream());
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				byte[] buffer = new byte[4096];
				baos.write(buffer, 0, in.read(buffer));

				byte[] result = baos.toByteArray();
				byte[] msgFrame = new byte[2];
				byte[] data = new byte[result.length - 2];
				System.arraycopy(result, 0, msgFrame, 0, 2);
				System.arraycopy(result, 2, data, 0, data.length);
				String arr = Arrays.toString(msgFrame);
				String res = new String(data, StandardCharsets.ISO_8859_1);
				System.out.println("Message Frame: " + arr);
				System.out.println("Server::Received client request: " + res);

				// Sending response to clients

				Map<String, String> isoBuffer = rs2Message.unpack(res);
				isoBuffer.put("MSG-TYP", "0110");
				isoBuffer.put("F-38", "S20296");
				isoBuffer.put("F-39", "00");
				rs2Message.msgMap.putAll(isoBuffer);
				byte[] response = rs2Message.pack();
				byte[] msgResFrame = new byte[2];
				byte[] responseData = new byte[response.length - 2];
				System.arraycopy(response, 0, msgResFrame, 0, 2);
				System.arraycopy(response, 2, responseData, 0, responseData.length);
				// String arr = Arrays.toString(msgResFrame);
				String isoMsg = new String(responseData, StandardCharsets.ISO_8859_1);

				out.write(isoMsg.getBytes(StandardCharsets.ISO_8859_1));
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new RS2Server().start(8376);
	}

}
