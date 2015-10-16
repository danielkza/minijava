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
	cp -f --parents minijava/lexer/lexer.dat minijava/parser/parser.dat $(CLASSDIR)/

$(CLASSDIR):
	mkdir -p $@/

$(OBJS): $(CLASSDIR)/%.class: %.java | $(CLASSDIR)
	echo $?
	javac -d $(CLASSDIR) $?

classes: $(OBJS)

clean:
	rm -rf $(CLASSDIR)/
	rm -rf minijava/analysis minijava/lexer minijava/node minijava/parser

