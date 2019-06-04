package Server;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ReplyServer implements Runnable {
	private Socket connectionSocket;
	private DataOutputStream outToClient;
	private String echo;

	public ReplyServer(Socket connectionSocket, String echo) {
		this.connectionSocket = connectionSocket;
		outToClient = crateConnectionOut();
		this.echo ="FROM SERVER: " + echo;
	}

	public DataOutputStream crateConnectionOut() {
		/* Cria uma stream de saída para enviar dados para o cliente */
		try {
			outToClient = new DataOutputStream(connectionSocket.getOutputStream());
			return outToClient;
		} catch (IOException e) {
			System.err.println("Erro durante criação da stream de saida de dados");
			e.printStackTrace();
		}
		return outToClient;
	}

	@Override
	public void run() {
		try {
			outToClient.writeBytes(echo);
		} catch (IOException e) {
			System.err.println("Erro na thread de envio, ao enviar a mensagem para o cliente");
			e.printStackTrace();
		}
	}

}
