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
			Thread listener = new Thread(new ListenServer(connectionSocket, palavrasReservadas, channels));

			/* Aguarda o envio de uma mensagem (e envia para o cliente) */
			listener.start();
			/*try {
				listener.join();
			} catch (InterruptedException e) {
				System.err.println("Erro ao esperar pela thread listener");
				e.printStackTrace();
			}

			 Encerra socket do cliente 
			try {
				System.out.println("fechou conexao");
				connectionSocket.close();
			} catch (IOException e) {
				System.err.println("Erro ao tentar encerrar a conexÃ£o do servidor");
				e.printStackTrace();
			}*/
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

	}
}
