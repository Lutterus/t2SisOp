package User;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class Reply implements Runnable {
	private Socket socket;
	private DataOutputStream outToServer;
	private String sentence;
	private BufferedReader inFromUser;

	public Reply(Socket clientSocket) {
		this.socket = clientSocket;
		outToServer = crateConnectionOut();
		inFromUser = new BufferedReader(new InputStreamReader(System.in));
	}

	public DataOutputStream crateConnectionOut() {
		try {
			DataOutputStream outToServer = new DataOutputStream(socket.getOutputStream());
			return outToServer;
		} catch (IOException e) {
			System.err.println("Erro durante criacao da stream de enviar dados");
			e.printStackTrace();
		}
		return outToServer;
	}

	@Override
	public void run() {
		System.out.print("Conectado");
		sentence = ";";
		while (sentence.contentEquals("/quit") == false) {
			/* Lê uma mensagem digitada pelo usuário */
			try {
				sentence = inFromUser.readLine();
			} catch (IOException e) {
				System.err.println("Erro durante leitura da mensagem digitada");
				// e.printStackTrace();
			}

			/* envia uma mensagem digitada pelo usuário para o servidor */
			try {
				outToServer.writeBytes(sentence + '\n');
			} catch (IOException e) {
				System.err.println("Erro durante envio da mensagem ao servidor");
				e.printStackTrace();
			}
		}

	}

}
