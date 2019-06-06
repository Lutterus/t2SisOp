package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import Channel.ChannelList;
import Channel.ReservedWords;

public class Server {
	public static void main(String[] args) {
		/* Cria socket do servidor */
		ServerSocket welcomeSocket = createServerSocket();

		/* Cria lista de palavras reservadas */
		ReservedWords palavrasReservadas = new ReservedWords();
		createReservedWords(palavrasReservadas);

		/* Cria lista de canais padroes */
		ChannelList channels = new ChannelList(palavrasReservadas);
		/* Cria uma referencia de si mesmo dentro de canais */
		channels.setSelfReference(channels);
		createStandardChannels(channels);

		while (true) {
			/* Aguarda o recebimento de uma conexao */
			Socket connectionSocket = createSocket(welcomeSocket);

			/* Cria uma stream de entrada (e saida) para receber os dados do cliente */
			Thread listener = new Thread(new ListenServer(connectionSocket, palavrasReservadas, channels, null));

			/* Aguarda o envio de uma mensagem (e envia para o cliente) */
			listener.start();
		}
	}

	private static void createStandardChannels(ChannelList channels) {
		channels.addChannelDefault("#pucrs");
		channels.addChannelDefault("#facin");
		channels.addChannelDefault("#portoalegre");
		channels.addChannelDefault("#linux");
	}

	private static Socket createSocket(ServerSocket welcomeSocket) {
		try {
			Socket connectionSocket = welcomeSocket.accept();
			return connectionSocket;
		} catch (IOException e) {
			System.err.println("Erro durante a tentativa de aceitar uma conexao");
			e.printStackTrace();
		}
		return null;
	}

	private static ServerSocket createServerSocket() {
		try {
			ServerSocket welcomeSocket = new ServerSocket(6790);
			return welcomeSocket;
		} catch (IOException e) {
			System.err.println("Erro durante a tentativa de aceitar um socket");
			e.printStackTrace();
		}
		return null;
	}

	private static void createReservedWords(ReservedWords palavrasReservadas) {
		palavrasReservadas.addWord("/nick");
		palavrasReservadas.addWord("/create");
		palavrasReservadas.addWord("/remove");
		palavrasReservadas.addWord("/list");
		palavrasReservadas.addWord("/join");
		palavrasReservadas.addWord("/part");
		palavrasReservadas.addWord("/names");
		palavrasReservadas.addWord("/kick");
		palavrasReservadas.addWord("/msg");

	}
}
