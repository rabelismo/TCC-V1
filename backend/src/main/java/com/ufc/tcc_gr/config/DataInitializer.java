package com.ufc.tcc_gr.config;

import com.ufc.tcc_gr.model.AcceptanceCriterion;
import com.ufc.tcc_gr.model.Module;
import com.ufc.tcc_gr.repository.AcceptanceCriterionRepository;
import com.ufc.tcc_gr.repository.ModuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    @Bean
    CommandLineRunner initModules(ModuleRepository moduleRepo,
                                  AcceptanceCriterionRepository criterionRepo) {
        return args -> {
            if (moduleRepo.count() > 0 && criterionRepo.count() > 0) return;
            if (moduleRepo.count() > 0 && criterionRepo.count() == 0) {
                moduleRepo.deleteAll();
            }

            // ── Módulo 1: Variáveis e Print ──

            Module m1 = moduleRepo.save(Module.builder()
                .orderIndex(1)
                .title("Variáveis e Print – Identidade do Jogo")
                .concept("VARIABLES")
                .description("Aprenda sobre variáveis e a função print() criando a tela de boas-vindas do Jogo da Velha.")
                .theoryMarkdown("""
                    # Módulo 1: Variáveis e Print

                    ## O que são variáveis?

                    Variáveis são **espaços na memória** do computador onde guardamos valores. Pense nelas como **etiquetas** que colamos em caixas para saber o que tem dentro.

                    ```python
                    nome_do_jogo = "Jogo da Velha"
                    versao = 1.0
                    max_jogadores = 2
                    ```

                    ## Tipos de dados básicos

                    | Tipo | Exemplo | Descrição |
                    |------|---------|-----------|
                    | `str` | `"texto"` | Texto (string) |
                    | `int` | `42` | Número inteiro |
                    | `float` | `3.14` | Número decimal |
                    | `bool` | `True` | Verdadeiro ou Falso |

                    ## A função print()

                    `print()` exibe informações no console. É a forma de seu programa **"falar"** com o usuário.

                    ```python
                    print("Olá, mundo!")
                    print(f"O jogo {nome_do_jogo} suporta {max_jogadores} jogadores")
                    ```

                    ## Sua missão

                    Crie variáveis para o jogo da velha e exiba uma tela de boas-vindas formatada no console.
                    """)
                .starterCode("""
                    # Módulo 1: Variáveis e Print
                    # Crie as variáveis do jogo e exiba a tela de boas-vindas

                    # 1. Crie uma variável 'nome_jogo' com o valor "Jogo da Velha"


                    # 2. Crie uma variável 'jogador_x' com o valor "X"


                    # 3. Crie uma variável 'jogador_o' com o valor "O"


                    # 4. Use print() para exibir uma tela de boas-vindas bonita
                    # Exemplo de saída esperada:
                    # ========================
                    #   Jogo da Velha
                    # ========================
                    # Jogador 1: X
                    # Jogador 2: O
                    # Boa sorte!

                    """)
                .expectedOutput("Jogo da Velha")
                .build());

            criterionRepo.save(AcceptanceCriterion.builder()
                .module(m1).orderIndex(1).type("OUTPUT_CONTAINS")
                .description("A saída deve conter o nome \"Jogo da Velha\"")
                .expectedOutput("Jogo da Velha")
                .hint("Use print() com o nome do jogo. Ex: print(nome_jogo)")
                .hidden(false).build());

            criterionRepo.save(AcceptanceCriterion.builder()
                .module(m1).orderIndex(2).type("OUTPUT_CONTAINS")
                .description("A saída deve conter o símbolo do jogador \"X\"")
                .expectedOutput("X")
                .hint("Crie a variável jogador_x = \"X\" e use print() para exibi-la.")
                .hidden(false).build());

            criterionRepo.save(AcceptanceCriterion.builder()
                .module(m1).orderIndex(3).type("OUTPUT_CONTAINS")
                .description("A saída deve conter o símbolo do jogador \"O\"")
                .expectedOutput("O")
                .hint("Crie a variável jogador_o = \"O\" e use print() para exibi-la.")
                .hidden(false).build());

            criterionRepo.save(AcceptanceCriterion.builder()
                .module(m1).orderIndex(4).type("OUTPUT_CONTAINS")
                .description("O código deve usar a função print()")
                .expectedOutput("Jogo da Velha")
                .hint("Você precisa usar print() para exibir a saída no console.")
                .hidden(true).build());

            // ── Módulo 2: Arrays 2D ──

            Module m2 = moduleRepo.save(Module.builder()
                .orderIndex(2)
                .title("Arrays 2D – O Tabuleiro")
                .concept("ARRAYS")
                .description("Use listas (arrays) para criar e exibir o tabuleiro 3x3 do Jogo da Velha.")
                .theoryMarkdown("""
                    # Módulo 2: Arrays 2D – O Tabuleiro

                    ## Listas em Python

                    Uma **lista** é uma coleção ordenada de elementos. Em Python, usamos colchetes `[]`.

                    ```python
                    frutas = ["maçã", "banana", "laranja"]
                    numeros = [1, 2, 3, 4, 5]
                    ```

                    ## Listas 2D (Matriz)

                    Uma **lista de listas** cria uma grade/tabuleiro – perfeita para nosso jogo!

                    ```python
                    tabuleiro = [
                        [" ", " ", " "],
                        [" ", " ", " "],
                        [" ", " ", " "]
                    ]
                    ```

                    ## Acessando elementos

                    Use `[linha][coluna]` para acessar ou modificar uma posição:

                    ```python
                    tabuleiro[0][0] = "X"  # Canto superior esquerdo
                    tabuleiro[1][1] = "O"  # Centro
                    ```

                    ## Sua missão

                    Crie o tabuleiro 3x3 e uma função para exibi-lo formatado no console.
                    """)
                .starterCode("""
                    # Módulo 2: Arrays 2D - O Tabuleiro

                    # 1. Crie o tabuleiro 3x3 (lista de listas) com espaços vazios
                    tabuleiro = [
                        [" ", " ", " "],
                        [" ", " ", " "],
                        [" ", " ", " "]
                    ]

                    # 2. Crie uma função que exibe o tabuleiro formatado
                    def exibir_tabuleiro(tab):
                        # Dica: use tab[linha][coluna] e print()
                        # Saída esperada:
                        #  X | O |
                        # -----------
                        #    | X |
                        # -----------
                        #    |   | O
                        pass

                    # 3. Faça algumas jogadas de teste
                    tabuleiro[0][0] = "X"
                    tabuleiro[1][1] = "O"

                    # 4. Exiba o tabuleiro
                    exibir_tabuleiro(tabuleiro)
                    """)
                .expectedOutput("X")
                .build());

            criterionRepo.save(AcceptanceCriterion.builder()
                .module(m2).orderIndex(1).type("OUTPUT_CONTAINS")
                .description("O tabuleiro exibido deve conter \"X\"")
                .expectedOutput("X")
                .hint("Certifique-se de que tabuleiro[0][0] = \"X\" e que exibir_tabuleiro() imprime o conteúdo.")
                .hidden(false).build());

            criterionRepo.save(AcceptanceCriterion.builder()
                .module(m2).orderIndex(2).type("OUTPUT_CONTAINS")
                .description("O tabuleiro exibido deve conter \"O\"")
                .expectedOutput("O")
                .hint("Certifique-se de que tabuleiro[1][1] = \"O\" e que exibir_tabuleiro() imprime o conteúdo.")
                .hidden(false).build());

            criterionRepo.save(AcceptanceCriterion.builder()
                .module(m2).orderIndex(3).type("OUTPUT_CONTAINS")
                .description("O tabuleiro deve ter separadores de linha \"---\"")
                .expectedOutput("---")
                .hint("Use print(\"----------\") ou similar entre as linhas do tabuleiro.")
                .hidden(false).build());

            criterionRepo.save(AcceptanceCriterion.builder()
                .module(m2).orderIndex(4).type("OUTPUT_CONTAINS")
                .description("O tabuleiro deve usar \"|\" para separar colunas")
                .expectedOutput("|")
                .hint("Formate cada linha como: print(f\" {tab[i][0]} | {tab[i][1]} | {tab[i][2]} \")")
                .hidden(false).build());

            // ── Módulo 3: If/Else ──

            Module m3 = moduleRepo.save(Module.builder()
                .orderIndex(3)
                .title("If/Else – Validação de Jogadas")
                .concept("CONTROL_FLOW")
                .description("Use estruturas condicionais para validar jogadas e verificar o vencedor.")
                .theoryMarkdown("""
                    # Módulo 3: If/Else – Validação de Jogadas

                    ## Estruturas Condicionais

                    O `if/elif/else` permite que seu programa **tome decisões**.

                    ```python
                    idade = 18
                    if idade >= 18:
                        print("Maior de idade")
                    elif idade >= 16:
                        print("Pode votar")
                    else:
                        print("Menor de idade")
                    ```

                    ## Operadores de Comparação

                    | Operador | Significado |
                    |----------|-------------|
                    | `==` | Igual a |
                    | `!=` | Diferente de |
                    | `>` | Maior que |
                    | `<` | Menor que |
                    | `>=` | Maior ou igual |
                    | `<=` | Menor ou igual |

                    ## Operadores Lógicos

                    ```python
                    if posicao_valida and casa_vazia:
                        fazer_jogada()
                    ```

                    ## Sua missão

                    Implemente a validação de jogadas e a checagem de vitória do Jogo da Velha.
                    """)
                .starterCode("""
                    # Módulo 3: If/Else - Validação de Jogadas

                    tabuleiro = [
                        ["X", "O", " "],
                        [" ", "X", " "],
                        [" ", " ", " "]
                    ]

                    # 1. Crie uma função que valida se uma jogada é válida
                    def jogada_valida(tab, linha, coluna):
                        # Verifique: linha e coluna entre 0 e 2?
                        # A casa está vazia (" ")?
                        pass

                    # 2. Crie uma função que verifica se alguém ganhou
                    def verificar_vencedor(tab):
                        # Verifique linhas, colunas e diagonais
                        # Retorne "X", "O" ou None
                        pass

                    # 3. Teste suas funções
                    print(jogada_valida(tabuleiro, 0, 0))  # False (já ocupada)
                    print(jogada_valida(tabuleiro, 2, 2))  # True (vazia)

                    tabuleiro[2][2] = "X"
                    print(verificar_vencedor(tabuleiro))    # "X" (diagonal)
                    """)
                .expectedOutput("False")
                .build());

            criterionRepo.save(AcceptanceCriterion.builder()
                .module(m3).orderIndex(1).type("OUTPUT_CONTAINS")
                .description("jogada_valida(tab, 0, 0) deve retornar False (casa ocupada)")
                .expectedOutput("False")
                .hint("Verifique se a casa tab[linha][coluna] == \" \" e se linha/coluna estão entre 0 e 2.")
                .hidden(false).build());

            criterionRepo.save(AcceptanceCriterion.builder()
                .module(m3).orderIndex(2).type("OUTPUT_CONTAINS")
                .description("jogada_valida(tab, 2, 2) deve retornar True (casa vazia)")
                .expectedOutput("True")
                .hint("A posição [2][2] está vazia no tabuleiro inicial, então deve retornar True.")
                .hidden(false).build());

            criterionRepo.save(AcceptanceCriterion.builder()
                .module(m3).orderIndex(3).type("OUTPUT_CONTAINS")
                .description("verificar_vencedor() deve detectar vitória do \"X\" na diagonal")
                .expectedOutput("X")
                .hint("Após tabuleiro[2][2] = \"X\", a diagonal principal fica X-X-X. Verifique tab[0][0]==tab[1][1]==tab[2][2].")
                .hidden(false).build());

            criterionRepo.save(AcceptanceCriterion.builder()
                .module(m3).orderIndex(4).type("OUTPUT_CONTAINS")
                .description("O código não deve gerar erros de execução")
                .expectedOutput("False")
                .hint("Revise a lógica das funções. Certifique-se de usar return em vez de print dentro delas.")
                .hidden(true).build());

            // ── Módulo 4: While Loops ──

            Module m4 = moduleRepo.save(Module.builder()
                .orderIndex(4)
                .title("While Loops – Motor do Jogo")
                .concept("LOOPS")
                .description("Use loops while para criar o ciclo principal do jogo, alternando turnos até haver um vencedor.")
                .theoryMarkdown("""
                    # Módulo 4: While Loops – Motor do Jogo

                    ## O loop while

                    O `while` repete um bloco de código **enquanto** uma condição for verdadeira.

                    ```python
                    contador = 0
                    while contador < 5:
                        print(f"Volta {contador}")
                        contador += 1
                    ```

                    ## Game Loop

                    Todo jogo tem um **loop principal** – o coração que mantém o jogo rodando:

                    ```python
                    jogo_ativo = True
                    while jogo_ativo:
                        exibir_estado()
                        receber_jogada()
                        atualizar_estado()
                        verificar_fim()
                    ```

                    ## Break e Continue

                    - `break`: Sai do loop imediatamente
                    - `continue`: Pula para a próxima iteração

                    ```python
                    while True:
                        jogada = input("Sua jogada (ou 'sair'): ")
                        if jogada == "sair":
                            break
                    ```

                    ## Sua missão

                    Junte tudo que aprendeu para criar o game loop completo do Jogo da Velha!
                    """)
                .starterCode("""
                    # Módulo 4: While Loops - Motor do Jogo Completo!

                    tabuleiro = [[" "]*3 for _ in range(3)]
                    jogador_atual = "X"
                    jogo_ativo = True
                    jogadas = 0

                    def exibir_tabuleiro(tab):
                        for i, linha in enumerate(tab):
                            print(f" {linha[0]} | {linha[1]} | {linha[2]} ")
                            if i < 2:
                                print("-----------")

                    def verificar_vencedor(tab):
                        for i in range(3):
                            if tab[i][0] == tab[i][1] == tab[i][2] != " ":
                                return tab[i][0]
                            if tab[0][i] == tab[1][i] == tab[2][i] != " ":
                                return tab[0][i]
                        if tab[0][0] == tab[1][1] == tab[2][2] != " ":
                            return tab[0][0]
                        if tab[0][2] == tab[1][1] == tab[2][0] != " ":
                            return tab[0][2]
                        return None

                    # COMPLETE O GAME LOOP ABAIXO:
                    # Use while jogo_ativo para:
                    # 1. Exibir o tabuleiro
                    # 2. Pedir a jogada do jogador atual (linha e coluna)
                    # 3. Validar a jogada
                    # 4. Colocar a peça no tabuleiro
                    # 5. Verificar vencedor ou empate
                    # 6. Alternar o jogador (X <-> O)

                    print("Jogo da Velha iniciado!")
                    exibir_tabuleiro(tabuleiro)
                    print("Implemente o game loop acima!")
                    """)
                .expectedOutput("Jogo da Velha iniciado!")
                .build());

            criterionRepo.save(AcceptanceCriterion.builder()
                .module(m4).orderIndex(1).type("OUTPUT_CONTAINS")
                .description("O jogo deve exibir \"Jogo da Velha iniciado!\"")
                .expectedOutput("Jogo da Velha iniciado!")
                .hint("Mantenha o print(\"Jogo da Velha iniciado!\") no início do código.")
                .hidden(false).build());

            criterionRepo.save(AcceptanceCriterion.builder()
                .module(m4).orderIndex(2).type("OUTPUT_CONTAINS")
                .description("O tabuleiro deve ser exibido com separadores \"|\"")
                .expectedOutput("|")
                .hint("Chame exibir_tabuleiro(tabuleiro) dentro do loop.")
                .hidden(false).build());

            criterionRepo.save(AcceptanceCriterion.builder()
                .module(m4).orderIndex(3).type("OUTPUT_CONTAINS")
                .description("O código deve conter um loop while funcional")
                .expectedOutput("---")
                .hint("Use 'while jogo_ativo:' e chame exibir_tabuleiro() que imprime '---' entre linhas.")
                .hidden(false).build());

            criterionRepo.save(AcceptanceCriterion.builder()
                .module(m4).orderIndex(4).type("TEST_CASE")
                .description("O game loop deve processar jogadas via input()")
                .input("0\n0\n1\n1\n")
                .expectedOutput("|")
                .hint("Use input() para ler linha e coluna. Ex: linha = int(input('Linha: '))")
                .hidden(true).build());
        };
    }
}
