# Simulador de Multithreading - Escalar e Superescalar
## Desenvolvido para simular IMT, BMT e SMT nas arquiteturas escalares e superescalares

Para rodar o código basta baixar ou clonar o repositório e executar os comandos: 

 `javac Simulador.java` ->
 `java Simulador`

O propósito é simular como as instruções se comportam com o multithreading a nível de instrução nas arquiteturas. Os arquivos de threads definem as instruções de cada thread, é possível adicionar quantas threads forem necessárias
mudando no código de cada arquitetura e criando os repsectivos .txt

# O código

A main Simulador.java contém a inicalização das classes necessárias para a execução do código

Intruction.java é a classe que representa uma instrução

Contexto.java é a classe que representa a thread

Escalar.java e Superescalar.java são as classes que representam ambas as arquiteturas
