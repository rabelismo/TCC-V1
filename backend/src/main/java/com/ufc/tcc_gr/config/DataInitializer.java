package com.ufc.tcc_gr.config;

import com.ufc.tcc_gr.model.Module;
import com.ufc.tcc_gr.repository.ModuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    @Bean
    CommandLineRunner initModules(ModuleRepository repo) {
        return args -> {
            if (repo.count() > 0) return;

            repo.save(Module.builder()
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

            repo.save(Module.builder()
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

            repo.save(Module.builder()
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

            repo.save(Module.builder()
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
        };
    }
}
