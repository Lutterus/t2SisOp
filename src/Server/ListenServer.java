package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import Channel.ChannelList;
import Channel.ReservedWords;
import Channel.Users;

public class ListenServer implements Runnable {
	private Socket connectionSocket;
	private BufferedReader inFromClient;
	private String clientSentence = "";
	private ReservedWords palavrasReservadas;
	private ChannelList channels;
	private boolean pending = true;
	private Users user;

	public ListenServer(Socket connectionSocket, ReservedWords palavrasReservadas, ChannelList channels, Users user) {
		this.connectionSocket = connectionSocket;
		inFromClient = createStreamIn();
		this.palavrasReservadas = palavrasReservadas;
		this.channels = channels;
		if (user == null) {
			this.user = createUser();
		} else {
			this.user = user;
		}

	}

	private Users createUser() {
		Users newUSer = new Users(null, connectionSocket, null);
		return newUSer;
	}

	public BufferedReader createStreamIn() {
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
		while (pending == true) {
			try {
				clientSentence = inFromClient.readLine();
			} catch (IOException e) {
				System.err.println("Erro ao ler a mensagem enviado pelo cliente");
				pending = false;
				e.printStackTrace();
				cleanUserChannels();
			}
			if (palavrasReservadas.isReserved(clientSentence)) {
				clientSentence = executeCommand(clientSentence, connectionSocket);
				clientSentence = clientSentence + "\n";
				Thread replier = new Thread(new ReplyServer(connectionSocket, clientSentence));
				replier.start();
			} else {
				if (clientSentence.contentEquals("/quit")) {
					pending = false;
					cleanUserChannels();
				} else {
					clientSentence = standardMsg(clientSentence);
				}
				clientSentence = clientSentence + "\n";

				Thread replier = new Thread(new ReplyServer(connectionSocket, clientSentence));
				replier.start();

			}
		}

	}

	private String executeCommand(String clientSentence, Socket connectionSocket) {
		if (clientSentence.startsWith("/nick ") && wordcount(clientSentence) == 2) {
			clientSentence = commandNick();
			return clientSentence;

		} else if (clientSentence.startsWith("/join ") && wordcount(clientSentence) == 2) {
			clientSentence = commandJoin();
			return clientSentence;
		} else if (clientSentence.startsWith("/create") && wordcount(clientSentence) == 2) {
			clientSentence = commandCreate();
			return clientSentence;
		} else if (clientSentence.contentEquals("/list")) {
			clientSentence = commandList();
			return clientSentence;
		} else if (clientSentence.startsWith("/remove") && wordcount(clientSentence) == 2) {
			clientSentence = commandRemove();
			return clientSentence;
		}

		return "Comando usado de forma incorreta";

	}

	private String commandNick() {
		clientSentence = clientSentence.replace("/nick ", "");
		if (clientSentence.contentEquals("'anonymous'")) {
			return "Nao e possivel alterar o nome de usuario para anonymous, visto que esta e uma palavra reservada";
		} else if (channels.isNameFree(clientSentence) == false) {
			return "Este nome esta sendo usado por outro usuario";
		} else {
			user.setName(clientSentence);
			return "NICK alterado com sucesso para " + clientSentence;
		}
	}

	private String commandRemove() {
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

	private String commandList() {
		String answer = channels.toString();
		return answer;
	}

	private String commandCreate() {
		clientSentence = clientSentence.replace("/create ", "");
		if (channels.channelExist(clientSentence)) {
			String answer = "Ja existe um canal com o nome '" + clientSentence + "'";
			return answer;
		} else {
			if(user.getName()==null) {
				String answer = "Para criar um canal, primeiro voce devera usar o comando '/nick' e criar um nome para que possamos reconhece-lo";
				return answer;
			}else {
				channels.addChannel(clientSentence, connectionSocket.getInetAddress());
				String answer = "Criado um canal com o nome '" + clientSentence + "', para o acessar digite '/join "
						+ clientSentence + "'";
				return answer;
			}
			
		}
	}

	private String commandJoin() {
		clientSentence = clientSentence.replace("/join ", "");
		if (channels.channelExist(clientSentence)) {
			channels.transferUser(clientSentence, connectionSocket, user);
			String answer = "Conectado ao canal '" + clientSentence + "'";
			pending = false;
			return answer;
		} else {
			String answer = "O canal '" + clientSentence + "' nao existe";
			return answer;
		}
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
	
	private void cleanUserChannels() {
		channels.cleanChannels(user);		
	}
}
