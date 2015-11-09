package visitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import minijava.analysis.DepthFirstAdapter;
import minijava.node.Node;
import minijava.node.Token;

public class PositionAnalysis extends DepthFirstAdapter {
    private Map<Node, Token> startTokens = new HashMap<>();
    private List<Node> pendingNodes = new ArrayList<>();

    @Override
    public void defaultIn(Node node) {
        pendingNodes.add(node);
    }

    @Override
    public void defaultCase(@SuppressWarnings("unused") Node node) {
        if(node instanceof Token) {
            for(Node pending : pendingNodes) {
                startTokens.put(pending, (Token)node);
            }
            pendingNodes.clear();
        }
    }

    public Map<Node, Token> getStartTokens() {
        return startTokens;
    }
}
