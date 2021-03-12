package com.adrianonna.pdist.nfssocket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Servidor {

    public static void main(String[] args) throws IOException {
        System.out.println("== Servidor ==");

        // Configurando o socket
        ServerSocket serverSocket = new ServerSocket(6000);
        Socket socket = serverSocket.accept();

        // pegando uma referência do canal de saída do socket. Ao escrever nesse canal, está se enviando dados para o
        // servidor
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
        // pegando uma referência do canal de entrada do socket. Ao ler deste canal, está se recebendo os dados
        // enviados pelo servidor
        DataInputStream dis = new DataInputStream(socket.getInputStream());

        // laço infinito do servidor
        while (true) {
            System.out.println("Cliente: " + socket.getInetAddress());

            String mensagem = dis.readUTF();
            System.out.println(mensagem);

            String[] arrMensagem = mensagem.split(" ");
            String commando = arrMensagem[0];
            String diretorio = arrMensagem[1];
            String nomeArquivo1 = "";
            String nomeArquivo2 = "";

            if(arrMensagem.length > 2) {
                nomeArquivo1 = arrMensagem[2];
            }

            if(arrMensagem.length > 3){
                nomeArquivo2 = arrMensagem[3];
            }


            if(commando.contains("readdir")){
                List<String> arquivosPasta;
                arquivosPasta = getArquivosDoDiretorio(diretorio);
                dos.writeUTF("READDIR: " + arquivosPasta);
            }else if(commando.contains("rename")){
                String result = rename(diretorio, nomeArquivo1, nomeArquivo2);
                dos.writeUTF("RENAME: " + result);
            }else if(commando.contains("create")) {
                String result = create(diretorio, nomeArquivo1);
                dos.writeUTF("CREATE: " + result);
            }else if(commando.contains("remove")) {
                remove(diretorio, nomeArquivo1);
                dos.writeUTF("REMOVE: OK");
            }
        }
        /*
         * Observe o while acima. Perceba que primeiro se lê a mensagem vinda do cliente (linha 29, depois se escreve
         * (linha 32) no canal de saída do socket. Isso ocorre da forma inversa do que ocorre no while do Cliente2,
         * pois, de outra forma, daria deadlock (se ambos quiserem ler da entrada ao mesmo tempo, por exemplo,
         * ninguém evoluiria, já que todos estariam aguardando.
         */
    }

    public static List<String> getArquivosDoDiretorio(String diretorio) {
        List<String> arquivosPasta = new ArrayList<String>();

        try(Stream<Path> paths = Files.walk(Paths.get(diretorio))){
            paths.filter(Files::isRegularFile).forEach(x -> arquivosPasta.add(x.toString()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return arquivosPasta;
    }

    public static String create(String diretorio, String fileName) throws IOException {
        Path p = Paths.get(diretorio + "/" + fileName);
        Files.createFile(p);
        return "Operação OK";
    }

    public static String rename(String diretorio, String fileName, String newFileName) throws IOException {
        Path p = Paths.get(diretorio + "/" + fileName);
        if(Files.exists(p)){
            Files.move(p, p.resolveSibling(newFileName));
            return "Sucesso!";
        }else {
            return "Falha -> Arquivo não existente!";
        }
    }

    public static String remove(String diretorio, String fileName) throws IOException {
        Path p = Paths.get(diretorio + "/" + fileName);
        if(Files.exists(p)){
            Files.delete(p);
            return "Removido com sucesso";
        } else {
            return "Arquivo não existente!";
        }
    }
}
