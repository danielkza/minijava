CLASSDIR := build

SRCS = $(wildcard *.java minijava/**/*.java frame/**/*.java symbol/**/*.java visitor/**/*.java)
OBJS = $(addprefix $(CLASSDIR)/,$(patsubst %.java,%.class,$(SRCS)))

.PHONY: all run clean classes

all: classes
	$(MAKE) classes

run: all
	java -classpath $(CLASSDIR)/ FrameMain

MiniJava.java: minijava/lexer/Lexer.java

minijava/lexer/Lexer.java: MiniJava.sablecc | $(CLASSDIR)
	sablecc $?
	mkdir -p $(CLASSDIR)/minijava/lexer $(CLASSDIR)/minijava/parser
	cp -f minijava/lexer/lexer.dat $(CLASSDIR)/minijava/lexer/
	cp -f minijava/parser/parser.dat $(CLASSDIR)/minijava/parser/

$(CLASSDIR):
	mkdir -p $@/

$(OBJS): $(CLASSDIR)/%.class: %.java | $(CLASSDIR)
	echo $?
	javac -d $(CLASSDIR) $?

classes: $(OBJS)

clean:
	rm -rf $(CLASSDIR)/
	rm -rf minijava/analysis minijava/lexer minijava/node minijava/parser

