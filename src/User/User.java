package User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class User {
	public static void main(String[] args) {
		/* Cria o socket cliente indicando o IP e porta de destino. */
		Socket clientSocket = createConectionInfo();

		/* Cria uma stream de entrada para receber os dados do servidor */
		Thread listener = new Thread(new Listen(clientSocket));

		/* Cria uma stream de saida para enviar dados para o servidor */
		Thread replier = new Thread(new Reply(clientSocket));

		/* Lê mensagem de resposta do servidor */
		initiateThread(listener);

		/* Envia a mensagem para o servidor. */
		initiateThread(replier);

		/* Encerra as Threads */
		terminateThread(replier);
		terminateThread(listener);

		/* Encerra conexao */
		terminate(clientSocket);

	}

	private static void terminate(Socket clientSocket) {
		try {
			clientSocket.close();
		} catch (IOException e) {
			System.err.println("Erro durante encerramento da conexão com o servidor");
			e.printStackTrace();
		}

	}

	private static void terminateThread(Thread thread) {
		try {
			thread.join();
		} catch (InterruptedException e) {
			System.err.println("nao foi possivel obter resposta da thread");
			e.printStackTrace();
		}
	}

	private static void initiateThread(Thread thread) {
		thread.start();
	}

	private static Socket createConectionInfo() {
		try {
			Socket clientSocket = new Socket("127.0.0.1", 6790);
			return clientSocket;
		} catch (IOException e) {
			System.err.println("Erro durante criacao do socket");
			e.printStackTrace();
		}

		return null;

	}
}
