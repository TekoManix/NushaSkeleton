import AST.Token;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;

public class Parser1Tests {
    @Test
    public void singleVarParser() throws Exception {
        var code =          "var Birds : Bird[6]\n"+
                "";

        var tokens = new LinkedList<Token>();
        tokens.add(new Token(Token.TokenTypes.VAR, 1, 3));
        tokens.add(new Token(Token.TokenTypes.IDENTIFIER, 1, 9, "Birds"));
        tokens.add(new Token(Token.TokenTypes.COLON, 1, 11));
        tokens.add(new Token(Token.TokenTypes.IDENTIFIER, 1, 16, "Bird"));
        tokens.add(new Token(Token.TokenTypes.LEFTBRACE, 1, 17));
        tokens.add(new Token(Token.TokenTypes.NUMBER, 1, 18, "6"));
        tokens.add(new Token(Token.TokenTypes.RIGHTBRACE, 1, 19));
        tokens.add(new Token(Token.TokenTypes.NEWLINE, 1, 19));

        var ast = new NushaFall2025Parser().Nusha(tokens).orElseThrow();
        Assertions.assertEquals("Bird", ast.variables.variable.get(0).type);
        Assertions.assertEquals("Birds", ast.variables.variable.get(0).variableName);
        Assertions.assertEquals("6", ast.variables.variable.get(0).size.orElseThrow());
    }

    @Test
    public void multiVarParser() throws Exception {
        var code =          "var Places : Place [5]\n"+
                "\n"+
                "var Songs : Song [5]\n"+
                "";

        var tokens = new LinkedList<Token>();
        tokens.add(new Token(Token.TokenTypes.VAR, 1, 3));
        tokens.add(new Token(Token.TokenTypes.IDENTIFIER, 1, 10, "Places"));
        tokens.add(new Token(Token.TokenTypes.COLON, 1, 12));
        tokens.add(new Token(Token.TokenTypes.IDENTIFIER, 1, 18, "Place"));
        tokens.add(new Token(Token.TokenTypes.LEFTBRACE, 1, 20));
        tokens.add(new Token(Token.TokenTypes.NUMBER, 1, 21, "5"));
        tokens.add(new Token(Token.TokenTypes.RIGHTBRACE, 1, 22));
        tokens.add(new Token(Token.TokenTypes.NEWLINE, 2, 0));
        tokens.add(new Token(Token.TokenTypes.NEWLINE, 3, 0));
        tokens.add(new Token(Token.TokenTypes.VAR, 3, 3));
        tokens.add(new Token(Token.TokenTypes.IDENTIFIER, 3, 9, "Songs"));
        tokens.add(new Token(Token.TokenTypes.COLON, 3, 11));
        tokens.add(new Token(Token.TokenTypes.IDENTIFIER, 3, 16, "Song"));
        tokens.add(new Token(Token.TokenTypes.LEFTBRACE, 3, 18));
        tokens.add(new Token(Token.TokenTypes.NUMBER, 3, 19, "5"));
        tokens.add(new Token(Token.TokenTypes.RIGHTBRACE, 3, 20));
        tokens.add(new Token(Token.TokenTypes.NEWLINE, 4, 0));
        tokens.add(new Token(Token.TokenTypes.NEWLINE, 4, 0));

        var ast = new NushaFall2025Parser().Nusha(tokens).orElseThrow();
        Assertions.assertEquals("Place", ast.variables.variable.get(0).type);
        Assertions.assertEquals("Places", ast.variables.variable.get(0).variableName);
        Assertions.assertEquals("5", ast.variables.variable.get(0).size.orElseThrow());
        Assertions.assertEquals("Song", ast.variables.variable.get(1).type);
        Assertions.assertEquals("Songs", ast.variables.variable.get(1).variableName);
        Assertions.assertEquals("5", ast.variables.variable.get(1).size.orElseThrow());
    }
}
