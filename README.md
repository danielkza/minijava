# Capítulo 2

## Instalação do SableCC

1. Acesse http://www.sablecc.org/downloads e baixe a versão 3.7
2. Descompacte o o arquivo sablecc-3.7.zip
3. Edite {sablecc-path}/bin/sablecc:
   java -jar {sablecc-fullpath}/lib/sablecc.jar $*
3. chmod +x {sablecc-path}/bin/sablecc
4. Adicione {sablecc-path}/bin/sablecc ao path (ou utilize o caminho completo para chamar o script)

## Gerar o Lexer

Execute o seguinte comando:

$ sablecc MiniJavaLexer.sable

Se o comando executar com sucesso, deverá surgir uma pasta chamada "minijava"

## Compilar o Lexer

$ javac Main.java

## Testar o Lexer

Utilize os programas da pasta "programs" para testar seu lexer:

$ java Main < programs/Factorial.java