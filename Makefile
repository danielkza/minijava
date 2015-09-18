MiniJava.class: minijava/lexer/Lexer.java MiniJava.java

run: MiniJava.class
	java MiniJava

minijava/lexer/Lexer.java: MiniJava.sablecc

%.class: %.java
	javac $<

%.java: %.sablecc
	sablecc $<

.PHONY: clean
clean:
	rm -f minijava.java
	rm -f minijava/parser minijava/lexer minijava/analysis
