package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;

import Channel.ChannelList;
import Channel.ReservedWords;

public class ListenServer implements Runnable {
	private Socket connectionSocket;
	private BufferedReader inFromClient;
	private String clientSentence;
	private String echo;
	private ReservedWords palavrasReservadas;
	private ChannelList channels;
	private boolean pending = true;

	public ListenServer(Socket connectionSocket, ReservedWords palavrasReservadas, ChannelList channels) {
		this.connectionSocket = connectionSocket;
		inFromClient = createStreamIn();
		this.palavrasReservadas = palavrasReservadas;
		this.channels = channels;
	}

	public BufferedReader createStreamIn() {
		/* Cria uma stream de entrada para receber os dados do cliente */

		try {
			inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
			return inFromClient;
		} catch (IOException e) {
			System.err.println("Erro durante criação da stream de entrada de dados");
			e.printStackTrace();
		}
		return inFromClient;
	}

	@Override
	public void run() {
		clientSentence = ";";
		while (pending == true) {
			/*
			 * Aguarda o envio de uma mensagem do cliente. Esta mensagem deve ser terminada
			 * em \n ou \r Neste exemplo espera-se que a mensagem seja textual (string).
			 * Para ler dados não textuais tente a chamada read()
			 */
			/* Determina o IP e Porta de origem */
			InetAddress IPAddress = connectionSocket.getInetAddress();
			int port = connectionSocket.getPort();

			try {
				clientSentence = inFromClient.readLine();
			} catch (IOException e) {
				System.err.println("Erro ao ler a mensagem enviado pelo cliente");
				pending = false;
				e.printStackTrace();
			}
			if (palavrasReservadas.isReserved(clientSentence)) {
				clientSentence = executeCommand(clientSentence, connectionSocket);
				/* Exibe, IP:port => msg */
				System.out.println(IPAddress.getHostAddress() + ":" + port + " => " + clientSentence);

				/* Adiciona o \n para que o cliente também possa ler usando readLine() */
				echo = clientSentence + '\n';

				Thread replier = new Thread(new ReplyServer(connectionSocket, echo));
				/* Envia mensagem para o cliente */
				replier.start();
			} else {
				if (clientSentence.contentEquals("/quit")) {
					pending = false;
				} else {
					clientSentence = standardMsg(clientSentence);
				}

				/* Exibe, IP:port => msg */
				System.out.println(IPAddress.getHostAddress() + ":" + port + " => " + clientSentence);

				/* Adiciona o \n para que o cliente também possa ler usando readLine() */
				echo = clientSentence + '\n';

				Thread replier = new Thread(new ReplyServer(connectionSocket, echo));
				/* Envia mensagem para o cliente */
				replier.start();

			}
		}

	}

	private String executeCommand(String clientSentence, Socket connectionSocket) {
		if (clientSentence.startsWith("/join ") && wordcount(clientSentence) == 2) {
			clientSentence = clientSentence.replace("/join ", "");
			if (channels.channelExist(clientSentence)) {
				channels.transferUser(clientSentence, connectionSocket);
				String answer = "Conectado ao canal '" + clientSentence + "'";
				pending = false;
				return answer;
			} else {
				String answer = "O canal '" + clientSentence + "' nao existe";
				return answer;
			}
		} else if (clientSentence.startsWith("/create") && wordcount(clientSentence) == 2) {
			clientSentence = clientSentence.replace("/create ", "");
			if (channels.channelExist(clientSentence)) {
				String answer = "Já existe um canal com o nome '" + clientSentence + "'";
				return answer;
			} else {
				channels.addChannel(clientSentence, connectionSocket.getInetAddress());
				String answer = "Criado um canal com o nome '" + clientSentence + "', para o acessar digite '/join "
						+ clientSentence + "'";
				return answer;
			}
		} else if (clientSentence.contentEquals("/list")) {
			String answer = channels.toString();
			return answer;
		} else if (clientSentence.startsWith("/remove") && wordcount(clientSentence) == 2) {
			clientSentence = clientSentence.replace("/remove ", "");
			if (channels.channelExist(clientSentence)) {
				if (channels.isAdm(clientSentence, connectionSocket.getInetAddress())) {
					String answer = "Canal Removido";
					channels.disconnectAll(clientSentence);
					channels.removeChannel(clientSentence);
					return answer;
				} else {
					String answer = "Nao e possivel executar esta acao, pois voce nao e o administrador deste canal";
					return answer;
				}

			} else {
				String answer = "Nao encontramos um servidor com o nome '" + clientSentence + "'";
				return answer;
			}
		}

		return "Comando usado de forma incorreta";

	}

	static int wordcount(String string) {
		int count = 0;

		char ch[] = new char[string.length()];
		for (int i = 0; i < string.length(); i++) {
			ch[i] = string.charAt(i);
			if (((i > 0) && (ch[i] != ' ') && (ch[i - 1] == ' ')) || ((ch[0] != ' ') && (i == 0)))
				count++;
		}
		return count;
	}

	private String standardMsg(String clientSentence2) {
		clientSentence = "voce nao esta em um canal, portanto os comandos e mensagens estao desabilitados, use "
				+ "'/list' para ver a lista de canais, " + "'/join' para entrar em um canal ou "
				+ "'/create' para criar um canal";
		return clientSentence;
	}
}
