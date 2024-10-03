package br.com.heric.tabelafipe.principal;

import br.com.heric.tabelafipe.model.Dados;
import br.com.heric.tabelafipe.model.Modelos;
import br.com.heric.tabelafipe.model.Veiculo;
import br.com.heric.tabelafipe.service.ConsumoApi;
import br.com.heric.tabelafipe.service.ConverteDados;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Principal {
    private final Scanner leitura = new Scanner(System.in);
    private final ConsumoApi consumo = new ConsumoApi();
    private final ConverteDados conversor = new ConverteDados();
    private final String URL_BASE = "https://parallelum.com.br/fipe/api/v1/";

    public void exibeMenu() {
        String menu = """
               *** OPÇÕES ***
               Carro
               Moto
               Caminhão
               
               Digite uma das opções para consulta: 
               """;
        System.out.println(menu);

        String opcao = leitura.nextLine().toLowerCase();
        String tipoVeiculo = getTipoVeiculo(opcao);
        String endereco = URL_BASE + tipoVeiculo + "/marcas";

        List<Dados> marcas = obterListaDados(endereco, Dados.class);


        //System.out.println("\nAs primeiras DEZ marcas disponíveis: ");
        marcas.stream()
                .sorted(Comparator.comparing(Dados::codigo))
                //.limit(10)
                .forEach(marca -> System.out.println(marca + "\n---"));

        System.out.println("Informe o código da marca para consulta: ");
        String codigoMarca = leitura.nextLine();
        endereco = String.format("%s/%s/modelos", endereco, codigoMarca);

        var modeloLista = conversor.obterDados(consumo.obterDados(endereco), Modelos.class);

        System.out.println("\nModelos dessa marca: ");
        modeloLista.modelos().stream()
                .sorted(Comparator.comparing(Dados::codigo))
                .forEach(System.out::println);

        System.out.println("\nDigite um trecho do nome do veículo a ser buscado: ");
        String nomeVeiculo = leitura.nextLine().toLowerCase();

        List<Dados> modelosFiltrados = modeloLista.modelos().stream()
                .filter(m -> m.nome().toLowerCase().contains(nomeVeiculo))
                .collect(Collectors.toList());

        System.out.println("\nModelos filtrados:");
        modelosFiltrados.forEach(System.out::println);

        System.out.println("Digite o código do modelo para buscar os valores de avaliação: ");
        String codigoModelo = leitura.nextLine();
        endereco = String.format("%s/%s/anos", endereco, codigoModelo);

        List<Dados> anos = obterListaDados(endereco, Dados.class);
        List<Veiculo> veiculos = obterVeiculosPorAno(endereco, anos);

        System.out.println("\nTodos os veículos filtrados com avaliações por ano:");
        veiculos.forEach(System.out::println);
    }

    private String getTipoVeiculo(String opcao) {
        return switch (opcao) {
            case "carro" -> "carros";
            case "moto" -> "motos";
            default -> "caminhoes";
        };
    }

    private <T> List<T> obterListaDados(String url, Class<T> clazz) {
        String json = consumo.obterDados(url);
        return conversor.obterLista(json, clazz);
    }

    private List<Veiculo> obterVeiculosPorAno(String enderecoBase, List<Dados> anos) {
        List<Veiculo> veiculos = new ArrayList<>();
        for (Dados ano : anos) {
            String urlAno = String.format("%s/%s", enderecoBase, ano.codigo());
            Veiculo veiculo = conversor.obterDados(consumo.obterDados(urlAno), Veiculo.class);
            veiculos.add(veiculo);
        }
        return veiculos;
    }
}