package org.example;


import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.example.model.Conversao;
import org.example.model.Moeda;
import org.example.model.Response;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Main {
    static final List<Moeda> moedas = addMoedas();
    static final String URL = "https://v6.exchangerate-api.com/v6/c60349905a50f7524358b782/pair/";
    static List<Conversao> historicoConversoes = new ArrayList<>();
    static HttpClient client = HttpClient.newHttpClient();
    static Scanner scanner = new Scanner(System.in);
    static  DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");


    static Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();


    public static void main(String[] args) {

        while (true) {
            menuPricipal();
            String escolha;
            try {
                escolha = scanner.nextLine();
                int escolhaConvertida = Integer.parseInt(escolha.trim());
                if (escolhaConvertida == 1) {
                    fazerConversao();
                }
                else if (escolhaConvertida == 2) {
                    mostrarHistoricoConversoes();
                }
                else if (escolhaConvertida == 3) {
                    System.out.println("Saindo...");
                    scanner.close();
                    break;
                } else {
                    System.out.println("Escolha inválida. Tente novamente.");
                }
            } catch (Exception e) {
                System.out.println("Escolha inválida. Tente novamente.");
            }
        }


    }

    private static void menuPricipal() {
        System.out.println("\n*********************************************** ");
        System.out.println("Seja bem-vindo(a) ao conversor de moedas $!\n");
        System.out.println("1) Iniciar conversão.");
        System.out.println("2) Historico.");
        System.out.println("3) Sair.");
        System.out.println("Escolha uma opção válida:");
    }

    private static void fazerConversao() throws URISyntaxException, IOException, InterruptedException {
        System.out.println("*********************************************** ");
        System.out.println("Escolha a moeda de origem: \n");
        Moeda moedaOrigem = escolherMoedaOrigem();

        System.out.println("*********************************************** ");
        System.out.println("Escolha a moeda de destino: \n");
        Moeda moedaDestino = escolherMoedaDestino(moedaOrigem);

        System.out.println("*********************************************** ");
        System.out.println("Selecione o valor da conversão:");
        double valor = escolherValorConversao(scanner);

        System.out.println("*********************************************** ");
        processaConversao(moedaOrigem, moedaDestino, valor);

    }

    private static void mostrarHistoricoConversoes() {
        System.out.println("*********************************************** ");
        if (historicoConversoes.isEmpty()) {
            System.out.println("Nenhuma conversão realizada.");
        }
        else {
            System.out.println("Lista de conversões:\n");
            historicoConversoes.forEach(conversao -> System.out.println(conversao.valor() + " em "
                    + conversao.moedaUm() + " equivalia a " + conversao.valorConvertido() + " em "
                    + conversao.moedaDois() + " no dia " + conversao.data() + ";"));
        }
    }

    private static Moeda escolherMoedaOrigem() {
        imprimirMoedas();
        String escolha;
        Moeda moedaEscolhida;
        while (true){
            try {
                escolha = scanner.nextLine();
                int escolhaConvertida = Integer.parseInt(escolha.trim());
                moedaEscolhida = moedas.get(escolhaConvertida - 1);
                System.out.println("*********************************************** ");
                System.out.println("Moeda de origem escolhida: " + moedaEscolhida.nome());
                break;
            }
            catch (Exception e){
                System.out.println("Escolha inválida! Tente novamente.");
            }
        }
        return moedaEscolhida;
    }

    private static Moeda escolherMoedaDestino(Moeda moedaOrigem) {
        imprimirMoedas();
        System.out.println("*********************************************** ");
        String escolha;
        Moeda moedaEscolhida;
        while (true){
            try {
                escolha = scanner.nextLine();
                int escolhaConvertida = Integer.parseInt(escolha.trim());
                moedaEscolhida = moedas.get(escolhaConvertida - 1);
                if (moedaEscolhida.equals(moedaOrigem)){
                    System.out.println("Moeda já escolhida! Selecione uma moeda diferente.");
                }
                else {
                    System.out.println("*********************************************** ");
                    System.out.println("Moeda de destino escolhida: " + moedaEscolhida.nome());
                    break;
                }
            }
            catch (Exception e){
                System.out.println("Escolha inválida! Tente novamente.");
            }
        }
        return moedaEscolhida;
    }

    private static double escolherValorConversao(Scanner scanner) {
        String valor;
        double valorConvertido;
        while (true) {
            try {
                valor = scanner.nextLine().replace(",", ".");
                valorConvertido = Double.parseDouble(valor.trim());
                if (valorConvertido <= 0) {
                    System.out.println("Valor inválido! Tente novamente (Valores maiores que 0).");
                }
                else {
                    break;
                }
            }
            catch (Exception e) {
                System.out.println("Valor inválido! Tente novamente (Valores maiores que 0).");
            }
        }
        return valorConvertido;
    }

    private static void processaConversao(Moeda moedaOrigem, Moeda moedaDestino, double valor) throws URISyntaxException, IOException, InterruptedException {
        String path = moedaOrigem.codigo()+ "/" + moedaDestino.codigo() + "/" + valor;
        HttpRequest request = HttpRequest.newBuilder().uri(new URI(URL + path)).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            Response resposta = gson.fromJson(response.body(), Response.class);

            String valorFormatado = String.format("%.2f", valor);

            String respostaFormatada = String.format("%.2f", resposta.conversionResult());

            LocalDateTime data = LocalDateTime.now();
            String agora = data.format(formatter);

            System.out.println("\n" + agora + ": " + valorFormatado + " em " + moedaOrigem.nome()
                    + " equivale a " + respostaFormatada
                    + " em " + moedaDestino.nome());

            Conversao conversao = new Conversao(valorFormatado, moedaOrigem.nome(), moedaDestino.nome(), respostaFormatada, agora);

            historicoConversoes.add(conversao);
        }
        else  {
            System.out.println("Ocorreu um erro ao processar conversão. Tente novamente.");
        }
    }

    private static void imprimirMoedas() {
        for (int i = 0; i < moedas.size(); i++) {
            System.out.println(i + 1 + ") " + moedas.get(i).nome() + ".");
        }
    }

    private static List<Moeda> addMoedas() {
        List<Moeda> colecaoMoedas = new ArrayList<>();
        colecaoMoedas.add(new Moeda("Real Brasileiro", "BRL"));
        colecaoMoedas.add(new Moeda("Dólar Americano", "USD"));
        colecaoMoedas.add(new Moeda("Euro", "EUR"));
        colecaoMoedas.add(new Moeda("Libra Esterlina", "GBP"));
        colecaoMoedas.add(new Moeda("Dólar Canadense", "CAD"));
        colecaoMoedas.add(new Moeda("Peso Argentino", "ARS"));
        colecaoMoedas.add(new Moeda("Peso Colombiano", "COP"));
        colecaoMoedas.add(new Moeda("Iene Japonês", "JPY"));
        colecaoMoedas.add(new Moeda("Yuan Chinês", "CNY"));
        return colecaoMoedas;
    }

}