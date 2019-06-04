package User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class Listen implements Runnable {
	private Socket socket;
	private BufferedReader inFromServer;
	private String echo;
	private String echo1;
	private String echo2;

	public Listen(Socket clientSocket) {
		this.socket = clientSocket;
		inFromServer = createConnectionIn();
	}

	public BufferedReader createConnectionIn() {
		try {
			BufferedReader inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			return inFromServer;
		} catch (IOException e) {
			System.err.println("Erro durante criacao da stream de receber dados");
			e.printStackTrace();
		}
		return inFromServer;
	}

	@Override
	public void run() {
		echo = ";";
		while (echo.contains("/quit") == false) {
			/* recebe uma mensagem digitada pelo usuario para o servidor */
			try {
				echo = inFromServer.readLine();
				String[] str_array = echo.split(":");
				echo1 = str_array[0];
				echo2 = str_array[1];
			} catch (IOException e) {
				System.err.println("Erro durante leitura da mensagem resposta do servidor");
				e.printStackTrace();
				echo = "/quit";
			}
			System.out.println(echo1 + ": " + echo2);
		}
	}
}
